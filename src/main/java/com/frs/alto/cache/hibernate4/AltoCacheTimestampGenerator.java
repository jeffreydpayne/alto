package com.frs.alto.cache.hibernate4;

import net.sf.ehcache.util.Timestamper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frs.alto.cache.AltoCache;

public class AltoCacheTimestampGenerator implements TimestampGenerator {
	
	private AltoCache altoCache = null;
	private String timestampKey = AltoCacheTimestampGenerator.class.getName();

	/**
     * Value for left shifting System.currentTimeMillis, freeing some space for the counter
     */
    public final int BIN_DIGITS = Integer.getInteger("net.sf.ehcache.util.Timestamper.shift", 12);

    /**
     * What is one milliseconds, based on "counter value reserved space", for this Timestamper
     */
    public final int ONE_MS = 1 << BIN_DIGITS;

    private final Logger LOG     = LoggerFactory.getLogger(Timestamper.class);

    protected long getValue() {
    	
    	Long result = (Long)altoCache.get(timestampKey, "value");
    	if (result == null) {
    		return 0L;
    	}
    	else {
    		return result.longValue();
    	}
    	
    }
    
    protected long getLogged() {
    	Long result = (Long)altoCache.get(timestampKey, "logged");
    	if (result == null) {
    		return 0L;
    	}
    	else {
    		return result.longValue();
    	}
    	
    }
    
    
	@Override
	public long next() {
		 int runs = 0;
	        while (true) {
	            long base = SlewClock.timeMillis() << BIN_DIGITS;
	            long maxValue = base + ONE_MS - 1;
	            
	            long currentValue = getValue();
	            
	            
	            for (long current = currentValue, update = Math.max(base, current + 1); update < maxValue;
		                 current = currentValue, update = Math.max(base, current + 1)) {
	                 altoCache.put(timestampKey, "value", update);
	                 if (runs > 1) {
	                        LOG.info("Spinning...");
	                 }
	                 return update;
	                
	            }
	            ++runs;
	        }
	}

	public AltoCache getAltoCache() {
		return altoCache;
	}

	public void setAltoCache(AltoCache altoCache) {
		this.altoCache = altoCache;
	}
	
	


}
