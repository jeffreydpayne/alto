package com.frs.alto.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * @author Jeffrey Payne
 *
 */

public abstract class BaseDomainObject implements Serializable {
	
	private static final long serialVersionUID = 42L;
	
	private String objectIdentifier = null;
	private String versionHash = null;
	private long cas = Long.MIN_VALUE;
	transient private boolean fromCache = false;
	
	private boolean persistent = false;
	
	
	private boolean readOnly = false;
	
	public BaseDomainObject() {
		super();
	}
	
	/**
	 * Returns the objectIdentifier.
	 * @return ObjectIdentifier
	 */
	public String getObjectIdentifier() {
		return objectIdentifier;
	}

	/**
	 * Sets the objectIdentifier.
	 * @param objectIdentifier The objectIdentifier to set
	 */
	public void setObjectIdentifier(String objectIdentifier) {
		this.objectIdentifier = objectIdentifier;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof BaseDomainObject)) {
			return false;
		}
		else if ( (this.getObjectIdentifier() != null) && ( ((BaseDomainObject)obj).getObjectIdentifier() != null) ) {
			return ((BaseDomainObject)obj).getObjectIdentifier().equals(this.getObjectIdentifier());
		}
		else {
			return super.equals(obj);
		}
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (getObjectIdentifier() != null) {
			return getObjectIdentifier().toString().hashCode();
		}
		else {
			return super.hashCode();
		}
	}
	
	@JsonIgnore
	public boolean isPersistent() {
		return persistent;
	}
	public void setPersistent(boolean value) {
		this.persistent = value;
	}

	public String getVersionHash() {
		return versionHash;
	}

	public void setVersionHash(String versionHash) {
		this.versionHash = versionHash;
	}

	@JsonIgnore
	public boolean isFromCache() {
		return fromCache;
	}

	public void setFromCache(boolean fromCache) {
		this.fromCache = fromCache;
	}

	@JsonIgnore
	public long getCas() {
		return cas;
	}

	public void setCas(long cas) {
		this.cas = cas;
	}

	@JsonIgnore
	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	
	
}
