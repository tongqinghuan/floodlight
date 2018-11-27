package com.react.verify;

import com.react.compiler.Flow;
import com.react.compiler.Instruction;
import com.react.compiler.MiniCompiler;
import com.react.compiler.VerifyTestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;

public class VerifyAndRepairThread implements Callable<HashMap<EC,HashSet<VerifyResult>>> {
    protected static Logger log = LoggerFactory.getLogger(VerifyAndRepairThread.class);
    EcFiled currentEcFiled;
    public VerifyAndRepairThread(EcFiled currentEcFiled){
        this.currentEcFiled=currentEcFiled;
    }
    @Override
    public HashMap<EC,HashSet<VerifyResult>> call() throws Exception {

        HashMap<EC,HashSet<VerifyResult>> res=new HashMap<>();
        HashSet<EcFiled> conflictEcfiled;
        HashSet<FlowRule> conflictFlowRule=new HashSet<>();
        HashSet<EC> ecsForCurrentFlowMod;
        HashMap<EC, HashSet<FlowRule>> matchedEcFlowRule;
        HashMap<EC, HashSet<Flow>> matchedEcFlow = null;
        HashMap<EC,ECGraph> ecGraph=null;
        //search conflict rule on trie
        log.info("---VerifyAndRepairThread is calculating conflict flow ruls---");
        //log.warn("currentEcFiled"+currentEcFiled.toString());
        conflictEcfiled = FlowModIntersepting.trie.searchConflictFlowRule(currentEcFiled);
       // log.warn("the size of conflictEcfiled:"+conflictEcfiled.size());
       // log.warn("the size of ecfiledFlowRulePair"+FlowModIntersepting.ecfiledFlowRulePair.size());
  //      log.warn("ecfiledFlowRulePair:"+FlowModIntersepting.ecfiledFlowRulePair.toString());
//        for(Map.Entry<EcFiled,HashSet<FlowRule>> entry:FlowModIntersepting.ecfiledFlowRulePair.entrySet()){
//            log.error(entry.getKey().toString());
//        }
        for(EcFiled ecFiled:conflictEcfiled){
           // log.error("ecFiled:"+ecFiled.toString());
            if(FlowModIntersepting.ecfiledFlowRulePair.get(ecFiled)==null||
                    FlowModIntersepting.ecfiledFlowRulePair.get(ecFiled).size()==0){
                log.warn("ecfiledFlowRulePair.get(ecFiled) is empty");
            }else{
                conflictFlowRule.addAll(FlowModIntersepting.ecfiledFlowRulePair.get(ecFiled));
            }

        }
//        log.warn("the size of conflictFlowRule: "+conflictFlowRule.size());
 //       log.info("conflict ecFiled is as listed:");
//        for(EcFiled ecFiled:conflictEcfiled){
//            log.info(ecFiled.toString());
//        }
       // log.debug("the size of conflict flow rules:"+conflictEcfiled.size());

//        generate ecs
        log.info("---VerifyAndRepairThread is calculating ecs---");
        ecsForCurrentFlowMod = ECOperations.getEcForMutidimension(conflictEcfiled, currentEcFiled);

//        construct forwarding graph
        log.info("---VerifyAndRepairThread is calculating matched flow rules---");
        matchedEcFlowRule = ECOperations.generate_ecmatchedFlowRule(ecsForCurrentFlowMod, conflictFlowRule);
       // log.info("matchedEcfiledForEc:" + matchedEcFlowRule.toString());
        if (VerifyTestData.flows != null) {
           // matchedEcFlow = ECOperations.generate_ecmatchedFlow(ecsForCurrentFlowMod, MiniCompiler.flows);
           log.debug("---System is calculating ecMatched flow---");
            matchedEcFlow = ECOperations.generate_ecmatchedFlow(ecsForCurrentFlowMod, VerifyTestData.flows);
        }
      //  log.info("---System is creating forwarding graph---");
        //  log.debug("matchedEcFlowRule:"+matchedEcFlowRule.toString());
         ecGraph=ECOperations.createEcGraph(matchedEcFlowRule);

         log.info("---System is verifying forwarding graph---");
         //log.debug("ECGraph:"+ecGraph.toString());
         for(Map.Entry<EC,ECGraph> entry:ecGraph.entrySet()){
             EC ec=entry.getKey();
             ECGraph ecGraph1=entry.getValue();
             log.warn(ec.toString());
             log.warn(ecGraph1.toString());
         }
         for(Map.Entry<EC,ECGraph> entry:ecGraph.entrySet()){
             EC ec=entry.getKey();
             ECGraph ecGraph1=entry.getValue();
             HashSet<Flow> matchedFlows=matchedEcFlow.get(ec);
            // log.debug("matchedFlows:"+matchedFlows.size());
             if(matchedFlows==null||matchedFlows.size()==0){
                 continue;
             }
             //log.debug("the size of matchedFlows:"+matchedFlows.size());
             if(ecGraph1!=null){
                 ForwardingGraph forwardingGraph=new ForwardingGraph();
                 log.debug("ECGraph:"+ec.toString()+"  "+ecGraph1.toString());
                 forwardingGraph.createGraph(ecGraph1);
                 log.debug("forwarding graph:"+forwardingGraph.toString());
                 if(forwardingGraph.getmVexs()==null||forwardingGraph.getmVexs().size()==0){
                     log.debug("---no matched rules---");
                     res.put(ec,null);
                     continue;
                 }
                 HashSet<VerifyResult> verifyResults= forwardingGraph.traverse(matchedFlows);
                 res.put(ec,verifyResults);
             }


         }
         log.info("the result is as listed:");
         for(Map.Entry<EC,HashSet<VerifyResult>> entry:res.entrySet()){
             EC ec=entry.getKey();
             HashSet<VerifyResult> result=entry.getValue();
             log.debug("ec:"+ec.toString()+"--->"+result.toString());
         }


        return res;
    }

}
