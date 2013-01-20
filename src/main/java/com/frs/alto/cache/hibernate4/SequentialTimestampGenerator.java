package com.frs.alto.cache.hibernate4;

import java.util.concurrent.atomic.AtomicLong;

import net.sf.ehcache.util.Timestamper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequentialTimestampGenerator implements TimestampGenerator {
	
	/**
     * Value for left shifting System.currentTimeMillis, freeing some space for the counter
     */
    public final int BIN_DIGITS = Integer.getInteger("net.sf.ehcache.util.Timestamper.shift", 12);

    /**
     * What is one milliseconds, based on "counter value reserved space", for this Timestamper
     */
    public final int ONE_MS = 1 << BIN_DIGITS;

    private final Logger LOG     = LoggerFactory.getLogger(Timestamper.class);
    private final int    MAX_LOG = Integer.getInteger("net.sf.ehcache.util.Timestamper.log.max", 1) * 1000;

    private final AtomicLong VALUE  = new AtomicLong();
    private final AtomicLong LOGGED = new AtomicLong();

	@Override
	public long next() {
		 int runs = 0;
	        while (true) {
	            long base = SlewClock.timeMillis() << BIN_DIGITS;
	            long maxValue = base + ONE_MS - 1;

	            for (long current = VALUE.get(), update = Math.max(base, current + 1); update < maxValue;
	                 current = VALUE.get(), update = Math.max(base, current + 1)) {
	                if (VALUE.compareAndSet(current, update)) {
	                    if (runs > 1) {
	                        log(base, "Thread spin-waits on time to pass. Looped "
	                                  + "{} times, you might want to increase -Dnet.sf.ehcache.util.Timestamper.shift", runs);
	                    }
	                    return update;
	                }
	            }
	            ++runs;
	        }
	}
	
	 private void log(final long base, final String message, final Object... params) {
	        if (LOG.isInfoEnabled()) {
	            long thisLog = (base >> BIN_DIGITS) / MAX_LOG;
	            long previousLog = LOGGED.get();
	            if (previousLog != thisLog) {
	                if (LOGGED.compareAndSet(previousLog, thisLog)) {
	                    LOG.info(message, params);
	                }
	            }
	        }
	    }

}
