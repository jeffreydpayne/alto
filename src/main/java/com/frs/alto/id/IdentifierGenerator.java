package com.frs.alto.id;


public interface IdentifierGenerator {
	
	public String generateStringIdentifier(Object object);
	public byte[] generateRawIdentifier(Object object);

}
