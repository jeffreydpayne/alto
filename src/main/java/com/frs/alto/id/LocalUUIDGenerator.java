package com.frs.alto.id;

import org.apache.commons.id.uuid.VersionOneGenerator;


public class LocalUUIDGenerator implements IdentifierGenerator {
	
	private static VersionOneGenerator generator = VersionOneGenerator.getInstance();

	@Override
	public String generateStringIdentifier(Object object) {
		return generator.nextUUID().toString();
	}

	@Override
	public byte[] generateRawIdentifier(Object object) {
		return generator.nextRawUUID();
	}
	
	

}
