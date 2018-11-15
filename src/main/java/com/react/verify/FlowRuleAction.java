package com.react.verify;

import java.util.Objects;

class FlowRuleAction{
    String action;
    Integer port;
    public FlowRuleAction(String action,Integer port){
        this.action=action;
        this.port=port;
    }

    public String getAction() {
        return action;
    }

    public Integer getPort() {
        return port;
    }
    @Override
    public String toString() {
        return "FlowRuleAction{" +
                "action='" + action + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowRuleAction that = (FlowRuleAction) o;
        return Objects.equals(action, that.action) &&
                Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, port);
    }
}