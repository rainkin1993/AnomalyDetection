package edu.zju.BasicClass.semantic;

public class SemanticEdge {
	public String operation;
	public String detail;
	public String result;
	public String time;
	
	public String semantic;

	public SemanticEdge(String operation, String detail, String result, String time, String semantic) {
		super();
		this.operation = operation;
		this.detail = detail;
		this.result = result;
		this.time = time;
		this.semantic = semantic;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((detail == null) ? 0 : detail.hashCode());
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + ((semantic == null) ? 0 : semantic.hashCode());
		result = prime * result + ((time == null) ? 0 : time.hashCode());
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
		SemanticEdge other = (SemanticEdge) obj;
		if (detail == null) {
			if (other.detail != null)
				return false;
		} else if (!detail.equals(other.detail))
			return false;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		if (semantic == null) {
			if (other.semantic != null)
				return false;
		} else if (!semantic.equals(other.semantic))
			return false;
		if (time == null) {
			if (other.time != null)
				return false;
		} else if (!time.equals(other.time))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return semantic;
	}
	
	
	
	
}
