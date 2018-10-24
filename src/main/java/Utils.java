import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.google.gson.GsonBuilder;

import edu.zju.BasicClass.AnomalModelForOneApp;

public class Utils {
	public static void writeObjectToFileUsingJsonFormat(String outputFilePath, Object object) throws IOException{
		BufferedWriter sigWriter = new BufferedWriter(new FileWriter(outputFilePath));
		
		sigWriter.write(new GsonBuilder().setPrettyPrinting().create().toJson(object));
		sigWriter.flush();
		sigWriter.close();
	}
}
