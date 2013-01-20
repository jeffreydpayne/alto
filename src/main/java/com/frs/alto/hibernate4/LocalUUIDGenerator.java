package com.frs.alto.hibernate4;

import java.io.Serializable;

import org.apache.commons.id.uuid.VersionOneGenerator;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

public class LocalUUIDGenerator implements IdentifierGenerator 
{

	private static VersionOneGenerator generator = VersionOneGenerator.getInstance();
	
	public LocalUUIDGenerator() {
		super();
	}

	@Override
	public Serializable generate(SessionImplementor session, Object object)
			throws HibernateException {
		return generator.nextUUID().toString();
	}

	
	
}
