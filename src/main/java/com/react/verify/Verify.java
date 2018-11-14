package com.react.verify;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.storage.IStorageSourceService;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFFlowModCommand;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Verify implements IFloodlightModule,
        IVerifyService, IOFMessageListener {
    protected static Logger log = LoggerFactory.getLogger(Verify.class);
    protected IRestApiService restApi;
    protected IFloodlightProviderService floodlightProvider;
    protected IStorageSourceService storageSource;
    protected DatapathId dpid;
    protected boolean enabled;
    public static Map<String, String> flowEntry = new HashMap<>();
    protected static HashMap<Flow, ArrayList<FlowRule>> ruleset = new HashMap<Flow, ArrayList<FlowRule>>();
    protected static HashSet<EC> ecs = new HashSet<>();
    protected static List<FlowRule> rule_per_flow = new ArrayList<>();
    protected static Set<ECMatchedRules> ec_matched_rules = new HashSet<>();
    public static boolean IsNeedRemoveOldEc = false;
    public static int i;
    public static boolean is_checkIntent = false;
    public static boolean is_check = false;
    protected static Trie trie = new Trie();


    public static int NumberOf1(int n) {
        if (n == 0) {
            return 0;
        }
        int count = 0;
        while (n != 0) {
            n = n & (n-1);
            count++;
        }
        return count;
    }

    public static int getMaskLength(String mask){
        if(mask==null){
            return 32;
        }
        String[] ipStrs=mask.split("\\.");
        int mask_length=0;
        for(int i=0;i<4;i++){
            if(Integer.parseInt(ipStrs[i])==0){
                continue;
            }
            mask_length+=NumberOf1(Integer.parseInt(ipStrs[i]));

        }
        return mask_length;


    }

    /**
     * generate IPwithNetmask
     *
     * @param ipAddress
     * @param masklength
     * @return String IPwithNetmask
     */
    public static String ipInt2String(int ipAddress, int masklength) {
        StringBuffer stringBuffer = new StringBuffer();
        int i = 0;
        for (; i < 31 - Integer.toBinaryString(ipAddress).length(); i++) {
            stringBuffer.append("0");
        }
        stringBuffer.append(Integer.toBinaryString(ipAddress));
        stringBuffer.delete(masklength - 1, 32);
        while (stringBuffer.length() != 31) {
            stringBuffer.append('x');
        }
        return stringBuffer.toString();
    }

    public static boolean checkNetworkInvariant(OFFlowMod flowMod, DatapathId dpid) {
        //	log.info("dpid:"+Long.toString(dpid.getLong()));
        HashMap<String, Integer> action0 = new HashMap<String, Integer>();
        // conflict flow
        Set<Flow> conflictFlow = new HashSet<Flow>();
        FlowRule flow_rule = null;
        Flow flow = null;
//        Flow currflow = null;
//        log.info(flowMod.toString());

        if (flowMod != null && flowMod.getMatch() != null) {
            if (flowMod.getMatch().get(MatchField.IPV4_DST) != null) {
                String dst_ip;
                if (!flowMod.getMatch().get(MatchField.IPV4_DST).isCidrMask()) {
                    dst_ip = ipInt2String(
                            flowMod.getMatch().get(MatchField.IPV4_DST).getInt(),getMaskLength(flowMod.getMatch().getMasked(MatchField.IPV4_DST).getMask().toString()));
                    log.info("dst_ip:"+dst_ip);
                } else {
                    dst_ip = ipInt2String(flowMod.getMatch().get(MatchField.IPV4_DST).getInt(),
                            flowMod.getMatch().getMasked(MatchField.IPV4_DST).getMask().asCidrMaskLength());
                }
                flow = new Flow(dst_ip);

                //extract actions from flow_mod
                List<OFAction> actions = flowMod.getActions();
                for (OFAction a : actions) {
                    switch (a.getType()) {
                        case OUTPUT:
                            action0.put("forward", (((OFActionOutput) a).getPort().getPortNumber()));
                            break;
                        case SET_FIELD:
                            break;
                    }
                }

                //extract flow_rule from flow_mod message
                //FlowRule dpid,ip,priority,action
                flow_rule = new FlowRule(
                        Long.toString(dpid.getLong()),
                        dst_ip,
                        flowMod.getPriority(),
                        action0);
                log.info("decode flow_mod message:" + flow_rule.toString());

//                    currflow = flow;
                if (flowMod.getCommand().equals(OFFlowModCommand.ADD) ||
                        flowMod.getCommand().equals(OFFlowModCommand.MODIFY) ||
                        flowMod.getCommand().equals(OFFlowModCommand.MODIFY_STRICT)) {
                    log.info("constructing the Trie...");
                }
            }
        }
        return true;
    }
//
//                        for (String currflow : flowEntry.keySet()) {
//                            if (trie.containFlow(currflow)) {
//                                continue;
//                            }
//                            Set<String> overlayflow = new HashSet<>();
//                            Set<String> tmp = trie.FlowSet2StringSet(
//                                    trie.searchConflictFlow(new Flow(currflow)));
//                            overlayflow.addAll(tmp);
//                            System.out.println("conflict flow in the trie:" + overlayflow);
//                            if (!trie.containFlow(currflow)) {
//                                trie.addFlow(new Flow(currflow));
//                            }
//                            overlayflow.add(currflow);
//                            EC tmpec = ECOperations.updateEC(
//                                    ecs, trie.StringSet2FlowSet(overlayflow),
//                                    new Flow(currflow), IsNeedRemoveOldEc);
////			ec_matched_rules = ECOperations.update_ecMatched_rules( ecs, trie.StringSet2FlowSet(overlayflow));
//                            ECOperations.update_ecMatched_rules(ecs,
//                                    tmpec, ec_matched_rules, trie.StringSet2FlowSet(overlayflow));
//                            System.out.println(ecs.size() + "ECs:" + ecs);
//                            System.out.println(ec_matched_rules);
//                        }
//
//                        if (!ruleset.containsKey(flow)) {
//                            ruleset.put(flow, new ArrayList<FlowRule>());
//                        }
//
//                        ruleset.get(flow).add(flow_rule);
//                        //	log.info("flowEntry--->"+flowEntry.toString()+"\n");
//                        log.info("RuleSet: " + ruleset);
//                    } else if (flowMod.getCommand().equals(OFFlowModCommand.DELETE) || flowMod.getCommand().equals(OFFlowModCommand.DELETE_STRICT)) {
//                        if (trie.containFlow(dst_ip)) {
//                            trie.deleteFlow(dst_ip);
//                            //<editor-fold desc="?">
//                            for (Iterator<Map.Entry<String, String>> it = flowEntry.entrySet().iterator(); it.hasNext(); ) {
//                                Entry<String, String> item = it.next();
//                                if (item.getKey().equals(dst_ip)) {
//                                    it.remove();
//                                    break;
//                                }
//                            }
//                            //</editor-fold>
//                            //delete ruleset
//                            if (ruleset.keySet().contains(flow)) {
//                                ruleset.get(flow).remove(flow_rule);
//                            }
//                        }
//                    }
//
//                    for (String currentflow : flowEntry.keySet()) {
//                        if (trie.containFlow(currentflow)) {
//                            continue;
//                        }
//                        Set<String> overlayflow = new HashSet<>();
//                        Set<String> tmp = Trie.FlowSet2StringSet(
//                                trie.searchConflictFlow(new Flow(currentflow)));
//                        overlayflow.addAll(tmp);
//                        System.out.println("conflict flow in the trie:" + overlayflow);
//                        if (!trie.containFlow(currentflow)) {
//                            trie.addFlow(new Flow(currentflow));
//                        }
//                        overlayflow.add(currentflow);
//                        EC tmpec = ECOperations.updateEC(
//                                ecs, Trie.StringSet2FlowSet(overlayflow),
//                                new Flow(currentflow), IsNeedRemoveOldEc);
////			ec_matched_rules = ECOperations.update_ecMatched_rules( ecs, trie.StringSet2FlowSet(overlayflow));
//                        ECOperations.update_ecMatched_rules(ecs,
//                                tmpec, ec_matched_rules, trie.StringSet2FlowSet(overlayflow));
//                        System.out.println(ecs.size() + "ECs:" + ecs);
//                        System.out.println(ec_matched_rules);
//                    }
//
//                    Map<String, String> flowEntry1 = new HashMap<String, String>();
////		flowEntry1.put("0000101100000001xxxxxxxxxxxxxxxx", "sw1");
////		flowEntry1.put("00001011xxxxxxxxxxxxxxxxxxxxxxxx", "sw1");
////		flowEntry1.put("1100000000000001xxxxxxxxxxxxxxxx", "sw2");
//
//                    for (String currentflow : flowEntry1.keySet()) {
////			if the flow is in the tree,there is no chang
//                        if (trie.containFlow(currentflow)) {
//                            continue;
//                        }
////			if the flow is in the tree, add flow to tree and update ec
////			if there is not conflict,add ec;
////			if conflict,if new flow rang is bigger,add , if smaller,remove  and add
//                        Set<String> overlayflow = new HashSet<>();
//                        Set<String> tmp = trie.FlowSet2StringSet(trie.searchConflictFlow(new Flow(currentflow)));
//                        overlayflow.addAll(tmp);
//                        if (trie.getflag() == false) {
//                            IsNeedRemoveOldEc = true;
//                        }
//                        System.out.println("conflict flow in the trie:" + overlayflow);
//                        if (!trie.containFlow(currentflow)) {
//                            trie.addFlow(new Flow(currentflow));
//                        }
//                        overlayflow.add(currentflow);
//                        EC tmpec = ECOperations.updateEC(ecs, trie.StringSet2FlowSet
//                                (overlayflow), new Flow(currentflow), IsNeedRemoveOldEc);
//                        ECOperations.update_ecMatched_rules(ecs, tmpec,
//                                ec_matched_rules, trie.StringSet2FlowSet(overlayflow));
//                        System.out.println(ecs.size() + " EC:" + ecs);
//                        System.out.println(ec_matched_rules.size() + " ec_matched_rules:" + ec_matched_rules);
//                    }
//
//
//                    System.out.println("flow--flowRules" + ruleset.toString());
//                    Set<ECGraph> ecGraph = ECOperations.getNodeEdge(ec_matched_rules, ruleset);
//                    System.out.println("ecGraph:" + ecGraph);
////		if(ecGraph.size()==Network.switches.size()){
//                    List<FowardingGraph> forwardingGraph = new ArrayList<FowardingGraph>();
//                    for (ECGraph graph : ecGraph) {
//                        FowardingGraph graph_per_ec = new FowardingGraph();
//                        graph_per_ec.ec = graph.ec;
//                        graph_per_ec.setDeviceset(graph.deviceset);
//                        graph_per_ec.setEdges(graph.edges);
//                        graph_per_ec.setSrcDstPair(graph.srcDstPair);
//
//                        System.out.println("deviceset:" + graph_per_ec.deviceset);
//                        System.out.println("edges:" + graph_per_ec.edges);
//
//                        graph_per_ec.createGraph();
//
////                        System.out.println("forwadingGraph:" + graph_per_ec.forwardingGraph);
//
//                        forwardingGraph.add(graph_per_ec);
//                    }
////			flowmod is listened one by one, compute EC，cannot predict whether forwarding graph complete
////			if(forwardingGraph.size()==ops){
//                    for (FowardingGraph graph : forwardingGraph) {
//                        graph.traverse();
//                        System.out.println("graph is loop\t" + graph.is_loop);
//                        System.out.println("graph is reachable\t" + graph.is_reachable);
//                        System.out.println("graph is backe_hole\t" + graph.is_back_hole);
//                    }
//            } else {
//                log.warn("match's dstip is null)");
//                return false;
//            }
//        }
//        is_checkIntent = true;
//        is_check = true;
//    } catch(
//    Exception e)
//
//    {
//        e.printStackTrace();
//    }
//        return true;


    //<editor-fold desc="verytest">
	/*public static int veriytest(OFFlowMod flowMod, DatapathId dpid) {
//		Trie Trie = new Trie();

		HashMap<String,Integer> action0 = new HashMap<String,Integer>();

		try{
			if(flowMod.getMatch().get(MatchField.IPV4_DST)!=null
					&&(flowMod.getCommand().equals(OFFlowModCommand.ADD)||
					flowMod.getCommand().equals(OFFlowModCommand.MODIFY))){
				flowEntry.put(ipInt2String(flowMod.getMatch().get(MatchField.IPV4_DST).getInt(),
						flowMod.getMatch().getMasked(MatchField.IPV4_DST).getMask().asCidrMaskLength())
						,Long.toString(dpid.getLong()));
//				ActionUtils.actionsToString( flowMod.getActions(),log);
				List<OFAction> actions = flowMod.getActions();
				for (OFAction a : actions) {
					switch(a.getType()) {
						case OUTPUT:
							action0.put("forward",(((OFActionOutput)a).getPort().getPortNumber()));
//							whole FlowRule 1flow -n flowRule
							rule_per_flow.add(
								new FlowRule(Long.toString(dpid.getLong()),
										ipInt2String(flowMod.getMatch().get(MatchField.IPV4_DST).getInt(),
												flowMod.getMatch().getMasked(MatchField.IPV4_SRC).getMask().asCidrMaskLength()),
										flowMod.getPriority(),
										action0));
//							Flow(DstIPWithNetmask) + FlowRule
							ruleset.put(
									new Flow(ipInt2String(flowMod.getMatch().get(MatchField.IPV4_DST).getInt(),
											flowMod.getMatch().getMasked(MatchField.IPV4_DST).getMask().asCidrMaskLength()))
									,rule_per_flow);
							break;
						case SET_FIELD:
							break;
					}
				}
				log.info((++i)+"flowmod msgs--"+ flowEntry.size()+"flow:"+flowEntry.toString());
			}
		} catch (NullPointerException e){
			e.printStackTrace();
		}

		for(String currflow :flowEntry.keySet()){
			if(Trie.containFlow(currflow)){
				continue;
			}
			Set<String> overlayflow = new HashSet<>();
			Set<String> tmp = Trie.FlowSet2StringSet(
					Trie.searchConflictFlow(new Flow(currflow)));
			overlayflow.addAll(tmp);
			log.info("conflict flow in the Trie:" + overlayflow);
			if(!Trie.containFlow(currflow)){
				Trie.addFlow(new Flow(currflow));
			}
			overlayflow.add(currflow);
			EC tmpec = ECOperations.updateEC(
					ecs,  Trie.StringSet2FlowSet(overlayflow),
					new Flow(currflow),  IsNeedRemoveOldEc);
//			ec_matched_rules = ECOperations.update_ecMatched_rules( ecs, Trie.StringSet2FlowSet(overlayflow));
			ECOperations.update_ecMatched_rules( ecs,
					tmpec, ec_matched_rules, Trie.StringSet2FlowSet(overlayflow));
			log.info(ecs.size() +"ECs锛�" + ecs);
			log.info(ec_matched_rules);
		}

		Map<String, String> flowEntry1 = new HashMap<String, String>();
//		flowEntry1.put("0000101100000001xxxxxxxxxxxxxxxx", "sw1");
//		flowEntry1.put("00001011xxxxxxxxxxxxxxxxxxxxxxxx", "sw1");
//		flowEntry1.put("1100000000000001xxxxxxxxxxxxxxxx", "sw2");

		for(String currflow: flowEntry1.keySet()) {
//			if the flow is in the tree,there is no chang
			if(Trie.containFlow(currflow)){
				continue;
			}
//			if the flow is in the tree, add flow to tree and update ec
//			if there is not conflict,add ec;
//			if conflict,if new flow rang is bigger,add , if smaller,remove  and add
			Set<String> overlayflow = new HashSet<>();
			Set<String> tmp = Trie.FlowSet2StringSet(Trie.searchConflictFlow(new Flow(currflow)));
			overlayflow.addAll(tmp);
			if(Trie.getflag()== false){
				IsNeedRemoveOldEc = true;
			}
			log.info("conflict flow in the Trie:" + overlayflow);
			if(!Trie.containFlow(currflow)){
				Trie.addFlow(new Flow(currflow));
			}
			overlayflow.add(currflow);
			EC tmpec = ECOperations.updateEC(ecs, Trie.StringSet2FlowSet
					(overlayflow), new Flow(currflow),IsNeedRemoveOldEc);
			ECOperations.update_ecMatched_rules( ecs, tmpec,
					ec_matched_rules, Trie.StringSet2FlowSet(overlayflow));
			log.info(ecs.size() +" EC:" + ecs);
			log.info(ec_matched_rules.size() + " ec_matched_rules:" + ec_matched_rules);
		}


		log.info("flow--flowRules"+ruleset.toString());
		Set<ECGraph> ecGraph = ECOperations.getNodeEdge(ec_matched_rules, ruleset);
		log.info("ecGraph:"+ecGraph);
//		if(ecGraph.size()==Network.switches.size()){
			List<FowardingGraph> forwardingGraph = new ArrayList<FowardingGraph>();
			for(ECGraph graph:ecGraph) {
				FowardingGraph graph_per_ec=new FowardingGraph();
				graph_per_ec.ec=graph.ec;
				graph_per_ec.setDeviceset(graph.deviceset);
				graph_per_ec.setEdges(graph.edges);
				graph_per_ec.setSrcDstPair(graph.srcDstPair);

				log.info("deviceset:"+graph_per_ec.deviceset);
				log.info("edges:"+graph_per_ec.edges);

				graph_per_ec.createGraph();

				log.info("forwadingGraph:"+graph_per_ec.forwardingGraph);

				forwardingGraph.add(graph_per_ec);
			}
//			flowmod is listened one by one, compute EC锛宑annot predict whether forwarding graph complete
//			if(forwardingGraph.size()==ops){
				for(FowardingGraph graph:forwardingGraph) {
				graph.traverse();
				log.info("graph is loop\t"+graph.is_loop);
				log.info("graph is reachable\t"+graph.is_reachable);
				log.info("graph is backe_hole\t"+graph.is_back_hole);
			}
//			}
//		}
		return 1;
	}
*/
    //</editor-fold>

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        // We are the class that implements the service
        m.put(IVerifyService.class, this);
        return m;
    }


    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
//     tell the module loader we depend on it
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IStorageSourceService.class);
        l.add(IRestApiService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
//        it primarily is run to load dependencies and initialize datastructures.
        log = LoggerFactory.getLogger(Verify.class);
        restApi = context.getServiceImpl(IRestApiService.class);
        floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
        storageSource = context.getServiceImpl(IStorageSourceService.class);
        i = 0;
        ruleset = new HashMap<Flow, ArrayList<FlowRule>>();
        rule_per_flow = new ArrayList<FlowRule>();
//		ecGraph = new HashSet<>();
        enabled = true;
    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
//        it's time to implement the basic listener.
//        We'll register for PACKET_IN messages in our startUp method.
        restApi.addRestletRoutable(new VerifyWebRoutetable());

        // always place firewall in pipeline at bootup
        floodlightProvider.addOFMessageListener(OFType.FLOW_MOD, this);

        // storage, create table and read rules
//		storageSource.createTable(TABLE_NAME, null);
//		storageSource.setTablePrimaryKeyName(TABLE_NAME, COLUMN_RULEID);
//		this.rules = readRulesFromStorage();
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        if (!this.enabled) {
            return Command.CONTINUE;
        }
        switch (msg.getType()) {
            case FLOW_MOD:
//                log.info("FlowMod message!");
                if (cntx != null) {
//					Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
                    checkNetworkInvariant((OFFlowMod) msg, sw.getId());
                }
                break;
            default:
                break;
        }
        return Command.CONTINUE;
    }

    @Override
    public String getName() {
        return "verify";
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return (type.equals(OFType.PACKET_IN) && name.equals("forwarding"));

    }

    @Override
    public void enableVerify(boolean enable) {
        log.info("Setting verify to {}", enabled);
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public List<SemeticRule> getRules() {
        return null;
    }

    @Override
    public void addRule(SemeticRule rule) {

    }

    @Override
    public void deleteRule(int ruleid) {

    }

    @Override
    public List<Map<String, Object>> getStorageRules() {
        return null;
    }
}

