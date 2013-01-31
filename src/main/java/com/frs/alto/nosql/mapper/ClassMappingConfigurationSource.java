package com.frs.alto.nosql.mapper;

import java.util.Map;

public interface ClassMappingConfigurationSource {
		
	public Map<Class, NoSqlClassMapping> configure() throws Exception;

}
