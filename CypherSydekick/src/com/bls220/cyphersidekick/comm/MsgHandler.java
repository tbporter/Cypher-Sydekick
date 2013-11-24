package com.bls220.cyphersidekick.comm;

import java.io.IOException;

import com.bls220.cyphersidekick.mapstuff.ActionMessage;

public interface MsgHandler {
	void postMessage(ActionMessage msg) throws IOException;

	ActionMessage recvAction();

	String syncAndGetMapPath() throws IOException;
}
