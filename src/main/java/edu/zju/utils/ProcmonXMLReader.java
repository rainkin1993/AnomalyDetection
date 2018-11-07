package edu.zju.utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;


import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public abstract class ProcmonXMLReader {
	static Logger logger = Logger.getLogger(ProcmonXMLReader.class); 
	private String xmlFilePath;
	
	


	public ProcmonXMLReader(String xmlFilePath) {
		super();
		this.xmlFilePath = xmlFilePath;
	}

	public void run() throws DocumentException, IOException{
		
		// if the xml file has not been splited, split it
		if (!isProcmonXMLSplited(xmlFilePath))
			splitXML(xmlFilePath, 100000);
		
		// get the processlist file
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
        File[] eventListPaths = f.listFiles(fileNameFilter);
        Arrays.sort(eventListPaths, new EventlistFileComparator());
        
        // start reading processlist
        logger.info("starting processing " + processlistFilePath);    
        processStart();
        Document proceseListDocument = loadXML(processlistFilePath);
        Iterator<Element> processListIter = proceseListDocument.getRootElement().elementIterator();
        while(processListIter.hasNext()){        	   
        	Element processElement = processListIter.next();
        	processProcess(processElement);
        }
        processEnd();
        
        // start reading eventlist
        eventStart();
        for(File eventlistFile : eventListPaths){
        	logger.info("starting processing " + eventlistFile);           
            Document eventListDocument = ProcmonXMLReader.loadXML(eventlistFile.getAbsolutePath());
            Iterator<Element> eventListIter = eventListDocument.getRootElement().elementIterator();
            while(eventListIter.hasNext()){
            	Element eventElement = eventListIter.next();
            	processEvent(eventElement);
            }
        }
        eventEnd();
        
        
        
	}
	
	public abstract void processProcess(Element processElement);
	public abstract void processEvent(Element eventElement);
	public abstract void processStart();
	public abstract void processEnd();
	public abstract void eventStart();
	public abstract void eventEnd();

	
	public static Document loadXML(String xmlFilePath) throws DocumentException{
		logger.info("load xml : " + xmlFilePath);
		
		// load xml file
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		logger.info("Starting Current Date: " + ft.format(dNow));
	
		File inputXmlFile = new File(xmlFilePath);
		SAXReader saxReader = new SAXReader();
//		saxReader.setEncoding("utf-8");
		Document document = saxReader.read(inputXmlFile);
	
		dNow = new Date();
		ft = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
		logger.info("Ending Current Date: " + ft.format(dNow));
		return document;
	}

	/**
	 * Split a procmon into one xml(process list) and several xml(event list).
	 * @param xmlFilePath
	 * @param limitedLineNumber
	 * @throws IOException
	 */
	public static void splitXML(String xmlFilePath, int limitedLineNumber) throws IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(xmlFilePath), "UTF8"));
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
				BufferedWriter outputFileWriter = new BufferedWriter(new OutputStreamWriter(
					    new FileOutputStream(xmlFilePath + "_" + "processlist"), "UTF-8"));
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
					BufferedWriter outputFileWriter = new BufferedWriter(new OutputStreamWriter(
						    new FileOutputStream(xmlFilePath + "_eventlist - " + splitIndex), "UTF-8"));
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
			BufferedWriter outputFileWriter = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(xmlFilePath + "_eventlist - " + splitIndex), "UTF-8"));
			splitIndex++;
			outputFileWriter.write(eventlistOutputContent.toString());
			outputFileWriter.close();
		}				
		
	}
	
	
	public boolean isProcmonXMLSplited(String xmlFilePath){
		String processlistFilePath = xmlFilePath + "_processlist";
		return new File(processlistFilePath).exists();
		
	}
	
}

