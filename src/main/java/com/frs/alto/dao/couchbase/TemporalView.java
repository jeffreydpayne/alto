package com.frs.alto.dao.couchbase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TemporalView {
	
	String name();
	String timestampKey();
	String rangeKey();
	

}
