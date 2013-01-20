package com.frs.alto.cache.memcached;

import java.util.UUID;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.frs.alto.cache.AltoCache;
import com.frs.alto.test.fixture.TestChild;
import com.frs.alto.test.fixture.TestDomain;

@ContextConfiguration(locations={"classpath:spring-env-test-memcached.xml"})
public class HibernateMemcachedTest extends AbstractTestNGSpringContextTests {

	
	@Autowired
	private SessionFactory factory = null;
	
	@Autowired
	private AltoCache altoCache = null;
	
	@Test
	public void testCacheIntegration() throws Exception {
		
		
		long startSize = altoCache.getStatistics().getItemCount();
		
		Assert.assertNotNull(factory);
		
		TestDomain domain = new TestDomain();
		domain.setName("test");
		domain.setNaturalId(UUID.randomUUID().toString());
		
		TestChild child = new TestChild();
		child.setName("child1");
		domain.addChild(child);
		
		child = new TestChild();
		child.setName("child2");
		domain.addChild(child);
		
		child = new TestChild();
		child.setName("child3");
		domain.addChild(child);
		
		Session session = factory.openSession();
		
		session.save(domain);
		
		session.flush();
		
		
		TestDomain loadedVersion = (TestDomain)session.byId(TestDomain.class).load(domain.getObjectIdentifier());
		
		Assert.assertNotNull(loadedVersion);
		Assert.assertNotNull(loadedVersion.getChildren());
		Assert.assertTrue(loadedVersion.getChildren().size() == 3);
		
		session.close();
		

		session = factory.openSession();
		
		
		Query query = session.createQuery("SELECT o FROM TestDomain AS o ORDER BY o.name");
		query.setCacheable(true);
		
		query.list();
		
		session.flush();
		
		Assert.assertNotNull(loadedVersion);
		Assert.assertNotNull(loadedVersion.getChildren());
		Assert.assertTrue(loadedVersion.getChildren().size() == 3);
		
		query = session.createQuery("SELECT o FROM TestDomain AS o ORDER BY o.name");
		query.setCacheable(true);
		
		query.list();
		
		Assert.assertNotNull(loadedVersion);
		Assert.assertNotNull(loadedVersion.getChildren());
		Assert.assertTrue(loadedVersion.getChildren().size() == 3);
		
		loadedVersion = (TestDomain)session.byId(TestDomain.class).load(domain.getObjectIdentifier());
		
		Assert.assertNotNull(loadedVersion);
		Assert.assertNotNull(loadedVersion.getChildren());
		Assert.assertTrue(loadedVersion.getChildren().size() == 3);
		
		session.flush();
		session.close();
		
		session = factory.openSession();
		
		loadedVersion = (TestDomain)session.bySimpleNaturalId(TestDomain.class).load(domain.getNaturalId());
		Assert.assertNotNull(loadedVersion);
		Assert.assertNotNull(loadedVersion.getChildren());
		Assert.assertTrue(loadedVersion.getChildren().size() == 3);
		
		session.flush();
		session.close();
		
		session = factory.openSession();
		
		loadedVersion = (TestDomain)session.bySimpleNaturalId(TestDomain.class).load(domain.getNaturalId());
		Assert.assertNotNull(loadedVersion);
		Assert.assertNotNull(loadedVersion.getChildren());
		Assert.assertTrue(loadedVersion.getChildren().size() == 3);
				
		Assert.assertTrue(altoCache.getStatistics().getItemCount() > startSize);
			
				
	}
	
}
