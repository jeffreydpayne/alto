package com.frs.alto.dao.couchbase;

public @interface HashAndRangeKeyView {
	
	String name();
	String hashKey();
	String rangeKey();
	

}
