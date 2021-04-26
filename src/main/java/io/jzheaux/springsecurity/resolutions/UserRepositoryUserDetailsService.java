package io.jzheaux.springsecurity.resolutions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.repository.CrudRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

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
	public UserDetails loadUserByUsername(String username) 
		throws UsernameNotFoundException {
		//throw new UsernameNotFoundException("no user");
		return this.users.findByUsername(username)
		        .map(BridgeUser::new)
		        .orElseThrow(() -> new UsernameNotFoundException("invalid user"));
	}
	
	/**
	 * convert your User entity into a UserDetails to satisfy the UserDetailsService contract.
	 * */
	private static class BridgeUser extends User implements UserDetails {
        public BridgeUser(User user) {
            super(user);
        }
        public List<GrantedAuthority> getAuthorities() {
            return this.userAuthorities.stream()
                .map(UserAuthority::getAuthority)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        }
        public boolean isAccountNonExpired() {
            return this.enabled;
        }
        public boolean isAccountNonLocked() {
            return this.enabled;
        }
        public boolean isCredentialsNonExpired() {
            return this.enabled;
        }
        
        @Repository
        public interface UserRepository extends CrudRepository<User, UUID> {
            Optional<User> findByUsername(String username);
        }
    }
}