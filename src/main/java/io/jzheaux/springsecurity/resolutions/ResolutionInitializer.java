package io.jzheaux.springsecurity.resolutions;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

/*add a user to the database that will replace the one you inlined in the code.

  this class si what we'll use to add test data throughout the project.

  add a dependency on UserRepository:*/
@Component
public class ResolutionInitializer implements SmartInitializingSingleton {

	private final UserRepository users;

	private final ResolutionRepository resolutions;

	// constructor requires both ResolutionRepository and UserRepository:
	public ResolutionInitializer(ResolutionRepository resolutions, UserRepository users) {
		this.resolutions = resolutions;
		this.users = users;
	}

	@Override
	public void afterSingletonsInstantiated() {
		this.resolutions.save(new Resolution("Read War and Peace", "user"));
		this.resolutions.save(new Resolution("Free Solo the Eiffel Tower", "user"));
		this.resolutions.save(new Resolution("Hang Christmas Lights", "user"));
		
		//add a couple more users and grant all users different authorities
		
		//create a user
		User user = new User("user",
				//password: password
	            "{bcrypt}$2a$10$MywQEqdZFNIYnx.Ro/VQ0ulanQAl34B5xVjK2I/SDZNVGS5tHQ08W");
		user.setFullName("User Userson");
		user.grantAuthority("resolution:read");
		/*
		 * there are as many ways to perform reconciliation as there are applications. 
		 * The important point to remember is that sometimes applications have their own internal representation 
		 * of authority. And, just because a client has been granted an authority, it doesn't mean 
		 * the underlying user can actually do that. The final list of authorities ought to represent 
		 * some kind of intersection of what the user is allowed to do and what the user has granted the 
		 * client to be able to do on their behalf.
		 * */
		//because we are doing an intersection,make sure to grant the user:read authority to 
		//the relevant users
		user.grantAuthority("user:read");
		user.grantAuthority("resolution:write");
	    this.users.save(user);
	    
	    User hasread = new User();
	    hasread.setUsername("hasread");
	    hasread.setPassword("{bcrypt}$2a$10$MywQEqdZFNIYnx.Ro/VQ0ulanQAl34B5xVjK2I/SDZNVGS5tHQ08W");
	    hasread.setFullName("Has Read");
	    hasread.grantAuthority("resolution:read");
	    user.grantAuthority("user:read");
	    this.users.save(hasread);
	    User haswrite = new User();
	    haswrite.setFullName("Has Write");
	    haswrite.setUsername("haswrite");
	    haswrite.setPassword("{bcrypt}$2a$10$MywQEqdZFNIYnx.Ro/VQ0ulanQAl34B5xVjK2I/SDZNVGS5tHQ08W");
	    haswrite.grantAuthority("resolution:write");
	    //make hasRead a friend of hasWrite and also give hasWrite a premium subscription
	    haswrite.addFriend(hasread);
	    haswrite.setSubscription("premium");
	    user.grantAuthority("user:read");
	    this.users.save(haswrite);
	    
	    
	    
	    //we'll consider the business requirement that admins have elevated permissions to see, 
	    //for example, resolutions for all users.
	    
	    //First, create an admin user in ResolutionInitializer:
	    User admin = new User("admin","{bcrypt}$2a$10$bTu5ilpT4YILX8dOWM/05efJnoSlX4ElNnjhNopL9aPoRyUgvXAYa");
	    admin.setFullName("Admin Adminson");
	    admin.grantAuthority("ROLE_ADMIN");
	    admin.grantAuthority("resolution:read");
	    admin.grantAuthority("resolution:write");
	    user.grantAuthority("user:read");
	    this.users.save(admin);
	    //Second, update the authorization expressions for both ResolutionController#read methods.
        //to:
        //@PostFilter("filterObject.owner == authentication.name || hasRole('ADMIN')")
	    //then we can logging in with admin/password will show all resolutions.
	}
}
