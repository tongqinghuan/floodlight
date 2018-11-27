package com.react.topo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Node {

	private  String flowStr;
	private Map children =new HashMap();
	private boolean isValidFlow;
	public Node(String argValue) {
		flowStr=argValue;
		//System.out.println(flowStr);
	}
	public boolean addChild(char c,Node argChild) {
		children.put(c, argChild);
		return true;
	}
	public boolean containsChild(char c) {
		return children.containsKey(c);
	}

	public String toString() {
		return this.flowStr;
	}
	public Node getChild(char c) {

		return (Node)children.get(c);
	}
	public boolean isFlow() {
		return this.isValidFlow;
	}
	public void setIsFlow(boolean bool) {
		this.isValidFlow=bool;
	}



}
