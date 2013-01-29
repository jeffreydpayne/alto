package com.frs.alto.id;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.StringUtils;


public class Base32IdentifierGenerator implements IdentifierGenerator {
	
	private IdentifierGenerator rootGenerator = null;
	private Base32 encoder = new Base32();

	public IdentifierGenerator getRootGenerator() {
		return rootGenerator;
	}

	public void setRootGenerator(IdentifierGenerator rootGenerator) {
		this.rootGenerator = rootGenerator;
	}

	@Override
	public String generateStringIdentifier(Object object) {
		return StringUtils.remove(encoder.encodeAsString(getRootGenerator().generateRawIdentifier(object)), '=');
	}

	@Override
	public byte[] generateRawIdentifier(Object object) {
		return generateStringIdentifier(object).getBytes();
	}
	
	
	
	
	

}
