package com.react.topo;

import com.react.compiler.Flow;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
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
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.routing.*;
import net.floodlightcontroller.staticflowentry.StaticFlowEntries;
import net.floodlightcontroller.staticflowentry.StaticFlowEntryPusher;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.topology.NodePortTuple;
import net.floodlightcontroller.util.OFMessageDamper;
import org.apache.commons.codec.binary.Hex;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Network is responsible for provide data structure about topology, e.g.,edge
 * ports, links , switch's ports ,etc.
 */
public class Network implements IFloodlightModule, IOFMessageListener {
	protected IFloodlightProviderService floodlightProviderService;
	// module dependency
	public static ITopologyService topologyService;
	public static IOFSwitchService switchService;
	public static IDeviceService deviceManagerService;
	public static IRoutingService routingEngineService;
	// define data structure
	public static Logger log = LoggerFactory.getLogger(Network.class);
	public static Map<String, String> ip_mac;
	public static Map<String, Switch> switches;
	public static Map<Port, Port> topology;
	// public static List<FourTuple> link;
	public static List<Port> edge_ports;
	public static Map<String, Port> host_edgeport;
	public static Set<DatapathId> switchSet;
	protected static OFMessageDamper messageDamper;
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
		messageDamper = new OFMessageDamper(10000,
				EnumSet.of(OFType.FLOW_MOD),
				250);
		topology = new HashMap<Port, Port>();
		switches = new HashMap<String, Switch>();
		edge_ports = new ArrayList<>();
		ip_mac = new HashMap<>();
		// link = new ArrayList<>();
		host_edgeport = new HashMap<>();
		switchSet = new HashSet<>();
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProviderService.addOFMessageListener(OFType.PACKET_IN, this);
	}

	public static DatapathId Port2Dpid(Port port) {
//        log.debug(port.sid);
		return DatapathId.of(Integer.toHexString(Integer.valueOf(port.sid)));
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
	 * @param srcIP
	 * @param dstIP
	 * @return
	 */

	public static Route calculateAllShortestPaths(String srcIP, String dstIP) {
		Port srcPort = host_edgeport.get(srcIP);
		Port dstPort = host_edgeport.get(dstIP);
		log.debug("$"+srcPort + "--" + dstPort);
		Route route=null;
		if (srcPort != null && dstPort != null
				&& routingEngineService.getRoutes(Port2Dpid(srcPort), Port2Dpid(dstPort), true).size() != 0) {

			 route= routingEngineService.getRoute(
					Port2Dpid(srcPort),
					Port2OFport(srcPort),
					Port2Dpid(dstPort),
					Port2OFport(dstPort),
					U64.of(0)); //cookie = 0, i.e., default route
			log.debug("$ "+route.toString());

		}
		return route;
	}

	/**
	 * get route by flow, then compute rule flows, write flowmod message
	 * As for ARP packets, only if get packet's mac, arp rules is got
	 *
	 * @param routing
	 * @param flow
	 */
	public static void install_rules(Route routing, Flow flow) {
		// System.out.println("installing the openflow rules\n");
		List<NodePortTuple> paths = routing.getPath();
		if (!(routing == null || routing.getPath().size() == 0)) {
			int index = 0;
			while (index < (paths.size() - 1)) {
				generateFlow(flow.getSource(), flow.getDestination(),
//                        ip_mac.get(flow.getSource().toString()), ip_mac.get(flow.getDestination().toString()),
						null, null,
						paths.get(index).getPortId().getPortNumber(),
						Integer.toString(paths.get(index + 1).getPortId().getPortNumber()),
						paths.get(index).getNodeId(),
						"FR_" + Integer.toString(++flow_counter),32,32,32767,true);

//                generateFlow(null, null,
//                        ip_mac.get(flow.getSource().toString()), ip_mac.get(flow.getDestination().toString()),
//                        paths.get(index).getPortId().getPortNumber(),
//                        Integer.toString(paths.get(index + 1).getPortId().getPortNumber()),
//                        paths.get(index).getNodeId(),
//                        "FR_" + Integer.toString(++flow_counter));
//                generateFlow(null,null,
//                        ip_mac.get(flow.getSource().toString()),"ff:ff:ff:ff:ff:ff",
//                        paths.get(index).getPortId().getPortNumber(),
//                        "ALL",
//                         paths.get(index).getNodeId(),
//                        "ARP_Broadcast" + Integer.toString(++flow_counter));
				index = index + 2;
			}
			// reverse
			index = 0;
			while (index < (paths.size() - 1)) {
				generateFlow(flow.getDestination(), flow.getSource(),
//                        ip_mac.get(flow.getDestination().toString()),
//                        ip_mac.get(flow.getSource().toString()),
						null, null,
						paths.get(index + 1).getPortId().getPortNumber(),
						Integer.toString(paths.get(index).getPortId().getPortNumber()),
						paths.get(index).getNodeId(),
						"FR_" + Integer.toString(++flow_counter),32,32,32767,true);

//                generateFlow(null, null,
//                        ip_mac.get(flow.getDestination().toString()),
//                        ip_mac.get(flow.getSource().toString()),
//                        paths.get(index + 1).getPortId().getPortNumber(),
//                        Integer.toString(paths.get(index).getPortId().getPortNumber()),
//                        paths.get(index).getNodeId(),
//                        "FR_" + Integer.toString(++flow_counter));
				index = index + 2;
			}
		}
	}

	public static void generateFlow(String srcIp, String dstIp,
									String srcMac, String dstMac,
									int srcport, String dstport, DatapathId dpid,
									String flowName,int srcipMask,int dstipMask,int priority,boolean is_in_port) {
 //       log.debug(srcIp + "-" + dstIp + "-" + srcMac + "-" + dstMac);
		String fmJson = new String();
//		int srcipMask = 32;
//		int dstipMask = 32;
//		int priority=32767;
//        if(srcIp.equals("10.0.0.1")&&dstIp.equals("10.0.0.2")){
//			srcipMask = 24;
//			dstipMask = 24;
//		}
		Map<String, Object> rowValues;
        if(is_in_port){
            fmJson = "{\"switch\":\"" + dpid.toString()
                    + "\", \"name\":\"" + flowName + "\", \"cookie\":\"0"
                    // + "\", \"priority\":\"32767"
                    + "\", \"priority\":\"" + priority
                    + "\", \"ipv4_src\":\"" + srcIp + "/" + srcipMask
                    + "\", \"ipv4_dst\":\"" + dstIp + "/" + dstipMask
//                    + "\", \"eth_src\":\"" + srcMac
//                    + "\", \"eth_dst\":\"" + dstMac
					+ "\", \"in_port\":\"" + srcport
                    + "\", \"active\":\"true"
                    + "\", \"idle_timeout\":\"0" + "\", \"table\":\"0"
                    + "\", \"eth_type\":\"2048"
                    + "\", \"actions\":\"" + "output=" + dstport + "\"}";
        }
        else{
            fmJson = "{\"switch\":\"" + dpid.toString()
                    + "\", \"name\":\"" + flowName + "\", \"cookie\":\"0"
                    // + "\", \"priority\":\"32767"
                    + "\", \"priority\":\"" + priority

                    + "\", \"ipv4_src\":\"" + srcIp + "/" + srcipMask
                    + "\", \"ipv4_dst\":\"" + dstIp + "/" + dstipMask
//                    + "\", \"eth_src\":\"" + srcMac
//                    + "\", \"eth_dst\":\"" + dstMac
//					+ "\", \"in_port\":\"" + srcport
                    + "\", \"active\":\"true"
                    + "\", \"idle_timeout\":\"0" + "\", \"table\":\"0"
                    + "\", \"eth_type\":\"2048"
                    + "\", \"actions\":\"" + "output=" + dstport + "\"}";
        }


		// + "\", \"ip_proto\":\"" + rule.nw_proto
//                + "\", \"instruction_goto_table\":\"1"
		try {
			//log.info("fmJson:"+fmJson.toString());
			rowValues = StaticFlowEntries.jsonToStorageEntry(fmJson);
			check(rowValues);
			log.warn("$ Install rule:" + fmJson.toString());
		} catch (IOException e) {
			log.error("$ Error parsing push flow mod request: " + fmJson, e);
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
//            log.warn("$ " + rowValues.toString());
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
		/* get switch port */
		for (IOFSwitch sw : switchService.getAllSwitchMap().values()) {
			Switch mySwitch = new Switch(Long.toString(sw.getId().getLong()), sw.getEnabledPortNumbers().size());
			switches.put(Long.toString(sw.getId().getLong()), mySwitch);
			// connectedSwitch = getConnectedSwitch(mySwitch);
		}
		// log.warn("1. all switch_ports in the topo");
		// printSwitch_ports();

		/* get edge port */
		Collection<? extends IDevice> allDevice = deviceManagerService.getAllDevices();
		List<Port> temp = new ArrayList<Port>();

		for (IDevice host : allDevice) {
			if (host.getAttachmentPoints().length == 0) {
				continue;
			}
//            log.debug(host.toString());
			SwitchPort switchPort = host.getAttachmentPoints()[0];
			if (topologyService.isEdge(switchPort.getSwitchDPID(), switchPort.getPort())) {
				temp.add(switchPort2port(switchPort));
                IPv4Address[] ips = host.getIPv4Addresses();
                if (ips.length != 0) {
                    //there is arp
                    host_edgeport.put(IPv4.fromIPv4Address(ips[0].getInt()),
                            switchPort2port(switchPort));
                }
			}
			// there's no arp
//			host_edgeport.put("10.0.0." +
//					host.getMACAddress().getLong(), switchPort2port(switchPort));
		}

		edge_ports = new ArrayList<>(temp);
		// log.info("2. switch port that host direct connected with");
		// printEdge_ports();

		/* get link */
		Map<DatapathId, Set<Link>> allLinks = topologyService.getAllLinks();
		Set<DatapathId> idset = allLinks.keySet();
		for (
				DatapathId id : idset) {
			switchSet.add(id);
			Set<Link> linkSet = allLinks.get(id);
			for (Link link : linkSet) {
				Port p1 = new Port(link.getSrcPort().getPortNumber(), Long.toString(link.getSrc().getLong()));
				Port p2 = new Port(link.getDstPort().getPortNumber(), Long.toString(link.getDst().getLong()));
				topology.put(p1, p2);
			}
		}
		// log.warn("3. all switch link in the digraphs");
		// printLinks();

		/* connected switch port */
		// log.warn("4. for each switch in 1~n, its port and connected switch");
		// printConnectSwitchPort();

//        Network.create_edge_port();
	}

//    public static void printConnectSwitchPort() {
//        for (int x = 1; x <= switches.size(); x++) {
//            System.out.println(getConnectedSwitch(switches.get(Integer.toString(x))));
//        }
//    }

	public static void printEdge_ports() {
		log.info("$ Edge_port: " + edge_ports.toString());
	}

	public static void printLinks() {
		log.info("$ two-ways links:" + topology.toString());
	}

	public static void printSwitch() {
		log.info("$ Switch: " + switchSet.toString());
	}

	public static void printEdgeHosts() {
		log.info("$ Host_edgePorts: " + host_edgeport.toString());
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {

		Ethernet eth = IFloodlightProviderService.bcStore.get
				(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
//        log.debug(eth.toString());
		if (eth != null && eth.getEtherType() != null) {
			if (eth.getEtherType().equals(EthType.LLDP) || eth.getEtherType().equals(EthType.IPv6)) {
//                35020 34525
				return Command.CONTINUE;//filt irrelevant packet
			}
			if (eth.getEtherType().equals(EthType.ARP)) {
//                log.debug("$ ARP" + eth.getDestinationMACAddress());
				if (eth.getDestinationMACAddress().toString().equals("ff:ff:ff:ff:ff:ff")) {
					ip_mac.put(((ARP) eth.getPayload()).getSenderProtocolAddress().toString(),
							eth.getSourceMACAddress().toString());
				}
				doFlood(sw, (OFPacketIn) msg, cntx);
			}

			if (eth.getEtherType().equals(EthType.IPv4)) {
				// 0x800 2048
				if (((IPv4) eth.getPayload()).getProtocol().getIpProtocolNumber() == 1) {
					log.info("$ ICMP");
				}

				if (((IPv4) eth.getPayload()).getProtocol().getIpProtocolNumber() == 4) {
					log.info("$ IPv4");
				}
				if (((IPv4) eth.getPayload()).getProtocol().getIpProtocolNumber() == 6) {
					log.info("$ TCP");
				}
				if (((IPv4) eth.getPayload()).getProtocol().getIpProtocolNumber() == 17) {
					if (((UDP) eth.getPayload().getPayload()).getDestinationPort().getPort() == 67) {
						return Command.CONTINUE;//payload is lldp, dl_dst broadcast, nw_src=0.0.0.0, nw_dst=255.255.255.255
					}
					log.info("$ UDP " + ((UDP) eth.getPayload().getPayload()).getDestinationPort().getPort());
				}
			}
		}
		return Command.CONTINUE;
	}

	protected static void doFlood(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx) {
		OFPort inPort = (pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT));
		// Set Action to flood
		OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();
		List<OFAction> actions = new ArrayList<OFAction>();
		Set<OFPort> broadcastPorts = topologyService.getSwitchBroadcastPorts(sw.getId());

		if (broadcastPorts == null) {
//            log.debug("BroadcastPorts returned null. Assuming single switch w/no links.");
			/* Must be a single-switch w/no links */
			broadcastPorts = Collections.singleton(OFPort.FLOOD);
		}

		for (OFPort p : broadcastPorts) {
			if (p.equals(inPort)) continue;
			actions.add(sw.getOFFactory().actions().output(p, Integer.MAX_VALUE));
		}
		pob.setActions(actions);
		// log.info("actions {}",actions);
		// set buffer-id, in-port and packet-data based on packet-in
		pob.setBufferId(OFBufferId.NO_BUFFER);
		pob.setInPort(inPort);
		pob.setData(pi.getData());

		try {
			if (log.isTraceEnabled()) {
				log.trace("Writing flood PacketOut switch={} packet-in={} packet-out={}",
						new Object[]{sw, pi, pob.build()});
			}
			messageDamper.write(sw, pob.build());
		} catch (IOException e) {
			log.error("Failure writing PacketOut switch={} packet-in={} packet-out={}",
					new Object[]{sw, pi, pob.build()}, e);
		}

		return;
	}

	@Override
	public String getName() {
		return "network";
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return false;
	}

}

