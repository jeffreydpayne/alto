package com.frs.alto.cache;

public interface AltoCacheStatistics {
	
	public long getSizeInBytes();
	public long getItemCount();
	public long getDeleteHits();
	public long getMissCount();
	public long getHitCount();
	
}
