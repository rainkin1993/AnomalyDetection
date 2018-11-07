package edu.zju.semantic;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.StringComponentNameProvider;
import org.omg.CORBA.PRIVATE_MEMBER;

import edu.zju.BasicClass.semantic.SemanticEdge;
import edu.zju.BasicClass.semantic.SemanticEvent;
import edu.zju.BasicClass.semantic.SemanticEventSig;
import edu.zju.BasicClass.semantic.SemanticFile;
import edu.zju.BasicClass.semantic.SemanticProcess;
import edu.zju.BasicClass.semantic.SemanticVertex;
import edu.zju.utils.CallstackUtils;
import edu.zju.utils.ProcmonXMLReader;
import edu.zju.utils.Utils;

public class SemanticSigMatcher extends ProcmonXMLReader {
	private Logger logger = Logger.getLogger(SemanticSigMatcher.class);

	private List<SemanticEvent> semanticEvents = new ArrayList<SemanticEvent>();
	private Map<SemanticProcess, List<SemanticEvent>> process2events = new HashMap<SemanticProcess, List<SemanticEvent>>();
	private Graph<SemanticVertex, SemanticEdge> graph = new DirectedPseudograph<SemanticVertex, SemanticEdge>(SemanticEdge.class);
	
	private List<SemanticEventSig> sigs;
	
	public SemanticSigMatcher(String xmlFilePath, List<SemanticEventSig> sigs) {
		super(xmlFilePath);
		this.sigs = sigs;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processProcess(Element processElement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processEvent(Element eventElement) {
		// TODO Auto-generated method stub
		
		// start matching
		for (SemanticEventSig sig : sigs){
			boolean isMatched = sig.matchEvent(eventElement);
			if (isMatched){
				processSemanticEvent(eventElement, sig.semantic); 
			}
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
	
	private void processSemanticEvent(Element eventElement, String semantic) {
		SemanticEvent semanticEvent = new SemanticEvent(eventElement, semantic);

//		logger.error(semanticEvent);
		// store all semantic events
		semanticEvents.add(semanticEvent);
		
		
		// add vertex and edge to the graph
		SemanticProcess process = new SemanticProcess(semanticEvent.pid, semanticEvent.tid, semanticEvent.ppid, semanticEvent.processName);
		SemanticVertex objectVertex = null;
		SemanticEdge edge = new SemanticEdge(semanticEvent.operation, semanticEvent.detail, semanticEvent.result, semanticEvent.time, semanticEvent.semantic);

		boolean isFromProcess2Object = true;
		switch (semanticEvent.operation) {
		case "WriteFile":
			isFromProcess2Object = true;
			objectVertex = new SemanticFile(Utils.convert2DotCompatibleString(semanticEvent.path));
			break;
			
		case "ReadFile":
			isFromProcess2Object = false;
			objectVertex = new SemanticFile(Utils.convert2DotCompatibleString(semanticEvent.path));
			break;
			
		case "SetRenameInformationFile":
			isFromProcess2Object = true;
			String renameNewFilePath = eventElement.selectSingleNode("Detail").getText();
			renameNewFilePath = renameNewFilePath.split("FileName: ")[1];
			objectVertex = new SemanticFile(Utils.convert2DotCompatibleString(renameNewFilePath));
			break;

		case "QueryBasicInformationFile":
			isFromProcess2Object = true;
			objectVertex = new SemanticFile(Utils.convert2DotCompatibleString(semanticEvent.path));
			break;
		
		case "CreateFile":
			isFromProcess2Object = true;
			objectVertex = new SemanticFile(Utils.convert2DotCompatibleString(semanticEvent.path));
			break;
			
		default:
			logger.error("unprocessed event");
			logger.error(semanticEvent);
			System.exit(1);
			break;
		}
		
		graph.addVertex(process);
		graph.addVertex(objectVertex);
		if (isFromProcess2Object)
			graph.addEdge(process, objectVertex, edge);
		else 
			graph.addEdge(objectVertex, process, edge);
		
		
		// split events by process
		if (!process2events.containsKey(process))
			process2events.put(process, new ArrayList<SemanticEvent>());
		process2events.get(process).add(semanticEvent);
		
		
	}
	
	public List<SemanticEvent> getSemanticEvents() {
		return semanticEvents;
	}
	
	public Map<SemanticProcess, List<SemanticEvent>> getSemanticEventsSplitedByProcess(){
		return process2events;
	}
	
	
	public void exportDotGraph(String outputFilePath, String dotExePath) throws IOException{

		DOTExporter<SemanticVertex, SemanticEdge> exporter = new DOTExporter<SemanticVertex, SemanticEdge>(new StringComponentNameProvider<SemanticVertex>(), null, new StringComponentNameProvider<SemanticEdge>());
		File outputFile = new File(outputFilePath);
		String dotOutputFilePath = outputFile.getParent() + File.separator + "Graph_" + outputFile.getName() + ".dot";
//		String dotOutputFilePath = outputFile.getName();
		String pdfOutputFilePath = outputFile.getParent() + File.separator + "Graph_" + outputFile.getName() + ".pdf";
		try (FileWriter fw = new FileWriter(dotOutputFilePath)) {
			exporter.exportGraph(graph, fw);
			fw.flush();
			
			String[] cmd = new String[]{dotExePath, "-v", "-Tpdf", dotOutputFilePath, "-o", pdfOutputFilePath};
			
			Runtime run = Runtime.getRuntime();// 返回与当前 Java 应用程序相关的运行时对象
			try {
				Process p = run.exec(cmd);// 启动另一个进程来执行命令
				
				BufferedInputStream in = new BufferedInputStream(p.getInputStream());
				BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
				String lineStr;
				while ((lineStr = inBr.readLine()) != null)
					// 获得命令执行后在控制台的输出信息
					logger.info(lineStr);// 打印输出信息
				// 检查命令是否执行失败。
				if (p.waitFor() != 0) {					
					int len;
					if ((len = p.getErrorStream().available()) > 0) {
					  byte[] buf = new byte[len]; 
					  p.getErrorStream().read(buf); 
					  logger.error("Command error:\t\""+new String(buf)+"\""); 
					}
				}
				else {
					logger.info("Successfully generate dot graph files");
				}
				inBr.close();
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}

		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
	
	}

}
