package com.react.compiler;

import java.util.HashSet;
import java.util.Set;

import com.react.topo.TwoTuple;

public class Annotation{
	public Set<TwoTuple<String>> annotation;
	
	public Annotation() {
		this.annotation=new HashSet<TwoTuple<String>>();
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotation == null) ? 0 : annotation.hashCode());
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
		Annotation other = (Annotation) obj;
		if (annotation == null) {
			if (other.annotation != null)
				return false;
		} else if (!annotation.equals(other.annotation))
			return false;
		return true;
	}


	
	//set operation
	public Set<TwoTuple<String>> union(Set<TwoTuple<String>> a,Set<TwoTuple<String>> b){
		Set<TwoTuple<String>> result=new HashSet<TwoTuple<String>>(a);
		result.addAll(b);
		return result;
	}
	public Set<TwoTuple<String>> intersection(Set<TwoTuple<String>> a,Set<TwoTuple<String>> b){
		Set<TwoTuple<String>> result=new HashSet<TwoTuple<String>>(a);
		result.retainAll(b);
		return result;
	}
	public Set<TwoTuple<String>> difference(Set<TwoTuple<String>> a,Set<TwoTuple<String>> b){
		Set<TwoTuple<String>> result=new HashSet<TwoTuple<String>>(a);
		result.removeAll(b);
		return result;
	}


	@Override
	public String toString() {
		return "Annotation [annotation=" + annotation + "]";
	}
	
	
	
}
