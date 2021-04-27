package io.jzheaux.springsecurity.resolutions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableGlobalMethodSecurity(prePostEnabled=true)
@SpringBootApplication
public class ResolutionsApplication extends WebSecurityConfigurerAdapter{

	public static void main(String[] args) {
		SpringApplication.run(ResolutionsApplication.class, args);
	}
	
	@Autowired
    UserRepositoryJwtAuthenticationConverter authenticationConverter;
	
	

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
            .httpBasic(basic -> {})
            /*To activate Spring Security's support for JWT-based Bearer Token Authentication, 
		      1, add the appropriate dependencies and 2, specify the location of the authorization server.
		      3, configure your WebSecurityConfigurerAdapter to allow JWT-based Bearer Token Authentication.
		      add oauth2ResourceServer to the DSL configuration, specifying its jwt configuration value
		      
		      note:
		      Security with Spring is naturally declarative, and so WebSecurityConfigurerAdapter requires you to 
		      specify a whitelist of allowed authentication mechanisms.
		      The reason that simple Spring Boot applications can get away without this step is that 
		      Spring Boot creates its own instance of WebSecurityConfigurerAdapter for you if you haven't yourself.
              * */
            .oauth2ResourceServer(oauth2 -> oauth2
            		//to replace the JwtAuthenticationConverter
                    .jwt().jwtAuthenticationConverter(this.authenticationConverter))
            //To configure Spring Security to allow CORS handshakes, call the cors() method in the Spring Security DSL
            .cors(cors -> {});;
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
	
	
	/*In each @CrossOrigin annotation, we can specify which origins, methods, and headers we'll allow, 
	 * but this can be tedious in a large application.	Instead, we
	 * add these restrictions globally in a filter-based fashion.*/

	@Bean
	WebMvcConfigurer webMvcConfigurer() {
	    return new WebMvcConfigurer() {
	        @Override
	        public void addCorsMappings(CorsRegistry registry) {
	            registry.addMapping("/**")
	                // .maxAge(0) // if using local verification
	                /* CORS requests will only work from http://localhost:4000. only HEAD requests will be allowed by default.*/
	                .allowedOrigins("http://localhost:4000")
	                /*Extra Credit
					The @CrossOrigin annotation augments the global configuration. So, 
					even though globally you are saying only HEAD is allowed, 
					the @CrossOrigin annotation on ResolutionController#read will effectively whitelist GET just for /resolutions*/
	                //Now, the server will accept Basic credentials that are handed over by the browser.
	                /*WARNING: This is a powerful header, and should only be used when there is a very high degree of 
	                 * trust between the two applications. Misuse of when the credentials are passed on can lead to 
	                 * CSRF and other vulnerabilities. The app we've got so far is safe since we're only allowing 
	                 * credentials on GET /resolutions, and because the client app is declaratively passing the credentials, 
	                 * instead of implicitly, like with cookies.
	                 * In the remaining modules, we'll learn about a different authentication mechanism that can better 
	                 * address this.*/
	                .allowedMethods("HEAD")
	                .allowedHeaders("Authorization");
	        }
	    };
	}


	/*By default, Spring Security will extract each bearer token scope into its own GrantedAuthority, 
	 * prefixing it with SCOPE_ along the way.
	 * 
	Our app uses no prefix:	@PreAuthorize("hasAuthority('resolution:read')")	
	configure your project to maintain this approach, where no prefixes are used, overriding the default behavior.

	add a JwtAuthenticationConverter as a @Bean to tell Spring Security to not add any prefix to the scopes that it finds:
	Now, JWTs that have the same scopes as our users' authorities will work for the same requests.	
	*/
	/*
	 * UserDetailsService and JwtAuthenticationConverter are similar, but they have an important difference that 
	 * changes the way that we use each.
	 * JwtAuthenticationConverter doesn't have a dedicated interface - you'll notice that JwtAuthenticationConverter 
	 * is a concrete class. This means that if we want to create our own implementation, 
	 * we are actually implementing Converter<Jwt, AbstractAuthenticationToken>. As of the latest Spring Security, 
	 * this means that it won't pick up the bean automatically, and for this reason, we need to place it 
	 * on the DSL directly.
	 * */
	/*
	 * @Bean JwtAuthenticationConverter jwtAuthenticationConverter() {
	 * JwtAuthenticationConverter authenticationConverter = new
	 * JwtAuthenticationConverter(); JwtGrantedAuthoritiesConverter
	 * authoritiesConverter = new JwtGrantedAuthoritiesConverter();
	 * authoritiesConverter.setAuthorityPrefix("");
	 * authenticationConverter.setJwtGrantedAuthoritiesConverter(
	 * authoritiesConverter); return authenticationConverter; }
	 *//*
	instead of adding the JwtAuthenticationConverter. While more complex, the nice thing about this approach is 
	that methods and filters can know where a given authority came from - for example: 
	Was it an authority from the database or from the JWT? In many applications, this distinction doesn't matter 
	to post-authentication.
	*/
}
