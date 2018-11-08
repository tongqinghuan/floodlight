package com.react.verify;

import java.util.HashMap;


class Flow{
	String dst_ip;
	Flow(String dst_ip){
		this.dst_ip=dst_ip;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dst_ip == null) ? 0 : dst_ip.hashCode());
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
		Flow other = (Flow) obj;
		if (dst_ip == null) {
			if (other.dst_ip != null)
				return false;
		} else if (!dst_ip.equals(other.dst_ip))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Flow [dst_ip=" + dst_ip + "]";
	}
	
	
}
// inport ?
class FlowRule{
	String sw;
	String dst_ip;
	int priority;
	HashMap<String,Integer> action;
	FlowRule(String sw,String dst_ip,int priority,HashMap<String,Integer> action){
	    this.sw=sw;
		this.dst_ip=dst_ip;
		this.priority=priority;
		this.action=action;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((dst_ip == null) ? 0 : dst_ip.hashCode());
		result = prime * result + priority;
		result = prime * result + ((sw == null) ? 0 : sw.hashCode());
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
		FlowRule other = (FlowRule) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (dst_ip == null) {
			if (other.dst_ip != null)
				return false;
		} else if (!dst_ip.equals(other.dst_ip))
			return false;
		if (priority != other.priority)
			return false;
		if (sw == null) {
			if (other.sw != null)
				return false;
		} else if (!sw.equals(other.sw))
			return false;
		return true;
	}

	public int getPriority() {
		return priority;
	}
	public HashMap<String, Integer> getAction() {
		return action;
	}

	@Override
	public String toString() {
		return "FlowRule [sw=" + sw + ", dst_ip=" + dst_ip + ", priority=" + priority + ", action=" + action + "]";
	}

	
}


