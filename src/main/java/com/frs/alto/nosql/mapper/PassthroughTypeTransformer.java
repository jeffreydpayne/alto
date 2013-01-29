package com.frs.alto.nosql.mapper;

public class PassthroughTypeTransformer implements TypeTransformer {

	@Override
	public Object toAttributeType(Object value, Class domainType) {
		return value;
	}

	@Override
	public Object toDomainValue(Object value, Class domainType) {
		return value;
	}

}
