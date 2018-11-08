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
		int index = 0;
		while (index < (edge_host_size - 1)) {
			String src = host_found_list.get(index);
			String dst = host_found_list.get(index + 1);
			int port = Network.host_edgeport.get(src).pid;
			random_flows.add(new Flow(src, dst, port));
			index = index + 2;
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
