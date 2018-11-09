package com.react.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFlowMod;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.U16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.staticflowentry.StaticFlowEntries;
import net.floodlightcontroller.staticflowentry.StaticFlowEntryPusher;

import net.floodlightcontroller.staticflowentry.web.StaticFlowEntryWebRoutable;
import net.floodlightcontroller.storage.IResultSet;
import net.floodlightcontroller.storage.IStorageSourceListener;
import net.floodlightcontroller.storage.IStorageSourceService;
import net.floodlightcontroller.util.ActionUtils;
import net.floodlightcontroller.util.InstructionUtils;
import net.floodlightcontroller.util.MatchUtils;

public class Veriflow implements IFloodlightModule,
        IOFMessageListener, IOFSwitchListener, IStorageSourceListener {

    protected static Logger log = LoggerFactory.getLogger(Veriflow.class);

    public static final String TABLE_NAME = "controller_staticflowtableentry";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SWITCH = "switch";
    public static final String COLUMN_TABLE_ID = "table";
    public static final String COLUMN_ACTIVE = "active";
    public static final String COLUMN_IDLE_TIMEOUT = "idle_timeout";
    public static final String COLUMN_HARD_TIMEOUT = "hard_timeout";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_COOKIE = "cookie";

    // Common location for Match Strings. Still the same, but relocated.
    public static final String COLUMN_IN_PORT = MatchUtils.STR_IN_PORT;

    public static final String COLUMN_DL_SRC = MatchUtils.STR_DL_SRC;
    public static final String COLUMN_DL_DST = MatchUtils.STR_DL_DST;
    public static final String COLUMN_DL_VLAN = MatchUtils.STR_DL_VLAN;
    public static final String COLUMN_DL_VLAN_PCP = MatchUtils.STR_DL_VLAN_PCP;
    public static final String COLUMN_DL_TYPE = MatchUtils.STR_DL_TYPE;

    public static final String COLUMN_NW_TOS = MatchUtils.STR_NW_TOS;

    public static final String COLUMN_NW_PROTO = MatchUtils.STR_NW_PROTO;
    public static final String COLUMN_NW_SRC = MatchUtils.STR_NW_SRC; // includes CIDR-style netmask, e.g. "128.8.128.0/24"
    public static final String COLUMN_NW_DST = MatchUtils.STR_NW_DST;

    public static final String COLUMN_SCTP_SRC = MatchUtils.STR_SCTP_SRC;
    public static final String COLUMN_SCTP_DST = MatchUtils.STR_SCTP_DST;
    public static final String COLUMN_UDP_SRC = MatchUtils.STR_UDP_SRC;
    public static final String COLUMN_UDP_DST = MatchUtils.STR_UDP_DST;
    public static final String COLUMN_TCP_SRC = MatchUtils.STR_TCP_SRC;
    public static final String COLUMN_TCP_DST = MatchUtils.STR_TCP_DST;
    public static final String COLUMN_TP_SRC = MatchUtils.STR_TP_SRC; // support for OF1.0 generic transport ports (possibly sent from the rest api). Only use these to read them in, but store them as the type of port their IpProto is set to.
    public static final String COLUMN_TP_DST = MatchUtils.STR_TP_DST;

    /* newly added matches for OF1.3 port start here */
    public static final String COLUMN_ICMP_TYPE = MatchUtils.STR_ICMP_TYPE;
    public static final String COLUMN_ICMP_CODE = MatchUtils.STR_ICMP_CODE;

    public static final String COLUMN_ARP_OPCODE = MatchUtils.STR_ARP_OPCODE;
    public static final String COLUMN_ARP_SHA = MatchUtils.STR_ARP_SHA;
    public static final String COLUMN_ARP_DHA = MatchUtils.STR_ARP_DHA;
    public static final String COLUMN_ARP_SPA = MatchUtils.STR_ARP_SPA;
    public static final String COLUMN_ARP_DPA = MatchUtils.STR_ARP_DPA;

    /* IPv6 related columns */
    public static final String COLUMN_NW6_SRC = MatchUtils.STR_IPV6_SRC;
    public static final String COLUMN_NW6_DST = MatchUtils.STR_IPV6_DST;
    public static final String COLUMN_IPV6_FLOW_LABEL = MatchUtils.STR_IPV6_FLOW_LABEL;
    public static final String COLUMN_ICMP6_TYPE = MatchUtils.STR_ICMPV6_TYPE;
    public static final String COLUMN_ICMP6_CODE = MatchUtils.STR_ICMPV6_CODE;
    public static final String COLUMN_ND_SLL = MatchUtils.STR_IPV6_ND_SSL;
    public static final String COLUMN_ND_TLL = MatchUtils.STR_IPV6_ND_TTL;
    public static final String COLUMN_ND_TARGET = MatchUtils.STR_IPV6_ND_TARGET;

    public static final String COLUMN_MPLS_LABEL = MatchUtils.STR_MPLS_LABEL;
    public static final String COLUMN_MPLS_TC = MatchUtils.STR_MPLS_TC;
    public static final String COLUMN_MPLS_BOS = MatchUtils.STR_MPLS_BOS;

    public static final String COLUMN_METADATA = MatchUtils.STR_METADATA;
    public static final String COLUMN_TUNNEL_ID = MatchUtils.STR_TUNNEL_ID;

    public static final String COLUMN_PBB_ISID = MatchUtils.STR_PBB_ISID;
    /* end newly added matches */

    public static final String COLUMN_ACTIONS = "actions";

    public static final String COLUMN_INSTR_GOTO_TABLE = InstructionUtils.STR_GOTO_TABLE; // instructions are each getting their own column, due to write and apply actions, which themselves contain a variable list of actions
    public static final String COLUMN_INSTR_WRITE_METADATA = InstructionUtils.STR_WRITE_METADATA;
    public static final String COLUMN_INSTR_WRITE_ACTIONS = InstructionUtils.STR_WRITE_ACTIONS;
    public static final String COLUMN_INSTR_APPLY_ACTIONS = InstructionUtils.STR_APPLY_ACTIONS;
    public static final String COLUMN_INSTR_CLEAR_ACTIONS = InstructionUtils.STR_CLEAR_ACTIONS;
    public static final String COLUMN_INSTR_GOTO_METER = InstructionUtils.STR_GOTO_METER;
    public static final String COLUMN_INSTR_EXPERIMENTER = InstructionUtils.STR_EXPERIMENTER;


    protected IOFSwitchService switchService;
    protected IStorageSourceService storageSourceService;

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        // TODO Auto-generated method stub
        Collection<Class<? extends IFloodlightService>> l =
                new ArrayList<Class<? extends IFloodlightService>>();

        l.add(IOFSwitchService.class);
        l.add(IStorageSourceService.class);

        return l;

    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub

        switchService = context.getServiceImpl(IOFSwitchService.class);
        storageSourceService = context.getServiceImpl(IStorageSourceService.class);

    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        // TODO Auto-generated method stub
        switchService.addOFSwitchListener(this);

        // assumes no switches connected at startup()
        storageSourceService.createTable(TABLE_NAME, null);
        storageSourceService.setTablePrimaryKeyName(TABLE_NAME, COLUMN_NAME);
        storageSourceService.addListener(TABLE_NAME, this);

    }


    void parseRow(Map<String, Object> row, Map<String, Map<String, OFFlowMod>> entries) {
        String switchName = null;
        String entryName = null;

        StringBuffer matchString = new StringBuffer();
        OFFlowMod.Builder fmb = null;

        if (!row.containsKey(COLUMN_SWITCH) || !row.containsKey(COLUMN_NAME)) {
            log.debug("skipping entry with missing required 'switch' or 'name' entry: {}", row);
            return;
        }
        // most error checking done with ClassCastException
        try {
            // first, snag the required entries, for debugging info
            switchName = (String) row.get(COLUMN_SWITCH);
            entryName = (String) row.get(COLUMN_NAME);
            if (!entries.containsKey(switchName)) {
                entries.put(switchName, new HashMap<String, OFFlowMod>());
            }

            // get the correct builder for the OF version supported by the switch
            try {
                fmb = OFFactories.getFactory(switchService.getSwitch(DatapathId.of(switchName)).getOFFactory().getVersion()).buildFlowModify();
            } catch (NullPointerException e) {
                /* switch was not connected/known */
                storageSourceService.deleteRowAsync(TABLE_NAME, entryName);
                log.error("Deleting entry {}. Switch {} was not connected to the controller, and we need to know the OF protocol version to compose the flow mod.", entryName, switchName);
                return;
            }

            StaticFlowEntries.initDefaultFlowMod(fmb, entryName);

            for (String key : row.keySet()) {
                if (row.get(key) == null) {
                    continue;
                }

                if (key.equals(COLUMN_SWITCH) || key.equals(COLUMN_NAME) || key.equals("id")) {
                    continue; // already handled
                }

                if (key.equals(COLUMN_ACTIVE)) {
                    if (!Boolean.valueOf((String) row.get(COLUMN_ACTIVE))) {
                        log.debug("skipping inactive entry {} for switch {}", entryName, switchName);
                        entries.get(switchName).put(entryName, null);  // mark this an inactive
                        return;
                    }
                } else if (key.equals(COLUMN_HARD_TIMEOUT)) {
                    fmb.setHardTimeout(Integer.valueOf((String) row.get(COLUMN_HARD_TIMEOUT)));
                } else if (key.equals(COLUMN_IDLE_TIMEOUT)) {
                    fmb.setIdleTimeout(Integer.valueOf((String) row.get(COLUMN_IDLE_TIMEOUT)));
                } else if (key.equals(COLUMN_TABLE_ID)) {
                    if (fmb.getVersion().compareTo(OFVersion.OF_10) > 0) {
                        fmb.setTableId(TableId.of(Integer.parseInt((String) row.get(key)))); // support multiple flow tables for OF1.1+
                    } else {
                        log.error("Table not supported in OpenFlow 1.0");
                    }
                } else if (key.equals(COLUMN_ACTIONS)) {
                    ActionUtils.fromString(fmb, (String) row.get(COLUMN_ACTIONS), log);
                } else if (key.equals(COLUMN_COOKIE)) {
                    fmb.setCookie(StaticFlowEntries.computeEntryCookie(Integer.valueOf((String) row.get(COLUMN_COOKIE)), entryName));
                } else if (key.equals(COLUMN_PRIORITY)) {
                    fmb.setPriority(U16.t(Integer.valueOf((String) row.get(COLUMN_PRIORITY))));
                } else if (key.equals(COLUMN_INSTR_APPLY_ACTIONS)) {
                    InstructionUtils.applyActionsFromString(fmb, (String) row.get(COLUMN_INSTR_APPLY_ACTIONS), log);
                } else if (key.equals(COLUMN_INSTR_CLEAR_ACTIONS)) {
                    InstructionUtils.clearActionsFromString(fmb, (String) row.get(COLUMN_INSTR_CLEAR_ACTIONS), log);
                } else if (key.equals(COLUMN_INSTR_EXPERIMENTER)) {
                    InstructionUtils.experimenterFromString(fmb, (String) row.get(COLUMN_INSTR_EXPERIMENTER), log);
                } else if (key.equals(COLUMN_INSTR_GOTO_METER)) {
                    InstructionUtils.meterFromString(fmb, (String) row.get(COLUMN_INSTR_GOTO_METER), log);
                } else if (key.equals(COLUMN_INSTR_GOTO_TABLE)) {
                    InstructionUtils.gotoTableFromString(fmb, (String) row.get(COLUMN_INSTR_GOTO_TABLE), log);
                } else if (key.equals(COLUMN_INSTR_WRITE_ACTIONS)) {
                    InstructionUtils.writeActionsFromString(fmb, (String) row.get(COLUMN_INSTR_WRITE_ACTIONS), log);
                } else if (key.equals(COLUMN_INSTR_WRITE_METADATA)) {
                    InstructionUtils.writeMetadataFromString(fmb, (String) row.get(COLUMN_INSTR_WRITE_METADATA), log);
                } else { // the rest of the keys are for Match().fromString()
                    if (matchString.length() > 0) {
                        matchString.append(",");
                    }
                    matchString.append(key + "=" + row.get(key).toString());
                }
            }
        } catch (ClassCastException e) {
            if (entryName != null && switchName != null) {
                log.warn("Skipping entry {} on switch {} with bad data : " + e.getMessage(), entryName, switchName);
            } else {
                log.warn("Skipping entry with bad data: {} :: {} ", e.getMessage(), e.getStackTrace());
            }
        }

        String match = matchString.toString();

        try {
            fmb.setMatch(MatchUtils.fromString(match, fmb.getVersion()));
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            log.error("Ignoring flow entry {} on switch {} with illegal OFMatch() key: " + match, entryName, switchName);
            return;
        } catch (Exception e) {
            log.error("OF version incompatible for the match: " + match);
            e.printStackTrace();
            return;
        }

        entries.get(switchName).put(entryName, fmb.build()); // add the FlowMod message to the table
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

    @Override
    public void rowsModified(String tableName, Set<Object> rowKeys) {
        // This handles both rowInsert() and rowUpdate()
        //		System.out.println("rowsModified is triggered");
        HashMap<String, Map<String, OFFlowMod>> entriesToAdd = new HashMap<String, Map<String, OFFlowMod>>();

        // build up list of what was added
        for (Object key : rowKeys) {
            IResultSet resultSet = storageSourceService.getRow(tableName, key);
            Iterator<IResultSet> it = resultSet.iterator();
            while (it.hasNext()) {
                Map<String, Object> row = it.next().getRow();
                parseRow(row, entriesToAdd);
            }
        }

        for (String dpid : entriesToAdd.keySet()) {
            for (String entry : entriesToAdd.get(dpid).keySet()) {
                //	System.out.println(Long.toString(DatapathId.of(dpid).getLong()));
                OFFlowMod newFlowMod = entriesToAdd.get(dpid).get(entry);
                Verify.checkNetworkInvariant(newFlowMod, DatapathId.of(dpid));
            }
        }
    }

    @Override
    public void rowsDeleted(String tableName, Set<Object> rowKeys) {
        // TODO Auto-generated method stub
        HashMap<String, Map<String, OFFlowMod>> entriesToAdd = new HashMap<String, Map<String, OFFlowMod>>();

        // build up list of what was added
        for (Object key : rowKeys) {
            IResultSet resultSet = storageSourceService.getRow(tableName, key);
            Iterator<IResultSet> it = resultSet.iterator();
            while (it.hasNext()) {
                Map<String, Object> row = it.next().getRow();
                parseRow(row, entriesToAdd);
            }
        }

        for (String dpid : entriesToAdd.keySet()) {
            for (String entry : entriesToAdd.get(dpid).keySet()) {
                //	System.out.println(Long.toString(DatapathId.of(dpid).getLong()));
                OFFlowMod newFlowMod = entriesToAdd.get(dpid).get(entry);
                Verify.checkNetworkInvariant(newFlowMod, DatapathId.of(dpid));
            }
        }
    }

    @Override
    public void switchAdded(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchRemoved(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchActivated(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
        // TODO Auto-generated method stub

    }

    @Override
    public void switchChanged(DatapathId switchId) {
        // TODO Auto-generated method stub

    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        // TODO Auto-generated method stub
        return null;
    }

}