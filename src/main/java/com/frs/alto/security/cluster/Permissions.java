package com.frs.alto.security.cluster;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD}) 

public @interface Permissions {

	String[] value() default {};
	PermissionGroupRule grouping() default PermissionGroupRule.ALL;
	
}
