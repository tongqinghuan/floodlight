
package com.react.verify;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.react.topo.Node;

public class Tire {
	private Node root=new Node("");
	static  boolean flag = true;
	public Tire() {}
	protected static  boolean getflag(){
		return flag;
	}
	public void addFlow(Flow entry) {
//		for(Flow entry:flowSet) {
		char[] flow=entry.dst_ip.toCharArray();
		Node currentNode=root;
		for(int i=0;i<flow.length;i++) {
			//System.out.println(currentNode.containsChild(flow[i]));
			if(!(currentNode.containsChild(flow[i]))) {
//				assign(1=49,0=48,x=120) and add child node
				currentNode.addChild(flow[i], new Node(currentNode.toString()+flow[i]));
			}
//				point shift down
			currentNode=currentNode.getChild(flow[i]);
		}
//			set the last node as leaf node
		currentNode.setIsFlow(true);
//		}
	}

	public void addFlow(Set<Flow> flowSet) {
		for(Flow entry:flowSet) {
			char[] flow=entry.dst_ip.toCharArray();
			Node currentNode=root;
			for(int i=0;i<flow.length;i++) {
				//System.out.println(currentNode.containsChild(flow[i]));
				if(!(currentNode.containsChild(flow[i]))) {
					currentNode.addChild(flow[i], new Node(currentNode.toString()+flow[i]));
				}
				currentNode=currentNode.getChild(flow[i]);
			}
			currentNode.setIsFlow(true);
		}

	}

	public static Set<String> FlowSet2StringSet(Set<Flow> flow){
		Set<String> r = new HashSet<>();
		for (Flow s: flow) {
			r.add(s.dst_ip);
		}
		return r;
	}
	public static Set<Flow> StringSet2FlowSet(Set<String> flow){
		Set<Flow> r = new HashSet<>();
		for (String s:flow) {
			r.add(new Flow(s));
		}
		return r;
	}
	public Node getNode(String argString) {
		char[] nodeString=argString.toCharArray();
		Node currentNode=root;
		for(int i=0;i<nodeString.length&&currentNode!=null;i++) {
			currentNode=currentNode.getChild(nodeString[i]);


		}
		return currentNode;
	}
	public boolean containFlow(String argString) {
		Node node=getNode(argString);
		if(node!=null&&node.isFlow()) {
			return true;
		}
		else {
			return false;
		}
	}
    public void  deleteFlow(String entry) {
    	char[] flow=entry.toCharArray();
		Node currentNode=root;
		for(int i=0;i<flow.length&&currentNode!=null;i++) {
			currentNode=currentNode.getChild(flow[i]);
		}
		currentNode.setIsFlow(false);
    }
	public void deleteFlow(Set<String> flowSet) {
		for(String entry:flowSet) {
			char[] flow=entry.toCharArray();
			Node currentNode=root;
			for(int i=0;i<flow.length&&currentNode!=null;i++) {
				currentNode=currentNode.getChild(flow[i]);
			}
			currentNode.setIsFlow(false);

		}
	}

	public Set<Flow> searchConflictFlow(Flow currentFlow){

		Set<Flow> conflictFlow=new HashSet<Flow>();
		HashSet<Node> currentNodeSet=new HashSet<Node>();
		HashSet<Node> temp=new HashSet<Node>();
//		 point to the root of the Tire Tree, like a pointer
		currentNodeSet.add(root);
		char[] flow=currentFlow.dst_ip.toCharArray();

		for(int i=0;i<currentFlow.dst_ip.length() && currentNodeSet!=null;i++) {
//		 	each character of flow maps a tier of tree
			ArrayList branches=new ArrayList();

			if(flow[i]=='0') {
				branches.add('0');
				branches.add('x');//overlay section in the tree
			}
			if(flow[i]=='1') {
				branches.add('1');
				branches.add('x');
			}
			if(flow[i]=='x') {
				branches.add('0');
				branches.add('1');
				branches.add('x');
				flag = false;
			}
			//System.out.println(branches);
			temp.clear();
			//System.out.println(currentNodeSet);
//			according to branch,creat nodes of the tree (top-down)
			for(Node node:currentNodeSet) {
				for(int j=0;j<branches.size();j++) {
//					wheather the tier of tree contain one of branches
//					if yes,add Chid Node to temp
					Node nodeTemp=node.getChild((char)(branches.get(j)));
					//System.out.println((node.getChild((char)(branches.get(j)))).toString());
					if(nodeTemp!=null) {
						temp.add(nodeTemp);
					}
				}
			}
			//System.out.println("temp"+temp);
			currentNodeSet.clear();
			currentNodeSet.addAll(temp);
//			currentNodeSet is a path of flow during traveling the tree
		}
//		get all leaf node of currentNodeSet
		for(Node node:currentNodeSet) {
//		 	IsLeafNode
			if(node.isFlow()) {
				conflictFlow.add(new Flow(node.toString()));
			}
		}
		return conflictFlow;
	}

}
