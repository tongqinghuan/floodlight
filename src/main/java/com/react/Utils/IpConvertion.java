package com.react.Utils;

public class IpConvertion {
    public  static  String ipIntToString(int ipAddress, int masklength) {
        //log.warn("maskLength:"+masklength);
        StringBuffer stringBuffer = new StringBuffer();
        int i = 0;
        for (; i < 32 - Integer.toBinaryString(ipAddress).length(); i++) {
            stringBuffer.append("0");
        }
        stringBuffer.append(Integer.toBinaryString(ipAddress));
        stringBuffer.delete(masklength, 32);
        while (stringBuffer.length() != 32) {
            stringBuffer.append('x');
        }
        return stringBuffer.toString();
    }
    public static String ipToBinaryString(String ip){
        int num=0;
        String[] sections=ip.split("\\.");
        int i=3;
        for(String str:sections){
            num+=(Integer.parseInt(str)<<(i*8));
            i--;
        }
        return Integer.toBinaryString(num);
    }
    private static int binaryStrToInt(String binaryStr){
        char[] str=binaryStr.toCharArray();
        int length=str.length;
        int res=0;
        for(int i=0;i<length;i++){
            if(!(str[i]=='0'||str[i]=='1')){
                return Integer.MAX_VALUE;
            }
           res+=(str[i]-'0')*Math.pow(2,length-i-1);
        }
        return res;
    }
    public static String binaryStrToIpString(String binaryStrIp){
        int num=binaryStrToInt(binaryStrIp);
        StringBuilder sb=new StringBuilder();
        for(int i=3;i>=0;i--){
            sb.append((num>>>(i*8))&0x000000ff);
            if(i!=0){
                sb.append('.');
            }

        }
        return sb.toString();
    }
    public static String numToIpString(int intIp,int maskLength){
        StringBuilder sb=new StringBuilder();
        for(int i=3;i>=0;i--){
            sb.append((intIp>>>(i*8))&0x000000ff);
            if(i!=0){
                sb.append('.');
            }

        }
        return sb.toString()+"\\"+maskLength;
    }
}
