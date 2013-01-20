package com.frs.alto.test.fixture;

public class TestChild {
	
	private String objectIdentifier;

	private TestDomain parent;
	private String name;
	public String getObjectIdentifier() {
		return objectIdentifier;
	}
	public void setObjectIdentifier(String objectIdentifier) {
		this.objectIdentifier = objectIdentifier;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public TestDomain getParent() {
		return parent;
	}
	public void setParent(TestDomain parent) {
		this.parent = parent;
	}

	

}
