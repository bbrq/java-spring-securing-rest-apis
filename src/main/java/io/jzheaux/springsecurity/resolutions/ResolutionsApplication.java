package io.jzheaux.springsecurity.resolutions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@SpringBootApplication
public class ResolutionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResolutionsApplication.class, args);
	}

	//override the Spring Security default UserDetailsService
	@Bean
	public UserDetailsService userDetailsService() {
	    return new InMemoryUserDetailsManager(
	        org.springframework.security.core.userdetails.User
	            .withUsername("user")
	            //To encode a password : leave the {bcrypt} prefix.
	            .password("{bcrypt}$2a$10$MywQEqdZFNIYnx.Ro/VQ0ulanQAl34B5xVjK2I/SDZNVGS5tHQ08W")
	            .authorities("resolution:read")
	            .build());
	}
}
