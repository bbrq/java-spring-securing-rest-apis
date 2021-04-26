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
		
		//create a user
		User user = new User("user",
	            "{bcrypt}$2a$10$MywQEqdZFNIYnx.Ro/VQ0ulanQAl34B5xVjK2I/SDZNVGS5tHQ08W");
	    this.users.save(user);
	}
}
