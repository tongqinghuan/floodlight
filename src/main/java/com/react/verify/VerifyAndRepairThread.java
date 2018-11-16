package com.react.verify;

import com.react.compiler.Flow;
import com.react.compiler.Instruction;
import com.react.compiler.MiniCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

public class VerifyAndRepairThread implements Callable<List<Instruction>> {
    protected static Logger log = LoggerFactory.getLogger(VerifyAndRepairThread.class);
    EcFiled currentEcFiled;
    public VerifyAndRepairThread(EcFiled currentEcFiled){
        this.currentEcFiled=currentEcFiled;
    }
    @Override
    public List<Instruction> call() throws Exception {
        List<Instruction> res = new ArrayList<>();
        HashSet<EcFiled> conflictEcfiled;
        HashSet<EC> ecsForCurrentFlowMod;
        HashMap<EC, HashSet<EcFiled>> matchedEcfiledForEc;
        HashMap<EC, HashSet<Flow>> matchedEcFlow = null;
        //search conflict rule on trie
        conflictEcfiled = FlowModIntersepting.trie.searchConflictFlowRule(currentEcFiled);
        //generate ecs
        ecsForCurrentFlowMod = ECOperations.getEC(conflictEcfiled, currentEcFiled);
        log.info("ecsForCurrentFlowMod:" + ecsForCurrentFlowMod.toString());
        //construct forwarding graph
        matchedEcfiledForEc = ECOperations.generate_ecmatchedecFiled(ecsForCurrentFlowMod, conflictEcfiled);
        log.info("matchedEcfiledForEc:" + matchedEcfiledForEc.toString());
        if (MiniCompiler.flows != null) {
            matchedEcFlow = ECOperations.generate_ecmatchedFlow(ecsForCurrentFlowMod, MiniCompiler.flows);
        }
        log.info("matchedEcFlow:" + matchedEcFlow.toString());
        return res;
    }

}
