package com.frs.alto.dao.couchbase;

public @interface TemporalViewWithHashKey {
	
	String name();
	String hashKey();
	String timestampKey();
	String rangeKey();
	

}
