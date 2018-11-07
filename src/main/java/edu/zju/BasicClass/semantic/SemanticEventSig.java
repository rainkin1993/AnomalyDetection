package edu.zju.BasicClass.semantic;

import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

import edu.zju.utils.CallstackUtils;

/**
 * Sig for semantic event
 * @author rainkin
 *
 */
public class SemanticEventSig {

	public String semantic;
	public List<String> callstacks;
	
	public SemanticEventSig(String semantic, List<String> callstacks) {
		super();
		this.semantic = semantic;
		this.callstacks = callstacks;
	}
	
	
	/**
	 * judge whether the sig match the event
	 * Note that event is matched has two conditions: callstack is matched and event is normal
	 * Event is normal means that some events are useless while the callstack can be matched with sig. 
	 * Such as the ReadFile operation whose result is END OF FILE is meaningless.
	 * @param eventElement
	 * @return
	 */
	public boolean matchEvent(Element eventElement){
		List<Node> stackFrameLocations = eventElement.selectNodes("stack/frame/location");
		List<Node> stackFrameAddresses = eventElement.selectNodes("stack/frame/address");
		List<String> callstacks = CallstackUtils.selectCallstacks(stackFrameLocations, stackFrameAddresses);
		
		boolean isCallstackMatched = this.callstacks.equals(callstacks);
		boolean isNormal = true;
		
		String operation = eventElement.selectSingleNode("Operation").getText();
		String result = eventElement.selectSingleNode("Result").getText();
		switch (operation) {
		case "ReadFile":
			if (result.equals("END OF FILE"))
				isNormal = false;
			break;

		default:
			break;
		}
		
		return isCallstackMatched && isNormal;
		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((callstacks == null) ? 0 : callstacks.hashCode());
		result = prime * result + ((semantic == null) ? 0 : semantic.hashCode());
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
		SemanticEventSig other = (SemanticEventSig) obj;
		if (callstacks == null) {
			if (other.callstacks != null)
				return false;
		} else if (!callstacks.equals(other.callstacks))
			return false;
		if (semantic == null) {
			if (other.semantic != null)
				return false;
		} else if (!semantic.equals(other.semantic))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SemanticEventSig [semantic=" + semantic + ", callstacks=" + callstacks + "]";
	}
	
	
	
	
}
