package com.react.compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.react.topo.Network;

public class RandomFlows {

	public static HashSet<Flow> generateFlowsRandomly() {
		HashSet<Flow> random_flows = new HashSet<Flow>();
		Set<String> host_found_set = Network.host_edgeport.keySet();
		List<String> host_found_list = new ArrayList<String>(host_found_set);

		int edge_host_size = Network.host_edgeport.entrySet().size();

		int left_node;
		left_node=(int)Math.random()*(edge_host_size-1);
		String src=host_found_list.get(left_node);
		int port = Network.host_edgeport.get(src).pid;

		int index=left_node-1;
		while(index>-1){
			String dst = host_found_list.get(index);
			random_flows.add(new Flow(src, dst, port));
			index = index -1;

		}
		 index=left_node+1;
		while(index<edge_host_size){
			String dst = host_found_list.get(index);
			random_flows.add(new Flow(src, dst, port));
			index = index +1;

		}

		return random_flows;

	}

	public static HashSet<String> generateWaypointRandomly() {
		HashSet<String> set = new HashSet<String>();
		int max = Network.switchSet.size();
		int min = 1;
		int random_num = (int) ((max - min) * 0.3);
		randomSet(min, max, random_num, set);
		return set;

	}

	public static void randomSet(int min, int max, int n, HashSet<String> set) {
		if (n > (max - min + 1) || max < min) {
			return;
		}
		for (int i = 0; i < n; i++) {
			int num = (int) (Math.random() * (max - min)) + min;
			set.add(String.valueOf(num));
		}
		int setSize = set.size();
		if (setSize < n) {
			randomSet(max, min, n, set);
		}
	}

}
