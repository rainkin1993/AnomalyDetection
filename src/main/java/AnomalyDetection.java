import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.zju.BasicClass.AnomalModelForOneApp;
import edu.zju.BasicClass.BasicOperation;
import edu.zju.BasicClass.BasicProcess;
import edu.zju.BasicClass.StatisticsForOneApp;

public class AnomalyDetection {

	public static void main(String[] args) throws DocumentException, IOException {
		// TODO Auto-generated method stub
		String xmlFilePath = "C:/Users/rainkin1993/Desktop/outlook_send_email_with_attachment_runqing_phf.py.XML";
		String sigFilePath = "./outlook.anomalModel";
		String detectionResultFilePath = "./result.txt";
		String command = "None";
		boolean isStopAtUserModuleAddress = false;
		if (args.length >= 1){
			command = args[0];
			switch (command) {
			case "extract":	
				if (args.length == 4){
					xmlFilePath = args[1];					
					sigFilePath = args[2];
					isStopAtUserModuleAddress = args[3].equals("true");
				} else {
					System.out.println("command format error");
					return;
				}
				
				break;
			case "match":
				if (args.length == 5){
					xmlFilePath = args[1];
					sigFilePath = args[2];
					detectionResultFilePath = args[3];
					isStopAtUserModuleAddress = args[4].equals("true");
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
				Map<String, AnomalModelForOneApp> extractedApp2anomalModel = extractAnomalyModel(document, isStopAtUserModuleAddress);
				BufferedWriter sigWriter = new BufferedWriter(new FileWriter(sigFilePath));
				
				sigWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(extractedApp2anomalModel));
				sigWriter.flush();
				sigWriter.close();
				
				break;
			case "match":
				Map<String, AnomalModelForOneApp> existingApp2anomalModel = new Gson().fromJson(new FileReader(sigFilePath), new TypeToken<Map<String, AnomalModelForOneApp>>(){}.getType());
				Map<String, Set<BasicOperation>> anomalOperations = matchAnomalModel(document, existingApp2anomalModel, isStopAtUserModuleAddress);
				
				BufferedWriter detectionResultwriter = new BufferedWriter(new FileWriter(detectionResultFilePath));
				detectionResultwriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(anomalOperations));
				detectionResultwriter.flush();
				detectionResultwriter.close();
				
				break;
			
			default:
				System.out.println("No command");

		}
	}

	
	private static Map<String, Set<BasicOperation>> matchAnomalModel(Document document, Map<String, AnomalModelForOneApp> app2anomalModel, boolean isStopAtUserModuleAddress) {
		Set<BasicOperation> anomalOperations = new HashSet<BasicOperation>();
		Map<String, Set<BasicOperation>> app2AnomalOperations = new HashMap<String, Set<BasicOperation>>();
		
		Element root = document.getRootElement();
		Element processList = root.element("processlist");
		Element eventlist = root.element("eventlist");		
		
		// processing processes
		Map<Integer, BasicProcess> pid2Process = new HashMap<Integer, BasicProcess>();		
		for (Iterator<Element> it = processList.elementIterator(); it.hasNext();) {
			Element event = it.next();
			int processId = new Integer(event.selectSingleNode("ProcessId").getText());
			int parentProcessId = new Integer(event.selectSingleNode("ParentProcessId").getText());
			String processName = event.selectSingleNode("ProcessName").getText();
			
			BasicProcess process = new BasicProcess(processId, parentProcessId, processName);
			pid2Process.put(processId, process);			
		}
		
		// processing events
		int i = 0;
		System.out.println(eventlist.selectNodes("event").size());
		for (Iterator<Element> it = eventlist.elementIterator(); it.hasNext();) {
			i++;
			Element event = it.next();
			int pid = new Integer(event.selectSingleNode("PID").getText());
			int tid = new Integer(event.selectSingleNode("TID").getText());
			String imagePath = event.selectSingleNode("Image_Path").getText();
//			String imagePath = event.selectSingleNode("Process_Name").getText();		
			String operation = event.selectSingleNode("Operation").getText();
			String path = event.selectSingleNode("Path").getText();
			String detail = event.selectSingleNode("Detail").getText();
			Element stack = (Element) event.selectSingleNode("stack");
//			System.out.println(i);
//			System.out.println(path);
			
			// add nomal operations with callstacks to the model
			if (!app2anomalModel.containsKey(imagePath)){
				// doesn't train a model for this app
				continue;
			}
			AnomalModelForOneApp anomalModel = app2anomalModel.get(imagePath);
			
			
			if (!app2AnomalOperations.containsKey(imagePath))
				app2AnomalOperations.put(imagePath, new HashSet<BasicOperation>());
			
					
			List<String> userModeLocations = selectCallstacks(stack, isStopAtUserModuleAddress);
			BasicOperation basicOperation = new BasicOperation(operation, userModeLocations, path, detail, imagePath);
			
			
			// check whether the operation is anomal or not
			if (anomalModel.isAnomal(operation, userModeLocations)){
				anomalOperations.add(basicOperation);
				app2AnomalOperations.get(imagePath).add(basicOperation);
			} 

		}
		return app2AnomalOperations;
	}


	public static Map<String, AnomalModelForOneApp> extractAnomalyModel(Document document, boolean isStopAtUserModuleAddress) {
		Map<String, AnomalModelForOneApp> app2anomalModel = new HashMap<String, AnomalModelForOneApp>();		
		Map<String, StatisticsForOneApp> app2Statistics = new HashMap<String, StatisticsForOneApp>();
		
//		Element root = document.getRootElement();
//		Element processList = root.element("processlist");
//		Element eventlist = root.element("eventlist");		
		Element eventlist = document.getRootElement();
				
		// processing processes
//		Map<Integer, BasicProcess> pid2Process = new HashMap<Integer, BasicProcess>();		
//		for (Iterator<Element> it = processList.elementIterator(); it.hasNext();) {
//			Element event = it.next();
//			int processId = new Integer(event.selectSingleNode("ProcessId").getText());
//			int parentProcessId = new Integer(event.selectSingleNode("ParentProcessId").getText());
//			String processName = event.selectSingleNode("ProcessName").getText();
//			
//			BasicProcess process = new BasicProcess(processId, parentProcessId, processName);
//			pid2Process.put(processId, process);			
//		}
		
		// processing events
		for (Iterator<Element> it = eventlist.elementIterator(); it.hasNext();) {
			Element event = it.next();
			int pid = new Integer(event.selectSingleNode("PID").getText());
//			int tid = new Integer(event.selectSingleNode("TID").getText());
			String imagePath = event.selectSingleNode("Image_Path").getText();
//			String imagePath = event.selectSingleNode("Process_Name").getText();
			String operation = event.selectSingleNode("Operation").getText();
			String path = event.selectSingleNode("Path").getText();
			String detail = event.selectSingleNode("Detail").getText();
			Element stack = (Element) event.selectSingleNode("stack");			
			
			// add nomal operations with callstacks to the model
			if (!app2anomalModel.containsKey(imagePath)){
				app2anomalModel.put(imagePath, new AnomalModelForOneApp());
			}
			AnomalModelForOneApp anomalModel = app2anomalModel.get(imagePath);
			
			if (!app2Statistics.containsKey(imagePath)){
				app2Statistics.put(imagePath, new StatisticsForOneApp());
			}
			StatisticsForOneApp statisticsForOneApp = app2Statistics.get(imagePath);
			
			List<String> userModeLocations = selectCallstacks(stack, isStopAtUserModuleAddress);
						
			anomalModel.addNormalOperationWithCallstack(operation, userModeLocations);			
			statisticsForOneApp.addNewPathForCallstack(path, userModeLocations);
		}
		
		try {
			BufferedWriter detectionResultwriter = new BufferedWriter(new FileWriter("statistics.txt"));
			detectionResultwriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(app2Statistics));
			detectionResultwriter.flush();
			detectionResultwriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return app2anomalModel;
	}


	private static List<String> selectCallstacks(Node stack, boolean isStopAtUserModuleAddress) {
		// extract user mode callstack
		List<String> userModeLocations = new ArrayList<String>();
		List<Node> frames = stack.selectNodes("frame");
		for (int index = 0; index < frames.size(); index++){
			Node frame = frames.get(index);
			Node locationNode = frame.selectSingleNode("location");
			String address = frame.selectSingleNode("address").getText();
			
			if (!address.startsWith("0xffff")){
				Node dllPathNode = frame.selectSingleNode("path");
//				System.out.println(locationNode);
				if (locationNode == null || dllPathNode == null){
					userModeLocations.add(address.toLowerCase());
				} else {
					userModeLocations.add(locationNode.getText().toLowerCase());
					if (isStopAtUserModuleAddress){ 
						String lowcaseDllPath = dllPathNode.getText().toLowerCase();
						if (lowcaseDllPath.contains("sogou"))
							break;
						if (!lowcaseDllPath.contains("\\windows\\system32") 
								&& !lowcaseDllPath.contains("\\windows\\syswow64")
								&& !lowcaseDllPath.contains("\\windows\\winsxs")){
							break;
						} 
					}
				}										
			}
		}
		
		return userModeLocations;
//		List<Node> stackFrameLocations = stack.selectNodes("frame/location");
//		List<Node> stackFrameAddresses = stack.selectNodes("frame/address");
//		List<Node> stackFramePath = stack.selectNodes("frame/path");
//		List<String> userModeLocations = new ArrayList<String>();
//		for (int index = 0; index < stackFrameLocations.size(); index++){
//			String location = stackFrameLocations.get(index).getText();
//			String address = stackFrameAddresses.get(index).getText();
//			if (!address.startsWith("0xffff")){
//				String dllPath = stackFramePath.get(index).getText().toLowerCase();	
//				System.out.println(stackFrameLocations);
//				if (location == null){
//					userModeLocations.add(address);
//				} else {
//					userModeLocations.add(location);
//					if (isStopAtUserModuleAddress){ 
//						if (!dllPath.contains("\\windows\\system32") 
//								&& !dllPath.contains("\\windows\\syswow64")
//								&& !dllPath.contains("\\windows\\winsxs")){
//							break;
//						} 
//					}
//				}										
//			}
//		}
	}
}
