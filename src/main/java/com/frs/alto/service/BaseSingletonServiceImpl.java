package com.frs.alto.service;

import java.util.Collection;

import com.frs.alto.dao.BaseDao;
import com.frs.alto.domain.BaseDomainObject;

public abstract class BaseSingletonServiceImpl<T extends BaseDomainObject, D extends BaseDao<T>> extends BaseServiceImpl<T, D> implements BaseSingletonService<T> {

	@Override
	public T findSingleton() {
		Collection<T> results = findAll();
		if (results.size() > 0) {
			return results.iterator().next();
		}
		else {
			try {
				return (T)getDomainClass().newInstance();
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}

	
	
}
