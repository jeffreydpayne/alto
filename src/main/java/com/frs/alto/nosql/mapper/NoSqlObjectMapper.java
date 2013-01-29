package com.frs.alto.nosql.mapper;

import java.util.Map;

import com.frs.alto.domain.BaseDomainObject;

/**
 * Base interface for a service that handles column to property mappings.
 * 
 * 
 * @author thelifter
 *
 */

public interface NoSqlObjectMapper {
	
	public Map<String, Object> toAttributes(BaseDomainObject object);
	public BaseDomainObject instantiate(Class<? extends BaseDomainObject> clazz);
	public BaseDomainObject fromAttributes(BaseDomainObject instance, Map<String, Object> attributes);
	public NoSqlKey getKey(BaseDomainObject instance);
	public NoSqlKey getKey(Class clazz, String serializedKey);
	public Class getRangeKeyType(Class clazz);
	public String getHashKeyAttribute(Class clazz);
	public String getRangeKeyAttribute(Class clazz);
	public String getHashKeyProperty(Class clazz);
	public String getRangeKeyProperty(Class clazz);
	public String getCacheRegion(Class clazz);
	public String getTableName(Class clazz);

}
