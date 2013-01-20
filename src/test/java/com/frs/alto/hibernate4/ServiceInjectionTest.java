package com.frs.alto.hibernate4;

import java.util.UUID;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.testng.annotations.Test;

import com.frs.alto.test.BaseSpringAwareTestCase;
import com.frs.alto.test.fixture.TestDomain;

@Test
public class ServiceInjectionTest extends BaseSpringAwareTestCase {
	
	
	public void testHibernateServiceInjection() throws Exception {
		
		SessionFactory factory = (SessionFactory)getApplicationContext().getBean("defaultSessionFactory");
		
		assertNotNull(factory);
		
		TestDomain domain = new TestDomain();
		domain.setName("test");
		
		Session session = factory.openSession();
		
		session.save(domain);
		
		
		TestDomain loadedVersion = (TestDomain)session.load(TestDomain.class, domain.getObjectIdentifier());
		
		assertNotNull(loadedVersion);
		
		session.close();
		
		
		session = factory.openSession();
		
		loadedVersion = (TestDomain)session.load(TestDomain.class, domain.getObjectIdentifier());
		
		assertNotNull(loadedVersion);
		
		session.close();
		
		
		
	}
	
}
