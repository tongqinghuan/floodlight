package com.react.verify;

public class FlowRule{
    String dpid;
    int priority;
    FlowRuleAction action;

    public String getDpid() {
        return dpid;
    }

    public void setDpid(String dpid) {
        this.dpid = dpid;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public FlowRuleAction getAction() {
        return action;
    }

    public void setAction(FlowRuleAction action) {
        this.action = action;
    }

    public FlowRule(String dpid, int priority, FlowRuleAction action) {
        this.dpid = dpid;
        this.priority = priority;
        this.action = action;
    }

    @Override
    public String toString() {
        return "FlowRule{" +
                "dpid='" + dpid + '\'' +
                ", priority=" + priority +
                ", action=" + action +
                '}';
    }
}
