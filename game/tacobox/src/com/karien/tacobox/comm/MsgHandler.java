package com.karien.tacobox.comm;

import java.io.IOException;

import com.karien.taco.mapstuff.ActionMessage;

public interface MsgHandler {
	void postMessage(ActionMessage msg) throws IOException;
	ActionMessage recvAction();
	String syncAndGetMapPath() throws IOException;
}
