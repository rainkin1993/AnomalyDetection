package edu.zju.BasicClass.semantic;

public class SemanticProcess extends SemanticVertex {
	public Integer pid;
	public Integer tid;
	public Integer ppid;
	public String 	processName;
	public SemanticProcess(Integer pid, Integer tid, Integer ppid, String processName) {
		super();
		this.pid = pid;
		this.tid = tid;
		this.ppid = ppid;
		this.processName = processName;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pid == null) ? 0 : pid.hashCode());
		result = prime * result + ((ppid == null) ? 0 : ppid.hashCode());
		result = prime * result + ((processName == null) ? 0 : processName.hashCode());
		result = prime * result + ((tid == null) ? 0 : tid.hashCode());
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
		SemanticProcess other = (SemanticProcess) obj;
		if (pid == null) {
			if (other.pid != null)
				return false;
		} else if (!pid.equals(other.pid))
			return false;
		if (ppid == null) {
			if (other.ppid != null)
				return false;
		} else if (!ppid.equals(other.ppid))
			return false;
		if (processName == null) {
			if (other.processName != null)
				return false;
		} else if (!processName.equals(other.processName))
			return false;
		if (tid == null) {
			if (other.tid != null)
				return false;
		} else if (!tid.equals(other.tid))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "\"" + processName + "\"";
	}
	
	
	
}
