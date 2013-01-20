package com.frs.alto.hibernate4;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.util.Assert;

public class SessionUtils {

	public static Session getSession(SessionFactory sessionFactory)
			throws HibernateException, IllegalStateException {

		Assert.notNull(sessionFactory, "No SessionFactory specified");
				
		return sessionFactory.getCurrentSession();
		
	}

	
}
