package com.frs.alto.nosql.dao;

import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.nosql.mapper.NoSqlObjectMapper;

/**
 * Designed for situations where the object mapper is a shared spring bean, probably with
 * mappings configured via XML, JSON or annotations.
 * 
 * @author thelifter
 *
 * @param <T>
 * @param <R>
 */

public abstract class ExternalMapperNoSqlDaoSupport<T extends BaseDomainObject, R extends Number & CharSequence> extends NoSqlDaoSupport<T, R> {
	
	private NoSqlObjectMapper objectMapper;

	@Override
	protected NoSqlObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	public void setObjectMapper(NoSqlObjectMapper mapper) {
		this.objectMapper = mapper;
	}
	
	

}
