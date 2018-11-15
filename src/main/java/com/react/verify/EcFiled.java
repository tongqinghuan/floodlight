package com.react.verify;

import java.util.Objects;

public class EcFiled {
    String dst_ip;
    EcFiled(String dst_ip) {
        this.dst_ip = dst_ip;
    }

    public void setDst_ip(String dst_ip) {
        this.dst_ip = dst_ip;
    }
    public String getDst_ip() {
        return dst_ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.react.verify.EcFiled ecFiled = (com.react.verify.EcFiled) o;
        return Objects.equals(dst_ip, ecFiled.dst_ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dst_ip);
    }

    @Override
    public String toString() {
        return "EcFiled{" +
                "dst_ip='" + dst_ip + '\'' +
                '}';
    }



}
