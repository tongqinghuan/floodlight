package com.react.verify;

import com.react.compiler.Flow;

public class VerifyResult {
    Flow flow;
    boolean is_violation;
    public VerifyResult(Flow flow,boolean is_violation){
        this.flow=flow;
        this.is_violation=is_violation;
    }

    @Override
    public String toString() {
        return "{" +
                "flow=" + flow +
                ", is_violation=" + is_violation +
                '}';
    }
}
