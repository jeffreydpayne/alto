package com.frs.alto.nosql.mapper;

public interface TypeTransformer {
	
	public Object toAttributeValue(Object value, Class domainType);
	public Object toDomainValue(Object value, Class domainType);

}
