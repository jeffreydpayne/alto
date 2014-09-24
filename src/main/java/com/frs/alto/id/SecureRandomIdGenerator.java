package com.frs.alto.id;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base32;

public class SecureRandomIdGenerator implements IdentifierGenerator {
	
	private Base32 encoder = new Base32();
	private SecureRandom random = new SecureRandom();
	private int length = 32;


	@Override
	public String generateStringIdentifier(Object object) {
		return encoder.encodeAsString(generateRawIdentifier(object));
	}

	@Override
	public byte[] generateRawIdentifier(Object object) {
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return bytes;
	}

}
