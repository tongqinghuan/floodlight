package com.react.verify;

import java.util.Objects;
import com.react.Utils.IpConvertion;

public class OneDimensionalEc {
    //binaryString
    String start;
    String end;

    public OneDimensionalEc(String start, String end) {
        this.start = start;
        this.end = end;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OneDimensionalEc that = (OneDimensionalEc) o;
        return Objects.equals(start, that.start) &&
                Objects.equals(end, that.end);
    }
    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
    @Override
    public String toString() {
        return  IpConvertion.binaryStrToIpString(start) + '\'' +
                IpConvertion.binaryStrToIpString(end) + '\'' ;
    }

}
