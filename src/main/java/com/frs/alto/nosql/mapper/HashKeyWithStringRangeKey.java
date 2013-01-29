package com.frs.alto.nosql.mapper;

public class HashKeyWithStringRangeKey implements NoSqlKey<String> {

	private String hashKey = null;
	private String rangeKey = null;
	
	public HashKeyWithStringRangeKey(String hashKey, String rangeKey) {
		this.hashKey = hashKey;
		this.rangeKey = rangeKey;
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
		this.rangeKey = rangeKey;		
	}

	@Override
	public String getHashKey() {
		return hashKey;
	}
	
	@Override
	public String getRangeKey() {
		return rangeKey;	
	}
	
	
	
	
	
}
