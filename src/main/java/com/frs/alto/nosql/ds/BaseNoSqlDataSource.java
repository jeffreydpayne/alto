package com.frs.alto.nosql.ds;

import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.id.IdentifierGenerator;
import com.frs.alto.id.LocalUUIDGenerator;

public abstract class BaseNoSqlDataSource implements NoSqlDataSource {
	
	private IdentifierGenerator identifierGenerator = new LocalUUIDGenerator();
	private boolean autoCreateEnabled = false;
	
	@Override 
	public String nextId(BaseDomainObject domain) {
		return identifierGenerator.generateStringIdentifier(domain);
	}

	@Override
	public boolean isAutoCreateEnabled() {
		return autoCreateEnabled;
	}

	public void setAutoCreateEnabled(boolean autoCreateEnabled) {
		this.autoCreateEnabled = autoCreateEnabled;
	}

	public IdentifierGenerator getIdentifierGenerator() {
		return identifierGenerator;
	}

	public void setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
		this.identifierGenerator = identifierGenerator;
	}
	
	
	

}
