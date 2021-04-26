package io.jzheaux.springsecurity.resolutions;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

// create a JPA-managed User entity to represent users.
@Entity(name = "users")
public class User implements Serializable {
	@Id
	UUID id;
	@Column
	String username;
	@Column
	String password;
	@Column
	boolean enabled = true;
	User() {}
	public User(String username, String password) {
	    this.id = UUID.randomUUID();
	    this.username = username;
	    this.password = password;
	}	
	
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}	

}
