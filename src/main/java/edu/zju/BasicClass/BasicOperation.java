package edu.zju.BasicClass;

import java.util.List;

public class BasicOperation {
	private String operation;
	private List<String> userModeCallstack;
	private String path;
	private String detail;
	private String imagePath;
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public List<String> getUserModeCallstack() {
		return userModeCallstack;
	}
	public void setUserModeCallstack(List<String> userModeCallstack) {
		this.userModeCallstack = userModeCallstack;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	public BasicOperation(String operation, List<String> userModeCallstack, String path, String detail,
			String imagePath) {
		super();
		this.operation = operation;
		this.userModeCallstack = userModeCallstack;
		this.path = path;
		this.detail = detail;
		this.imagePath = imagePath;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((detail == null) ? 0 : detail.hashCode());
		result = prime * result + ((imagePath == null) ? 0 : imagePath.hashCode());
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((userModeCallstack == null) ? 0 : userModeCallstack.hashCode());
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
		BasicOperation other = (BasicOperation) obj;
		if (detail == null) {
			if (other.detail != null)
				return false;
		} else if (!detail.equals(other.detail))
			return false;
		if (imagePath == null) {
			if (other.imagePath != null)
				return false;
		} else if (!imagePath.equals(other.imagePath))
			return false;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (userModeCallstack == null) {
			if (other.userModeCallstack != null)
				return false;
		} else if (!userModeCallstack.equals(other.userModeCallstack))
			return false;
		return true;
	}
	
	
	
	
}
