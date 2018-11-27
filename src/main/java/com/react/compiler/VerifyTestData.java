package com.react.compiler;

import com.react.topo.Network;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

class ThreeTuple{
    int in_port;
    String dpid;
    int exit_port;

    public int getIn_port() {
        return in_port;
    }

    public String getDpid() {
        return dpid;
    }

    public int getExit_port() {
        return exit_port;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreeTuple that = (ThreeTuple) o;
        return in_port == that.in_port &&
                exit_port == that.exit_port &&
                Objects.equals(dpid, that.dpid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(in_port, dpid, exit_port);
    }

    public ThreeTuple(int in_port, String dpid, int exit_port){
        this.in_port=in_port;
        this.dpid=dpid;
        this.exit_port=exit_port;
    }

}
public class VerifyTestData {

    public static HashSet<Flow> flows=new HashSet<>();
    public static HashMap<Flow, EnAndExEntry> entry_exit_flow = new HashMap<>();

    public static  Flow flow1;
    public static  Flow flow2;
    public static  Flow flow3;
    public static  Flow flow4;
    public static  ArrayList<ThreeTuple> flow_path1=new ArrayList<>();
    public static  ArrayList<ThreeTuple> flow_path2=new ArrayList<>();
    public static  int counter=1;
    public static void generateFlowWithLoopForSpecificTopo(){
        flow1=new Flow("10.5.10.230","10.5.10.9",1);
        flow2=new Flow("10.5.10.235","10.5.10.230",3);
      //  flow3=new Flow("10.5.10.1","10.5.0.1",3);
       // flow4=new Flow("10.5.0.1","10.0.0.1",1);
        flows.add(flow1);
        flows.add(flow2);
       // flows.add(flow3);
       // flows.add(flow4);
        entry_exit_flow.put(flow1,new EnAndExEntry("1","5"));
        entry_exit_flow.put(flow2,new EnAndExEntry("3","1"));
      //  entry_exit_flow.put(flow3,new EnAndExEntry("5","1"));
      //  entry_exit_flow.put(flow4,new EnAndExEntry("1","3"));


    }
    public static void setPathForSpecficFlow(){

        flow_path1.add(new ThreeTuple(1,"1",2));
        flow_path1.add(new ThreeTuple(1,"2",2));
        flow_path1.add(new ThreeTuple(1,"3",2));
        flow_path1.add(new ThreeTuple(1,"5",3));


        flow_path2.add(new ThreeTuple(3,"3",4));
        flow_path2.add(new ThreeTuple(2,"6",1));
        flow_path2.add(new ThreeTuple(3,"1",1));



    }
    public static void installFlowRulesForLoopTopo(){
        Network.TopologyInit();
        generateFlowWithLoopForSpecificTopo();
        setPathForSpecficFlow();
        String srcIp1=flow1.getSource();
        String dstIp1=flow1.getDestination();
        boolean is_in_port1=false;
        int srcIpMask1=16;
        int dstIpmask1=24;
        int priority1=32760;
        for(ThreeTuple tuple:flow_path1){
            int srcPort=tuple.getIn_port();
            String dstPort=String.valueOf(tuple.getExit_port());
            String flow_name_in="flow_rule_"+counter;
            String flow_name_out="flow_rule_"+(counter+1);
            if(tuple.getDpid().equals("1")){

                priority1=32761;
                srcIpMask1=28;

            }
            else {

                priority1=32760;
                srcIpMask1=16;
                dstIpmask1=24;
            }
            Network.generateFlow(srcIp1,dstIp1,null,null,
                    srcPort,dstPort, DatapathId.of(tuple.getDpid()),flow_name_in,srcIpMask1,dstIpmask1,priority1,is_in_port1);
           // Network.generateFlow(dstIp1,srcIp1,null,null,
           //         Integer.valueOf(dstPort),String.valueOf(srcPort), DatapathId.of(tuple.getDpid()),flow_name_out,dstIpmask1,srcIpMask1,priority1,is_in_port1);
            counter=counter+2;

        }
        String srcIp2=flow2.getSource();
        String dstIp2=flow2.getDestination();
        boolean is_in_port2=false;
        int srcIpMask2=8;
        int dstIpmask2=16;
        int priority2=32761;
        for(ThreeTuple tuple:flow_path2){
            int srcPort=tuple.getIn_port();
            String dstPort=String.valueOf(tuple.getExit_port());
            String flow_name_in="flow_rule_"+counter;
            String flow_name_out="flow_rule_"+(counter+1);
            if(tuple.getDpid().equals("3")){

                priority1=32761;

            }
            else {

                priority1=32760;

            }
            Network.generateFlow(srcIp2,dstIp2,null,null,
                    srcPort,dstPort, DatapathId.of(tuple.getDpid()),flow_name_in,srcIpMask2,dstIpmask2,priority2,is_in_port2);
           // Network.generateFlow(dstIp2,srcIp2,null,null,
            //        Integer.valueOf(dstPort),String.valueOf(srcPort), DatapathId.of(tuple.getDpid()),flow_name_out,dstIpmask2,srcIpMask2,priority2,is_in_port2);
            counter=counter+2;

        }

    }

}
