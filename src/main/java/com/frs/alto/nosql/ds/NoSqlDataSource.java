package com.frs.alto.nosql.ds;

import java.util.Collection;

import com.frs.alto.domain.BaseDomainObject;
import com.frs.alto.nosql.mapper.NoSqlKey;
import com.frs.alto.nosql.mapper.NoSqlObjectMapper;

/**
 * Base interface for no sql hash key w/ range key style datasources.
 * 
 * @author jeffreydpayne
 *
 */

public interface NoSqlDataSource {
	
	public BaseDomainObject findByKey(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, NoSqlKey key);
	public Collection<BaseDomainObject> findByHashKeys(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, Collection<NoSqlKey> hashKeys);
	public Collection<BaseDomainObject> findWholeRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey);
	public Collection<BaseDomainObject> findByRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey, String startRange, String endRange);
	public Collection<BaseDomainObject> findByUpperRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey, String startRange);
	public Collection<BaseDomainObject> findByLowerRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper,  String hashKey, String endRange);
	public Collection<BaseDomainObject> findByRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey, Number startRange, Number endRange);
	public Collection<BaseDomainObject> findByUpperRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey, Number startRange);
	public Collection<BaseDomainObject> findByLowerRange(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, String hashKey, Number endRange);
	public void createTable(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper);
	public boolean tableExists(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper);
	public boolean isAutoCreateEnabled();
	
	public void delete(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, NoSqlKey key);
	public void delete(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper, Collection<NoSqlKey> keys);
	public String save(BaseDomainObject domain, NoSqlObjectMapper mapper);
	public Collection<String> save(Collection<BaseDomainObject> domains, NoSqlObjectMapper mapper);
	public Collection<String> findAllIds(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper);
	public Collection<BaseDomainObject> findAll(Class<? extends BaseDomainObject> clazz, NoSqlObjectMapper mapper);
	public String nextId(BaseDomainObject domain);
	

}
