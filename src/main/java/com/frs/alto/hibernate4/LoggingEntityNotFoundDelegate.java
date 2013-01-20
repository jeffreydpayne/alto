package com.frs.alto.hibernate4;


import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.proxy.EntityNotFoundDelegate;

public class LoggingEntityNotFoundDelegate implements EntityNotFoundDelegate {
	
	private static Log log = LogFactory.getLog(LoggingEntityNotFoundDelegate.class);

	@Override
	public void handleEntityNotFound(String entityName, Serializable id) {
		log.error("Entity Not Found: " + entityName + "#" + id.toString());
	}
	
	

}
