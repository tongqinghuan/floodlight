package com.react.verify;

import java.util.List;

/**fattree and link structure
 * link: string sw,int port,string sw,int port
 * fattree: k, edgeswitch ,egg switch,core switch, host
 */
class Link{
	String src;
	String dst;
	int entry;
	int exit;
	public Link(String src,String dst,int entry,int exit) {
		this.src=src;
		this.dst=dst;
		this.entry=entry;
		this.exit=exit;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + entry;
		result = prime * result + exit;
		result = prime * result + ((src == null) ? 0 : src.hashCode());
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
		Link other = (Link) obj;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (entry != other.entry)
			return false;
		if (exit != other.exit)
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}
}

public class FatTree {
	int k;
	List<String> host;
	List<String> eS;
	List<String> aS;
	List<String> cS;
	public FatTree(int k) {
		this.k=k;
	}
	public void createEdgeLayerSwitch() {
		for(int i=0;i<k;i++) {
			eS.add("e"+String.valueOf(i+1));
		}
	}
	public void createAggLayerSwitch() {
		for(int i=0;i<k;i++) {
			aS.add("a"+String.valueOf(i+1+k));
		}
	}
	public void createCoreLayerSwitch() {
		for(int i=0;i<4;i++) {
			cS.add("c"+String.valueOf(i+1+2*k));
		}
	}
	public void addLink() {
		System.out.println("add link between core and agg");
		for(int i=0;i<k/2;i++) {
			
		}
	}

}
