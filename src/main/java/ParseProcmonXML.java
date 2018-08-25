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

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
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
	static Logger logger = Logger.getLogger(ParseProcmonXML.class); 

	public static void main(String[] args) throws DocumentException, IOException {		
		PropertyConfigurator.configure("log/log4j.properties");
		
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
					Document document = loadXML(xmlFilePath);
					extractCallstackForFile(document, fileKeyword, sigFilePath);
				} else {
					logger.error("command format error");
					return;
				}
				
				break;
				
			case "match":
				if (args.length == 3){
					xmlFilePath = args[1];
					sigFilePath = args[2];
					Document document = loadXML(xmlFilePath);
					matchCallstackSigs(document, xmlFilePath, sigFilePath);
				} else {
					logger.error("command format error");
					return;
				}
				
				break;
				
			case "convertXML2SimplifedFormat":
				if (args.length == 3){
					xmlFilePath = args[1];
					String outputFilePath = args[2];
					logger.info("\n==========starting===========\n");
					Document document = loadXML(xmlFilePath);
					convertProcmonXML2SimplifiedFormat(document, outputFilePath);
					logger.info("\n==========ending===========\n");
				} else {
					logger.error("command format error");
					return;
				}
				
				break;
				
			default:
				logger.error("command error");
				return;
				
			}
			
			
		} else {
			logger.error("command error");
			return;
		}		
	}

	private static Document loadXML(String xmlFilePath) throws DocumentException{
		logger.info("load xml : " + xmlFilePath);
		
		// load xml file
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		logger.info("Starting Current Date: " + ft.format(dNow));

		File inputXmlFile = new File(xmlFilePath);
		SAXReader saxReader = new SAXReader();
		Document document = saxReader.read(inputXmlFile);

		dNow = new Date();
		ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		logger.info("Ending Current Date: " + ft.format(dNow));
		return document;
	}


	public static void treeWalk(Element element) {
		for (int i = 0, size = element.nodeCount(); i < size; i++) {
			Node node = element.node(i);
			if (node instanceof Element) {
				System.out.println(node);
				treeWalk((Element) node);
			} else {
				// do something¡­
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
	
	public static void convertProcmonXML2SimplifiedFormat(Document document, String outputFilePath) throws DocumentException, IOException{
		
		BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(outputFilePath));
		List<SimplifiedEvent> simplifiedEvents = new ArrayList<SimplifiedEvent>();
		
		
		Element root = document.getRootElement();
		Element processList = root.element("processlist");
		Element eventlist = root.element("eventlist");
		
		for (Iterator<Element> it = eventlist.elementIterator(); it.hasNext();) {
			Element event = it.next();
			String process_name = event.selectSingleNode("Process_Name").getText();
			String operation = event.selectSingleNode("Operation").getText();
			String path = event.selectSingleNode("Path").getText();
			Element stack = (Element) event.selectSingleNode("stack");			
			
				
			List<Node> stackFrameLocations = stack.selectNodes("frame/location");
			List<Node> stackFrameAddresses = stack.selectNodes("frame/address");
			List<String> userModeLocations = new ArrayList<String>();
			for (int index = 0; index < stackFrameLocations.size(); index++){
				String location = stackFrameLocations.get(index).getText();
				String address = stackFrameAddresses.get(index).getText();
				if (!address.startsWith("0xf")){
					userModeLocations.add(location);
				}								
			}
			SimplifiedEvent newEvent = new SimplifiedEvent(process_name, operation, path, userModeLocations);
			simplifiedEvents.add(newEvent);		
		}
		
		outputFileWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(simplifiedEvents));
		outputFileWriter.flush();
		outputFileWriter.close();
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

class SimplifiedEvent{
	public String process_name;
	public String operation;
	public String path;
	public List<String> stack;
	
	public SimplifiedEvent(String process_name, String operation, String path, List<String> stack) {
		super();
		this.process_name = process_name;
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
		result = prime * result + ((process_name == null) ? 0 : process_name.hashCode());
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
		SimplifiedEvent other = (SimplifiedEvent) obj;
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
		if (process_name == null) {
			if (other.process_name != null)
				return false;
		} else if (!process_name.equals(other.process_name))
			return false;
		if (stack == null) {
			if (other.stack != null)
				return false;
		} else if (!stack.equals(other.stack))
			return false;
		return true;
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
