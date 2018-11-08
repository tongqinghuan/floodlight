package com.react.topo;

import com.react.compiler.Flow;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.staticflowentry.StaticFlowEntries;
import net.floodlightcontroller.staticflowentry.StaticFlowEntryPusher;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.topology.NodePortTuple;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Network is responsible for provide data structure about topology, e.g.,edge
 * ports, links , switch's ports ,etc.
 */
public class Network implements IFloodlightModule {
	// module dependency
	public IFloodlightProviderService floodlightProviderService;
	public static ITopologyService topologyService;
	public static IOFSwitchService switchService;
	public static IDeviceService deviceManagerService;
	public static IRoutingService routingEngineService;
	// define data structure
	public static Logger log = LoggerFactory.getLogger(Network.class);

	public static Map<String, Switch> switches;
	public static Map<Port, Port> topology;
	// public static List<FourTuple> link;
	public static List<Port> edge_ports;
	public static Map<String, Port> host_edgeport;
	public static Set<DatapathId> switchSet;

	protected static IStorageSourceService storageSource;
	protected static int flow_counter = 0;

	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// tell the module loader we depend on it
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(IDeviceService.class);
		l.add(IRoutingService.class);
		l.add(ITopologyService.class);
		l.add(IDebugCounterService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// it primarily is run to load dependencies and initialize datastructures.
		this.floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
		this.deviceManagerService = context.getServiceImpl(IDeviceService.class);
		this.topologyService = context.getServiceImpl(ITopologyService.class);
		this.switchService = context.getServiceImpl(IOFSwitchService.class);
		this.routingEngineService = context.getServiceImpl(IRoutingService.class);
		this.storageSource = context.getServiceImpl(IStorageSourceService.class);

		topology = new HashMap<Port, Port>();
		switches = new HashMap<String, Switch>();
		edge_ports = new ArrayList<>();
		// link = new ArrayList<>();
		host_edgeport = new HashMap<>();
		switchSet = new HashSet<>();

	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// We'll register for PACKET_IN messages in our startUp method.
	}

	public static DatapathId Port2Dpid(String dst) {
		return DatapathId.of(dst);

	}

	public Port OFPort2Port(DatapathId pid, OFPort ofPort) {
		return new Port(ofPort.getPortNumber(), Long.toString(pid.getLong()));
	}

	public static OFPort Port2OFport(Port port) {
		return OFPort.of(port.pid);
	}

	public static Port switchPort2port(SwitchPort switchPort) {
		return new Port(switchPort.getPort().getPortNumber(), Long.toString(switchPort.getSwitchDPID().getLong()));
	}

	// <editor-fold desc="redundant code">
	/**
	 * compute switches and topology by links
	 */
	// public static void create_node(List<FourTuple> links) {
	// for(FourTuple link:links) {
	// if(!switches.keySet().contains(link.src)) {
	// switches.put(link.src,new Switch(link.src,5));
	// }
	// if(!switches.keySet().contains(link.dst)) {
	// switches.put(link.dst,new Switch(link.dst,5));
	// }
	// add_double_link(switches.get(link.src).ports.get(link.entry-1),switches.get(link.dst).ports.get(link.exit-1));
	// }
	// }

	/**
	 * compute link by topology
	 * 
	 * @param current_switch
	 * @return
	 */
	// public static List<FourTuple> topo2fourtuple(Map<Port,Port> topology) {
	// List<FourTuple> links = new ArrayList<>();
	// for(Port scrPort:topology.keySet()) {
	// if(!switches.keySet().contains(scrPort.sid)) {
	// switches.put(scrPort.sid,new Switch(scrPort.sid,5));
	// }
	// if(!switches.keySet().contains(topology.get(scrPort))) {
	// switches.put(topology.get(scrPort).sid,new
	// Switch(topology.get(scrPort).sid,5));
	// }
	// add_double_link(scrPort,topology.get(scrPort));
	// }
	// return links;
	// }

	// public static void add_double_link(Port p1,Port p2) {
	// topology.put(p1, p2);
	// topology.put(p2, p1);
	// }
	// </editor-fold>

	/**
	 * compute edg_ports by topology
	 */
	public static void create_edge_port() {
		for (Switch sw : switches.values()) {
			for (Port port : sw.ports) {
				if (!(topology.keySet().contains(port) && topology.values().contains(port))) {
					edge_ports.add(port);
				}
			}
		}
	}

	public static Map<String, ConnectedSwitch> getConnectedSwitch(Switch current_switch) {
		Map<String, ConnectedSwitch> connected = new HashMap<String, ConnectedSwitch>();
		for (Port port : current_switch.ports) {
			if (!topology.keySet().contains(port)) {
				continue;
			} else {
				Port linked_port = topology.get(port);// get connetced switch port
				if (!edge_ports.contains(linked_port)) {
					connected.put(linked_port.sid, new ConnectedSwitch(port.pid, linked_port.sid));
				}
			}
		}
		return connected;
	}

	/**
	 * @param src
	 * @param dst
	 * @return
	 */

	public static Route calculateAllShortestPaths(String src, String dst) {
		// Port src = host_edgeport.get(srcIP);
		// Port dst = host_edgeport.get(dstIP);

		// System.out.println("host_edgeport:"+host_edgeport.toString());
		System.out.println("System is getting route by routingEngineService");
		Route route = routingEngineService.getRoute(Port2Dpid(src),

				Port2Dpid(dst),

				U64.of(0)); // cookie = 0, i.e., default route
		/*
		 * if(route!=null){ System.out.println(src+"--->"+dst+":\n");
		 * System.out.println(route.toString()); }
		 */

		return route;
	}

	public static void install_rules(Route routing, Flow flow) {
		// System.out.println("installing the openflow rules\n");
		List<NodePortTuple> paths = routing.getPath();
		if (!(routing == null || routing.getPath().size() == 0)) {
			int index = 0;
			while (index < (paths.size() - 1)) {
				generateFlow(flow.getSource(), flow.getDestination(), paths.get(index).getPortId().getPortNumber(),
						paths.get(index + 1).getPortId().getPortNumber(), paths.get(index).getNodeId(),
						"flow_rule_" + Integer.toString(++flow_counter));
				index = index + 2;
			}
			// reverse
			index = 0;
			while (index < (paths.size() - 1)) {
				generateFlow(flow.getDestination(), flow.getSource(), paths.get(index + 1).getPortId().getPortNumber(),
						paths.get(index).getPortId().getPortNumber(), paths.get(index).getNodeId(),
						"flow_rule_" + Integer.toString(++flow_counter));
				index = index + 2;
			}
		}
		System.out.println("System finished installing flow rules");
	}

	private static void generateFlow(String src, String dst, int srcport, int dstport, DatapathId dpid,
			String flowName) {
		String fmJson;
		int srcipMask=32;
		int dstipMask=32;
		Map<String, Object> rowValues;
		fmJson = "{\"switch\":\"" + dpid.toString() + "\", \"name\":\"" + flowName + "\", \"cookie\":\"0"
				+ "\", \"priority\":\"32767"
				+ "\", \"ipv4_src\":\"" + src +"/"+srcipMask
				+ "\", \"ipv4_dst\":\"" + dst+"/"+dstipMask
				+ "\", \"in_port\":\"" + srcport
				+ "\", \"active\":\"true"
				// + "\", \"instruction_goto_table\":\"1"
				+ "\", \"idle_timeout\":\"0" + "\", \"table\":\"0"
				//+ "\", \"eth_type\":\"2048"
				// + "\", \"ip_proto\":\"" + rule.nw_proto
				+ "\", \"actions\":\"" + "output=" + dstport + "\"}";
		try {
			rowValues = StaticFlowEntries.jsonToStorageEntry(fmJson);
			check(rowValues);
			System.out.println("installing rule:" + fmJson.toString() + "\n");
		} catch (IOException e) {
			log.error("Error parsing push flow mod request: " + fmJson, e);
		}

	}

	private static void check(Map<String, Object> rowValues) {
		String status;
		int state = checkFlow(rowValues);
		if (state == 1) {
			status = "Warning! Must specify eth_type of IPv4/IPv6 to "
					+ "match on IPv4/IPv6 fields! The flow has been discarded.";
			log.error(status);
		} else if (state == 2) {
			status = "Warning! eth_type not recognized! The flow has been discarded.";
			log.error(status);
		} else if (state == 3) {
			status = "Warning! Must specify ip_proto to match! The flow has been discarded.";
			log.error(status);
		} else if (state == 4) {
			status = "Warning! ip_proto invalid! The flow has been discarded.";
			log.error(status);
		} else if (state == 5) {
			status = "Warning! Must specify icmp6_type to match! The flow has been discarded.";
			log.error(status);
		} else if (state == 6) {
			status = "Warning! icmp6_type invalid! The flow has been discarded.";
			log.error(status);
		} else if (state == 7) {
			status = "Warning! IPv4 & IPv6 fields cannot be specified in the same flow! The flow has been discarded.";
			log.error(status);
		} else if (state == 8) {
			status = "Warning! Must specify switch DPID in flow. The flow has been discarded.";
			log.error(status);
		} else if (state == 9) {
			status = "Warning! Switch DPID invalid! The flow has been discarded.";
			log.error(status);
		} else if (state == 0) {
			status = "Entry pushed";
			storageSource.insertRowAsync(StaticFlowEntryPusher.TABLE_NAME, rowValues);
			// log.info("!!!!!!!!!!!!!!!!!!!"+status);
		}
	}

	private static int checkFlow(Map<String, Object> rows) {
		// Declaring & Initializing flags
		int state = 0;
		boolean dl_type = false;
		boolean nw_proto = false;
		boolean nw_layer = false;
		boolean icmp6_type = false;
		boolean icmp6_code = false;
		boolean nd_target = false;
		boolean nd_sll = false;
		boolean nd_tll = false;
		boolean ip6 = false;
		boolean ip4 = false;

		int eth_type = -1;
		int nw_protocol = -1;
		int icmp_type = -1;
		return state;
	}

	public static void TopologyInit() {
		// System.out.println("print topo!");
		/* get switch port */
		for (IOFSwitch sw : switchService.getAllSwitchMap().values()) {
			Switch mySwitch = new Switch(Long.toString(sw.getId().getLong()), sw.getEnabledPortNumbers().size());
			switches.put(Long.toString(sw.getId().getLong()), mySwitch);
			// connectedSwitch = getConnectedSwitch(mySwitch);
		}
		// log.info("-----------------1. all switch_ports in the
		// topo---------------------");
		// printSwitch_ports();

		/* get edge port */
		Collection<? extends IDevice> allDevice = deviceManagerService.getAllDevices();
		List<Port> temp = new ArrayList<Port>();

		for (IDevice host : allDevice) {
			for (SwitchPort switchPort : host.getAttachmentPoints()) {
				if (topologyService.isEdge(switchPort.getSwitchDPID(), switchPort.getPort())) {
					temp.add(switchPort2port(switchPort));
					IPv4Address[] ips = host.getIPv4Addresses();
					if (ips.length != 0) {
						// System.out.println("host:"+ IPv4.fromIPv4Address(ips[0].getInt()));
						host_edgeport.put(IPv4.fromIPv4Address(ips[0].getInt()), switchPort2port(switchPort));
					}
				}
			}
		}
		// System.out.println("host_edgeport:"+host_edgeport.toString());
		// create_edge_port();
		edge_ports = new ArrayList<>(temp);
		// log.info("-------------2. switch port that host direct connected
		// with----------------");
		// printEdge_ports();

		/* get link */
		Map<DatapathId, Set<Link>> allLinks = topologyService.getAllLinks();
		Set<DatapathId> idset = allLinks.keySet();
		for (DatapathId id : idset) {
			switchSet.add(id);
			Set<Link> linkSet = allLinks.get(id);
			for (Link link : linkSet) {
				Port p1 = new Port(link.getSrcPort().getPortNumber(), Long.toString(link.getSrc().getLong()));
				Port p2 = new Port(link.getDstPort().getPortNumber(), Long.toString(link.getDst().getLong()));
				topology.put(p1, p2);
			}
		}
		// log.info("---------3. all switch link in the digraphs--------");
		// printLinks();

		/* connected switch port */
		// log.info("-----4. for each switch in 1~n, its port and connected
		// switch--------");
		// printConnectSwitchPort();

		Network.create_edge_port();

	}

	public static void printConnectSwitchPort() {
		for (int x = 1; x <= switches.size(); x++) {
			System.out.println(getConnectedSwitch(switches.get(Integer.toString(x))));
		}
	}

	public static void printEdge_ports() {
		for (Port p : edge_ports) {
			System.out.println(p.toString());
		}
		// for (String host:host_edgeport.keySet()){
		// System.out.println(host_edgeport.get(host));
		// }
	}

	public static void printLinks() {
		/* print link */
		Set<Port> scrports = topology.keySet();
		StringBuilder links = new StringBuilder();
		for (Port scrport : scrports) {
			links.append("(" + scrport.toString() + "," + topology.get(scrport).toString() + ")" + "\t");
		}
		log.info(links.toString());
	}

	public static void printSwitch() {
		StringBuilder switchId = new StringBuilder();

		for (DatapathId id : switchSet) {
			switchId.append(String.valueOf(id.getLong()) + "---");
		}
		switchId.append("end");
		log.info(switchId.toString());
	}

	public static void printEdgeHosts() {
		Set<String> hosts = host_edgeport.keySet();
		StringBuilder hostStr = new StringBuilder();
		for (String host : hosts) {
			hostStr.append(host + "---");
		}
		hostStr.append("end");
		log.info(hostStr.toString());
	}

	public static void printSwitch_ports() {
		/* print switch port */
		Set<String> set = switches.keySet();
		for (String key : set) {
			System.out
					.println("Switch" + key + ": Switch" + switches.get(key).sid + switches.get(key).ports.toString());
		}
	}

	public static void getNetwork() {

	}
}
