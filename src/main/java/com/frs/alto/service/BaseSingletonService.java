package com.frs.alto.service;

import com.frs.alto.domain.BaseDomainObject;

public interface BaseSingletonService<T extends BaseDomainObject> extends BaseService<T> {
	
	public T findSingleton();

}
