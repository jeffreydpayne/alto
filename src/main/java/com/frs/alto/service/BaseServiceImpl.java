package com.frs.alto.service;

import java.util.Collection;

import com.frs.alto.dao.BaseDao;
import com.frs.alto.domain.BaseDomainObject;

public abstract class BaseServiceImpl<T extends BaseDomainObject, D extends BaseDao<T>> implements BaseService<T> {

	
	protected abstract D getPrimaryDao();

	@Override
	public String save(T anObject) {
		return getPrimaryDao().save(anObject);
	}

	@Override
	public void delete(T anObject) {
		getPrimaryDao().delete(anObject);
	}

	@Override
	public void delete(String id) {
		getPrimaryDao().delete(id);
	}

	@Override
	public T findById(String id) {
		return getPrimaryDao().findById(id);
	}

	@Override
	public Collection<String> findAllIds() {
		return getPrimaryDao().findAllIds();
	}

	@Override
	public Collection<T> findAll() {
		return getPrimaryDao().findAll();
	}
	
}
