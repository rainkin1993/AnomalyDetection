package edu.zju.semantic;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

import com.google.gson.GsonBuilder;

import edu.zju.BasicClass.OperationWithCallstack;
import edu.zju.utils.CallstackUtils;
import edu.zju.utils.ProcmonXMLReader;

public class PathEventExtractor extends ProcmonXMLReader{
	
	List<String> keywords;
	String outputFilePath;
	
	private BufferedWriter outputFileWriter;
	private List<OperationWithCallstack> sigs = new ArrayList<OperationWithCallstack>();

	
	

	public PathEventExtractor(String xmlFilePath, List<String> keywords, String outputFilePath) {
		super(xmlFilePath);
		this.keywords = keywords;
		this.outputFilePath = outputFilePath;
	}

	@Override
	public void processProcess(Element processElement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processEvent(Element eventElement) {
		// TODO Auto-generated method stub		
			String operation = eventElement.selectSingleNode("Operation").getText();
			String path = eventElement.selectSingleNode("Path").getText();
			Element stack = (Element) eventElement.selectSingleNode("stack");			
			
			for(String keyword : keywords){
				if (path.contains(keyword)) {
					
					List<Node> stackFrameLocations = stack.selectNodes("frame/location");
					List<Node> stackFrameAddresses = stack.selectNodes("frame/address");
					List<String> userModeLocations = CallstackUtils.selectCallstacks(stackFrameLocations, stackFrameAddresses);
					
					OperationWithCallstack newSig = new OperationWithCallstack(operation, path, userModeLocations);
					sigs.add(newSig);
			}
			
		}
		

	}



	@Override
	public void processStart() {
		// TODO Auto-generated method stub
		try {
			BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(outputFilePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		List<OperationWithCallstack> sigs = new ArrayList<OperationWithCallstack>();

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
		try {
			outputFileWriter = new BufferedWriter(new FileWriter(outputFilePath));
			outputFileWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(sigs));
			outputFileWriter.flush();
			outputFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		
	}

}
