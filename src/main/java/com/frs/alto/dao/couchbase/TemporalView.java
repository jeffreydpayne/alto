package com.frs.alto.dao.couchbase;

public @interface TemporalView {
	
	String name();
	String timestampKey();
	String rangeKey();
	

}
