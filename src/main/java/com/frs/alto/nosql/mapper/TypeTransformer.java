package com.frs.alto.nosql.mapper;

import java.util.Date;

public interface TypeTransformer {
	
	public Object toAttributeValue(Object value, Class domainType);
	public Object toDomainValue(Object value, Class domainType);
	public Date parseTimeStamp(String value);
	public String formatTimeStamp(Date date);

}
