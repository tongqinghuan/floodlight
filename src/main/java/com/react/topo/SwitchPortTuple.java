package com.react.topo;

public class SwitchPortTuple {
	String dpid;
	int portid;
	public SwitchPortTuple(String dpid,int portid) {
		this.dpid=dpid;
		this.portid=portid;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dpid == null) ? 0 : dpid.hashCode());
		result = prime * result + portid;
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
		SwitchPortTuple other = (SwitchPortTuple) obj;
		if (dpid == null) {
			if (other.dpid != null)
				return false;
		} else if (!dpid.equals(other.dpid))
			return false;
		if (portid != other.portid)
			return false;
		return true;
	}
	public String getDpid() {
		return dpid;
	}
	public void setDpid(String dpid) {
		this.dpid = dpid;
	}
	public int getPortid() {
		return portid;
	}
	public void setPortid(int portid) {
		this.portid = portid;
	}
	@Override
	public String toString() {
		return "SwitchPort [dpid=" + dpid + ", portid=" + portid + "]";
	}
	

}
