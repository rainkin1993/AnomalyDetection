package edu.zju.semantic;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.dom4j.Element;
import org.dom4j.Node;

import edu.zju.BasicClass.semantic.ProcmonEventIdentifier;
import edu.zju.BasicClass.semantic.SemanticEventSig;
import edu.zju.utils.CallstackUtils;
import edu.zju.utils.ProcmonXMLReader;

public class IdentifierEventExtractor extends ProcmonXMLReader {

	List<ProcmonEventIdentifier> identifiers;
	private List<SemanticEventSig> sigs = new ArrayList<SemanticEventSig>();
	
	
	public IdentifierEventExtractor(String xmlFilePath, List<ProcmonEventIdentifier> identifiers) {
		super(xmlFilePath);
		this.identifiers = identifiers;
	}

	@Override
	public void processProcess(Element processElement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processEvent(Element eventElement) {
		// TODO Auto-generated method stub
		boolean isMatched = false;
		String semantic = "";
		for (ProcmonEventIdentifier identifier : identifiers){
			isMatched = identifier.matchEventElement(eventElement);			
			if (isMatched){
				semantic = identifier.semantic;
				break;
			}
				
		}
		
		if (isMatched){
			List<Node> stackFrameLocations = eventElement.selectNodes("stack/frame/location");
			List<Node> stackFrameAddresses = eventElement.selectNodes("stack/frame/address");
			List<String> callstacks = CallstackUtils.selectCallstacks(stackFrameLocations, stackFrameAddresses);
			SemanticEventSig sig = new SemanticEventSig(semantic, callstacks);
			sigs.add(sig);
		}
		
		
	}

	@Override
	public void processStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventEnd() {
		// TODO Auto-generated method stub
		
	}
	
	public List<SemanticEventSig> getSigs(){
		return sigs;
	}

}
