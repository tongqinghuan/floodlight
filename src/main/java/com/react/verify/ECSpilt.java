package com.react.verify;


import net.floodlightcontroller.packet.IPv4;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.react.topo.TwoTuple;

public class ECSpilt {
	static List<TwoTuple<Integer>> masked_rules=new ArrayList<TwoTuple<Integer>>();
	static List<String> ecs=new ArrayList<String>();
	protected static Logger log = LoggerFactory.getLogger(ECSpilt.class);//int has 64 bits
	public static int getLeft(int number) {
		int n=number;
		n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        n = n >>> 1;
        return (n<0)?1:n+1;
	}

	public static int getRight(int number) {
		int n=number-1;
		n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
		return (n<0)?1:n+1;
	}

    public  static void ecSpilt(int start,int end,int mid_result) {
    	int left=getLeft(start);
    	//System.out.println(left);
    	int right=getRight(end);
    	//System.out.println(right);
    	int left_power=(int)(Math.log(left)/Math.log(2.0));
    	int right_power=(int)(Math.log(right)/Math.log(2.0));
    	if(right_power-left_power==1) {
    		start=start-left;
    		end=end-left;
    		mid_result=mid_result|(1<<left_power);
    		if(start==0) {
    			int right_end=getRight(end);
    			int mask=(int)(Math.log(right_end)/Math.log(
    					2.0));
    			masked_rules.add(new TwoTuple<Integer>(mid_result,mask));
    		
    		}
    		else {
    			ecSpilt(start,end,mid_result);
    		}
    		
    	}
    	else {
    		int j;
    		ecSpilt(start,(int)Math.pow(2,left_power+1 ),mid_result);
    		for( j=left_power+2;j<right_power;j++) {
    			start=(int)Math.pow(2, j-1);
				ecSpilt(start,(int)Math.pow(2, j),mid_result);
				
			}
    		ecSpilt((int)Math.pow(2, j-1),end,mid_result);
    	}
    }
/*
 */
    public static String intToIp(int ipInteger) {
        return new StringBuilder().append(((ipInteger >> 24) & 0xff)).append('.')
                .append((ipInteger >> 16) & 0xff).append('.').append(
                        (ipInteger >> 8) & 0xff).append('.').append((ipInteger& 0xff))
                .toString();
    }

    public static void getMaskedRule() {
    	for(TwoTuple<Integer> tuple:masked_rules) {
    		int first=tuple.first;
    		int second=tuple.second;
    		StringBuilder str=new StringBuilder().append(intToIp(first)).append('/').append(String.valueOf(32-second));
    		ecs.add(str.toString());
    	}
    }

    public static void test(String[] args) {
    	ECSpilt.ecSpilt(184678917, 184679166, 0);
    	log.info(IPv4.fromIPv4Address(184678917)+
		"-"+IPv4.fromIPv4Address(184679166));

        ECSpilt.getMaskedRule();
    	System.out.println(ECSpilt.getLeft(32));
    	System.out.println(masked_rules);
    	System.out.println(ecs);
    	//System.out.println((int)Math.pow(2, 32));
    	//System.out.println(ECSpilt.ipToInteger("11.1.250.254"));
    }
}
