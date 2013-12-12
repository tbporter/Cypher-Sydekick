// Class file for User Keys that gives access to user ids, keys, and names. 
// Enables setting and retrieving information for each UserKey class

package com.github.tbporter.cypher_sydekick.database;

public class UserKey {
	private long id_;
	private String username_, key_;

	public long getId() {
		return id_;
	}

	public void setId(long id) {
		this.id_ = id;
	}

	public String getUsername() {
		return username_;
	}

	public void setUsername(String username) {
		this.username_ = username;
	}

	public String getKey() {
		return key_;
	}

	public void setKey(String key) {
		this.key_ = key;
	}

	@Override
	public String toString() {
		return "Username: " + username_ + "Public key: " + key_;
	}
}
