package com.react.verify;

import com.react.compiler.Flow;
import com.react.topo.TwoTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.react.Utils.*;
import java.util.*;
class EC {
   OneDimensionalEc src_ip;
   OneDimensionalEc dst_ip;

    public EC(OneDimensionalEc src_ip, OneDimensionalEc dst_ip) {
        this.src_ip = src_ip;
        this.dst_ip = dst_ip;
    }

    public OneDimensionalEc getSrc_ip() {
        return src_ip;
    }

    public OneDimensionalEc getDst_ip() {
        return dst_ip;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EC ec = (EC) o;
        return Objects.equals(src_ip, ec.src_ip) &&
                Objects.equals(dst_ip, ec.dst_ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src_ip, dst_ip);
    }

    @Override
    public String toString() {
        return "EC{" +
                "src_ip=" + src_ip +
                ", dst_ip=" + dst_ip +
                '}';
    }
}


class ECGraph {
    EC ec;
    List<String> deviceset;
    List<TwoTuple<String>> edges;

    public ECGraph(EC ec, List<String> deviceset2, List<TwoTuple<String>> edges2) {
        this.deviceset = deviceset2;
        this.edges = edges2;
        this.ec = ec;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ECGraph ecGraph = (ECGraph) o;
        return Objects.equals(ec, ecGraph.ec) &&
                Objects.equals(deviceset, ecGraph.deviceset) &&
                Objects.equals(edges, ecGraph.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ec, deviceset, edges);
    }

    @Override
    public String toString() {
        return "ECGraph{" +
                "ec=" + ec +
                ", deviceset=" + deviceset +
                ", edges=" + edges +
                '}';
    }
}

public class ECOperations {
    protected static Logger log = LoggerFactory.getLogger(ECOperations.class);
    public static ArrayList<OneDimensionalEc> getEcForOnedimension(Set<String> conflictSetForOneFiled,String currentFiled){
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

        for (int i = index_start; i < index_end; i++) {
            OneDimensionalEc temp = new OneDimensionalEc(ecRangeList.get(i), ecRangeList.get(i + 1));
            onedimensionalECs.add(temp);
        }
        return onedimensionalECs;

    }
    public static HashSet<EC> getEcForMutidimension(Set<EcFiled> conflictEcFileds, EcFiled currentEcFiled){
        HashSet<EC> ecs=new HashSet<>();
        Set<String> srcIpFiled=new HashSet<>();
        Set<String> dstIpFiled=new HashSet<>();
        String srcIp=currentEcFiled.getSrc_ip();
        String dstIp=currentEcFiled.getDst_ip();
        for(EcFiled ecFiled:conflictEcFileds){
            srcIpFiled.add(ecFiled.getSrc_ip());
            dstIpFiled.add(ecFiled.getDst_ip());
        }
        log.info("System is calculating ecs for one dimension");
        List<List<OneDimensionalEc>> ecsForMutiFiledSet=new ArrayList<>();
        List<OneDimensionalEc> ecForSrcIp=getEcForOnedimension(srcIpFiled,srcIp);
        List<OneDimensionalEc> ecForDstIp=getEcForOnedimension(dstIpFiled,dstIp);
        ecsForMutiFiledSet.add(ecForSrcIp);
        ecsForMutiFiledSet.add(ecForDstIp);
        List<List<OneDimensionalEc>> ecsForMutiDimension=new ArrayList<List<OneDimensionalEc>>();
        Cartesian.recursive(
                ecsForMutiFiledSet,ecsForMutiDimension,0,
                new ArrayList<OneDimensionalEc>());
        log.info("System is generating multiDimensional ecs");
        for(List<OneDimensionalEc> list:ecsForMutiDimension){
            ecs.add(new EC(list.get(0),list.get(1)));
        }
        log.warn(" ecs for multi dimension :"+ecs.toString());
        return ecs;

    }

    public static HashMap<EC,HashSet<Flow>>  generate_ecmatchedFlow(HashSet<EC> ecs,HashSet<Flow> currenFlowSet){
        if(currenFlowSet==null||currenFlowSet.size()==0){
            return null;
        }
        HashMap<EC,HashSet<Flow>> ecMatchedFlow=new HashMap<>();
        for(EC ec:ecs){
            HashSet<Flow> matchedFlowSet=new HashSet<>();
            for(Flow flow:currenFlowSet){
                if(ec.src_ip.start.compareTo(IpConvertion.ipToBinaryString(flow.getSource()))<=0
                        &&ec.src_ip.end.compareTo(IpConvertion.ipToBinaryString(flow.getSource()))>0
                        &&ec.dst_ip.start.compareTo(IpConvertion.ipToBinaryString(flow.getDestination()))<=0
                        &&ec.dst_ip.end.compareTo(IpConvertion.ipToBinaryString(flow.getDestination()))>0){
                   matchedFlowSet.add(flow);
                }
            }
            ecMatchedFlow.put(ec,matchedFlowSet);
        }
        log.warn("matched flow  for ec:"+ecMatchedFlow.toString());
        return ecMatchedFlow;
    }
    public static HashMap<EC, HashSet<FlowRule>> generate_ecmatchedecFiled(HashSet<EC> ecs, HashSet<EcFiled> conflictEcfiled) {
        //HashMap<EC, HashSet<EcFiled>> ecMatchedEcFileds = new HashMap<>();
        HashMap<EC,HashSet<FlowRule>> ecMatchedFlowRule=new HashMap<>();
        for (EC ec : ecs) {
            String srcIplowerboundForCurEc = ec.src_ip.start;
            String srcIPupperboundForCurEc = ec.src_ip.end;
            String dstIplowerboundForCurEc = ec.dst_ip.start;
            String dstIPupperboundForCurEc = ec.dst_ip.end;
           // HashSet<EcFiled> matchedEcfileds = new HashSet<>();
            HashSet<FlowRule> matchedFlowRule=new HashSet<>();
            for (EcFiled ecField : conflictEcfiled) {

                String cursrcIPUpperBound = FlowScopeOperation.getUpperbound(ecField.getDst_ip());
                String cursrcIpLowerBound = FlowScopeOperation.getLowerbound(ecField.getDst_ip());
                String curdstIPUpperBound = FlowScopeOperation.getUpperbound(ecField.getDst_ip());
                String curdstIpLowerBound = FlowScopeOperation.getLowerbound(ecField.getDst_ip());
                if (cursrcIpLowerBound.compareTo(srcIPupperboundForCurEc) < 0
                        && cursrcIPUpperBound.compareTo(srcIplowerboundForCurEc) >=0
                        &&curdstIpLowerBound.compareTo(dstIPupperboundForCurEc) < 0
                        && curdstIPUpperBound.compareTo(dstIplowerboundForCurEc) >=0) {
                   // matchedEcfileds.add(ecField);
                    matchedFlowRule.add(FlowModIntersepting.ecfiledFlowRulePair.
                            get(ecField));
                }
            }
           // ecMatchedEcFileds.put(ec, matchedEcfileds);
            ecMatchedFlowRule.put(ec,matchedFlowRule);


        }
        log.warn("matched flow rule for ec:"+ecMatchedFlowRule.toString());
        return ecMatchedFlowRule;
    }

//    public static Set<ECGraph> createEcGraph(HashMap<EC, HashSet<EcFiled>> matchedEcfiledForEc, HashMap<EcFiled, FlowRule> ecfiledFlowRulePair) {
//        //forwarding graph for ec
//        Set<ECGraph> ecGraph = new HashSet<ECGraph>();
//        List<FlowRule> flow_rule_perec = new ArrayList<FlowRule>();
//        HashMap<String, FlowRule> flow_rule_perswitch = new HashMap<String, FlowRule>();
//        for (Map.Entry<EC, HashSet<EcFiled>> entry : matchedEcfiledForEc.entrySet()) {
//            EC ec = entry.getKey();
//            flow_rule_perec.clear();
//            flow_rule_perswitch.clear();
//            for (EcFiled per_ec_filed : entry.getValue()) {
//                flow_rule_perec.add(ecfiledFlowRulePair.get(per_ec_filed));
//            }
//
//            for (FlowRule flowRule : flow_rule_perec) {
//                String sw = flowRule.getDpid();
//                if (flow_rule_perswitch.keySet().contains(sw)) {
//                    if (flow_rule_perswitch.get(sw).getPriority() < flowRule.getPriority()) {
//                        flow_rule_perswitch.replace(sw, flow_rule_perswitch.get(sw), flowRule);
//                    }
//                } else {
//                    flow_rule_perswitch.put(sw, flowRule);
//                }
//            }
//
//            log.info("matched rules for switch:"+flow_rule_perswitch.toString());
//
//            List<String> deviceset = new ArrayList<String>();
//            List<TwoTuple<String>> edges = new ArrayList<TwoTuple<String>>();
//            for (String sw : flow_rule_perswitch.keySet()) {
//                deviceset.add(sw);
//                int portNumber = flow_rule_perswitch.get(sw).getAction().getPort();
//                Port port = new Port(portNumber, sw);
//
//                if (Network.edge_ports.contains(port)) {
//                    continue;
//                } else {
//                    Port linkedPort = Network.topology.get(port);
//                    if (!Network.edge_ports.contains(linkedPort)) {
//                        edges.add(new TwoTuple(sw, linkedPort.sid));
//                    }
//                }
//            }
//            ecGraph.add(new ECGraph(ec, deviceset, edges));
//            log.info("ecGraph:"+ecGraph.toString());
//        }
//        return ecGraph;
//    }
}
