package io.jzheaux.springsecurity.resolutions;

import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.stereotype.Component;

//add the class to the application context, calling it post
@Component("post")
public class ResolutionAuthorizer {
	//The MethodSecurityExpressionOperations instance is the root object of 
	//method-based security SpEL expressions. 
	//in ResolutionController, change the @PostFilter expression to:
	public boolean filter(MethodSecurityExpressionOperations operations) {
        //use the MethodSecurityExpressionOperations to programmatically 
		//perform the same logic as before: 
		//filterObject.owner == authentication.name || hasRole('ADMIN')
		if (operations.hasRole("ADMIN")) {
	        return true;
	    }
	    String name = operations.getAuthentication().getName();
	    Resolution resolution = (Resolution) operations.getFilterObject();
	    return resolution.getOwner().equals(name);
    }
}
