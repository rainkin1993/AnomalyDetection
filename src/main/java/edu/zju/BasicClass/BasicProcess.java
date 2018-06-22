package edu.zju.BasicClass;

public class BasicProcess {
	private Integer processId;
	private Integer parentProcessId;
	private String 	processName;
	
	
	
	public BasicProcess(Integer processId, Integer parentProcessId, String processName) {
		super();
		this.processId = processId;
		this.parentProcessId = parentProcessId;
		this.processName = processName;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parentProcessId == null) ? 0 : parentProcessId.hashCode());
		result = prime * result + ((processId == null) ? 0 : processId.hashCode());
		result = prime * result + ((processName == null) ? 0 : processName.hashCode());
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
		BasicProcess other = (BasicProcess) obj;
		if (parentProcessId == null) {
			if (other.parentProcessId != null)
				return false;
		} else if (!parentProcessId.equals(other.parentProcessId))
			return false;
		if (processId == null) {
			if (other.processId != null)
				return false;
		} else if (!processId.equals(other.processId))
			return false;
		if (processName == null) {
			if (other.processName != null)
				return false;
		} else if (!processName.equals(other.processName))
			return false;
		return true;
	}






	public Integer getProcessId() {
		return processId;
	}
	public void setProcessId(Integer processId) {
		this.processId = processId;
	}
	public Integer getParentProcessId() {
		return parentProcessId;
	}
	public void setParentProcessId(Integer parentProcessId) {
		this.parentProcessId = parentProcessId;
	}
	public String getProcessName() {
		return processName;
	}
	public void setProcessName(String processName) {
		this.processName = processName;
	}
	
	
}
