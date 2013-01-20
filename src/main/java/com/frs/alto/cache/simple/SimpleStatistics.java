package com.frs.alto.cache.simple;

import com.frs.alto.cache.AltoCacheStatistics;

public class SimpleStatistics implements AltoCacheStatistics {

	private long sizeInBytes = 0;
	private long itemCount = 0;
	private long deleteHits = 0;
	private long missCount = 0;
	private long hitCount = 0;
	
	@Override
	public long getSizeInBytes() {
		return sizeInBytes;
	}

	@Override
	public long getItemCount() {
		return itemCount;
	}

	@Override
	public long getDeleteHits() {
		return deleteHits;
	}

	@Override
	public long getMissCount() {
		return missCount;
	}

	@Override
	public long getHitCount() {
		return hitCount;
	}

	public void setSizeInBytes(long sizeInBytes) {
		this.sizeInBytes = sizeInBytes;
	}

	public void setItemCount(long itemCount) {
		this.itemCount = itemCount;
	}

	public void setDeleteHits(long deleteHits) {
		this.deleteHits = deleteHits;
	}

	public void setMissCount(long missCount) {
		this.missCount = missCount;
	}

	public void setHitCount(long hitCount) {
		this.hitCount = hitCount;
	}
	
	
	

}
