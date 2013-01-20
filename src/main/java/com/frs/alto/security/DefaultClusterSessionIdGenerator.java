package com.frs.alto.security;

import java.security.SecureRandom;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.InitializingBean;

public class DefaultClusterSessionIdGenerator implements ClusterSessionIdGenerator, InitializingBean {
	
	
	private String algorithm = null;
	
	private SecureRandom random = null;
	
	
	
	
	public DefaultClusterSessionIdGenerator() {
		super();
		try {
			afterPropertiesSet();
		} catch (Exception e) {}
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		
		if (algorithm != null) {
			random = SecureRandom.getInstance(algorithm);
		}
		else {
			random = new SecureRandom();
		}
		random.setSeed(System.currentTimeMillis());
		
		
	}

	
	@Override
	public String generate(HttpServletRequest request) {
		
		byte[] buffer = new byte[128];
		random.nextBytes(buffer);
		return DigestUtils.sha256Hex(buffer);
		
	}

}
