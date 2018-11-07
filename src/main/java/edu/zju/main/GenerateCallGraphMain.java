package edu.zju.main;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.StringComponentNameProvider;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import edu.zju.BasicClass.AnomalModelForOneApp;
import edu.zju.BasicClass.BasicOperation;

public class GenerateCallGraphMain {

	public static void main(String[] args) throws JsonIOException, JsonSyntaxException, FileNotFoundException {
		// TODO Auto-generated method stub
		String sigFilePath = "./outlook.anomalModel";
		String outputDirPath = "./";
		String command = "None";
		if (args.length >= 1){
			command = args[0];
			switch (command) {
			case "generateCallGraph":	
				if (args.length == 3){						
					sigFilePath = args[1];
					outputDirPath = args[2];
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
		
		switch (command){
		case "generateCallGraph":
			Map<String, AnomalModelForOneApp> existingApp2anomalModel = new Gson().fromJson(new FileReader(sigFilePath), new TypeToken<Map<String, AnomalModelForOneApp>>(){}.getType());
			generateCallGraphForServerlApps(existingApp2anomalModel, outputDirPath);
			
			break;
		
		default:
			System.out.println("No command");
			

	}
	
	}
	
	public static void generateCallGraphForServerlApps(Map<String, AnomalModelForOneApp> existingApp2anomalModel, String outputDirPath){
		Map<String, Map<String, Graph<String, DefaultEdge>>> app2operation2Graphs = new HashMap<String, Map<String, Graph<String, DefaultEdge>>>();
		for (String app : existingApp2anomalModel.keySet()){
			app2operation2Graphs.put(app, generateCallGraphForOneAnomalModel(existingApp2anomalModel.get(app)));
		}
		
		for (String app : app2operation2Graphs.keySet()){
			Map<String, Graph<String, DefaultEdge>> operation2Graphs = app2operation2Graphs.get(app);
			for (String operation : operation2Graphs.keySet()){
				Graph<String, DefaultEdge> graph = operation2Graphs.get(operation);
				String dotExePath = "F://Graphviz//bin//dot.exe";
				String appNameWithoutPath = app.substring(app.lastIndexOf("\\")+1, app.length());
				exportCallstackItemToDotGraph(graph, dotExePath, outputDirPath, appNameWithoutPath, operation, ".calltree_dot");
			}
		}
	}
	
	public static Map<String, Graph<String, DefaultEdge>> generateCallGraphForOneAnomalModel(AnomalModelForOneApp anomalModel){
		Map<String, Graph<String, DefaultEdge>> operation2Graphs = new HashMap<String, Graph<String, DefaultEdge>>();
		
		for (String operation : anomalModel.operation2normalCallstacks.keySet()){
			Set<List<String>> nomalCallstacksForOneOperation = anomalModel.operation2normalCallstacks.get(operation);
			operation2Graphs.put(operation, generateCallGraphForEachOperation(nomalCallstacksForOneOperation));
		}
		return operation2Graphs;
	}
	
	public static Graph<String, DefaultEdge> generateCallGraphForEachOperation(Set<List<String>> callstackSet){
		Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		for (List<String> callstack : callstackSet){
			String lastItem = null;
			for (String callstackItem : callstack){
				callstackItem = callstackItem.replace(" ", "_").replace("+", "").replace(".", "_").replace("~", "");
				if (lastItem != null){
					graph.addVertex(callstackItem);
					graph.addEdge(callstackItem, lastItem);
					lastItem = callstackItem;
				}else{
					graph.addVertex(callstackItem);
					lastItem = callstackItem;
				}
			}
		}
		
		return graph;
	}
	
	public static void exportCallstackItemToDotGraph(Graph<String, DefaultEdge> graph, String dotExePath, String traceDirPath, String appName, String opeartionName, String graphFileExtend){
		 DOTExporter<String, DefaultEdge> exporter = new DOTExporter<String, DefaultEdge>(new StringComponentNameProvider<String>(), null, null);
		 File outputDir = new File(traceDirPath);
		 if (!outputDir.exists())
			 outputDir.mkdirs();
		 String outputFilePath = outputDir + "\\" + appName + "_" + opeartionName + graphFileExtend;
		 File outputFile = new File(outputFilePath);
		 try (FileWriter fw = new FileWriter(outputFilePath))
		 {
			 exporter.exportGraph(graph, fw);
			 fw.flush();
			 String cmd = dotExePath + " -Tpdf \"" + outputFile.getAbsolutePath() + "\" -o \"" + outputFile.getAbsolutePath() + ".pdf\"";
	        Runtime run = Runtime.getRuntime();//返回与当前 Java 应用程序相关的运行时对象  
	        try {  
	            Process p = run.exec(cmd);// 启动另一个进程来执行命令  
	            BufferedInputStream in = new BufferedInputStream(p.getInputStream());  
	            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));  
	            String lineStr;  
	            while ((lineStr = inBr.readLine()) != null)  
	                //获得命令执行后在控制台的输出信息  
	                System.out.println(lineStr);// 打印输出信息  
	            //检查命令是否执行失败。  
	            if (p.waitFor() != 0) {  
	                if (p.exitValue() == 1)//p.exitValue()==0表示正常结束，1：非正常结束  
	                    System.err.println("命令执行失败!");  
	            }  
	            inBr.close();  
	            in.close();  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
		    
			 
		 }
		 catch (IOException e)
		 {
			 e.printStackTrace();
		 }
	}
}
