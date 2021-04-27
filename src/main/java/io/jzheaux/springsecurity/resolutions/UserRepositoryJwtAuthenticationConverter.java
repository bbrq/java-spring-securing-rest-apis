package io.jzheaux.springsecurity.resolutions;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

/**
 * create customized JWT-to-Authentication converter that will query
 * UserRepository to augment the final authentication statement.
 */
@Component
public class UserRepositoryJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

	private final UserRepository users;
	private final JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();	
	@Autowired
    UserRepositoryJwtAuthenticationConverter authenticationConverter;

	public UserRepositoryJwtAuthenticationConverter(UserRepository users) {
		this.users = users;
		this.authoritiesConverter.setAuthorityPrefix("");
	}
	
	//look up the user using the JWT's sub claim. The result will be a JwtAuthenticationToken
	//that contains the authorities extracted from the JWT:
	@Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String username = jwt.getSubject();
        User user = this.users.findByUsername(username)
             .orElseThrow(() -> new UsernameNotFoundException("no user"));
        Collection<GrantedAuthority> authorities = this.authoritiesConverter.convert(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }
}