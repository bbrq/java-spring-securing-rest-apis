package io.jzheaux.springsecurity.resolutions;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

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
        
        OAuth2AuthenticatedPrincipal principal = 
        		new UserOAuth2AuthenticatedPrincipal(user, jwt.getClaims(), authorities);
        
        //create an OAuth2AccessToken credentials
        OAuth2AccessToken credentials = new OAuth2AccessToken(BEARER, jwt.getTokenValue(), null, null);
        
        return new BearerTokenAuthentication(principal, credentials, authorities);
    }
	
	//Customize the User Principal:merging the UserDetails with OAuth2AuthenticatedPrincipal
	private static class UserOAuth2AuthenticatedPrincipal extends User
    		implements OAuth2AuthenticatedPrincipal {

		private final Map<String, Object> attributes;
	    private final Collection<GrantedAuthority> authorities;
	    public UserOAuth2AuthenticatedPrincipal(User user, Map<String, Object> attributes, Collection<GrantedAuthority> authorities) {
	        super(user);
	        this.attributes = attributes;
	        this.authorities = authorities;
	    }
	    @Override
	    public Map<String, Object> getAttributes() {
	        return this.attributes;
	    }
	    @Override
	    public Collection<GrantedAuthority> getAuthorities() {
	        return this.authorities;
	    }
	    @Override
	    public String getName() {
	        return this.username;
	    }
	
	}
}