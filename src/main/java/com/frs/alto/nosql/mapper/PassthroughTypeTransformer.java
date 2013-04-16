package com.frs.alto.nosql.mapper;

import java.util.Date;

public class PassthroughTypeTransformer implements TypeTransformer {

	@Override
	public Object toAttributeValue(Object value, Class domainType) {
		return value;
	}

	@Override
	public Object toDomainValue(Object value, Class domainType) {
		return value;
	}

	@Override
	public Date parseTimeStamp(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String formatTimeStamp(Date date) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
