package com.frs.alto.nosql.mapper;

public class HashKeyWithNumericRangeKey implements NoSqlKey<Number> {

	private String hashKey = null;
	private Number number = null;
	
	public HashKeyWithNumericRangeKey(String hashKey, Number number) {
		this.hashKey = hashKey;
		this.number = number;
	}
	
	@Override
	public String composeUniqueString() {
		return hashKey + "#" + number.toString();
	}

	@Override
	public void setHashKey(String key) {
		this.hashKey = key;		
	}

	@Override
	public void setRangeKey(Number rangeKey) {
		this.number = rangeKey;		
	}

	@Override
	public String getHashKey() {
		return hashKey;
	}
	
	@Override
	public Number getRangeKey() {
		return number;	
	}
	
	
	
	
	
}
