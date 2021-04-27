package io.jzheaux.springsecurity.resolutions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

// create a JPA-managed User entity to represent users.
@Entity(name = "users")
/*
 * we could have changed User to implement UserDetails. 
 * the reason the demonstrated way is preferred is that it hides the relationship 
 * between domain and Spring Security behind Spring Security interfaces.
 * */
public class User implements Serializable {
	@Id
	@GeneratedValue //otherwise : ids for this class must be manually assigned before calling save()
	UUID id;
	
	/*
	 * Adding PII (Personally Identifiable Information) to an application can be tricky; Spring Security can help with 
	 * keeping that data private.
	 * In preparation, add the user's full name to the User object, 
	 * and will change ResolutionController#read() to include their name as part of the results.
	 * First, add the fullName field to User to simplifies the automated tests):
	 * */
	@Column
	String username;
	@Column(name="full_name")
	String fullName;
	
	@Column
	String password;
	@Column
	boolean enabled = true;
	// manage the bi-directional relationship between User and UserAuthority,
	// add a Set<UserAuthority> field as well as a grantAuthority method to User
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	Collection<UserAuthority> userAuthorities = new ArrayList<>();

	public Collection<UserAuthority> getUserAuthorities() {
		return Collections.unmodifiableCollection(this.userAuthorities);
	}

	public void grantAuthority(String authority) {
		UserAuthority userAuthority = new UserAuthority(this, authority);
		this.userAuthorities.add(userAuthority);
	}

	User() {
	}

	public User(String username, String password) {
		this.id = UUID.randomUUID();
		this.username = username;
		this.password = password;
	}

	/**
	 add a copy constructor to User, in preparation for building a custom UserDetailsService
	 */
	public User(User user) {
	    this.id = user.id;
	    this.fullName = user.fullName;
	    this.username = user.username;
	    this.password = user.password;
	    this.enabled = user.enabled;
	    this.userAuthorities = user.userAuthorities;
	}
	
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
	
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setUserAuthorities(Collection<UserAuthority> userAuthorities) {
		this.userAuthorities = userAuthorities;
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
