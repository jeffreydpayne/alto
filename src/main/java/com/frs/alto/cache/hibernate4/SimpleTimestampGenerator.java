package com.frs.alto.cache.hibernate4;

public class SimpleTimestampGenerator implements TimestampGenerator {

	@Override
	public long next() {
		return System.currentTimeMillis();
	}

}
