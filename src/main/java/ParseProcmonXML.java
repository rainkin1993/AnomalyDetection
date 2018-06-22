import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ParseProcmonXML {
	

	public static void main(String[] args) throws DocumentException, IOException {
		String xmlFilePath = "C:/Users/rainkin1993/Desktop/Logfile1.XML";
		String fileKeyword = "None";
		String sigFilePath = "./callstackSig.txt";
		String command = "None";
		if (args.length >= 1){
			command = args[0];
			switch (command) {
			case "extract":	
				if (args.length == 4){
					xmlFilePath = args[1];
					fileKeyword  = args[2];
					sigFilePath = args[3];
				} else {
					System.out.println("command format error");
					return;
				}
				
				break;
			case "match":
				if (args.length == 3){
					xmlFilePath = args[1];
					sigFilePath = args[2];
				} else {
					System.out.println("command format error");
					return;
				}
				
				break;
				
			default:
				System.out.println("command error");
				return;
				
			}
			
			
		}		
		
		// load xml file
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		System.out.println("Current Date: " + ft.format(dNow));

		File inputXmlFile = new File(xmlFilePath);
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(inputXmlFile);

		dNow = new Date();
		ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		System.out.println("Current Date: " + ft.format(dNow));
		
		
		switch (command){
			case "extract":
				extractCallstackForFile(document, fileKeyword, sigFilePath);
				break;
			case "match":
				matchCallstackSigs(document, xmlFilePath, sigFilePath);
				break;
			
			default:
				System.out.println("No command");

		}
		
		
		
		
		
		
	}



	public static void treeWalk(Element element) {
		for (int i = 0, size = element.nodeCount(); i < size; i++) {
			Node node = element.node(i);
			if (node instanceof Element) {
				System.out.println(node);
				treeWalk((Element) node);
			} else {
				// do something��
				// System.out.println(node);
			}
		}
	}
	
	
	private static void matchCallstackSigs(Document document, String xmlFilePath, String sigFilePath) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		// load sigs
		List<OperationWithCallstack> sigs = new Gson().fromJson(new FileReader(sigFilePath), new TypeToken<List<OperationWithCallstack>>(){}.getType());
		
		// start matching
		List<OperationWithCallstack> matchedResult = new ArrayList<OperationWithCallstack>();
		Element root = document.getRootElement();
		Element processList = root.element("processlist");
		Element eventlist = root.element("eventlist");
		
		for (Iterator<Element> it = eventlist.elementIterator(); it.hasNext();) {
			Element event = it.next();
			String operation = event.selectSingleNode("Operation").getText();
			String path = event.selectSingleNode("Path").getText();
			Element stack = (Element) event.selectSingleNode("stack");

			List<Node> stackFrameLocations = stack.selectNodes("frame/location");
			List<Node> stackFrameAddresses = stack.selectNodes("frame/address");
			List<String> userModeLocations = new ArrayList<String>();
			for (int index = 0; index < stackFrameLocations.size(); index++){
				String location = stackFrameLocations.get(index).getText();
				String address = stackFrameAddresses.get(index).getText();
				if (!address.startsWith("0xffff")){
					userModeLocations.add(location);
				}
				
			}

			for (OperationWithCallstack sig : sigs){
				if (sig.stack.equals(userModeLocations)) {
					OperationWithCallstack newSig = new OperationWithCallstack(operation, path, userModeLocations);
					matchedResult.add(newSig);
				}
			}
		}
		
		System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(matchedResult));
			
	}
	

	
	public static void extractCallstackForFile(Document document, String fileKeyword, String outputFilePath) throws DocumentException, IOException{
		
		BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(outputFilePath));
		List<OperationWithCallstack> sigs = new ArrayList<OperationWithCallstack>();
		
		

		Element root = document.getRootElement();
		Element processList = root.element("processlist");
		Element eventlist = root.element("eventlist");
		
		for (Iterator<Element> it = eventlist.elementIterator(); it.hasNext();) {
			Element event = it.next();
			String operation = event.selectSingleNode("Operation").getText();
			String path = event.selectSingleNode("Path").getText();
			Element stack = (Element) event.selectSingleNode("stack");			
			
			if (path.contains(fileKeyword)) {
				
				List<Node> stackFrameLocations = stack.selectNodes("frame/location");
				List<Node> stackFrameAddresses = stack.selectNodes("frame/address");
				List<String> userModeLocations = new ArrayList<String>();
				for (int index = 0; index < stackFrameLocations.size(); index++){
					String location = stackFrameLocations.get(index).getText();
					String address = stackFrameAddresses.get(index).getText();
					if (!address.startsWith("0xffff")){
						userModeLocations.add(location);
					}
					
				}
				
				OperationWithCallstack newSig = new OperationWithCallstack(operation, path, userModeLocations);
				sigs.add(newSig);
			}

		}
		
		outputFileWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(sigs));
		outputFileWriter.flush();
		outputFileWriter.close();
		
	}
}

class OperationWithCallstack{
	public String operation;
	public String path;
	public List<String> stack;
	
	public OperationWithCallstack(String operation, String path, List<String> stack) {
		super();
		this.operation = operation;
		this.path = path;
		this.stack = stack;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operation == null) ? 0 : operation.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((stack == null) ? 0 : stack.hashCode());
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
		OperationWithCallstack other = (OperationWithCallstack) obj;
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
		if (stack == null) {
			if (other.stack != null)
				return false;
		} else if (!stack.equals(other.stack))
			return false;
		return true;
	}
	
	
	
}