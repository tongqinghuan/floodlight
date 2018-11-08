package com.react.topo;
import java.util.ArrayList;
import java.util.List;

public class Switch{
    public String sid;
    public List<Port> ports;
    public Switch(String sid,int max_port) {
        this.sid=sid;
        ports=new ArrayList<Port>();
        for(int i=0;i<max_port;i++) {
            ports.add(new Port(i+1,sid));
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ports == null) ? 0 : ports.hashCode());
        result = prime * result + ((sid == null) ? 0 : sid.hashCode());
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
        Switch other = (Switch) obj;
        if (ports == null) {
            if (other.ports != null)
                return false;
        } else if (!ports.equals(other.ports))
            return false;
        if (sid == null) {
            if (other.sid != null)
                return false;
        } else if (!sid.equals(other.sid))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Switch [sid=" + sid + ", ports=" + ports + "]";
    }
}
