package edu.zju.BasicClass.semantic;

import org.dom4j.Element;

public class ProcmonEventIdentifier {
	String time;
	String duration;
	
	public String semantic;

	public ProcmonEventIdentifier(String time, String duration, String semantic) {
		super();
		this.time = time;
		this.duration = duration;
		this.semantic = semantic;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
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
		ProcmonEventIdentifier other = (ProcmonEventIdentifier) obj;
		if (duration == null) {
			if (other.duration != null)
				return false;
		} else if (!duration.equals(other.duration))
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

	public boolean matchEventElement(Element eventElement) {
		// TODO Auto-generated method stub
		String eventTime = eventElement.selectSingleNode("Time_of_Day").getText();
		String eventDuration = eventElement.selectSingleNode("Duration").getText();
		if (time.equals(eventTime) && duration.equals(eventDuration))
			return true;
		else
			return false;
	}
	
	
	
	
}
