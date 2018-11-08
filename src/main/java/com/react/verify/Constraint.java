package com.react.verify;

import java.util.HashMap;
import java.util.List;

public class Constraint{
	HashMap<String,Integer> action;
	int hop;
	
	
	public Constraint(List<String> name,List<Integer> action_list,int hop) {
		
		if(name.size()==action_list.size()) {
			action=new HashMap<String,Integer>();
			
			for(int i=0;i<name.size();i++) {
				action.put(name.get(i),action_list.get(i));
			}
		}
		else {
			throw new RuntimeException("pattern error");
		}
		this.hop=hop;
		
	}
	
	
	public Constraint() {
		action=new HashMap<String,Integer>();
	}
	public HashMap<String, Integer> getAction() {
		return action;
	}
	public void setAction(HashMap<String, Integer> action) {
		this.action = action;
	}
	
	@Override
	public String toString() {
		return "Constraint [action=" + action.toString() + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
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
		Constraint other = (Constraint) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		return true;
	}
	
	
	
	
}