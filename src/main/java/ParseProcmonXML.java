import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.print.attribute.standard.MediaSize.ISO;

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

import edu.zju.BasicClass.AnomalModelForOneApp;

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
			
			case "splitXML":
				if (args.length == 3){
					xmlFilePath = args[1];
					int limitedLineNumber = Integer.parseInt(args[2]);
					logger.info("\n==========starting===========\n");
					splitXML(xmlFilePath, limitedLineNumber);
					logger.info("\n==========ending===========\n");
				} else {
					logger.error("command format error");
					return;
				}
				break;
				
			case "statisticsUniqueCallstack":
				if (args.length == 4){
					xmlFilePath = args[1];
					int windowNumber = Integer.parseInt(args[2]);
					boolean isStopAtUserModuleAddress = Boolean.parseBoolean(args[3]);
					logger.info("\n==========starting===========\n");
					logger.info(String.format("method:%s\nxmlFilePath:%s\nwindowNumber:%s\nisStopAtUserModuleAddress:%s\n", "statisticsUniqueCallstack", xmlFilePath, windowNumber, isStopAtUserModuleAddress));
					statisticsUniqueCallstack(xmlFilePath, windowNumber, isStopAtUserModuleAddress);
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
	

	private static void statisticsUniqueCallstack(String xmlFilePath, int windowNumber, boolean isStopAtUserModuleAddress) throws DocumentException, IOException {
		// TODO Auto-generated method stub
		String processlistFilePath = xmlFilePath + "_processlist";
		
		String xmlFileDirPath = xmlFilePath.substring(0, xmlFilePath.lastIndexOf(File.separator));
		String xmlFileName = xmlFilePath.substring(xmlFilePath.lastIndexOf(File.separator)+1);
        File f = new File(xmlFileDirPath);
        
        // get all eventlist files
        FilenameFilter fileNameFilter = new FilenameFilter() {
  
           @Override
           public boolean accept(File dir, String name) {
              if (name.startsWith(xmlFileName + "_eventlist - "))
            	  return true;
              else 
            	  return false;
           }
        };
        // returns pathnames for files and directory
        File[] paths = f.listFiles(fileNameFilter);
        Arrays.sort(paths, new EventlistFileComparator());
        
        logger.info(new Gson().toJson(paths));
        
        Map<String, AnomalModelForOneApp>  app2TotalUniqueCallstacks = new HashMap<String, AnomalModelForOneApp>();
        Map<String, Map<String, List<Integer>>> app2UniqueCallstacksTracking = new HashMap<String, Map<String, List<Integer>>>();
        Map<String, List<AnomalModelForOneApp>> app2NewGeneratedCallstacks = new HashMap<String, List<AnomalModelForOneApp>>();
        // for each pathname in pathname array
        for(File path:paths)
        {
           // prints file and directory paths
           logger.info("starting processing " + path);           
           Document document = loadXML(path.getAbsolutePath());
           Map<String, AnomalModelForOneApp> app2AnomalMode = AnomalyDetection.extractAnomalyModel(document, isStopAtUserModuleAddress);
           for (String appName : app2AnomalMode.keySet()){
        	   logger.info("&&&&&&& " + appName + "&&&&&&&");
        	   AnomalModelForOneApp anomalModelForOneApp = app2AnomalMode.get(appName);        	         	  
        	   if (!app2TotalUniqueCallstacks.containsKey(appName))
        		   app2TotalUniqueCallstacks.put(appName, new AnomalModelForOneApp());
        	   AnomalModelForOneApp totalUniqueCallstacks = app2TotalUniqueCallstacks.get(appName);
        	   
        	   
        	   // new generated callstacks 	   
        	   if (!app2NewGeneratedCallstacks.containsKey(appName))
        		   app2NewGeneratedCallstacks.put(appName, new ArrayList<AnomalModelForOneApp>());
        	   AnomalModelForOneApp newGeneratedCallstacks = asymmetricDifferenceOfTwoHashMap(totalUniqueCallstacks, anomalModelForOneApp);
        	   app2NewGeneratedCallstacks.get(appName).add(newGeneratedCallstacks);
        	   
        	   // merge new generated callstacks per file       	   
        	   mergeTwoHashMap(totalUniqueCallstacks.operation2normalCallstacks, anomalModelForOneApp.operation2normalCallstacks);
        	   
        	   // statistics: the number of unique callstacks per file
        	   if (!app2UniqueCallstacksTracking.containsKey(appName))
        		   app2UniqueCallstacksTracking.put(appName, new HashMap<String, List<Integer>>());
        	   Map<String, List<Integer>> uniqueCallstacksTracking = app2UniqueCallstacksTracking.get(appName);
        	   for (String operation : totalUniqueCallstacks.operation2normalCallstacks.keySet()){
        		   System.out.println(operation + " : " + totalUniqueCallstacks.operation2normalCallstacks.get(operation).size());
        		   if (!uniqueCallstacksTracking.containsKey(operation))
        			   uniqueCallstacksTracking.put(operation, new ArrayList<>());
        		   uniqueCallstacksTracking.get(operation).add(totalUniqueCallstacks.operation2normalCallstacks.get(operation).size());
        	   }
        	   
           }
           
        }
        
        // write results to file
        String uniqueCallstacksNumberOutputFilePath = "uniqueCallstacksNumber.json";
        Utils.writeObjectToFileUsingJsonFormat(uniqueCallstacksNumberOutputFilePath, app2UniqueCallstacksTracking);
        
        String uniqueCallstacksDetailsOutputFilePath = "uniqueCallstacksDetail.json";
        Utils.writeObjectToFileUsingJsonFormat(uniqueCallstacksDetailsOutputFilePath, app2TotalUniqueCallstacks);
        
        String newGeneratedCallstacksOutputFilePath = "newGeneratedCallstacks.json";
        Utils.writeObjectToFileUsingJsonFormat(newGeneratedCallstacksOutputFilePath, app2NewGeneratedCallstacks);
	}
	
	/**
	 * merge mergee into merger
	 * @param merger
	 * @param mergee
	 */
	private static void mergeTwoHashMap(Map<String, Set<List<String>>> merger, Map<String, Set<List<String>>> mergee){
		for (String operation : mergee.keySet()){
			if (merger.containsKey(operation))
				merger.get(operation).addAll(mergee.get(operation));
			else
				merger.put(operation, mergee.get(operation));			
		}
	}
	
	/**
	 * asymmetric set difference of newMap From totalMap.
	 * @param totalMap
	 * @param newMap
	 * @return 
	 */
	private static AnomalModelForOneApp asymmetricDifferenceOfTwoHashMap(AnomalModelForOneApp totalAnomalModel, AnomalModelForOneApp newAnomalModel){
		Map<String, Set<List<String>>> newMap = newAnomalModel.operation2normalCallstacks;
		Map<String, Set<List<String>>> totalMap = totalAnomalModel.operation2normalCallstacks;
		AnomalModelForOneApp asymmetricAnomalModelForOneApp = new AnomalModelForOneApp();
		for (String operation : newMap.keySet()){
			Set<List<String>> asymmetricSet = new HashSet<List<String>>();
			if (totalMap.containsKey(operation)){				
				asymmetricSet.addAll(newMap.get(operation));
				asymmetricSet.removeAll(totalMap.get(operation));
				asymmetricAnomalModelForOneApp.operation2normalCallstacks.put(operation, asymmetricSet);
			}
			else{
				asymmetricSet.addAll(newMap.get(operation));
				asymmetricAnomalModelForOneApp.operation2normalCallstacks.put(operation, asymmetricSet);
			}
							
		}
		return asymmetricAnomalModelForOneApp;
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
	
	public static void splitXML(String xmlFilePath, int limitedLineNumber) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(xmlFilePath));
		int currentEventNumber = 0;
		StringBuilder eventlistOutputContent = new StringBuilder();
		StringBuilder processlistOutputContent = new StringBuilder();
		int splitIndex = 0;
		
		String line = reader.readLine(); // <?xml version="1.0" encoding="UTF-8"?>
		line = reader.readLine(); // <procmon><processlist><process>
		line = reader.readLine(); // <ProcessIndex>xxx</ProcessIndex>
		processlistOutputContent.append("<processlist><process>");
		while (line != null){					
			if (!line.startsWith("</processlist>")){
				processlistOutputContent.append(line).append("\n"); // adding process list
			} else {
				processlistOutputContent.append("</processlist>");
				BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(xmlFilePath + "_" + "processlist"));
				outputFileWriter.write(processlistOutputContent.toString());
				outputFileWriter.close();
				break; // end at </processlist><eventlist>
			}				
			line = reader.readLine();
		}
		
		
		line = reader.readLine(); // start at <event>
		while (line != null){
			eventlistOutputContent.append(line).append("\n");
			if (line.startsWith("</event>")){
				currentEventNumber++;
				if (currentEventNumber >= limitedLineNumber){	
					// write to a file
					eventlistOutputContent.insert(0, "<eventlist>");
					eventlistOutputContent.append("</eventlist>");
					BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(xmlFilePath + "_eventlist - " + splitIndex));
					splitIndex++;
					outputFileWriter.write(eventlistOutputContent.toString());
					outputFileWriter.close();
					
					// reset
					currentEventNumber = 0;
					eventlistOutputContent = new StringBuilder();
				}
			}
				
			line = reader.readLine();
		}
		reader.close();
		
		// rest
		eventlistOutputContent.delete(eventlistOutputContent.length() - "</eventlist></procmon>".length() - 1, eventlistOutputContent.length());
		eventlistOutputContent.insert(0, "<eventlist>");
		eventlistOutputContent.append("</eventlist>");
		if (eventlistOutputContent.length() != 0){
			BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(xmlFilePath + "_eventlist - " + splitIndex));
			splitIndex++;
			outputFileWriter.write(eventlistOutputContent.toString());
			outputFileWriter.close();
		}				
		
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

class EventlistFileComparator implements Comparator<File> {

	@Override
	public int compare(File file1, File file2) {
		// TODO Auto-generated method stub
		String filePath1 = file1.getPath();
		String filePath2 = file2.getPath();
		int fileIndex1 = Integer.parseInt(filePath1.substring(filePath1.lastIndexOf("eventlist - ") + "eventlist - ".length()));
		int fileIndex2 = Integer.parseInt(filePath2.substring(filePath2.lastIndexOf("eventlist - ") + "eventlist - ".length()));
//		System.out.println(fileIndex1 + " " + fileIndex2);
		return fileIndex1 - fileIndex2;
	}
}
