package edu.zju.semantic.command;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.google.gson.GsonBuilder;

import edu.zju.BasicClass.semantic.SemanticEvent;
import edu.zju.BasicClass.semantic.SemanticEventSig;
import edu.zju.BasicClass.semantic.SemanticProcess;
import edu.zju.semantic.SemanticSigMatcher;
import edu.zju.utils.Utils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class Matcher implements Callable<Void>{
	Logger logger = Logger.getLogger(Matcher.class);
	
	@Option(names = "-o", required = true, description = "output file path")
	File outputFilePath;
	
	@Option(names = "-i_files", description = "input xml file path")
	List<File> inputFiles;
	
	@Option(names = "-sigFilePath", description = "sig file path")
	List<File> sigFiles;
	
	@Option(names = "-g", description = "path of dot executable file, which mean will generate the graph")
	String dotExePath;
	

	@Override
	public Void call() throws Exception {
		// TODO Auto-generated method stub
		
		// load sigs
		List<SemanticEventSig> sigs = Utils.loadSemanticEventSigs(sigFiles);
		
		// matching
		if (inputFiles != null){
			for (File file : inputFiles){
				SemanticSigMatcher matcher = new SemanticSigMatcher(file.getAbsolutePath(), sigs);
				matcher.run();
				
				// generate graph 
				if (dotExePath != null){
					matcher.exportDotGraph(this.outputFilePath.getAbsolutePath(), dotExePath);					
				}
				
				// semantic events splited by process
				Map<SemanticProcess, List<SemanticEvent>> process2events = matcher.getSemanticEventsSplitedByProcess();				
				Utils.writeObjectToFileUsingJsonFormat(outputFilePath + ".semanticEventsSplitedByProcess", process2events);
				
			}
		}
		return null;
	}
	

}
