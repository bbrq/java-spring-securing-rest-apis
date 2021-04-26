package io.jzheaux.springsecurity.resolutions;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Because a UserDetailsService should throw a UsernameNotFoundException if it
 * can't find the user, the simplest UserDetailsService is one that does only
 * that.
 */
public class UserRepositoryUserDetailsService implements UserDetailsService {
	// UserDetailsService to depend on Spring Data UserRepository.To do this,
	// add UserRepository as a dependency
	private final UserRepository users;
	public UserRepositoryUserDetailsService(UserRepository users) {
		this.users = users;
	}

	@Override
	public UserDetails loadUserByUsername(String username) {
		throw new UsernameNotFoundException("no user");
	}
}