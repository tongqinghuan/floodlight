package com.react.verify;

import java.util.Arrays;
import java.util.HashMap;

public class Scope{
	private String[] name= {"switch_id","dst_ip"};
    HashMap<String,String> scope;
	public Scope(String switch_id,String dst_ip) {
		scope=new HashMap<String,String>();
		this.scope.put(name[0], switch_id);
		this.scope.put(name[1],dst_ip);
	}
	public HashMap getScope() {
		return scope;
	}
	public void setScope(HashMap scope) {
		this.scope = scope;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(name);
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Scope other = (Scope) obj;
		if (!Arrays.equals(name, other.name))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return this.scope.toString();
	}
	
	
	
}