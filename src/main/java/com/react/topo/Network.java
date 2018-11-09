package com.react.topo;

import java.io.IOException;
import java.util.*;

import com.react.verify.Verify;
import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.debugcounter.IDebugCounterService;
import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.IDeviceService;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.routing.BroadcastTree;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.staticflowentry.StaticFlowEntries;
import net.floodlightcontroller.staticflowentry.StaticFlowEntryPusher;
import net.floodlightcontroller.staticflowentry.web.StaticFlowEntryPusherResource;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.topology.ITopologyService;
import net.floodlightcontroller.topology.NodePortTuple;
import net.floodlightcontroller.topology.TopologyInstance;
import net.floodlightcontroller.util.MatchUtils;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.types.*;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.react.schronized_rpc.Flow;

/**
 * Network is responsible for provide data structure about topology,
 * e.g.,edge ports, links , switch's ports ,etc.
 */
public class Network
        implements IFloodlightModule {
    //  module dependency
    public IFloodlightProviderService floodlightProviderService;
    public static ITopologyService topologyService;
    public static IOFSwitchService switchService;
    public static IDeviceService deviceManagerService;
    public static IRoutingService routingEngineService;
    //  define data structure
    public static Logger log = LoggerFactory.getLogger(Network.class);
    public static Map<String, Switch> switches;
    public static Map<Port, Port> topology;
    //    public static List<FourTuple> link;
    public static List<Port> edge_ports;
    public static Map<String, Port> host_edgeport;
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
//  tell the module loader we depend on it
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IDeviceService.class);
        l.add(IRoutingService.class);
        l.add(ITopologyService.class);
        l.add(IDebugCounterService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
//        it primarily is run to load dependencies and initialize datastructures.
        this.floodlightProviderService = context.getServiceImpl(IFloodlightProviderService.class);
        this.deviceManagerService = context.getServiceImpl(IDeviceService.class);
        this.topologyService = context.getServiceImpl(ITopologyService.class);
        this.switchService = context.getServiceImpl(IOFSwitchService.class);
        this.routingEngineService = context.getServiceImpl(IRoutingService.class);
        this.storageSource = context.getServiceImpl(IStorageSourceService.class);

        topology = new HashMap<Port, Port>();
        switches = new HashMap<String, Switch>();
        edge_ports = new ArrayList<>();
        host_edgeport = new HashMap<>();
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
//        We'll register for PACKET_IN messages in our startUp method.
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
        return new Port(switchPort.getPort().getPortNumber(),
                Long.toString(switchPort.getSwitchDPID().getLong()));
    }

    //  <editor-fold desc="redundant code">
    /**
     * 	compute switches and topology by links
     */
//    public static void create_node(List<FourTuple> links) {
//        for(FourTuple link:links) {
//            if(!switches.keySet().contains(link.src)) {
//                switches.put(link.src,new Switch(link.src,5));
//            }
//            if(!switches.keySet().contains(link.dst)) {
//                switches.put(link.dst,new Switch(link.dst,5));
//            }
//            add_double_link(switches.get(link.src).ports.get(link.entry-1),switches.get(link.dst).ports.get(link.exit-1));
//        }
//    }

    /**
     * compute link by topology
     * @param current_switch
     * @return
     */
//    public static List<FourTuple> topo2fourtuple(Map<Port,Port> topology) {
//        List<FourTuple> links = new ArrayList<>();
//        for(Port scrPort:topology.keySet()) {
//            if(!switches.keySet().contains(scrPort.sid)) {
//                switches.put(scrPort.sid,new Switch(scrPort.sid,5));
//            }
//            if(!switches.keySet().contains(topology.get(scrPort))) {
//                switches.put(topology.get(scrPort).sid,new Switch(topology.get(scrPort).sid,5));
//            }
//            add_double_link(scrPort,topology.get(scrPort));
//        }
//        return links;
//    }

//    public static void add_double_link(Port p1,Port p2) {
//        topology.put(p1, p2);
//        topology.put(p2, p1);
//    }
    //</editor-fold>

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
                Port linked_port = topology.get(port);//get connetced switch port
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
        Route route = routingEngineService.getRoute(Port2Dpid(src), Port2Dpid(dst),
                U64.of(0)); //cookie = 0, i.e., default route

        /*if(route!=null){
        	log.info(src+"--->"+dst+":\n");
            log.info(route.toString());
        }*/
        return route;
    }

    public static void install_rules(Route routing, Flow flow) {
        List<NodePortTuple> paths = routing.getPath();
        if (!(routing == null || routing.getPath().size() == 0)) {
            int index = 0;
            String tmpMask = "32";
            while (index < (paths.size() - 1)) {
                generateFlow(flow.getSource(), tmpMask,
                        flow.getDestiantion(), tmpMask,
                        paths.get(index).getPortId().getPortNumber(),
                        paths.get(index + 1).getPortId().getPortNumber(),
                        paths.get(index).getNodeId(),   //dpid
                        "FR_" + Integer.toString(++flow_counter)
                );
                index = index + 2;
            }
            //install reverse rules
            index = 0;
            while (index < (paths.size() - 1)) {
                generateFlow(flow.getSource(), tmpMask,
                        flow.getDestiantion(), tmpMask,
                        paths.get(index + 1).getPortId().getPortNumber(),
                        paths.get(index).getPortId().getPortNumber(),
                        paths.get(index).getNodeId(),
                        "FR_" + Integer.toString(++flow_counter)
                );
                index = index + 2;
            }
        }
        log.info("installing flow rules...");
    }

    private static void generateFlow(String src, String srcIpMask, String dst, String dstIpMask, int srcport, int dstport, DatapathId dpid, String flowName) {
        String fmJson;
        Map<String, Object> rowValues;
//        if (srcIpMask.equals("32")) {
//            srcIpMask = "255.255.255.255";
//        }
//        if (dstIpMask.equals("32")) {
//            dstIpMask = "255.255.255.255";
//        }
        fmJson = "{\"switch\":\"" + dpid.toString()
                + "\", \"name\":\"" + flowName
                + "\", \"cookie\":\"0"
                + "\", \"priority\":\"1111"
                + "\", \"eth_type\":\"2048"
                + "\", \"ipv4_src\":\"" + src + "/" + srcIpMask
                + "\", \"ipv4_dst\":\"" + dst + "/" + dstIpMask
                + "\", \"in_port\":\"" + srcport
                + "\", \"active\":\"true"
                + "\", \"idle_timeout\":\"0"
                + "\", \"table\":\"0"
                + "\", \"actions\":\"" + "output=" + dstport
                + "\"}";
//                + "\", \"instruction_goto_table\":\"1"
//                    + "\", \"ip_proto\":\"" +


        try {
            rowValues = StaticFlowEntries.jsonToStorageEntry(fmJson);
            checkThenInsert(rowValues);
            log.info("installing rule:" + fmJson.toString());
        } catch (IOException e) {
            log.error("Error parsing push flow mod request: " + fmJson, e);
        }
    }

    private static void checkThenInsert(Map<String, Object> rowValues) {
        String status = null;
        int state = checkFlow(rowValues);
        if (state == 1) {
            status = "Warning! Must specify eth_type of IPv4/IPv6 to " +
                    "match on IPv4/IPv6 fields! The flow has been discarded.";
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
        }
    }

    private static int checkFlow(Map<String, Object> rows) {
        //Declaring & Initializing flags
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

        //Determine the dl_type if set
        if (rows.containsKey(StaticFlowEntryPusher.COLUMN_DL_TYPE)) {
            if (((String) rows.get(StaticFlowEntryPusher.COLUMN_DL_TYPE)).startsWith("0x")) {
                eth_type = Integer.parseInt(((String) rows.get(StaticFlowEntryPusher.COLUMN_DL_TYPE)).replaceFirst("0x", ""), 16);
                dl_type = true;
            } else {
                eth_type = Integer.parseInt((String) rows.get(StaticFlowEntryPusher.COLUMN_DL_TYPE));
                dl_type = true;
            }
            if (eth_type == 0x86dd) { /* or 34525 */
                ip6 = true;
                dl_type = true;
            } else if (eth_type == 0x800 || /* or 2048 */
                    eth_type == 0x806 || /* or 2054 */
                    eth_type == 0x8035) { /* or 32821*/
                ip4 = true;
                dl_type = true;
            }
            //else {
            //	state = 2;
            //	return state;
            //}
        }
        if (rows.containsKey(StaticFlowEntryPusher.COLUMN_NW_DST) ||
                rows.containsKey(StaticFlowEntryPusher.COLUMN_NW_SRC)) {
            nw_layer = true;
            ip4 = true;
        }
        if (rows.containsKey(StaticFlowEntryPusher.COLUMN_ICMP_CODE) ||
                rows.containsKey(StaticFlowEntryPusher.COLUMN_ICMP_TYPE) ||
                rows.containsKey(StaticFlowEntryPusher.COLUMN_ARP_DHA) ||
                rows.containsKey(StaticFlowEntryPusher.COLUMN_ARP_SHA) ||
                rows.containsKey(StaticFlowEntryPusher.COLUMN_ARP_SPA) ||
                rows.containsKey(StaticFlowEntryPusher.COLUMN_ARP_DPA) ||
                rows.containsKey(StaticFlowEntryPusher.COLUMN_ARP_OPCODE)) {
            ip4 = true;
        }
        if (rows.containsKey(StaticFlowEntryPusher.COLUMN_IPV6_FLOW_LABEL) ||
                rows.containsKey(StaticFlowEntryPusher.COLUMN_NW6_SRC) ||
                rows.containsKey(StaticFlowEntryPusher.COLUMN_NW6_DST)) {
            nw_layer = true;
            ip6 = true;
        }
        if (rows.containsKey(StaticFlowEntryPusher.COLUMN_NW_PROTO)) {
            nw_proto = true;
            if (((String) rows.get(StaticFlowEntryPusher.COLUMN_NW_PROTO)).startsWith("0x")) {
                nw_protocol = Integer.parseInt(((String) rows.get(StaticFlowEntryPusher.COLUMN_NW_PROTO)).replaceFirst("0x", ""), 16);
            } else {
                nw_protocol = Integer.parseInt((String) rows.get(StaticFlowEntryPusher.COLUMN_NW_PROTO));
            }
        }
        if (rows.containsKey(StaticFlowEntryPusher.COLUMN_ICMP6_CODE)) {
            icmp6_code = true;
            ip6 = true;
        }
        if (rows.containsKey(StaticFlowEntryPusher.COLUMN_ICMP6_TYPE)) {
            icmp6_type = true;
            ip6 = true;
            if (((String) rows.get(StaticFlowEntryPusher.COLUMN_ICMP6_TYPE)).startsWith("0x")) {
                icmp_type = Integer.parseInt(((String) rows.get(StaticFlowEntryPusher.COLUMN_ICMP6_TYPE)).replaceFirst("0x", ""), 16);
            } else {
                icmp_type = Integer.parseInt((String) rows.get(StaticFlowEntryPusher.COLUMN_ICMP6_TYPE));
            }
        }
        if (rows.containsKey(StaticFlowEntryPusher.COLUMN_ND_SLL)) {
            nd_sll = true;
            ip6 = true;
        }
        if (rows.containsKey(StaticFlowEntryPusher.COLUMN_ND_TLL)) {
            nd_tll = true;
            ip6 = true;
        }
        if (rows.containsKey(StaticFlowEntryPusher.COLUMN_ND_TARGET)) {
            nd_target = true;
            ip6 = true;
        }

        if (nw_layer == true || nw_proto == true) {
            if (dl_type == true) {
                if (!(ip4 == true || ip6 == true)) {
                    //invalid dl_type
                    state = 2;
                    return state;
                }
            } else {
                //dl_type not set
                state = 1;
                return state;
            }
        }
        if (icmp6_type == true || icmp6_code == true) {
            if (nw_proto == true) {
                if (nw_protocol != 0x3A) { /* or 58 */
                    //invalid nw_proto
                    state = 4;
                    return state;
                }
            } else {
                //nw_proto not set
                state = 3;
                return state;
            }
        }

        if (nd_sll == true || nd_tll == true || nd_target == true) {
            if (icmp6_type == true) {
                //icmp_type must be set to 135/136 to set ipv6_nd_target
                if (nd_target == true) {
                    if (!(icmp_type == 135 || icmp_type == 136)) { /* or 0x87 / 0x88 */
                        //invalid icmp6_type
                        state = 6;
                        return state;
                    }
                }
                //icmp_type must be set to 136 to set ipv6_nd_tll
                else if (nd_tll == true) {
                    if (!(icmp_type == 136)) {
                        //invalid icmp6_type
                        state = 6;
                        return state;
                    }
                }
                //icmp_type must be set to 135 to set ipv6_nd_sll
                else if (nd_sll == true) {
                    if (!(icmp_type == 135)) {
                        //invalid icmp6_type
                        state = 6;
                        return state;
                    }
                }
            } else {
                //icmp6_type not set
                state = 5;
                return state;
            }
        }

        int result = StaticFlowEntryPusherResource.checkActions(rows);

        if ((ip4 == true && ip6 == true) || (result == -1) ||
                (result == 1 && ip6 == true) || (result == 2 && ip4 == true)) {
            //ipv4 & ipv6 conflict
            state = 7;
            return state;
        }

        if (rows.containsKey(StaticFlowEntryPusher.COLUMN_SWITCH)) {
            try {
                DatapathId.of((String) rows.get(StaticFlowEntryPusher.COLUMN_SWITCH));
            } catch (Exception e) {
                state = 9;
            }
        } else {
            state = 8;
        }
        return state;
    }

    public static void TopologyInit() {
        /* get switch port  */
        for (IOFSwitch sw : switchService.getAllSwitchMap().values()) {
            Switch mySwitch = new Switch(Long.toString(sw.getId().getLong()),
                    sw.getEnabledPortNumbers().size());
            switches.put(Long.toString(sw.getId().getLong()), mySwitch);
//            connectedSwitch = getConnectedSwitch(mySwitch);
        }
        log.info("1. all switch and their nodePorts: ");
        printSwitch_ports();

        /* get edge port*/
        Collection<? extends IDevice> allDevice = deviceManagerService.getAllDevices();
        List<Port> temp = new ArrayList<Port>();

        for (IDevice host : allDevice) {
            for (SwitchPort switchPort : host.getAttachmentPoints()) {
                if (topologyService.isEdge(switchPort.getSwitchDPID(), switchPort.getPort())) {
                    temp.add(switchPort2port(switchPort));
                    IPv4Address[] ips = host.getIPv4Addresses();
                    if (ips.length != 0) {
//                        log.info("host:"+  IPv4.fromIPv4Address(ips[0].getInt()));
                        host_edgeport.put(IPv4.fromIPv4Address(ips[0].getInt()), switchPort2port(switchPort));
                    }
                }
            }
        }
        edge_ports = new ArrayList<>(temp);

        log.info("2. edge nodePort: ");
        printEdge_ports();

        /* get link */
        Map<DatapathId, Set<Link>> allLinks = topologyService.getAllLinks();
        Set<DatapathId> idset = allLinks.keySet();
        for (DatapathId id : idset) {
            Set<Link> linkSet = allLinks.get(id);
            for (Link link : linkSet) {
                Port p1 = new Port(link.getSrcPort().getPortNumber(),
                        Long.toString(link.getSrc().getLong()));
                Port p2 = new Port(link.getDstPort().getPortNumber(),
                        Long.toString(link.getDst().getLong()));
                topology.put(p1, p2);
            }
        }
        log.info("3. all links: ");
        printLinks();

        /*connected switch port*/
        log.info("4. nodePort connected switch: ");
        printConnectSwitchPort();
//        Network.create_edge_port();
    }

    public static void printConnectSwitchPort() {
        for (int x = 1; x <= switches.size(); x++) {
            log.info("switch " + x + getConnectedSwitch(switches.get(Integer.toString(x))).values());
        }
    }

    public static void printEdge_ports() {
        for (Port p : edge_ports) {
            log.info(p.toString());
        }
//        for (String host : host_edgeport.keySet()) {
//            log.info(host_edgeport.get(host).toString());
//        }
    }

    public static void printLinks() {
        /* print link*/
        Set<Port> scrports = topology.keySet();
        for (Port scrport : scrports) {
            log.info(scrport.toString() + "--" + topology.get(scrport).toString());
        }
    }

    public static void printSwitch_ports() {
        /* print switch port*/
        Set<String> set = switches.keySet();
        for (String key : set) {
            log.info("Switch" + key + ": Switch" + switches.get(key).sid + switches.get(key).ports.toString());
        }
    }
}