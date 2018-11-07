package edu.zju.semantic.command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.google.gson.GsonBuilder;

import edu.zju.BasicClass.semantic.ProcmonEventIdentifier;
import edu.zju.BasicClass.semantic.SemanticEventSig;
import edu.zju.semantic.IdentifierEventExtractor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public class Transformer implements Callable<Void>{
	Logger logger = Logger.getLogger(Transformer.class);
	
	@Option(names = "-o", required = true, description = "output file path")
	String outputFilePath;
	
	@Option(names = "-i", required = true, description = "directory path of procmon txt files ")
	String inputSigDirPath;
	
	@Option(names = "-sigFileExtension", description = "file extension of sig file")
	String sigFileExtension = ".semanticEventSig";
//	@Option(names = "-i_trace", required = true, description = "trace ")
//	String inputTFilePath;
	

	private List<ProcmonEventIdentifier> getEventIdentifiers() throws IOException {
		// parse raw sig file to get the time and duration field
				List<ProcmonEventIdentifier> identifiers = new ArrayList<ProcmonEventIdentifier>();
				File inputDir = new File(inputSigDirPath);
				File[] sigFiles = inputDir.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						// TODO Auto-generated method stub
						return name.toLowerCase().endsWith(".txt");
					}
				});
				for (File sigFile : sigFiles){
					String time = "";
					String duration = "";
					
					BufferedReader reader = new BufferedReader(new FileReader(sigFile));
					String line = reader.readLine();
					while (line != null){
						if (line.startsWith("High Resolution Date")){
							String[] tmpSplit = line.split(" ");
							time = tmpSplit[tmpSplit.length-2] + " " + tmpSplit[tmpSplit.length-1];
						} else if (line.startsWith("Duration")){
							duration = line.split("\t")[1];
						}
							
						line = reader.readLine();				
					}			
					
					String[] tmpSplit = sigFile.getName().split("_\\$_");
					String semanticOperation = tmpSplit[0] + "_$_" + tmpSplit[1];					
					identifiers.add(new ProcmonEventIdentifier(time, duration, semanticOperation));
				}
				
				return identifiers;
	}
	@Override
	public Void call() throws Exception {
		// TODO Auto-generated method stub
		
		this.outputFilePath = this.outputFilePath + this.sigFileExtension;
		
		// get identifiers for sig
		List<ProcmonEventIdentifier> identifiers = getEventIdentifiers();
		
		// get trace file whiose extensio is xml
		File inputDir = new File(inputSigDirPath);
		File[] inputTraceFiles = inputDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return name.toLowerCase().endsWith(".xml");
			}
		});
		
		// get events which match two fields from trace 
		Set<SemanticEventSig> sigs = new HashSet<SemanticEventSig>();
		for (File traceFile : inputTraceFiles){
			IdentifierEventExtractor extractor = new IdentifierEventExtractor(traceFile.getAbsolutePath(), identifiers);
			extractor.run();
			sigs.addAll(extractor.getSigs());
		}
		
		BufferedWriter outputFileWriter = new BufferedWriter(new FileWriter(new File(this.outputFilePath)));
		outputFileWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(sigs));
		outputFileWriter.close();
		
		logger.info("Successfully generat sigs: " + this.outputFilePath);
		
		
		
		return null;
	}
}