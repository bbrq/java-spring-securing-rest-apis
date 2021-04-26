package io.jzheaux.springsecurity.resolutions;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

//will be the beginnings of adding authorization to the project
@Entity(name="authorities")
public class UserAuthority {
	//create the JPA-annotated field authority
	@Id
    UUID id;
    @Column
    String authority;
    //a join column between Users and UserAuthoritys. Spring Security expects this column to be called username in user table
    @JoinColumn(name="username", referencedColumnName="username")
    @ManyToOne
    User user;
    
    /**
	  While not strictly necessary,  add a copy constructor to UserAuthority will allow you to make a deep copy 
	  of user.userAuthorities in User's copy constructor.
	 */
	public UserAuthority(UserAuthority auth) {
	    this.id = user.id;
	    this.authority = auth.authority;
	    this.user = auth.user;
	}
    
    UserAuthority() {}
    public UserAuthority(User user, String authority) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.authority = authority;
    }
    
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	public String getAuthority() {
		return authority;
	}
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
}
