package com.react.verify;
import java.util.HashSet;

public class FlowScopeOperation {
	public static String encode(String flow) {
		char[] flow_arry=flow.toCharArray();
		char[] coding_flow=new char[flow.length()*2];
		for(int i=0,j=0;i<flow.length();i++) {
			if(flow_arry[i]=='0') {
				coding_flow[j]='0';
				coding_flow[j+1]='1';
			}
			if(flow_arry[i]=='1') {
				coding_flow[j]='1';
				coding_flow[j+1]='0';
			}
			if(flow_arry[i]=='x') {
				coding_flow[j]='1';
				coding_flow[j+1]='1';
			}
			if(flow_arry[i]=='z') {
				coding_flow[j]='0';
				coding_flow[j+1]='0';
			}

			j=j+2;
		}
		return String.valueOf(coding_flow);

	}



	public static String  decode(String flow) {
		int str_len=0;
		String str=null;
		int j=0;
		char[] res;
		char[] decoding_flow=new char[flow.length()/2];
		int i=0;
		while(str_len<flow.length()) {
			str=flow.substring(str_len, str_len+2);
			if(str.equals("01")) {
				decoding_flow[i]='0';
			}
			if(str.equals("10")) {
				decoding_flow[i]='1';
			}
			if(str.equals("11")) {
				decoding_flow[i]='x';
			}
			if(str.equals("00")) {
				decoding_flow[i]='z';
			}
			str_len=str_len+2;
			i=i+1;
		}
		res=String.valueOf(decoding_flow).toCharArray();
		while(j<res.length) {
			if(res[j]=='z') {
				break;
			}
			j++;
		}

		if(j==res.length) {
			return String.valueOf(res);
		}
		else {
			return null;
		}
	}
	public static String getUpperbound(String flow) {
		char[] str=flow.toCharArray();
		for(int i=0;i<str.length;i++) {
			if(str[i]=='x') {
				str[i]='1';
			}

		}
		return String.valueOf(str);

	}
	public static String getLowerbound(String flow) {
		char[] str=flow.toCharArray();
		for(int i=0;i<str.length;i++) {
			if(str[i]=='x') {
				str[i]='0';
			}

		}
		return String.valueOf(str);

	}
	//?��??????
	public static  String insetc(String flow1,String flow2) {
		System.out.println(flow1);
		System.out.println(flow2);
		if(flow1.length()!=flow2.length()) {
			return "error";
		}
		else {

			char[] str1=FlowScopeOperation.encode(flow1).toCharArray();
			char[] str2=FlowScopeOperation.encode(flow2).toCharArray();
			for(int i=0;i<str1.length;i++) {
				str1[i]=(char) (str1[i]&str2[i]);
			}
			System.out.println(FlowScopeOperation.decode(String.valueOf(str1)));
			return String.valueOf(FlowScopeOperation.decode(String.valueOf(str1)));
		}
	}
	//?��??????
	public static HashSet<String> complement(String flow) {
		int i=0;
		int len=flow.length();
		HashSet<String> flow_complement=new HashSet<String>();
		char[] flow_scope=flow.toCharArray();
		//System.out.println(flow);
		while(i<len) {
			char[] complementation=new char[len];
			if(flow_scope[i]!='x') {

				for(int j=0;j<len;j++) {
					if(j==i) {
						if(flow_scope[i]=='0') {
							complementation[j]='1';
						}
						else {
							complementation[j]='0';
						}
					}
					else {
						complementation[j]='x';
					}

				}

				flow_complement.add(String.valueOf(complementation));
			}
			i++;
		}
		return flow_complement;

	}
	//?��??????
	public static HashSet<String> Difference(String flow1,String flow2){
		HashSet<String> difference=new HashSet<String>();
		HashSet<String> complementation;
		complementation =FlowScopeOperation.complement(flow2);
		//System.out.println(complementation);
		for(String str:complementation) {
			difference.add(FlowScopeOperation.insetc(flow1, str));
		}

		return difference;
	}



}

