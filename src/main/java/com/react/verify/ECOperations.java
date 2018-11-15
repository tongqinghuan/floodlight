package com.react.verify;

import com.react.compiler.Flow;
import com.react.topo.Network;
import com.react.topo.Port;
import com.react.topo.TwoTuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

class EC {
    String start;
    String end;

    public EC(String start, String end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((end == null) ? 0 : end.hashCode());
        result = prime * result + ((start == null) ? 0 : start.hashCode());
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
        EC other = (EC) obj;
        if (end == null) {
            if (other.end != null)
                return false;
        } else if (!end.equals(other.end))
            return false;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EC [start=" + start + ", end=" + end + ")";
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

    public static HashSet<EC> getEC(Set<EcFiled> conflict_flow, EcFiled currentFlowRule) {
        HashSet<EC> ECs = new HashSet<EC>();
        Collection<String> EC_range = new TreeSet<String>();
        List<String> Ec_range = new ArrayList<String>();

        String currentLowerbound = FlowScopeOperation.getLowerbound(currentFlowRule.getDst_ip());
        String currentUpperbound = FlowScopeOperation.getUpperbound(currentFlowRule.getDst_ip());

        for (EcFiled filed : conflict_flow) {
            EC_range.add(FlowScopeOperation.getLowerbound(filed.getDst_ip()));
            EC_range.add(FlowScopeOperation.getUpperbound(filed.getDst_ip()));
        }
        EC_range.add(currentLowerbound);
        EC_range.add(currentUpperbound);
        Ec_range.addAll(EC_range);
        log.info("Ec_range:\t" + Ec_range);

        int index_start = Ec_range.indexOf(currentLowerbound);
        int index_end = Ec_range.indexOf(currentUpperbound);

        for (int i = index_start; i < index_end; i++) {
            EC ECTemp = new EC(Ec_range.get(i), Ec_range.get(i + 1));
            ECs.add(ECTemp);
        }
        return ECs;
    }
    public static HashMap<EC,HashSet<Flow>>  generate_ecmatchedFlow(HashSet<EC> ecs,HashSet<Flow> currenFlowSet){
        if(currenFlowSet==null||currenFlowSet.size()==0){
            return null;
        }
        HashMap<EC,HashSet<Flow>> ecMatchedFlow=new HashMap<>();
        for(EC ec:ecs){
            HashSet<Flow> matchedFlowSet=new HashSet<>();
            for(Flow flow:currenFlowSet){
                if(ec.start.compareTo(flow.getDestination())<=0&&ec.end.compareTo(flow.getDestination())>=0){
                   matchedFlowSet.add(flow);
                }
            }
            ecMatchedFlow.put(ec,matchedFlowSet);
        }
        return ecMatchedFlow;
    }
    public static HashMap<EC, HashSet<EcFiled>> generate_ecmatchedecFiled(HashSet<EC> ecs, HashSet<EcFiled> conflictEcfiled) {
        HashMap<EC, HashSet<EcFiled>> ecMatchedEcFileds = new HashMap<>();
        for (EC ec : ecs) {
            String lowerboundForCurEc = ec.start;
            String upperboundForCurEc = ec.end;
            HashSet<EcFiled> matchedEcfileds = new HashSet<>();
            for (EcFiled ecField : conflictEcfiled) {
                String curUpperBound = FlowScopeOperation.getUpperbound(ecField.getDst_ip());
                String curLowerBound = FlowScopeOperation.getLowerbound(ecField.getDst_ip());
                if (curLowerBound.compareTo(upperboundForCurEc) < 0 && curUpperBound.compareTo(lowerboundForCurEc) > 0) {
                    matchedEcfileds.add(ecField);
                }
            }
            ecMatchedEcFileds.put(ec, matchedEcfileds);

        }
        return ecMatchedEcFileds;
    }

    public static Set<ECGraph> createEcGraph(HashMap<EC, HashSet<EcFiled>> matchedEcfiledForEc, HashMap<EcFiled, FlowRule> ecfiledFlowRulePair) {
        //forwarding graph for ec
        Set<ECGraph> ecGraph = new HashSet<ECGraph>();
        List<FlowRule> flow_rule_perec = new ArrayList<FlowRule>();
        HashMap<String, FlowRule> flow_rule_perswitch = new HashMap<String, FlowRule>();
        for (Map.Entry<EC, HashSet<EcFiled>> entry : matchedEcfiledForEc.entrySet()) {
            EC ec = entry.getKey();
            flow_rule_perec.clear();
            flow_rule_perswitch.clear();
            for (EcFiled per_ec_filed : entry.getValue()) {
                flow_rule_perec.add(ecfiledFlowRulePair.get(per_ec_filed));
            }

            for (FlowRule flowRule : flow_rule_perec) {
                String sw = flowRule.getDpid();
                if (flow_rule_perswitch.keySet().contains(sw)) {
                    if (flow_rule_perswitch.get(sw).getPriority() < flowRule.getPriority()) {
                        flow_rule_perswitch.replace(sw, flow_rule_perswitch.get(sw), flowRule);
                    }
                } else {
                    flow_rule_perswitch.put(sw, flowRule);
                }
            }

            log.info("matched rules for switch:"+flow_rule_perswitch.toString());

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
            ecGraph.add(new ECGraph(ec, deviceset, edges));
            log.info("ecGraph:"+ecGraph.toString());
        }
        return ecGraph;
    }
}
