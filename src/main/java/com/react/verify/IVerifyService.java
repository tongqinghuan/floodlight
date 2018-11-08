package com.react.verify;

import net.floodlightcontroller.core.module.IFloodlightService;

import java.util.List;
import java.util.Map;

public interface IVerifyService extends IFloodlightService{
    public void enableVerify(boolean enable);
    public boolean isEnabled();
    public List<SemeticRule> getRules();
    public void addRule(SemeticRule rule);
    public void deleteRule(int ruleid);
    public List<Map<String, Object>> getStorageRules();
}
