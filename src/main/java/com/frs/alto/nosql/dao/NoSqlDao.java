package com.frs.alto.nosql.dao;

import java.io.Serializable;
import java.util.Collection;

import com.frs.alto.dao.BaseDao;
import com.frs.alto.domain.BaseDomainObject;

public interface NoSqlDao<T extends BaseDomainObject, R extends Serializable> extends BaseDao<T> {

	
	public T findByHashKey(String hashKey);
	public T findById(String hashKey, R rangeKey);
	public Collection<T> findByHashKeys(Collection<String> hashKeys);
	public Collection<T> findWholeRange(String hashKey);
	public Collection<T> findByRange(String hashKey, R startRange, R endRange);
	public Collection<T> findUpperRange(String hashKey, R startRange);
	public Collection<T> findLowerRange(String hashKey, R endRange);
	public void delete(String hashKey);
	public void delete(Collection<String> hashKeys);
	public String save(T domain);
	public Collection<String> save(Collection<T> domains);
	public String nextId(BaseDomainObject domain);
	
}
