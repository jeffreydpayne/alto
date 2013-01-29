package com.frs.alto.nosql.mapper;

import java.io.Serializable;

public interface NoSqlKey<R extends Serializable> {

	public String composeUniqueString();
	public void setHashKey(String key);
	public String getHashKey();
	public void setRangeKey(R rangeKey);
	public R getRangeKey();
	
}
