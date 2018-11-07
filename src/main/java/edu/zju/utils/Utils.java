package edu.zju.utils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import edu.zju.BasicClass.AnomalModelForOneApp;
import edu.zju.BasicClass.semantic.SemanticEventSig;

public class Utils {
	public static void writeObjectToFileUsingJsonFormat(String outputFilePath, Object object) throws IOException{
		BufferedWriter sigWriter = new BufferedWriter(new FileWriter(outputFilePath));
		
		sigWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(object));
		sigWriter.flush();
		sigWriter.close();
	}

	public static List<SemanticEventSig> loadSemanticEventSigs(List<File> sigFiles) {
		// TODO Auto-generated method stub
		List<SemanticEventSig> totalSigs = new ArrayList<SemanticEventSig>();
		
		for (File sigFile : sigFiles){
			Type listType = new TypeToken<ArrayList<SemanticEventSig>>(){}.getType();
			try {
				List<SemanticEventSig> sigs = new Gson().fromJson(new FileReader(sigFile), listType);
				totalSigs.addAll(sigs);
			} catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		return totalSigs;
		
		
	}
	
	public static String convert2DotCompatibleString(String oldString) {
		String newString = oldString.replace("\\", "/");
		return newString;		
	}
}
