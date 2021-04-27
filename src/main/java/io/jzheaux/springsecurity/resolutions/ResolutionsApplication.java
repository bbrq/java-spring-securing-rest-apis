package io.jzheaux.springsecurity.resolutions;

import static org.springframework.http.HttpMethod.GET;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication
public class ResolutionsApplication extends WebSecurityConfigurerAdapter{

	public static void main(String[] args) {
		SpringApplication.run(ResolutionsApplication.class, args);
	}

	// override the Spring Security default UserDetailsService,
	/*
	 * now that users actually have authorities, remove the hard-coded
	 * resolution:read permission from JdbcUserDetailsManager definition in
	 * ResolutionsApplication.
	 */
	/*
	 * @Bean // replace in-memory user store with a JDBC-based one,
	 * JdbcUserDetailsManager. public UserDetailsService
	 * userDetailsService(DataSource dataSource) {
	 * 
	 * return new InMemoryUserDetailsManager(
	 * org.springframework.security.core.userdetails.User .withUsername("user") //To
	 * encode a password : leave the {bcrypt} prefix. .password(
	 * "{bcrypt}$2a$10$MywQEqdZFNIYnx.Ro/VQ0ulanQAl34B5xVjK2I/SDZNVGS5tHQ08W")
	 * .authorities("resolution:read") .build());
	 * 
	 * 
	 * //replace the use of InMemoryUserDetailsManager with an instance of
	 * JdbcUserDetailsManager return new JdbcUserDetailsManager(dataSource) {
	 * 
	 * @Override protected List<GrantedAuthority> loadUserAuthorities(String
	 * username) { return AuthorityUtils.createAuthorityList("resolution:read"); }
	 * }; }
	 */
	
	/*
	 * @Bean UserDetailsService userDetailsService(DataSource dataSource) { return
	 * new JdbcUserDetailsManager(dataSource); }
	 */	
	@Bean
	UserDetailsService userDetailsService(UserRepository users) {
	    return new UserRepositoryUserDetailsService(users);
	}
	
	/*
	override configure(HttpSecurity http) method to:
		Specify that /resolutions and GET /resolution/** require the resolution:read permission.
		Specify that all other endpoints require the resolution:write permission.
		And set HTTP Basic as the only authentication mechanism allowed.
	 * */
	@Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            //to replace following with method-base security, 
            //annotate each request-mapped method in ResolutionController with
            //@PreAuthorixe to indicate what authority that method requires
            /*.authorizeRequests(authz -> authz
            	//add authorization rules via the Spring Security DSL
                .mvcMatchers(GET, "/resolutions", "/resolution/**").hasAuthority("resolution:read")
                .anyRequest().hasAuthority("resolution:write"))
            .httpBasic(basic -> {}); ==>*/
        	.authorizeRequests(authz -> authz
                .anyRequest().authenticated())
            .httpBasic(basic -> {});
        //more on above code:
        /*Clearly in a large application, pre-authorizing using filter expressions is 
         * going to be quite a bit easier than method-based in the long run. It's a bit of a 
         * temptation to want to address it at the method level and avoid the DSL. Note, however,
         * that we've gone from having our authorization declarations centralized to having 
         * them dispersed throughout the application which is a drawback.
         * That said, we'll be adding additional annotations in just a moment that 
         * can't be expressed in the filter-based fashion, so method-based security 
         * certainly has its place.*/
        
        
        
        /*at this point is that the application is no longer allowing a login page. 
          navigate to http://localhost:8080/resolutions, you'll instead see a modal dialogue, 
          which is how browsers respond to requests for HTTP Basic authentication.
          if haswrite tries to use the /resolutions endpoint, they'll get a 403:
          curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/resolutions
          403*/
    }

}
