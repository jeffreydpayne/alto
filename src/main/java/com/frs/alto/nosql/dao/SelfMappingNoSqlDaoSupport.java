package com.frs.alto.nosql.dao;

import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.nosql.mapper.NoSqlObjectMapper;

/**
 * Base dao for dao's that provide their own object mapper.  Reflection based mapping can be a performance bottleneck
 * in certain cases.
 * 
 * 
 * @author thelifter
 *
 * @param <T>
 * @param <R>
 */

public abstract class SelfMappingNoSqlDaoSupport<T extends BaseDomainObject, R extends Number & CharSequence> extends NoSqlDaoSupport<T, R> implements NoSqlObjectMapper {

	@Override
	protected NoSqlObjectMapper getObjectMapper() {
		return this;
	}

	
	
	
}
