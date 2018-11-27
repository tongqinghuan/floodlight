package com.react.verify;

import com.react.compiler.Flow;
import com.react.compiler.VerifyTestData;
import com.react.topo.TwoTuple;
import jdk.nashorn.internal.ir.VarNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ForwardingGraph {
    private class ENode{
        int itex;
        ENode nextEdge;
        public ENode(int itex,ENode nextEdge){
            this.itex=itex;
            this.nextEdge=nextEdge;
        }

        @Override
        public String toString() {
            return "{" +
                    "itex=" + itex +
                    ", nextEdge=" + nextEdge +
                    '}';
        }
    }
    private class VNode{
        String device;
        ENode firstEdge;
        public VNode(String device,ENode firstEdge){
            this.device=device;
            this.firstEdge=firstEdge;
        }

        @Override
        public String toString() {
            return "{" +
                    "device='" + device + '\'' +
                    ", firstEdge=" + firstEdge +
                    '}';
        }
    }
    private  List<VNode> mVexs;
    protected static Logger log = LoggerFactory.getLogger(ForwardingGraph.class);
    public ForwardingGraph(){
        this.mVexs=new ArrayList<>();
    }

    @Override
    public String toString() {
        return "{" +
                "mVexs=" + mVexs +
                '}';
    }

    public List<VNode> getmVexs() {
        return mVexs;
    }

    public void createGraph(ECGraph ecGraph){
        if(ecGraph==null){
            return;
        }
        mVexs=new ArrayList<VNode>();
        List<String> deiceSet=ecGraph.getDeviceset();
        List<TwoTuple<String>> edges=ecGraph.getEdges();
        for(int i=0;i<deiceSet.size();i++){
            mVexs.add(new VNode(deiceSet.get(i),null));
        }
        for(int i=0;i<edges.size();i++){
            String src=edges.get(i).first;
            String dst=edges.get(i).second;
            int p1=getPosition(src);
            int p2=getPosition(dst);
            ENode eNode=new ENode(p2,null);
            if(mVexs.get(p1).firstEdge==null){
                mVexs.get(p1).firstEdge=eNode;
            }else {
                ENode current=mVexs.get(p1).firstEdge;
                while(current!=null){
                    current=current.nextEdge;
                }
                current=eNode;
            }

            }
        }

    public int getPosition(String device){
        int i;
        for(i=0;i<mVexs.size();i++){
            if(mVexs.get(i).device.equals(device)){
                return i;
            }
        }
        return -1;
    }

    private boolean traverse(String entry,String exit){
        boolean is_violation=true;
        int p1=getPosition(entry);
        List<String> traversed=new ArrayList<>();
        traversed.add(entry);
        VNode vNode=mVexs.get(p1);
        while(vNode.firstEdge!=null&&vNode.firstEdge.itex<mVexs.size()){
            String coreNode=mVexs.get(vNode.firstEdge.itex).device;
            if(traversed.contains(coreNode)){
                is_violation=false;
                log.debug("loop");
                break;

            }
            traversed.add(coreNode);
            vNode=mVexs.get(vNode.firstEdge.itex);
        }
        if(!vNode.device.equals(exit)){
            is_violation=false;
            log.debug("unreachable");
        }
        return is_violation;
    }
    public HashSet<VerifyResult> traverse(HashSet<Flow> matchedFlows){
        if(matchedFlows==null||matchedFlows.size()==0){
            return null;
        }
        HashSet<VerifyResult> verifyResults=new HashSet<>();
        for(Flow flow:matchedFlows){
            log.debug("traverse");
            String entry=VerifyTestData.entry_exit_flow.get(flow).getSrcDpid();
            String exit=VerifyTestData.entry_exit_flow.get(flow).getDstDpid();
            boolean is_violation=traverse(entry,exit);
            log.debug("current flow is violation:"+is_violation);
            verifyResults.add(new VerifyResult(flow,is_violation));
        }
        return verifyResults;


    }

}
