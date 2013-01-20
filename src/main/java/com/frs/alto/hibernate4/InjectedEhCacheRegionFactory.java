package com.frs.alto.hibernate4;

import java.util.Properties;

import net.sf.ehcache.CacheManager;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.ehcache.EhCacheMessageLogger;
import org.hibernate.cache.ehcache.EhCacheRegionFactory;
import org.hibernate.cfg.Settings;
import org.jboss.logging.Logger;

public class InjectedEhCacheRegionFactory extends EhCacheRegionFactory {
	
	 	private static final EhCacheMessageLogger LOG = Logger.getMessageLogger(
	            EhCacheMessageLogger.class,
	            InjectedEhCacheRegionFactory.class.getName()
	    );


	    private CacheManager cacheManager = null;

		public InjectedEhCacheRegionFactory() {
			super();
		}
	
		public InjectedEhCacheRegionFactory(Properties prop) {
			super(prop);
		}

		/**
	     * {@inheritDoc}
	     */
	    public void start(Settings settings, Properties properties) throws CacheException {
	        this.settings = settings;
	        if ( manager != null ) {
	            LOG.attemptToRestartAlreadyStartedEhCacheProvider();
	            return;
	        }
	        
	        if (cacheManager != null) {
	        	manager = cacheManager;
	        }
	        else {
	        	super.start(settings, properties);
	        }
       
	    }

	
		public CacheManager getCacheManager() {
			return cacheManager;
		}

		public void setCacheManager(CacheManager cacheManager) {
			this.cacheManager = cacheManager;
		}
	    
	    
	    

}
