package com.frs.alto.nosql.mapper;

public class HashKeyOnly implements NoSqlKey<String> {

	private String hashKey = null;
	
	public HashKeyOnly(String key) {
		this.hashKey = key;
	}
	
	@Override
	public String composeUniqueString() {
		return hashKey;
	}

	@Override
	public void setHashKey(String key) {
		this.hashKey = key;		
	}

	@Override
	public void setRangeKey(String rangeKey) {
		throw new UnsupportedOperationException("Range keys not supported");		
	}

	@Override
	public String getHashKey() {
		return hashKey;
	}
	
	@Override
	public String getRangeKey() {
		throw new UnsupportedOperationException("Range keys not supported");	
	}
	
	
	
	
	
}
