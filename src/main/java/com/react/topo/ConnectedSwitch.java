package com.react.topo;

public class ConnectedSwitch {
    public int pid;
    public String sid;

    public ConnectedSwitch(int pid, String sid) {
        this.pid = pid;
        this.sid = sid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pid;
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
        ConnectedSwitch other = (ConnectedSwitch) obj;
        if (pid != other.pid)
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
        return " port " + pid + " connect with switch " + sid ;
    }
}
