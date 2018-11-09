package com.react.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.react.topo.Network;
import com.react.topo.Port;
import com.react.topo.TwoTuple;

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

class ECMatchedRules {
    EC ec;
    HashSet<Flow> ruleset;

    public ECMatchedRules(EC ec, HashSet<Flow> ruleset) {
        this.ec = ec;
        this.ruleset = ruleset;
    }

    @Override
    public String toString() {
        return "ECMatchedRules [ec=" + ec + ", ruleset=" + ruleset + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ec == null) ? 0 : ec.hashCode());
        result = prime * result + ((ruleset == null) ? 0 : ruleset.hashCode());
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
        ECMatchedRules other = (ECMatchedRules) obj;
        if (ec == null) {
            if (other.ec != null)
                return false;
        } else if (!ec.equals(other.ec))
            return false;
        if (ruleset == null) {
            if (other.ruleset != null)
                return false;
        } else if (!ruleset.equals(other.ruleset))
            return false;
        return true;
    }
}

class ECGraph {
    EC ec;
    List<TwoTuple<String>> srcDstPair;
    List<String> deviceset;
    List<TwoTuple<String>> edges;

    public ECGraph(EC ec, List<String> deviceset2, List<TwoTuple<String>> edges2, List<TwoTuple<String>> srcDstPair) {
        this.deviceset = deviceset2;
        this.edges = edges2;
        this.ec = ec;
        this.srcDstPair = srcDstPair;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deviceset == null) ? 0 : deviceset.hashCode());
        result = prime * result + ((ec == null) ? 0 : ec.hashCode());
        result = prime * result + ((edges == null) ? 0 : edges.hashCode());
        result = prime * result + ((srcDstPair == null) ? 0 : srcDstPair.hashCode());
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
        ECGraph other = (ECGraph) obj;
        if (deviceset == null) {
            if (other.deviceset != null)
                return false;
        } else if (!deviceset.equals(other.deviceset))
            return false;
        if (ec == null) {
            if (other.ec != null)
                return false;
        } else if (!ec.equals(other.ec))
            return false;
        if (edges == null) {
            if (other.edges != null)
                return false;
        } else if (!edges.equals(other.edges))
            return false;
        if (srcDstPair == null) {
            if (other.srcDstPair != null)
                return false;
        } else if (!srcDstPair.equals(other.srcDstPair))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ECGraph [ec=" + ec + ", srcDstPair=" + srcDstPair + ", deviceset=" + deviceset + ", edges=" + edges
                + "]";
    }
}

public class ECOperations {
    //	public static HashSet<EC> getEC(Set<Flow> conflict_flow,Flow currentFlow){
//		HashSet<EC> ECs = new HashSet<EC>();
//		Collection<String> EC_range = new TreeSet<String>();
//		List<String> Ec_range=new ArrayList<String>();
//
//		String currentLowerbound = FlowScopeOperation.getLowerbound(currentFlow.dst_ip);
//		String currentUpperbound = FlowScopeOperation.getUpperbound(currentFlow.dst_ip);
//
//		for(Flow str:conflict_flow) {
//			EC_range.add(FlowScopeOperation.getLowerbound(str.dst_ip));
//			EC_range.add(FlowScopeOperation.getUpperbound(str.dst_ip));
//		}
//		//EC_range.add(currentLowerbound);
//		//EC_range.add(currentUpperbound);
//        Ec_range.addAll(EC_range);
//        System.out.println("Ec_range:\t"+Ec_range);
//
//        int index_start = Ec_range.indexOf(currentLowerbound);
//        int index_end = Ec_range.indexOf(currentUpperbound);
//
//        for(int i = index_start; i < index_end; i++) {
//            if(i == index_start) {
//        		EC ECTemp = new EC(Ec_range.get(i),Ec_range.get(i+1));
//        		ECs.add(ECTemp);
//            }
//            else {
//            	EC ECTemp = new EC(Ec_range.get(i),Ec_range.get(i+1));
//            	ECs.add(ECTemp);
//            }
//        }
//		return ECs;
//	}

    public static EC updateEC(HashSet<EC> ECs, Set<Flow> conflict_flow, Flow currentFlow, boolean IsNeedRemoveOldEc) {
        EC result = new EC("", "");
        Collection<String> EC_range = new TreeSet<String>();
//		Ec_range compute by conflict_flows and Flow currentFlow
        List<String> Ec_range = new ArrayList<String>();
//		System.out.println(currentFlow.dst_ip);
        String currentLowerbound = FlowScopeOperation.getLowerbound(currentFlow.dst_ip);
        String currentUpperbound = FlowScopeOperation.getUpperbound(currentFlow.dst_ip);
//		System.out.println("currentFlow:" + currentLowerbound + "--" + currentUpperbound);
        for (Flow str : conflict_flow) {
            EC_range.add(FlowScopeOperation.getLowerbound(str.dst_ip));
            EC_range.add(FlowScopeOperation.getUpperbound(str.dst_ip));
//			if(currentLowerbound.compareTo(FlowScopeOperation.getLowerbound(str.dst_ip)) < 0
//					&& currentUpperbound.compareTo(FlowScopeOperation.getUpperbound(str.dst_ip)) > 0 ){
            HashSet<EC> temp = new HashSet<>(ECs);//resolve ConcurrentModificationException
            if (IsNeedRemoveOldEc == true) {
                for (EC tmpec : temp) {
                    if (tmpec.start.equals(FlowScopeOperation.getLowerbound(str.dst_ip))
                            && tmpec.end.equals(FlowScopeOperation.getUpperbound(str.dst_ip))) {
                        ECs.remove(tmpec);
                        result = tmpec;
                    }
                }
            }
        }
        EC_range.add(currentLowerbound);
        EC_range.add(currentUpperbound);
        Ec_range.addAll(EC_range);
        System.out.println("Ec_range:\t" + Ec_range);
        int j = 0;
//        int index_start = Ec_range.indexOf(currentLowerbound);//0  1
//        int index_end = Ec_range.indexOf(currentUpperbound);  //3  2\


        for (int i = 0; i < EC_range.size() - 1; i = i + 1) {
            EC ECTemp = new EC(Ec_range.get(i), Ec_range.get(i + 1));
            ECs.add(ECTemp);
        }
//		System.out.println("need remove:"+result);
        return result;
    }

//    public static Set<ECMatchedRules> generate_ecMatched_rules(HashSet<EC> ECs, Set<Flow> confict_flow) {
//        Set<ECMatchedRules> ec_matched_rules = new HashSet<ECMatchedRules>();
//        for (EC ec : ECs) {
////			String upper_bound = ec.start;
////			String lower_bound = ec.end;
//            HashSet<Flow> ruleset = new HashSet<Flow>();
//            for (Flow entry : confict_flow) {
//                String flow_upper_bound = FlowScopeOperation.getUpperbound(entry.dst_ip);
//                String flow_lower_bound = FlowScopeOperation.getLowerbound(entry.dst_ip);
//                if (flow_upper_bound.compareTo(ec.start) > 0 && flow_lower_bound.compareTo(ec.end) < 0) {
//                    ruleset.add(entry);//ec.start | flow | ec.end
//                }
//            }
//            ec_matched_rules.add(new ECMatchedRules(ec, ruleset));
//        }
//        return ec_matched_rules;
//    }


    /**
     * each ec maps all flows in the tree
     *
     * @param ECs
     * @param ec_matched_rules
     * @param confict_flow
     * @return
     */
    public static Set<ECMatchedRules> update_ecMatched_rules(HashSet<EC> ECs,
                                                             EC needRemoveEC,
                                                             Set<ECMatchedRules> ec_matched_rules,
                                                             Set<Flow> confict_flow) {
//		Set<ECMatchedRules> ec_matched_rules=new HashSet<ECMatchedRules>();
        if (!needRemoveEC.start.equals("")) {
//			System.out.println("need Remove :" + needRemoveEC);
            for (ECMatchedRules isContain : ec_matched_rules) {
                if (isContain.ec.equals(needRemoveEC)) {
                    ec_matched_rules.remove(isContain);
                    break;
                }
            }
        }
        for (EC eachec : ECs) {
            boolean flag = true;
            for (ECMatchedRules isContain : ec_matched_rules) {
                if (isContain.ec.equals(eachec)) {
                    flag = false;
                    break;
                }
            }
            if (!flag) {
                continue;
            }
            String upper_bound = eachec.start;
            String lower_bound = eachec.end;
            HashSet<Flow> ruleset = new HashSet<Flow>();

            for (Flow entry : confict_flow) {
                String flow_upper_bound = FlowScopeOperation.getUpperbound(entry.dst_ip);
                String flow_lower_bound = FlowScopeOperation.getLowerbound(entry.dst_ip);
//				System.out.println(flow_lower_bound + "--" + flow_upper_bound + " " + ec.start + "--" + ec.end);
//				System.out.println(flow_lower_bound.compareTo(ec.start) + " " + flow_upper_bound.compareTo(ec.end));
//				if conflict flow range include an ec range, then the ec can influence the flow
                if (flow_lower_bound.compareTo(eachec.end) < 0 && flow_upper_bound.compareTo(eachec.start) > 0) {
                    ruleset.add(entry);
                }
            }
//			if (!ruleset.equals(new HashSet<Flow>())){
            ec_matched_rules.add(new ECMatchedRules(eachec, ruleset));
//			}
        }

        return ec_matched_rules;
    }

    public static Set<ECGraph> getNodeEdge(Set<ECMatchedRules> ec_matched_rules, HashMap<Flow, ArrayList<FlowRule>> rules) {
        Set<ECGraph> ecGraph = new HashSet<ECGraph>();
        List<FlowRule> rule_per_ec = new ArrayList<FlowRule>();
        HashMap<String, FlowRule> flow_rules_ec = new HashMap<String, FlowRule>();
// sw上优先级最高的flowrule
        for (ECMatchedRules entry : ec_matched_rules) {
            EC ec = entry.ec;
            rule_per_ec.clear();
            flow_rules_ec.clear();
            List<TwoTuple<String>> srcdst = new ArrayList<TwoTuple<String>>();

            for (Flow per_flow : entry.ruleset) {
                rule_per_ec.addAll(rules.get(per_flow));
                int size = rules.get(per_flow).size();
                srcdst.add(new TwoTuple(rules.get(per_flow).get(0).sw, rules.get(per_flow).get(size - 1).sw));
                //有问题。这里假设了一条流只有两条流表
            }

            for (FlowRule temp : rule_per_ec) {
                String sw = temp.sw;
                if (flow_rules_ec.keySet().contains(sw)) {//sw相同
                    if (flow_rules_ec.get(sw).getPriority() < temp.getPriority()) {
                        flow_rules_ec.replace(sw, flow_rules_ec.get(sw), temp);
                    }//有问题。这里选出的是交换机上流表优先级最高的。没考虑flow是否一致。
                } else {
                    flow_rules_ec.put(sw, temp);
                }
            }

            System.out.println(flow_rules_ec);

            List<String> deviceset = new ArrayList<String>();
            List<TwoTuple<String>> edges = new ArrayList<TwoTuple<String>>();
            for (String sw : flow_rules_ec.keySet()) {
                deviceset.add(sw);
                int portNumber = flow_rules_ec.get(sw).getAction().get("forward");
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
            ecGraph.add(new ECGraph(ec, deviceset, edges, srcdst));
        }
        return ecGraph;
    }
}
