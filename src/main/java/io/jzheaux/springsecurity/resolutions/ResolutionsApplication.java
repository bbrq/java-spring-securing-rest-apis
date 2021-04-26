package io.jzheaux.springsecurity.resolutions;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

@SpringBootApplication
public class ResolutionsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResolutionsApplication.class, args);
	}

	// override the Spring Security default UserDetailsService,
	@Bean
	// replace in-memory user store with a JDBC-based one, JdbcUserDetailsManager.
	public UserDetailsService userDetailsService(DataSource dataSource) {
		/*
		 * return new InMemoryUserDetailsManager(
		 * org.springframework.security.core.userdetails.User .withUsername("user") //To
		 * encode a password : leave the {bcrypt} prefix. .password(
		 * "{bcrypt}$2a$10$MywQEqdZFNIYnx.Ro/VQ0ulanQAl34B5xVjK2I/SDZNVGS5tHQ08W")
		 * .authorities("resolution:read") .build());
		 */

		//replace the use of InMemoryUserDetailsManager with an instance of JdbcUserDetailsManager
		return new JdbcUserDetailsManager(dataSource) {
			@Override
			protected List<GrantedAuthority> loadUserAuthorities(String username) {
				return AuthorityUtils.createAuthorityList("resolution:read");
			}
		};
	}

}
