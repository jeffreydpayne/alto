package com.frs.alto.security.hmac;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.GenericFilterBean;

public class HmacSecurityFilter extends GenericFilterBean {
	
	static final String FILTER_APPLIED = "__spring_security_hmacsf_applied";
	
	private String headerPrefix = "HMAC";
	private Integer validTimeWindowInMinutes = 15;
	private HmacCredentialsRepository credentialsRepository = null;
	private HmacStringAssembler assembler = new DefaultHmacStringAssembler();
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String authHeader = request.getHeader("Authorization");
        
        if ( (authHeader == null) || !authHeader.startsWith(headerPrefix)) {
        	chain.doFilter(req, res);
        	return;
        }
        
        String[] tokens = StringUtils.split(authHeader, " ");
        if (tokens.length != 2) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
            return;
        }
        String authToken = tokens[1];
        
        tokens = StringUtils.split(authToken, ":");
        if (tokens.length != 2) {
        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
            return;
        }
        String apiId = tokens[0];
        String presentedHash = tokens[1];
        
        HmacPrincipal principalWrapper = credentialsRepository.getPrincipalByApiId(apiId);
        
        if ( (principalWrapper == null) || !principalWrapper.isEnabled() ) {
        	if (principalWrapper != null) {
        		credentialsRepository.authenticationFailed(principalWrapper);
        	}
        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
            return;
        }
        
        String dateString = request.getHeader("Date");
        if (dateString == null) {
        	credentialsRepository.authenticationFailed(principalWrapper);
        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
            return;
        }
        
        //validate date header
        Date parsedDate = null;
        try {
        	parsedDate = dateFormat.parse(dateString);
        }
        catch (Exception e) {
        	credentialsRepository.authenticationFailed(principalWrapper);
        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
            return;
        }
        
        long rightNow = System.currentTimeMillis();
        long presentedDate = parsedDate.getTime();
        
        long maxDiff = validTimeWindowInMinutes * 60 * 1000;
        
        if (Math.abs(rightNow - presentedDate) > maxDiff) {
        	credentialsRepository.authenticationFailed(principalWrapper);
        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
            return;
        }
        
        String stringToSign = assembler.assemble(request, principalWrapper);
        
        if (stringToSign == null) {
        	credentialsRepository.authenticationFailed(principalWrapper);
        	response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
            return;
        }
        
        String expectedHash = null;
        
    	try {
    		Mac mac = Mac.getInstance("HmacSHA1");
    		SecretKeySpec secret = new SecretKeySpec(Base64.decodeBase64(principalWrapper.getSecretKey().getBytes()),"HmacSHA1");
    		mac.init(secret);
    		byte[] digest = mac.doFinal(stringToSign.getBytes());
    		expectedHash = new String(Base64.encodeBase64(digest));
		}
		catch (Exception e) {
			throw new SecurityException(e);
		}
        
    	
    	if (!expectedHash.equals(presentedHash)) {
    		credentialsRepository.authenticationFailed(principalWrapper);
    		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed");
            return;
    	}
		
    	UserDetails details = principalWrapper.getUser();
    	
    	PreAuthenticatedAuthenticationToken authResult = new PreAuthenticatedAuthenticationToken(details, principalWrapper, details.getAuthorities());
    	authResult.setAuthenticated(true);
    	authResult.setDetails(principalWrapper.getUser());
    	
    	
    	SecurityContextHolder.getContext().setAuthentication(authResult);
    	
    	credentialsRepository.authenticationSuccessful(principalWrapper);
    	
    	chain.doFilter(req, res);

	}

	public void setCredentialsRepository(HmacCredentialsRepository credentialsRepository) {
		this.credentialsRepository = credentialsRepository;
	}

	public void setHeaderPrefix(String headerPrefix) {
		this.headerPrefix = headerPrefix;
	}

	public void setValidTimeWindowInMinutes(Integer validTimeWindowInMinutes) {
		this.validTimeWindowInMinutes = validTimeWindowInMinutes;
	}
	
	
	

}
