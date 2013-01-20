package com.frs.alto.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.InitializingBean;

public abstract class AsynchronousCacheSupport implements AltoCache, InitializingBean {
	
	private int asyncThreadCount = 1;
	
	private ExecutorService asyncHandler = null;
	
	@Override
	public void get(final String region, final String key, Future<Object> callback) {
		
		callback = asyncHandler.submit(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				return get(region, key);
			}
		
		
		});
		
	}

	@Override
	public void put(final String region, final String key, final Object value, boolean async) {
		if (async) {
			asyncHandler.execute(new Runnable() {
				
				@Override
				public void run() {
					put(region, key, value);
					
				}
			});
		}
		else {
			put(region, key, value);
		}
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (asyncThreadCount > 1) {
			asyncHandler = Executors.newSingleThreadExecutor();
		}
		else {
			asyncHandler = Executors.newFixedThreadPool(asyncThreadCount);
		}
		
	}
	
	
	
	
	

}
