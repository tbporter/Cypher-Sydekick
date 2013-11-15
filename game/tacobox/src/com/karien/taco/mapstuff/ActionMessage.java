package com.karien.taco.mapstuff;

/**
 * Abstracted version of a message that is sent between clients regarding actions.
 *
 */
public class ActionMessage {
	public final String id;
	public final MapAction act;

	public ActionMessage(String id, MapAction act) {
		this.id = id;
		this.act = act;
	}
	
	public static ActionMessage fromString(String str) {
		int sep = str.indexOf('|');
		return new ActionMessage(str.substring(0, sep), MapAction.valueOf(str.substring(sep+1)));
	}
	
	public String toString() {
		return id + "|" + act;
	}
}
