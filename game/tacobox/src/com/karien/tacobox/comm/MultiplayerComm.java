package com.karien.tacobox.comm;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import com.karien.taco.mapstuff.ActionMessage;
import com.karien.taco.mapstuff.map.MapID;

public class MultiplayerComm implements Runnable, MsgHandler {
	private final Socket sock;
	private final boolean master;

	private ArrayBlockingQueue<Msg> inActs = new ArrayBlockingQueue<Msg>(5);

	public MultiplayerComm(Socket s, boolean master) {
		sock = s;
		this.master = master;

		new Thread(this).start();
	}

	/**
	 * Call this in some tick loop to see if an action from the other player.
	 * 
	 * @return The coordinate send from the partner or null if there was none to
	 *         read.
	 */
	public ActionMessage recvAction() {
		Msg msg = inActs.peek();
		if (msg == null) {
			return null;
		} else if (msg.msg != null) {
			return inActs.poll().msg;
		}
		// Else it's a sync or map choice
		return null;
	}

	public static MultiplayerComm connect(int port) throws IOException {
		ServerSocket serv = new ServerSocket(port);

		Socket sock;
		while (true) {
			Socket rec = serv.accept();

			int d = rec.getInputStream().read();
			if (d == 42) {
				sock = rec;
				rec.getOutputStream().write(44);
				serv.close();
				break;
			} else {
				rec.close();
			}
		}

		return new MultiplayerComm(sock, true);
	}

	public static MultiplayerComm connect(String addr, int port)
			throws IOException {
		Socket s;
		while (true) {
			s = new Socket(addr, port);
			s.getOutputStream().write(42);

			if (s.getInputStream().read() != 44) {
				s.close();
			} else {
				break;
			}
		}

		return new MultiplayerComm(s, false);
	}

	@SuppressWarnings("resource")
	@Override
	public void run() {
		Scanner sc;
		try {
			sc = new Scanner(sock.getInputStream());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		while (true) {
			String line = sc.nextLine();

			char first = line.charAt(0);
			Msg msg;
			if (first == ':') {
				// ActionMessage:
				msg = new Msg(ActionMessage.fromString(line.substring(1)), null);
			} else if (first == '#') {
				msg = new Msg(null, null);
			} else {
				msg = new Msg(null, MapID.valueOf(line));
			}

			// We want an exception on full because something's wrong in that
			// case.
			inActs.add(msg);
		}
	}

	@Override
	public void postMessage(ActionMessage msg) throws IOException {
		// TODO: Check that this won't block so that we don't block the gui
		// thread

		write(":" + msg.toString());
	}

	/**
	 * You can tell that we're getting down to the wire in the hackathon
	 * 
	 */
	private static class Msg {
		final ActionMessage msg;
		final MapID newMap;

		Msg(ActionMessage msg, MapID newMap) {
			this.msg = msg;
			this.newMap = newMap;
		}
	}

	private MapID last;

	@Override
	public String syncAndGetMapPath() throws IOException {
		if (master) {
			MapID next = null;
			if (last == null) {
				next = MapID.Test;
			} else {
				switch (last) {
				case Test:
					next = MapID.Normal;
					break;
				case Normal:
					next = MapID.Hard;
					break;
				case Hard:
					next = MapID.End;
					break;
				default:
					throw new RuntimeException("Unknown map: " + last);
				}
			}
			last = next;
			write(next.toString());
			
			do {
				try {
					Msg msg = inActs.take();
					if (msg.newMap == null) {
						break;
					}
				} catch (InterruptedException e) {}
			}while (true);
			
			return next.getPath(true);
		} else {
			do {
				Msg msg;
				try {
					msg = inActs.take();
				} catch (InterruptedException ex) {
					System.out.println("Ignored interrupted: " + ex);
					continue;
				}

				if (msg.msg != null) {
					System.out
							.println("Consumed unused map action: " + msg.msg);
				} else {
					last = msg.newMap;
					System.out.println("Got map: " + msg);
					write("#");
					return msg.newMap.getPath(false);
				}
			} while (true);
		}
	}

	private void write(String s) throws IOException {
		sock.getOutputStream().write((s + "\n").getBytes());
	}
}
