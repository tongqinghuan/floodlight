package com.react.verify;

public class FlowRule{
    String src_ip;
    String dst_ip;
    String dpid;
    int priority;
    FlowRuleAction action;

    public FlowRule(String src_ip, String dst_ip, String dpid, int priority, FlowRuleAction action) {
        this.src_ip = src_ip;
        this.dst_ip = dst_ip;
        this.dpid = dpid;
        this.priority = priority;
        this.action = action;
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
    public String toString() {
        return
                "src_ip='" + src_ip + '\'' +
                ", dst_ip='" + dst_ip + '\'' +
                ", dpid='" + dpid + '\'' +
                ", priority=" + priority +
                ", action=" + action 
                ;
    }
}
