package com.frs.alto.domain;


/**
 * @author Jeffrey Payne
 *
 */

public abstract class BaseDomainObject {
	
	private String objectIdentifier = null;
	
	private boolean persistent = false;
	
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
	
	public boolean isPersistent() {
		return persistent;
	}
	public void setPersistent(boolean value) {
		this.persistent = value;
	}
	
}