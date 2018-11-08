package com.react.topo;
public class FourTuple{
    public String src;
    public  String dst;
    int entry;
    int exit;
    public FourTuple(String src,String dst,int entry,int exit) {
        this.src=src;
        this.dst=dst;
        this.entry=entry;
        this.exit=exit;
    }
    @Override
    public String toString() {
        return "FourTuple [src=" + src + ", dst=" + dst + ", entry=" + entry + ", exit=" + exit + "]";
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dst == null) ? 0 : dst.hashCode());
        result = prime * result + entry;
        result = prime * result + exit;
        result = prime * result + ((src == null) ? 0 : src.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FourTuple other = (FourTuple) obj;
        if (dst == null) {
            if (other.dst != null)
                return false;
        } else if (!dst.equals(other.dst))
            return false;
        if (entry != other.entry)
            return false;
        if (exit != other.exit)
            return false;
        if (src == null) {
            if (other.src != null)
                return false;
        } else if (!src.equals(other.src))
            return false;
        return true;
    }
}
