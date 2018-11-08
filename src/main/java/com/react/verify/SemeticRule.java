package com.react.verify;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.projectfloodlight.openflow.types.*;

@JsonSerialize(using=SemeticRuleSerializer.class)
public class SemeticRule implements Comparable<SemeticRule> {
    public int ruleid;
    public DatapathId dpid;
    public OFPort in_port;
    public MacAddress dl_src;
    public MacAddress dl_dst;
    public EthType dl_type;
    public IPv4AddressWithMask nw_src_prefix_and_mask;
    public IPv4AddressWithMask nw_dst_prefix_and_mask;
    public IpProtocol nw_proto;
    public TransportPort tp_src;
    public TransportPort tp_dst;

    public boolean any_dpid;
    public boolean any_in_port;
    public boolean any_dl_src;
    public boolean any_dl_dst;
    public boolean any_dl_type;
    public boolean any_nw_src;
    public boolean any_nw_dst;
    public boolean any_nw_proto;
    public boolean any_tp_src;
    public boolean any_tp_dst;

    public int priority = 0;

    public SemeticRule(){
        this.dpid = DatapathId.NONE;
        this.in_port = OFPort.ANY;
        this.dl_src = MacAddress.NONE;
        this.dl_dst = MacAddress.NONE;
        this.dl_type = EthType.NONE;
        this.nw_src_prefix_and_mask = IPv4AddressWithMask.NONE;
        this.nw_dst_prefix_and_mask = IPv4AddressWithMask.NONE;
        this.nw_proto = IpProtocol.NONE;
        this.tp_src = TransportPort.NONE;
        this.tp_dst = TransportPort.NONE;
        this.any_dpid = true;
        this.any_in_port = true;
        this.any_dl_src = true;
        this.any_dl_dst = true;
        this.any_dl_type = true;
        this.any_nw_src = true;
        this.any_nw_dst = true;
        this.any_nw_proto = true;
        this.any_tp_src = true;
        this.any_tp_dst = true;
        this.priority = 0;
        this.ruleid = 0;
    }

    @Override
    public int compareTo(SemeticRule o) {
        return this.priority - o.priority;
    }
    public boolean isSameAs(SemeticRule r) {
        if (
                 this.any_dl_type != r.any_dl_type
                || (this.any_dl_type == false && !this.dl_type.equals(r.dl_type))
                || this.any_tp_src != r.any_tp_src
                || (this.any_tp_src == false && !this.tp_src.equals(r.tp_src))
                || this.any_tp_dst != r.any_tp_dst
                || (this.any_tp_dst == false && !this.tp_dst.equals(r.tp_dst))
                || this.any_dpid != r.any_dpid
                || (this.any_dpid == false && !this.dpid.equals(r.dpid))
                || this.any_in_port != r.any_in_port
                || (this.any_in_port == false && !this.in_port.equals(r.in_port))
                || this.any_nw_src != r.any_nw_src
                || (this.any_nw_src == false && !this.nw_src_prefix_and_mask.equals(r.nw_src_prefix_and_mask))
                || this.any_dl_src != r.any_dl_src
                || (this.any_dl_src == false && !this.dl_src.equals(r.dl_src))
                || this.any_nw_proto != r.any_nw_proto
                || (this.any_nw_proto == false && !this.nw_proto.equals(r.nw_proto))
                || this.any_nw_dst != r.any_nw_dst
                || (this.any_nw_dst == false && !this.nw_dst_prefix_and_mask.equals(r.nw_dst_prefix_and_mask))
                || this.any_dl_dst != r.any_dl_dst
                || (this.any_dl_dst == false && this.dl_dst != r.dl_dst)) {
            return false;
        }
        return true;
    }
}
