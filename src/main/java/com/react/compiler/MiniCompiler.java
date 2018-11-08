package com.react.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.react.topo.Network;
import com.react.topo.Port;
import com.react.topo.TwoTuple;
import com.react.verify.Annotation;
import com.react.verify.Constraint;
import com.react.verify.Instruction;
import com.react.verify.Scope;

import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.topology.NodePortTuple;

public class MiniCompiler {

	public static HashSet<Flow> flows;
	public static HashSet<Integer> waypoints;
	public static HashMap<Flow, EnAndExEntry> entry_exit_flow = new HashMap<>();
	public static HashMap<Flow, ArrayList<Instruction>> semantic_rules = new HashMap<>();

	private static List<NodePortTuple> route_list = new ArrayList<NodePortTuple>();
	private static HashMap<Flow, Route> route = new HashMap<>();

	private static final Logger logger = LoggerFactory.getLogger(MiniCompiler.class);

	public static void installPathByIntent() {
		Network.TopologyInit();
		flows = (HashSet<Flow>) RandomFlows.generateFlowsRandomly();
		logger.info("install path by intent!----");
		logger.info(flows.toString());
		for (Flow flow : flows) {
			List<NodePortTuple> route_list = new ArrayList<NodePortTuple>();
			// get entry switch and exit switch for some flow;
			String srcIp = flow.getSource();
			String dstIp = flow.getDestination();
			Port srcp = Network.host_edgeport.get(srcIp);
			Port dstp = Network.host_edgeport.get(dstIp);
			String srcDpid = srcp.sid;
			String dstDpid = dstp.sid;

			entry_exit_flow.put(flow, new EnAndExEntry(srcDpid, dstDpid));

			// create route
			Route some_route = new Route(DatapathId.of(srcDpid), DatapathId.of(dstDpid));
			// cal path
			route_list.add(new NodePortTuple(DatapathId.of(srcp.sid), OFPort.ofInt(srcp.pid)));
			logger.info("srcdpid+dstdpid:" + srcDpid + "," + dstDpid);
			Route temp = Network.calculateAllShortestPaths(srcDpid, dstDpid);
			logger.info(temp.toString());
			route_list.addAll(temp.getPath());
			route_list.add(new NodePortTuple(DatapathId.of(dstp.sid), OFPort.ofInt(dstp.pid)));
			some_route.getPath().addAll(route_list);
			route.put(flow, some_route);
		}
		for (Map.Entry<Flow, Route> entry : route.entrySet()) {
			logger.info(entry.getKey().getSource() + "," + entry.getKey().getDestination() + ":"
					+ entry.getValue().getPath().toString());
		}

	}

	public static void installSemanticRules() {
		if (route == null || route.size() == 0) {
			logger.info("---there is something with routing---");
		}
		// generate random semantics
		logger.info("begginning to install semantic rules!");
		HashSet<String> waypoint;
		waypoint = RandomFlows.generateWaypointRandomly();
		// logger.info("random semantics:" + waypoint.toString());
		for (Map.Entry<Flow, Route> entry : route.entrySet()) {
			Flow flow = entry.getKey();
			Route routing = entry.getValue();
			ArrayList<Instruction> ins = new ArrayList<>();
			if (routing == null || routing.getPath().size() == 0) {
				logger.info("flow:" + flow.getSource() + "--->" + flow.getDestination() + " is unreachable");
				continue;
			}
			List<NodePortTuple> path = routing.getPath();
			// logger.info("path for " + flow.toString() + path.toString());
			Collections.reverse(path);
			int index = 0;
			String dst_dpid = entry_exit_flow.get(flow).getDstDpid();
			String cur_waypoint = dst_dpid;
			while (index < (path.size() - 1)) {
				String cur_node_id = Long.toString(path.get(index).getNodeId().getLong());
				Scope scope = new Scope(cur_node_id, flow.getDestination());
				// logger.info("scope:" + scope.toString());
				List<String> actions = new ArrayList<String>();
				actions.add("forward");
				int out_port = path.get(index).getPortId().getPortNumber();
				List<Integer> ports = new ArrayList<Integer>();
				ports.add(out_port);
				Constraint constriant = new Constraint(actions, ports, 16);
				// logger.info("constraint:" + constriant.toString());
				Annotation annotation = new Annotation();
				if (cur_node_id.equals(dst_dpid)) {
					annotation.annotation.add(new TwoTuple("fixed_foward", out_port));
				} else {
					annotation.annotation.add(new TwoTuple("towards", cur_waypoint));
					annotation.annotation.add(new TwoTuple("forbid", cur_node_id));
					if (waypoint.contains(cur_node_id)) {
						cur_waypoint = cur_node_id;
					}
				}
				// logger.info("annotation:" + annotation.toString());
				ins.add(new Instruction(scope, constriant, annotation));
				index = index + 2;
			}
			semantic_rules.put(flow, ins);
			logger.info(flow.toString() + "---" + ins.toString());

		}
		logger.info("finishing to install semantic rules!");

	}

	public static void installFlowRules() {
		if (route == null || route.size() == 0) {
			logger.info("---there is something with routing----");
		}
		for (Map.Entry<Flow, Route> entry : route.entrySet()) {
			Network.install_rules(entry.getValue(), entry.getKey());
		}

	}

	public static void installRules() {
		installPathByIntent();
		installFlowRules();
		installSemanticRules();

	}

}
