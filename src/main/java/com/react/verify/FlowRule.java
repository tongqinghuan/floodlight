package com.react.verify;

import com.react.Utils.IpConvertion;

import java.util.Objects;

public class FlowRule{
    int src_ip;
    int src_mask;
    int dst_ip;
    int dst_mask;
    int in_port;
    int port_mask;
    String dpid;
    int priority;
    FlowRuleAction action;

    public FlowRule(int src_ip, int src_mask, int dst_ip, int dst_mask,
                    int in_port, int port_mask, String dpid, int priority,
                    FlowRuleAction action) {
        this.src_ip = src_ip;
        this.src_mask = src_mask;
        this.dst_ip = dst_ip;
        this.dst_mask = dst_mask;
        this.in_port = in_port;
        this.port_mask = port_mask;
        this.dpid = dpid;
        this.priority = priority;
        this.action = action;
    }

    public int getSrc_ip() {
        return src_ip;
    }

    public int getSrc_mask() {
        return src_mask;
    }

    public int getDst_ip() {
        return dst_ip;
    }

    public int getDst_mask() {
        return dst_mask;
    }

    public int getIn_port() {
        return in_port;
    }

    public int getPort_mask() {
        return port_mask;
    }

    public String getDpid() {
        return dpid;
    }

    public int getPriority() {
        return priority;
    }

    public FlowRuleAction getAction() {
        return action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowRule flowRule = (FlowRule) o;
        return src_ip == flowRule.src_ip &&
                src_mask == flowRule.src_mask &&
                dst_ip == flowRule.dst_ip &&
                dst_mask == flowRule.dst_mask &&
                in_port == flowRule.in_port &&
                port_mask == flowRule.port_mask &&
                priority == flowRule.priority &&
                Objects.equals(dpid, flowRule.dpid) &&
                Objects.equals(action, flowRule.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src_ip, src_mask, dst_ip, dst_mask, in_port, port_mask, dpid, priority, action);
    }

    @Override
    public String toString() {
        return "{" +
                "src_ip=" + IpConvertion.numToIpString(src_ip,src_mask) +
                ", dst_ip=" + IpConvertion.numToIpString(dst_ip,dst_mask) +
                ", in_port=" + in_port +
                ", dpid='" + dpid + '\'' +
                ", priority=" + priority +
                ", action=" + action +
                '}';
    }
}
