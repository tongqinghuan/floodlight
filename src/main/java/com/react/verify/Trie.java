package com.react.verify;

import com.react.topo.Node;

import java.util.ArrayList;
import java.util.HashSet;

public class Trie {
    private Node root = new Node("");
    static boolean flag = true;

    public Trie() {
    }

    protected static boolean getflag() {
        return flag;
    }

    public void addFlowRule(EcFiled ecFiled,String switchId) {
//		for(Flow entry:flowSet) {
        String srcIP=ecFiled.getSrc_ip();
        String dstIP=ecFiled.getDst_ip();
        String srcAndDst=srcIP+dstIP;
        char[] srcAndDstArr = srcAndDst.toCharArray();
        Node currentNode = root;
        for (int i = 0; i < srcAndDstArr.length; i++) {
            //System.out.println(currentNode.containsChild(flow[i]));
            if (!(currentNode.containsChild(srcAndDstArr[i]))) {
//				assign(1=49,0=48,x=120) and add child node
                currentNode.addChild(srcAndDstArr[i], new Node(currentNode.toString() + srcAndDstArr[i]));
            }
//				point shift down
            currentNode = currentNode.getChild(srcAndDstArr[i]);
        }
//			set the last node as leaf node
        currentNode.setIsFlow(true);
        currentNode.setValue(switchId);
//		}
    }
    public Node getLeafNode(String argString) {
        char[] nodeString = argString.toCharArray();
        Node currentNode = root;
        for (int i = 0; i < nodeString.length && currentNode != null; i++) {
            currentNode = currentNode.getChild(nodeString[i]);
        }
        return currentNode;
    }

    public boolean containFlowRule(EcFiled argString) {
        String srcIP=argString.getSrc_ip();
        String dstIP=argString.getDst_ip();
        String srcAndDst=srcIP+dstIP;

        Node node = getLeafNode(srcAndDst);
        if (node != null && node.isFlow()) {
            return true;
        } else {
            return false;
        }
    }

    public void deleteFlowRule(EcFiled ecFiled) {
        String srcIP=ecFiled.getSrc_ip();
        String dstIP=ecFiled.getDst_ip();
        String srcAndDst=srcIP+dstIP;
        char[] srcAndDstArr = srcAndDst.toCharArray();
        Node currentNode = root;
        for (int i = 0; i < srcAndDstArr.length && currentNode != null; i++) {
            currentNode = currentNode.getChild(srcAndDstArr[i]);
        }
        currentNode.setIsFlow(false);
    }
    public HashSet<EcFiled> searchConflictFlowRule(EcFiled currentFlowRule) {
        String srcIP=currentFlowRule.getSrc_ip();
        String dstIP=currentFlowRule.getDst_ip();
        String srcAndDst=srcIP+dstIP;
        HashSet<EcFiled> conflictFlowRules = new HashSet<EcFiled>();
        HashSet<Node> currentNodeSet = new HashSet<Node>();
        HashSet<Node> temp = new HashSet<Node>();
//		 point to the root of the trie Tree, like a pointer
        currentNodeSet.add(root);
        char[] srcAndDstArr = srcAndDst.toCharArray();

        for (int i = 0; i < srcAndDst.length() && currentNodeSet != null; i++) {
//		 	each character of flow maps a tier of tree
            ArrayList branches = new ArrayList();

            if (srcAndDstArr[i] == '0') {
                branches.add('0');
                branches.add('x');//overlay section in the tree
            }

            if (srcAndDstArr[i] == '1') {
                branches.add('1');
                branches.add('x');
            }

            if (srcAndDstArr[i] == 'x') {
                branches.add('0');
                branches.add('1');
                branches.add('x');
                flag = false;
            }
            //System.out.println(branches);
            temp.clear();
            //System.out.println(currentNodeSet);

//			according to branch, creat nodes of the tree (top-down)
            for (Node node : currentNodeSet) {
                for (int j = 0; j < branches.size(); j++) {
//					wheather the tier of tree contain one of branches
//					if yes,add Chid Node to temp
                    Node nodeTemp = node.getChild((char) (branches.get(j)));
                    //System.out.println((node.getChild((char)(branches.get(j)))).toString());
                    if (nodeTemp != null) {
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
        for (Node node : currentNodeSet) {
//		 	IsLeafNode
            if (node.isFlow()) {
                String ecFiledStr=new String(node.toString());
                conflictFlowRules.add(new EcFiled(ecFiledStr.substring(0,32),ecFiledStr.substring(32,64)));
            }
        }
        return conflictFlowRules;
    }
}
