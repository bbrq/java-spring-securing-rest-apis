package io.jzheaux.springsecurity.resolutions;

import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
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

	public ResolutionController(ResolutionRepository resolutions) {
		this.resolutions = resolutions;
	}

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
		return this.resolutions.findAll();
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
	@PreAuthorize("hasAuthority('resolution:read')")
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
