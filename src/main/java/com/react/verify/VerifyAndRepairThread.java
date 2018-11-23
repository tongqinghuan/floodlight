package com.react.verify;

import com.react.compiler.Flow;
import com.react.compiler.Instruction;
import com.react.compiler.MiniCompiler;
import com.react.compiler.VerifyTestData;
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
        HashMap<EC, HashSet<FlowRule>> matchedEcFlowRule;
        HashMap<EC, HashSet<Flow>> matchedEcFlow = null;
        //search conflict rule on trie
        log.warn("---VerifyAndRepairThread is calculating conflict flow ruls---");
        conflictEcfiled = FlowModIntersepting.trie.searchConflictFlowRule(currentEcFiled);
 //       log.info("conflict ecFiled is as listed:");
//        for(EcFiled ecFiled:conflictEcfiled){
//            log.info(ecFiled.toString());
//        }
//        generate ecs
        log.info("---VerifyAndRepairThread is calculating ecs---");
        ecsForCurrentFlowMod = ECOperations.getEcForMutidimension(conflictEcfiled, currentEcFiled);
        log.info("ecsForCurrentFlowMod:" + ecsForCurrentFlowMod.toString());
//        construct forwarding graph
        log.info("---VerifyAndRepairThread is calculating matched flow rules---");
        matchedEcFlowRule = ECOperations.generate_ecmatchedecFiled(ecsForCurrentFlowMod, conflictEcfiled);
       // log.info("matchedEcfiledForEc:" + matchedEcFlowRule.toString());
        if (MiniCompiler.flows != null) {
           // matchedEcFlow = ECOperations.generate_ecmatchedFlow(ecsForCurrentFlowMod, MiniCompiler.flows);
            matchedEcFlow = ECOperations.generate_ecmatchedFlow(ecsForCurrentFlowMod, VerifyTestData.flows);
        }
       // log.info("matchedEcFlow:" + matchedEcFlow.toString());
        return res;
    }

}
