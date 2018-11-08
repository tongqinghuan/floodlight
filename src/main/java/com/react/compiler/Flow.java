package com.react.compiler;

public class Flow {
	private String source;
	private String destination;
	int in_port;

	public Flow(String src, String dst, int port) {
		// TODO Auto-generated constructor stub
		source = src;
		destination = dst;
		in_port = port;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public int getIn_port() {
		return in_port;
	}

	public void setIn_port(int in_port) {
		this.in_port = in_port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + in_port;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (in_port != other.in_port)
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Flow [source=" + source + ", destination=" + destination + ", in_port=" + in_port + "]";
	}

	/*
	 * public static void main(String[] args) { String flowJson=""; //Flow flow=new
	 * Flow("A","B"); String src="10.0.0.1";
	 * flowJson="{\"source\":\"A\",\"destiantion\":\""+src+"\"}"; JSONObject
	 * jsonObject = JSONObject.fromObject(flowJson); Flow jb =
	 * (Flow)JSONObject.toBean(jsonObject,Flow.class);
	 * System.out.println(jb.toString());
	 * 
	 * 
	 * 
	 * }
	 */

}
