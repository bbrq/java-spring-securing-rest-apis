package io.jzheaux.springsecurity.resolutions;

import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * to enable @EnableGlobalMethodSecurity(prePostEnabled = true) on
 * ResolutionApplication
 * annotate each request-mapped method in ResolutionController with
 * @PreAuthorixe to indicate what authority that method requires
 *
 */
@RestController
public class ResolutionController {
	private final ResolutionRepository resolutions;
	private final UserRepository users;
    public ResolutionController(ResolutionRepository resolutions, UserRepository users) {
        this.resolutions = resolutions;
        this.users = users;
    }

	//open the /resolutions endpoint to CORS requests
	/*(The reason to specify a maxAge of 0 is so that the browser doesn't cache any CORS preflight responses 
	 * while you are making changes throughout the module.)
	 * What the @CrossOrigin annotation does is alert Spring MVC to manage the CORS handshake. 
	 * However, because Spring Security requires authentication on all endpoints, we also need to add some 
	 * configuration to Spring Security, which we'll see in the next task.
	 * Extra Credit
	 * It's not very common for HTTP Basic and CORS to go together, as you'll see by the end of this module. 
	 * Note; however, that these configurations will come in handy as we change over to JWT, 
	 * a more modern authentication mechanism.

Check out the final task in this module for details on why securing an application that uses both HTTP Basic and CORS can be challenging.*/
	@CrossOrigin(allowCredentials = "true")//(maxAge = 0) if locally verifying
	@GetMapping("/resolutions")
	@PreAuthorize("hasAuthority('resolution:read')")
	//filter results from a query, only returning the ones that belong to the logged-in user.
	//so this endpoint will only return resolutions that belong to the logged-in user.
	/*note: For large queries, this option doesn't really scale since it's having to pull 
	 * large result sets from the database, hydrate them into instances of Resolution, 
	 * only to simply throw away the majority of them. This causes needless GC pressure.
	 * Check out the next step for a more scalable approach.*/
	//@PostFilter("filterObject.owner == authentication.name || hasRole('ADMIN')")
	//The MethodSecurityExpressionOperations instance in ResolutionAuthorizer.filter()
	//is the root object of method-based security SpEL expressions. 
	@PostFilter("@post.filter(#root)")
	public Iterable<Resolution> read() {
		Iterable<Resolution> resolutions = this.resolutions.findAll();
		
		//get the current authentication
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		//only authentications with the user:read authority will see the user's PII
		/*use @CurrentSecurityContext also works, This shows you another way to get the currently logged in user,
		 * In a real application, this comes in handy in non-controller layers when you need to get 
		 * the current user and can use method-injection to get it passed in to you.*/
		boolean hasuserRead = authentication.getAuthorities().contains(new SimpleGrantedAuthority("user:read"));
		if (hasuserRead) {
		
			/*The problem we've got here is that we're including PII in our response, and 
			 * having the resolution:read authority probably isn't a strong enough indication that 
			 * something untrusted like an OAuth 2.0 client should be allowed to view that info. 
			 * We'll need something finer-grained than method-based security in this case.*/
			for (Resolution resolution : resolutions) {
				String name = this.users.findByUsername(resolution.getOwner())
						.map(User::getFullName).orElse("none");
				resolution.setText(resolution.getText() + ", by " + name);
			}
		}
	    return resolutions;
	}

	@GetMapping("/resolution/{id}")
	@PreAuthorize("hasAuthority('resolution:read')")
	//prevent an Insecure Direct Object Reference using the @PostAuthorize annotation
	//to compare the currently logged-in user to the owner of the Resolution retrieved
	//so if a user obtains the id of a Resolution that doesn't belong to them, 
	//the API call will return a 403
	@PostAuthorize("returnObject.orElse(null)?.owner == authentication.name")
	//@PostFilter("filterObject.owner == authentication.name || hasRole('ADMIN')")
	//The MethodSecurityExpressionOperations instance in ResolutionAuthorizer.filter()
	//is the root object of method-based security SpEL expressions. 
	@PostFilter("@post.filter(#root)")
	public Optional<Resolution> read(@PathVariable("id") UUID id) {
		return this.resolutions.findById(id);
	}

	//with custom UserDetailsService in place, we can access 
	//domain-specific information in controller method parameters.
	//@CurrentUsername will cause Spring to look up the username of the currently logged in user, 
	//and use it when calling this method.
	@PostMapping("/resolution")
	@PreAuthorize("hasAuthority('resolution:write')")
	public Resolution make(@CurrentUsername String owner, @RequestBody String text) {
		Resolution resolution = new Resolution(text, owner);
		return this.resolutions.save(resolution);
	}
	
	
	
	@PutMapping(path="/resolution/{id}/revise")
	@PreAuthorize("hasAuthority('resolution:read')")
	//@PostFilter("filterObject.owner == authentication.name || hasRole('ADMIN')")
	//The MethodSecurityExpressionOperations instance in ResolutionAuthorizer.filter()
	//is the root object of method-based security SpEL expressions. 
	@PostFilter("@post.filter(#root)")
	@Transactional
	public Optional<Resolution> revise(@PathVariable("id") UUID id, @RequestBody String text) {
		this.resolutions.revise(id, text);
		return read(id);
	}

	@PutMapping("/resolution/{id}/complete")
	@PreAuthorize("hasAuthority('resolution:read')")
	//@PostFilter("filterObject.owner == authentication.name || hasRole('ADMIN')")
	//The MethodSecurityExpressionOperations instance in ResolutionAuthorizer.filter()
	//is the root object of method-based security SpEL expressions. 
	@PostFilter("@post.filter(#root)")
	@Transactional
	public Optional<Resolution> complete(@PathVariable("id") UUID id) {
		this.resolutions.complete(id);
		return read(id);
	}
}
