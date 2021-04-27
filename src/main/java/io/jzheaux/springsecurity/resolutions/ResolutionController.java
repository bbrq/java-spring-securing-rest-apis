package io.jzheaux.springsecurity.resolutions;

import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

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
	public Iterable<Resolution> read() {
		return this.resolutions.findAll();
	}

	@GetMapping("/resolution/{id}")
	@PreAuthorize("hasAuthority('resolution:read')")
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
	@Transactional
	public Optional<Resolution> revise(@PathVariable("id") UUID id, @RequestBody String text) {
		this.resolutions.revise(id, text);
		return read(id);
	}

	@PutMapping("/resolution/{id}/complete")
	@PreAuthorize("hasAuthority('resolution:read')")
	@Transactional
	public Optional<Resolution> complete(@PathVariable("id") UUID id) {
		this.resolutions.complete(id);
		return read(id);
	}
}
