package com.frs.alto.nosql.ds;

import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.id.IdentifierGenerator;
import com.frs.alto.id.LocalUUIDGenerator;

public abstract class BaseNoSqlDataSource implements NoSqlDataSource {
	
	private IdentifierGenerator identifierGenerator = new LocalUUIDGenerator();
	private boolean autoCreateEnabled = false;
	
	protected String nextId(BaseDomainObject domain) {
		return identifierGenerator.generateStringIdentifier(domain);
	}

	@Override
	public boolean isAutoCreateEnabled() {
		return autoCreateEnabled;
	}

	public void setAutoCreateEnabled(boolean autoCreateEnabled) {
		this.autoCreateEnabled = autoCreateEnabled;
	}
	
	
	

}
