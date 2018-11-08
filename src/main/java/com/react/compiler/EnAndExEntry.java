package com.react.compiler;

public class EnAndExEntry {
	String srcDpid;
	String dstDpid;

	public EnAndExEntry(String src, String dst) {
		srcDpid = src;
		dstDpid = dst;
	}

	public String getSrcDpid() {
		return srcDpid;
	}

	public String getDstDpid() {
		return dstDpid;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dstDpid == null) ? 0 : dstDpid.hashCode());
		result = prime * result + ((srcDpid == null) ? 0 : srcDpid.hashCode());
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
		EnAndExEntry other = (EnAndExEntry) obj;
		if (dstDpid == null) {
			if (other.dstDpid != null)
				return false;
		} else if (!dstDpid.equals(other.dstDpid))
			return false;
		if (srcDpid == null) {
			if (other.srcDpid != null)
				return false;
		} else if (!srcDpid.equals(other.srcDpid))
			return false;
		return true;
	}

}
