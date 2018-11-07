package edu.zju.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.gson.Gson;

import edu.zju.semantic.command.Extractor;
import edu.zju.semantic.command.Matcher;
import edu.zju.semantic.command.Transformer;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command
public class SemanticAwareEventMain implements Callable<Void>{
	
	Logger logger = Logger.getLogger(SemanticAwareEventMain.class);

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PropertyConfigurator.configure("log/log4j.properties");
		
		// Set up the parser
	    CommandLine commandLine = new CommandLine(new SemanticAwareEventMain());

	    // add subcommands 
	    commandLine.addSubcommand("extract",   new Extractor());
	    commandLine.addSubcommand("transform", new Transformer());
	    commandLine.addSubcommand("match", new Matcher());
	    // Invoke the parse method to parse the arguments
	    List<Object> results = commandLine.parseWithHandler(new CommandLine.RunAll(), args);
	}


	public Void call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}