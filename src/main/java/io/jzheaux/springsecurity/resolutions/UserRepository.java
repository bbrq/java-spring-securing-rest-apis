package io.jzheaux.springsecurity.resolutions;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;


public interface UserRepository extends CrudRepository<User, UUID>{
	 Optional<User> findByUsername(String username);
}
