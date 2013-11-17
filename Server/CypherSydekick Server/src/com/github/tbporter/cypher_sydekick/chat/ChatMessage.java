package com.github.tbporter.cypher_sydekick.chat;

/**
 * Container class to hold chat message fields.
 * 
 * @author ayelix
 * 
 */
public final class ChatMessage {
	/** Username of the recipient. */
	private String m_receiver;
	/** Username of the sender. */
	private String m_sender;
	/** Message contents. */
	private String m_contents;

	/**
	 * Creates a ChatMessage with the given values.
	 */
	public ChatMessage(final String receiver, final String sender,
			final String contents) {
		m_receiver = receiver;
		m_sender = sender;
		m_contents = contents;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append(" Object {receiver:")
				.append(m_receiver).append(", sender:").append(m_sender)
				.append(", contents:").append(m_contents).append("}");
		return sb.toString();
	}

	public String getReceiver() {
		return m_receiver;
	}

	public void setReceiver(final String m_receiver) {
		this.m_receiver = m_receiver;
	}

	public String getSender() {
		return m_sender;
	}

	public void setSender(final String m_sender) {
		this.m_sender = m_sender;
	}

	public String getContents() {
		return m_contents;
	}

	public void seContents(final String m_contents) {
		this.m_contents = m_contents;
	}
}
