package com.react.verify;

import com.react.compiler.Flow;
import com.react.topo.Network;
import com.react.topo.Port;
import com.react.topo.TwoTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.react.Utils.*;
import java.util.*;
class EC {
   OneDimensionalEc src_ip;
   OneDimensionalEc dst_ip;
   OneDimensionalEc in_port;

    public EC(OneDimensionalEc src_ip, OneDimensionalEc dst_ip,
              OneDimensionalEc in_port) {
        this.src_ip = src_ip;
        this.dst_ip = dst_ip;
        this.in_port = in_port;
    }

    public OneDimensionalEc getSrc_ip() {
        return src_ip;
    }

    public OneDimensionalEc getDst_ip() {
        return dst_ip;
    }

    public OneDimensionalEc getIn_port() {
        return in_port;
    }

    @Override
    public String toString() {
        return "EC{" +
                "src_ip=" + src_ip +
                ", dst_ip=" + dst_ip +
                ", in_port=" + IpConvertion.binaryStrToInt(in_port.start)+"-" + IpConvertion.binaryStrToInt(in_port.end)+
                '}';
    }
}


class ECGraph {
    List<String> deviceset;
    List<TwoTuple<String>> edges;

    public ECGraph(List<String> deviceset, List<TwoTuple<String>> edges) {
        this.deviceset = deviceset;
        this.edges = edges;
    }

    public List<String> getDeviceset() {
        return deviceset;
    }

    public List<TwoTuple<String>> getEdges() {
        return edges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ECGraph ecGraph = (ECGraph) o;
        return Objects.equals(deviceset, ecGraph.deviceset) &&
                Objects.equals(edges, ecGraph.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceset, edges);
    }

    @Override
    public String toString() {
        return "{" +
                "deviceset=" + deviceset +
                ", edges=" + edges +
                '}';
    }
}

public class ECOperations {
    protected static Logger log = LoggerFactory.getLogger(ECOperations.class);
    public static ArrayList<OneDimensionalEc> getEcForOnedimension(Set<String> conflictSetForOneFiled,String currentFiled){
        if(conflictSetForOneFiled==null||conflictSetForOneFiled.size()==0){
            return null;
        }
        ArrayList<OneDimensionalEc> onedimensionalECs = new ArrayList<>();
        Collection<String> ecRangeTreeSet = new TreeSet<String>();
        List<String> ecRangeList = new ArrayList<String>();

        String currentLowerbound = FlowScopeOperation.getLowerbound(currentFiled);
        String currentUpperbound = FlowScopeOperation.getUpperbound(currentFiled);

        for (String filed : conflictSetForOneFiled) {
            ecRangeTreeSet.add(FlowScopeOperation.getLowerbound(filed));
            ecRangeTreeSet.add(FlowScopeOperation.getUpperbound(filed));
        }
        ecRangeTreeSet.add(currentLowerbound);
        ecRangeTreeSet.add(currentUpperbound);
        ecRangeList.addAll(ecRangeTreeSet);
      //  log.info("Ec_range:\t" + Ec_range);

        int index_start = ecRangeList.indexOf(currentLowerbound);
        int index_end = ecRangeList.indexOf(currentUpperbound);

        for (int i = 0; i < (ecRangeList.size()-1); i++) {
            OneDimensionalEc temp = new OneDimensionalEc(ecRangeList.get(i), ecRangeList.get(i + 1));
            onedimensionalECs.add(temp);
        }
        return onedimensionalECs;

    }
    public static HashSet<EC> getEcForMutidimension(Set<EcFiled> conflictEcFileds, EcFiled currentEcFiled){
        if(conflictEcFileds==null||conflictEcFileds.size()==0){
            return null;
        }
        HashSet<EC> ecs=new HashSet<>();
        Set<String> srcIpFiled=new HashSet<>();
        Set<String> dstIpFiled=new HashSet<>();
        Set<String> inPortFiled=new HashSet<>();
        String srcIp=currentEcFiled.getSrc_ip();
        String dstIp=currentEcFiled.getDst_ip();
        String in_port=currentEcFiled.getIn_port();
        for(EcFiled ecFiled:conflictEcFileds){
            srcIpFiled.add(ecFiled.getSrc_ip());
            dstIpFiled.add(ecFiled.getDst_ip());
            inPortFiled.add(ecFiled.getIn_port());
        }
        log.info("System is calculating ecs for one dimension");
        List<List<OneDimensionalEc>> ecsForMutiFiledSet=new ArrayList<>();
        List<OneDimensionalEc> ecForSrcIp=getEcForOnedimension(srcIpFiled,srcIp);
        log.debug("the size of OneDimensionalEcForSrcIP:"+ecForSrcIp.size());
        List<OneDimensionalEc> ecForDstIp=getEcForOnedimension(dstIpFiled,dstIp);
        log.debug("the size of OneDimensionalEcForDstIP:"+ecForDstIp.size());
        List<OneDimensionalEc> ecForInport=getEcForOnedimension(inPortFiled,in_port);
        log.debug("the size of OneDimensionalEcForInPort:"+ecForInport.size());
        ecsForMutiFiledSet.add(ecForSrcIp);
        ecsForMutiFiledSet.add(ecForDstIp);
        ecsForMutiFiledSet.add(ecForInport);
        List<List<OneDimensionalEc>> ecsForMutiDimension=new ArrayList<List<OneDimensionalEc>>();
        Cartesian.recursive(
                ecsForMutiFiledSet,ecsForMutiDimension,0,
                new ArrayList<OneDimensionalEc>());
        log.info("System is generating multiDimensional ecs");
        for(List<OneDimensionalEc> list:ecsForMutiDimension){
            ecs.add(new EC(list.get(0),list.get(1),list.get(2)));
        }
        log.warn("ecsForCurrentFlowMod:" );
        for(EC ec:ecs){
            log.warn(ec.toString());
        }
        return ecs;

    }

    public static HashMap<EC,HashSet<Flow>>  generate_ecmatchedFlow(HashSet<EC> ecs,HashSet<Flow> currenFlowSet){
        if(currenFlowSet==null||currenFlowSet.size()==0){
            return null;
        }
        HashMap<EC,HashSet<Flow>> ecMatchedFlow=new HashMap<>();
        for(EC ec:ecs){
            HashSet<Flow> matchedFlowSet=new HashSet<>();
            //log.debug("ecMatched flow:"+ec.toString());
            for(Flow flow:currenFlowSet){
                //log.debug("ecMatched flow:"+flow.toString());
//                log.debug("data:"+ec.src_ip.start+","+IpConvertion.ipToBinaryString(flow.getSource()));
//                log.debug("data:"+ec.src_ip.end+","+IpConvertion.ipToBinaryString(flow.getSource()));
//                log.debug("data:"+ec.dst_ip.start+","+IpConvertion.ipToBinaryString(flow.getDestination()));
//                log.debug("data:"+ec.dst_ip.end+","+IpConvertion.ipToBinaryString(flow.getDestination()));
//
//                log.debug("flow:"+flow.getSource()+","+flow.getDestination());
//
//                log.debug(String.valueOf(ec.src_ip.start.compareTo(IpConvertion.ipToBinaryString(flow.getSource()))));
//                log.debug(String.valueOf(ec.src_ip.end.compareTo(IpConvertion.ipToBinaryString(flow.getSource()))));
//                log.debug(String.valueOf(ec.dst_ip.start.compareTo(IpConvertion.ipToBinaryString(flow.getDestination()))));
//                log.debug(String.valueOf(ec.dst_ip.end.compareTo(IpConvertion.ipToBinaryString(flow.getDestination()))));
                if(ec.src_ip.start.compareTo(IpConvertion.ipToBinaryString(flow.getSource()))<=0
                        &&ec.src_ip.end.compareTo(IpConvertion.ipToBinaryString(flow.getSource()))>0
                        &&ec.dst_ip.start.compareTo(IpConvertion.ipToBinaryString(flow.getDestination()))<=0
                        &&ec.dst_ip.end.compareTo(IpConvertion.ipToBinaryString(flow.getDestination()))>0
                     //   &&ec.in_port.start.compareTo(IpConvertion.portToBinaryStr(flow.getIn_port()))<=0
                      //
                    //  &&ec.in_port.end.compareTo(IpConvertion.portToBinaryStr(flow.getIn_port()))>0
                ){
                   //log.debug("flow is matched");
                   matchedFlowSet.add(flow);
                }
            }
           if(!(matchedFlowSet==null||matchedFlowSet.size()==0)){
               ecMatchedFlow.put(ec,matchedFlowSet);
           }
        }
        log.warn("matched flow  for ec:");
        for(Map.Entry<EC,HashSet<Flow>> entry:ecMatchedFlow.entrySet()){
            log.warn("ec:"+entry.getKey().toString());
            for(Flow flow:entry.getValue()){
                log.warn(flow.toString());
            }
        }
        return ecMatchedFlow;
    }
    public static HashMap<EC, HashSet<FlowRule>> generate_ecmatchedFlowRule(HashSet<EC> ecs, HashSet<FlowRule> conflictFlowRule) {
        //HashMap<EC, HashSet<EcFiled>> ecMatchedEcFileds = new HashMap<>();
        if(conflictFlowRule==null||conflictFlowRule.size()==0){
            return null;
        }
        HashMap<EC,HashSet<FlowRule>> ecMatchedFlowRule=new HashMap<>();
        for (EC ec : ecs) {
            String srcIplowerboundForCurEc = ec.src_ip.start;
            String srcIPupperboundForCurEc = ec.src_ip.end;
            String dstIplowerboundForCurEc = ec.dst_ip.start;
            String dstIPupperboundForCurEc = ec.dst_ip.end;
            String portlowerboundForCurEc = ec.in_port.start;
            String portupperboundForCurEc = ec.in_port.end;

           // HashSet<EcFiled> matchedEcfileds = new HashSet<>();
            HashSet<FlowRule> matchedFlowRule=new HashSet<>();
            for (FlowRule flowRule : conflictFlowRule) {
                //log.info("size of ecFiled"+FlowModIntersepting.ecfiledFlowRulePair.get(ecField).size());

                String cursrcIPUpperBound = FlowScopeOperation.getUpperbound(IpConvertion.ipIntToString(flowRule.getSrc_ip(),flowRule.getSrc_mask()));
                String cursrcIpLowerBound = FlowScopeOperation.getLowerbound(IpConvertion.ipIntToString(flowRule.getSrc_ip(),flowRule.getSrc_mask()));
                String curdstIPUpperBound = FlowScopeOperation.getUpperbound(IpConvertion.ipIntToString(flowRule.getDst_ip(),flowRule.getDst_mask()));
                String curdstIpLowerBound = FlowScopeOperation.getLowerbound(IpConvertion.ipIntToString(flowRule.getDst_ip(),flowRule.getDst_mask()));
                String curportUpperBound = FlowScopeOperation.getUpperbound(IpConvertion.ipIntToString(flowRule.getIn_port(),flowRule.getPort_mask()));
                String curportLowerBound = FlowScopeOperation.getLowerbound(IpConvertion.ipIntToString(flowRule.getIn_port(),flowRule.getPort_mask()));
                if((!(cursrcIPUpperBound.compareTo(srcIplowerboundForCurEc)<=0
                        || (cursrcIpLowerBound.compareTo(srcIPupperboundForCurEc)>=0)))
                        && (!(((curdstIPUpperBound.compareTo(dstIplowerboundForCurEc)<=0))
                        ||((curdstIpLowerBound.compareTo(dstIPupperboundForCurEc)>=0))))
                        &&(!(((curdstIPUpperBound.compareTo(portlowerboundForCurEc)<=0))
                        ||((curportLowerBound.compareTo(portupperboundForCurEc)>=0))))
                ){
                    matchedFlowRule.add(flowRule);
                }
//                if (cursrcIpLowerBound.compareTo(srcIPupperboundForCurEc) < 0
//                        && cursrcIPUpperBound.compareTo(srcIplowerboundForCurEc) >=0
//                        &&curdstIpLowerBound.compareTo(dstIPupperboundForCurEc) < 0
//                        && curdstIPUpperBound.compareTo(dstIplowerboundForCurEc) >=0) {
//                   // matchedEcfileds.add(ecField);
//                    //log.info("size of ecFiled"+FlowModIntersepting.ecfiledFlowRulePair.get(ecField).size());
//
//                }
            }
           // ecMatchedEcFileds.put(ec, matchedEcfileds);
            if(!(matchedFlowRule==null||matchedFlowRule.size()==0)){
                ecMatchedFlowRule.put(ec,matchedFlowRule);
            }


        }
        log.warn("matched flow rule for ec:");
        for(Map.Entry<EC,HashSet<FlowRule>> entry:ecMatchedFlowRule.entrySet()){
            log.warn("ec:"+entry.getKey().toString());
            log.debug("size of matched flow rule :"+entry.getValue().size());

            for(FlowRule flowRule:entry.getValue()){
                log.warn(flowRule.toString());
            }
        }
        return ecMatchedFlowRule;
    }

    public static HashMap<EC,ECGraph> createEcGraph(HashMap<EC, HashSet<FlowRule>> matchedFlowRuleForEc) {
        //forwarding graph for ec
        HashMap<EC,ECGraph> ecGraphHashMap=new HashMap<>();
        HashMap<String, FlowRule> flow_rule_perswitch = new HashMap<String, FlowRule>();
        for (Map.Entry<EC, HashSet<FlowRule>> entry : matchedFlowRuleForEc.entrySet()) {
            EC ec = entry.getKey();
            HashSet<FlowRule> matchedFlowRulePerEc=entry.getValue();
            if(matchedFlowRuleForEc==null||matchedFlowRuleForEc.size()==0){
                ecGraphHashMap.put(ec,null);
                continue;
            }
            flow_rule_perswitch.clear();
            for (FlowRule flowRule : matchedFlowRulePerEc) {
                String sw = flowRule.getDpid();
                if (flow_rule_perswitch.keySet().contains(sw)) {
                    if (flow_rule_perswitch.get(sw).getPriority() < flowRule.getPriority()) {
                        flow_rule_perswitch.put(sw,flowRule);
                    }
                } else {
                    flow_rule_perswitch.put(sw, flowRule);
                }
            }

//            log.info("matched rules for switch:");
//            for(Map.Entry<String,FlowRule> entry1:flow_rule_perswitch.entrySet()){
//                String sw=entry1.getKey();
//                FlowRule flowRule=entry1.getValue();
//                log.warn("sw:"+sw.toString()+","+"flowRule:"+flowRule.toString());
//            }

            List<String> deviceset = new ArrayList<String>();
            List<TwoTuple<String>> edges = new ArrayList<TwoTuple<String>>();
            for (String sw : flow_rule_perswitch.keySet()) {
                deviceset.add(sw);
                int portNumber = flow_rule_perswitch.get(sw).getAction().getPort();
                Port port = new Port(portNumber, sw);

                if (Network.edge_ports.contains(port)) {
                    continue;
                } else {
                    Port linkedPort = Network.topology.get(port);
                    if (!Network.edge_ports.contains(linkedPort)) {
                        edges.add(new TwoTuple(sw, linkedPort.sid));
                    }
                }
            }
            ecGraphHashMap.put(ec,new ECGraph(deviceset, edges));
           // log.info("ecGraph:"+ecGraphHashMap.toString());
        }
        return ecGraphHashMap;
    }
}
