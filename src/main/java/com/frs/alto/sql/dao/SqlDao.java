package com.frs.alto.sql.dao;

import java.util.Collection;

import com.frs.alto.dao.BaseDao;
import com.frs.alto.domain.BaseDomainObject;

public interface SqlDao<T extends BaseDomainObject> extends BaseDao<T> {
	
	public void delete(String hashKey);
	public void delete(Collection<String> hashKeys);
	public String save(T domain);
	public Collection<String> save(Collection<T> domains);
	public String nextId(BaseDomainObject domain);
	
}
