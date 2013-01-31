package com.frs.alto.nosql.mapper;

import java.util.HashMap;
import java.util.Map;

public class NoSqlClassMappingImpl implements NoSqlClassMapping {
	
	private String className = null;
	private String tableName = null;
	private String hashKeyProperty = null;
	private String hashKeyAttribute = null;
	private String rangeKeyProperty = null;
	private String rangeKeyAttribute = null;
	private Class rangeKeyType = null;
	private String cacheRegion = null;
	
	private Map<String, String> propertyToAttributeNameMap = null;

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public String getHashKeyProperty() {
		return hashKeyProperty;
	}

	@Override
	public String getHashKeyAttribute() {
		return hashKeyAttribute;
	}

	@Override
	public String getRangeKeyProperty() {
		return rangeKeyProperty;
	}

	@Override
	public String getRangeKeyAttribute() {
		return rangeKeyAttribute;
	}

	@Override
	public Class getRangeKeyType() {
		return rangeKeyType;
	}

	@Override
	public String getCacheRegion() {
		return cacheRegion;
	}

	@Override
	public Map<String, String> getPropertyToAttributeNameMap() {
		return propertyToAttributeNameMap;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setHashKeyProperty(String hashKeyProperty) {
		this.hashKeyProperty = hashKeyProperty;
	}

	public void setHashKeyAttribute(String hashKeyAttribute) {
		this.hashKeyAttribute = hashKeyAttribute;
	}

	public void setRangeKeyProperty(String rangeKeyProperty) {
		this.rangeKeyProperty = rangeKeyProperty;
	}

	public void setRangeKeyAttribute(String rangeKeyAttribute) {
		this.rangeKeyAttribute = rangeKeyAttribute;
	}

	public void setRangeKeyType(Class rangeKeyType) {
		this.rangeKeyType = rangeKeyType;
	}

	public void setCacheRegion(String cacheRegion) {
		this.cacheRegion = cacheRegion;
	}

	public void setPropertyToAttributeNameMap(
			Map<String, String> propertyToAttributeNameMap) {
		this.propertyToAttributeNameMap = propertyToAttributeNameMap;
	}
	
	public void addPropertyToAttributeMapping(String propertyName, String attributeName) {
		if (propertyToAttributeNameMap == null) {
			propertyToAttributeNameMap = new HashMap<String, String>();
		}
		propertyToAttributeNameMap.put(propertyName, attributeName);
	}

}
