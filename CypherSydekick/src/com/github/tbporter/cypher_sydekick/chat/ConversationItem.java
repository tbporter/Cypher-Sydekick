package com.github.tbporter.cypher_sydekick.chat;

public class ConversationItem {
	private String subtitle_ = "";
	private String message_ = "";
	private int icon_;

	public void setMessage(String message) {
		this.message_ = message;
	}

	public String getMessage() {
		return message_;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle_ = subtitle;
	}

	public String getSubtitle() {
		return subtitle_;
	}

	public void setIcon(int icon) {
		this.icon_ = icon;
	}

	public int getIcon() {
		return icon_;
	}
}
