package com.frs.alto.nosql.mapper;

import java.util.Map;

public interface NoSqlClassMapping {
	
	public String getClassName();
	public String getTableName();
	public String getHashKeyProperty();
	public String getHashKeyAttribute();
	public String getRangeKeyProperty();
	public String getRangeKeyAttribute();
	public Class getRangeKeyType();
	public String getCacheRegion();
	
	//property to attribute names
	public Map<String, String> getPropertyToAttributeNameMap();
	
	

}
