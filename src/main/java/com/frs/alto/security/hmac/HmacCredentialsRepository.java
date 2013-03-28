package com.frs.alto.security.hmac;

public interface HmacCredentialsRepository {
	/**
	 * Look up the principal by the apiId.  We use a wrapper object because the 
	 * user might not be specific enough.  For example, a single user might have
	 * multiple mobile devices, each with different credentials.
	 * 
	 * @param apiId
	 * @return
	 */
	public HmacPrincipal getPrincipalByApiId(String apiId);
	
	/**
	 * Called when an hmac request is successful.  Intended for logging or otherwise letting the client
	 * application track the success.
	 * 
	 * @param principal
	 */
	public void authenticationSuccessful(HmacPrincipal principal);
	
	/**
	 * Called when an hmac request fails.  Intended for logging or otherwise letting the client
	 * application track the failure.
	 * 
	 * @param principal
	 */
	
	public void authenticationFailed(HmacPrincipal principal);
	

}
