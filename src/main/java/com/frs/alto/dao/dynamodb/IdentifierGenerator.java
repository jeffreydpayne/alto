package com.frs.alto.dao.dynamodb;


public interface IdentifierGenerator {
	
	public String generateStringIdentifier(Object object);
	public byte[] generateRawIdentifier(Object object);

}
