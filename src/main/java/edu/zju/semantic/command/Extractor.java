package edu.zju.semantic.command;

import java.util.List;
import java.util.concurrent.Callable;

import edu.zju.semantic.PathEventExtractor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class Extractor implements Callable<Void>{

	@Option(names = "-k", required = true, description = "KeyWord to match event details")
    List<String> keywords;
	
	@Option(names = "-o", required = true, description = "output file path")
	String outputFilePath;
	
	@Option(names = "-i", required = true, description = "input xml file path")
	String inputFilePath;
	
	public Void call() throws Exception {
		// TODO Auto-generated method stub
		PathEventExtractor extractor = new PathEventExtractor(inputFilePath, keywords, outputFilePath);
		extractor.run();
		
		return null;
	}
	
}