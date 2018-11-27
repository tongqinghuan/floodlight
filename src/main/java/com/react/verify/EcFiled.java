package com.react.verify;

import java.util.Objects;

public class EcFiled {
    String src_ip;
    String dst_ip;
    String in_port;

    public EcFiled(String src_ip, String dst_ip, String in_port) {
        this.src_ip = src_ip;
        this.dst_ip = dst_ip;
        this.in_port = in_port;
    }

    public String getSrc_ip() {
        return src_ip;
    }

    public String getDst_ip() {
        return dst_ip;
    }

    public String getIn_port() {
        return in_port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EcFiled ecFiled = (EcFiled) o;
        return Objects.equals(src_ip, ecFiled.src_ip) &&
                Objects.equals(dst_ip, ecFiled.dst_ip) &&
                Objects.equals(in_port, ecFiled.in_port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(src_ip, dst_ip, in_port);
    }

    @Override
    public String toString() {
        return "EcFiled{" +
                "src_ip='" + src_ip + '\'' +
                ", dst_ip='" + dst_ip + '\'' +
                ", in_port='" + in_port + '\'' +
                '}';
    }
}
