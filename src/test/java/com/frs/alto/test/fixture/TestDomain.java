package com.frs.alto.test.fixture;

import java.util.LinkedHashSet;
import java.util.Set;

public class TestDomain {
	
	private String objectIdentifier;
	private String naturalId;
	private String name;
	private Set<TestChild> children;
	
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
	public String getNaturalId() {
		return naturalId;
	}
	public void setNaturalId(String naturalId) {
		this.naturalId = naturalId;
	}
	public Set<TestChild> getChildren() {
		return children;
	}
	public void setChildren(Set<TestChild> children) {
		this.children = children;
	}
	public void addChild(TestChild child){
		if (children == null) {
			children = new LinkedHashSet<TestChild>();
		}
		child.setParent(this);
		children.add(child);
	}
	

}
