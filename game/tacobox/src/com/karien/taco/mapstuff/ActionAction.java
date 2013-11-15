package com.karien.taco.mapstuff;

public class ActionAction {
	public final boolean remote;
	public final String targetId;
	public final MapAction act;
	
	ActionAction(boolean remote,  String targetId, MapAction act) {
		this.remote = remote;
		this.targetId = targetId;
		this.act = act;
	}
	
	public String toString() {
		return "remote=" + remote + ", target=" + targetId + ", action=" + act;
	}
}
