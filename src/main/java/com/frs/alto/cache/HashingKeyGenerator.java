package com.frs.alto.cache;

import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class HashingKeyGenerator implements CacheKeyGenerator {
	
	private CacheKeyGenerator keyGenerator = null;

	@Override
	public String generateGlobalKey(String regionId, String baseKey) {
		return toHash(keyGenerator.generateGlobalKey(regionId, baseKey));
	}

	@Override
	public String generateRegionKey(String baseKey) {
		return toHash(keyGenerator.generateRegionKey(baseKey));
	}
	
	protected String toHash(String rawKey) {
		return DigestUtils.sha256Hex(rawKey);
	}

	public CacheKeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	public void setKeyGenerator(CacheKeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}
	
	

}
