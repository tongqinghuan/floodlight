package com.react.verify;

import java.util.Objects;

public class EcFiled {
    String dst_ip;
    String src_ip;

    public String getDst_ip() {
        return dst_ip;
    }

    public String getSrc_ip() {
        return src_ip;
    }
    public EcFiled(String src_ip,String dst_ip){
        this.src_ip=src_ip;
        this.dst_ip=dst_ip;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EcFiled ecFiled = (EcFiled) o;
        return Objects.equals(dst_ip, ecFiled.dst_ip) &&
                Objects.equals(src_ip, ecFiled.src_ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dst_ip, src_ip);
    }
    @Override
    public String toString() {
        return "EcFiled{" +
                "dst_ip='" + dst_ip + '\'' +
                ", src_ip='" + src_ip + '\'' +
                '}';
    }

}
