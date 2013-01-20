package com.frs.alto.cache.hibernate4;


final class TimeProviderLoader {

	private static SlewClock.TimeProvider timeProvider = new SlewClock.TimeProvider() {
	        public final long currentTimeMillis() {
	            return System.currentTimeMillis();
	        }
	    };

	    private TimeProviderLoader() {
	        // Do not instantiate me!
	    }

	    /**
	     * Getter
	     * @return the currently set timeProvider
	     */
	    public static synchronized SlewClock.TimeProvider getTimeProvider() {
	        return timeProvider;
	    }

	    /**
	     * Setter, needs to be set before the {@link SlewClock} class is being loaded!
	     * @param timeProvider the {@link SlewClock.TimeProvider} implementation to use.
	     */
	    public static synchronized void setTimeProvider(final SlewClock.TimeProvider timeProvider) {
	        TimeProviderLoader.timeProvider = timeProvider;
	    }
	
	
}
