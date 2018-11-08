package com.react.verify;



import java.util.ArrayList;
import java.util.List;

import com.react.topo.TwoTuple;

public class FowardingGraph {
	private class ENode{
		int ivex;
		ENode nextEdge;
		public ENode(int ivex,ENode nextEdge) {
			this.ivex=ivex;
			this.nextEdge=nextEdge;
		}
		@Override
		public String toString() {
			return "ENode [ivex=" + ivex + ", nextEdge=" + nextEdge + "]";
		}
	}

    private class VNode{
		String device;
		ENode firstEdge;
		public VNode(String device,ENode firstEdge) {
			this.device=device;
			this.firstEdge=firstEdge;
		}
		@Override
		public String toString() {
			return "VNode [device=" + device + ", firstEdge=" + firstEdge + "]";
		}
		
    }

    EC ec;
    List<TwoTuple<String>> srcDstPair;
	ArrayList<VNode> forwardingGraph;
	List<String> deviceset;
	List<TwoTuple<String>> edges;
	public static boolean is_loop=false;
	public static boolean is_reachable=true;
	public static boolean is_back_hole=false;

	public FowardingGraph() {
		this.forwardingGraph=new ArrayList<VNode>();
	}

	public FowardingGraph(EC ec) {
		this.ec=ec;
		this.forwardingGraph=new ArrayList<VNode>();
	}
	
	public List<TwoTuple<String>> getSrcDstPair() {
		return srcDstPair;
	}
	public void setSrcDstPair(List<TwoTuple<String>> srcDstPair) {
		this.srcDstPair = srcDstPair;
	}
	public List<String> getDeviceset() {
		return deviceset;
	}
	public void setDeviceset(List<String> deviceset) {
		this.deviceset = deviceset;
	}
	public List<TwoTuple<String>> getEdges() {
		return edges;
	}
	public void setEdges(List<TwoTuple<String>> edges) {
		this.edges = edges;
	}
	
	@Override
	public String toString() {
		return "FowardingGraph [ec=" + ec + ", srcDstPair=" + srcDstPair + ", forwardingGraph=" + forwardingGraph
				+ ", deviceset=" + deviceset + ", edges=" + edges + ", is_loop=" + is_loop + ", is_reachable="
				+ is_reachable + ", is_back_hole=" + is_back_hole + "]";
	}

	public int getPostion(String device) {
		int i;
		for( i=0;i<forwardingGraph.size();i++) {
			if(forwardingGraph.get(i).device.equals(device)) {
				return i;
			}
		}
		return i;
	}
    public boolean createGraph() {
    	boolean flag=false;
    	System.out.println("create graph"+deviceset);
    	for(int i=0;i<deviceset.size();i++) {
    		this.forwardingGraph.add(new VNode(deviceset.get(i),null));
    	}
    	for(int i=0;i< edges.size();i++) {
    		String source = edges.get(i).first;
            String destination = edges.get(i).second;
            int p1=getPostion(source);
            int p2=getPostion(destination);
            ENode node=new ENode(p2,null);
            if(forwardingGraph.get(p1).firstEdge==null) {
            	forwardingGraph.get(p1).firstEdge=node;
            	
            }
            else {
            	ENode current=forwardingGraph.get(p1).firstEdge.nextEdge;
            	while(current!=null) {
            		current=current.nextEdge;
            	}
            	current=node;
            	
            }
            
    	}
    	flag =true;
    	return flag;
    	
    }
    
    public boolean updateGraph(List<String> deviceset_updating,List<TwoTuple<String>> edges_updating) {
    	boolean flag=false;
    	for(int i=0;i<deviceset_updating.size();i++) {
    		if(this.deviceset.contains(deviceset_updating.get(i))) {
    			continue;
    		}
    		
    		this.deviceset.add(deviceset_updating.get(i));
    		this.forwardingGraph.add(new VNode(deviceset_updating.get(i),null));
    	}
    	for(int i=0;i<edges_updating.size();i++) {
    		if(this.edges.contains(edges_updating.get(i))) {
    			continue;
    		}
    		this.edges.add(edges_updating.get(i));
    		String source=edges_updating.get(i).first;
    		String destination=edges_updating.get(i).second;
    		int p1=getPostion(source);
    		int p2=getPostion(destination);
    		ENode node=new ENode(p2,null);
    		if(forwardingGraph.get(p1).firstEdge==null) {
    			forwardingGraph.get(p1).firstEdge=node;
    			
    		}
    		else {
    			ENode current=forwardingGraph.get(p1).firstEdge.nextEdge;
    			while(current!=null) {
    				current=current.nextEdge;
    			}
    			current=node;
    		}
    	}
    	flag=true;
    	return flag;
    }

    public void traverse(){
    	System.out.println("srcDstPair:"+srcDstPair.toString());
    	for(TwoTuple<String> tuple:srcDstPair) {
    		String src=tuple.first;
    		String dst=tuple.second;

            int pos=getPostion(src);
    		List<String> traversed=new ArrayList<String>();
    		traversed.add(src);
    		VNode  enode = forwardingGraph.get(pos);
    		while(enode.firstEdge!= null
					&&enode.firstEdge.ivex<forwardingGraph.size()
					) {
    			String midevice = forwardingGraph
						.get(enode.firstEdge.ivex).device;
    			if(traversed.contains(midevice)) {
    				this.is_loop=true;
    				this.is_back_hole=true;
    				this.is_reachable=false;
    		        break;
    			}
    			traversed.add(forwardingGraph.get(enode.firstEdge.ivex).device);
    			
    			enode=forwardingGraph.get(enode.firstEdge.ivex);
    		}
    		if(!enode.device.equals(dst)) {
    			this.is_back_hole=true;
    		}
    	}
    	
    }
    
    
    public static void test(String[] arrgs) {
    	List<String> deviceset=new ArrayList<String>();
    	deviceset.add("A");
    	deviceset.add("B");
    	deviceset.add("C");
    	deviceset.add("D");
    	deviceset.add("E");
    	deviceset.add("F");
    	deviceset.add("G");
    	List<TwoTuple<String>> edges=new ArrayList<TwoTuple<String>>();
    	FowardingGraph graph=new FowardingGraph();
    	edges.add(new TwoTuple("A","B"));
    	edges.add(new TwoTuple("B","C"));
    	edges.add(new TwoTuple("B","E"));
    	edges.add(new TwoTuple("B","F"));
    	edges.add(new TwoTuple("C","E"));
    	edges.add(new TwoTuple("D","C"));
    	edges.add(new TwoTuple("E","B"));
    	edges.add(new TwoTuple("E","D"));
    	edges.add(new TwoTuple("F","G"));
    	graph.setDeviceset(deviceset);
    	graph.setEdges(edges);
    	graph.createGraph();
    	List<String> deviceset_updating=new ArrayList<String>();
    	
    	deviceset_updating.add("M");
    	deviceset_updating.add("A");
    	deviceset_updating.add("X");
    	
    	List<TwoTuple<String>> edges_updating=new ArrayList<TwoTuple<String>>();
    	edges_updating.add(new TwoTuple("M","N"));
    	edges_updating.add(new TwoTuple("C","N"));
    	graph.updateGraph(deviceset_updating, edges_updating);
    }
}
