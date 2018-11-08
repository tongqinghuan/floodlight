
package com.react.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.react.topo.ConnectedSwitch;
import com.react.topo.Network;
import com.react.topo.Port;
import com.react.topo.TwoTuple;

public class SemanticRepair {
	static List<Instruction> unchanged_rules = new ArrayList<Instruction>();
	static List<Instruction> added_rules = new ArrayList<Instruction>();
	static List<Instruction> deleted_rules = new ArrayList<Instruction>();
	static List<Instruction> new_flow_semantic_rules = new ArrayList<Instruction>();

	public static HashMap<Flow, HashMap<String, Instruction>> flow_semantic_rules = new HashMap<Flow, HashMap<String, Instruction>>();

	public static boolean semanticRepair(Instruction semantic_rules, Set<TwoTuple<String>> dof) {
		String current_switch_id = semantic_rules.scope.scope.get("switch_id");
		Map<String, ConnectedSwitch> connected_switch = Network
				.getConnectedSwitch(Network.switches.get(current_switch_id));
		Set<TwoTuple<String>> art_dof = new HashSet<TwoTuple<String>>();
		Set<TwoTuple<String>> forbid = new HashSet<TwoTuple<String>>();
		Set<TwoTuple<String>> fixed_forward = new HashSet<TwoTuple<String>>();
		Set<TwoTuple<String>> towards = new HashSet<TwoTuple<String>>();
		// System.out.println("semantic_rules"+semantic_rules);
		art_dof.addAll(semantic_rules.annotation.annotation);
		if (dof != null) {
			art_dof.addAll(dof);
		}
		// System.out.println("art_dof"+art_dof);
		for (TwoTuple<String> tuple : art_dof) {
			if (tuple.first.equals("forbid")) {
				forbid.add(tuple);
			}
			if (tuple.first.equals("towards")) {
				towards.add(tuple);
			}
			if (tuple.first.equals("fixed_forward")) {
				fixed_forward.add(tuple);
			}
		}
		for (TwoTuple<String> tuple : forbid) {

			connected_switch.remove(tuple.second, connected_switch.get(tuple.second));
		}
		for (TwoTuple<String> tuple : fixed_forward) {
			if (Network.edge_ports.contains(new Port(Integer.parseInt(tuple.second), current_switch_id))) {
				return true;
			} else if (connected_switch.containsKey(current_switch_id)) {
				connected_switch.clear();
				connected_switch.put(current_switch_id, connected_switch.get(current_switch_id));
			} else {
				return false;
			}
		}
		boolean final_result = false;
		if (!towards.isEmpty()) {
			for (ConnectedSwitch sw : connected_switch.values()) {
				if (final_result) {
					break;
				}
				Set<TwoTuple<String>> next_dof = new HashSet<TwoTuple<String>>();
				String dst_ip = semantic_rules.scope.scope.get("dst_ip");
				// System.out.println(dst_ip);
				// System.out.println(sw.sid);
				// System.out.println(flow_semantic_rules);
				Instruction ins = flow_semantic_rules.get(dst_ip).get(sw.sid);

				if (ins == null) {
					ins = new Instruction(new Scope(sw.sid, semantic_rules.scope.scope.get("dst_ip")), new Constraint(),
							new Annotation());
				}

				// System.out.println("next_dof"+next_dof);
				next_dof.addAll(towards);
				next_dof.remove(new TwoTuple("towards", sw.sid));
				// System.out.println("next_dof"+next_dof);
				next_dof.addAll(forbid);
				// System.out.println("next_dof"+next_dof);
				// System.out.println("next_dof"+next_dof);
				// System.out.println(ins);
				final_result = semanticRepair(ins, next_dof);
				if (final_result) {
					HashMap<String, Integer> new_action = new HashMap<String, Integer>();
					new_action.put("forward", sw.pid);
					semantic_rules.constraint.setAction(new_action);
					Set<TwoTuple<String>> new_dofs = new HashSet<TwoTuple<String>>();
					new_dofs.addAll(towards);
					new_dofs.add(new TwoTuple("forbid", current_switch_id));
					semantic_rules.annotation.annotation = new_dofs;
					// System.out.println(ins);
					new_flow_semantic_rules.add(semantic_rules);
					return true;
				}
			}
		}
		if (towards.isEmpty() && !final_result) {
			final_result = true;
		}
		return final_result;
	}

	public static void update_rules(Collection<Instruction> old, Collection<Instruction> newed) {
		unchanged_rules.addAll(old);
		unchanged_rules.retainAll(newed);
		added_rules.addAll(newed);
		added_rules.removeAll(unchanged_rules);
		deleted_rules.addAll(old);
		deleted_rules.removeAll(unchanged_rules);

	}
	/*
	 * public static void test(String[] args) { List<FourTuple> link=new
	 * ArrayList<FourTuple>();
	 * 
	 * link.add(new FourTuple("A","B",3,3)); link.add(new FourTuple("A","C",1,1));
	 * link.add(new FourTuple("B","D",1,1)); link.add(new FourTuple("C","D",2,2));
	 * link.add(new FourTuple("B","E",2,2)); link.add(new FourTuple("D","F",3,3));
	 * link.add(new FourTuple("E","G",1,1)); link.add(new FourTuple("F","G",2,2));
	 * // Network.create_node(link); // Network.create_edge_port();
	 * //System.out.println(Network.edge_ports);
	 * //System.out.println(Network.switches);
	 * //System.out.println(Network.topology);
	 * //System.out.println(Network.topology.size());
	 * 
	 * HashMap<String,Instruction> semantic_rules=new HashMap<String,Instruction>();
	 * Scope scope=new Scope("A","11.1.0.0/16"); List<String> actions=new
	 * ArrayList<String>(); actions.add("forward"); List<Integer> ports=new
	 * ArrayList<Integer>(); ports.add(3); Constraint constriant=new
	 * Constraint(actions,ports); Annotation annotation=new Annotation();
	 * annotation.annotation.add(new TwoTuple("towards","B"));
	 * annotation.annotation.add(new TwoTuple("forbid","A"));
	 * semantic_rules.put("A", new Instruction(scope,constriant,annotation));
	 * 
	 * 
	 * List<Instruction> instruction1=new ArrayList<Instruction>(); Scope scope1=new
	 * Scope("B","11.1.0.0/16"); List<String> actions1=new ArrayList<String>();
	 * actions.add("forward"); List<Integer> ports1=new ArrayList<Integer>();
	 * ports.add(1); Constraint constriant1=new Constraint(actions,ports);
	 * Annotation annotation1=new Annotation(); annotation1.annotation.add(new
	 * TwoTuple("towards","G")); annotation1.annotation.add(new
	 * TwoTuple("forbid","B")); semantic_rules.put("B", new
	 * Instruction(scope1,constriant1,annotation1));
	 * 
	 * List<Instruction> instruction2=new ArrayList<Instruction>(); Scope scope2=new
	 * Scope("D","11.1.0.0/16"); List<String> actions2=new ArrayList<String>();
	 * actions.add("forward"); List<Integer> ports2=new ArrayList<Integer>();
	 * ports.add(3); Constraint constriant2=new Constraint(actions,ports);
	 * Annotation annotation2=new Annotation(); annotation2.annotation.add(new
	 * TwoTuple("towards","G")); annotation2.annotation.add(new
	 * TwoTuple("forbid","D")); semantic_rules.put("D", new
	 * Instruction(scope2,constriant2,annotation2));
	 * 
	 * List<Instruction> instruction3=new ArrayList<Instruction>(); Scope scope3=new
	 * Scope("F","11.1.0.0/16"); List<String> actions3=new ArrayList<String>();
	 * actions.add("forward"); List<Integer> ports3=new ArrayList<Integer>();
	 * ports.add(2); Constraint constriant3=new Constraint(actions,ports);
	 * Annotation annotation3=new Annotation(); annotation3.annotation.add(new
	 * TwoTuple("towards","G")); annotation3.annotation.add(new
	 * TwoTuple("forbid","F")); semantic_rules.put("F", new
	 * Instruction(scope3,constriant3,annotation3));
	 * 
	 * List<Instruction> instruction4=new ArrayList<Instruction>(); Scope scope4=new
	 * Scope("G","11.1.0.0/16"); List<String> actions4=new ArrayList<String>();
	 * actions.add("forward"); List<Integer> ports4=new ArrayList<Integer>();
	 * ports.add(3); Constraint constriant4=new Constraint(actions,ports);
	 * Annotation annotation4=new Annotation(); annotation4.annotation.add(new
	 * TwoTuple("fixed_foward","3")); annotation4.annotation.add(new
	 * TwoTuple("forbid","G")); semantic_rules.put("G", new
	 * Instruction(scope4,constriant4,annotation4));
	 * 
	 * flow_semantic_rules.put("11.1.0.0/16",semantic_rules );
	 * //System.out.println(flow_semantic_rules);
	 * 
	 * Set<TwoTuple<String>> art_dof=new HashSet<TwoTuple<String>>();
	 * art_dof.add(new TwoTuple("forbid","D"));
	 * 
	 * String exit_switch_id="G"; SemanticRepair.semanticRepair(new
	 * Instruction(scope,constriant,annotation),art_dof);
	 * Collections.reverse(new_flow_semantic_rules);
	 * new_flow_semantic_rules.add(flow_semantic_rules.get("11.1.0.0/16").get(
	 * exit_switch_id)); System.out.println(new_flow_semantic_rules);
	 * 
	 * update_rules(flow_semantic_rules.get("11.1.0.0/16").values(),
	 * new_flow_semantic_rules);
	 * 
	 * System.out.println(added_rules); System.out.println(deleted_rules);
	 * 
	 * }
	 */
}
