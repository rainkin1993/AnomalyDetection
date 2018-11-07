package edu.zju.utils;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Node;

import edu.zju.BasicClass.OperationWithCallstack;

public class CallstackUtils {

	/**
	 * select callstacks which should be added into sigs
	 * @param stackFrameLocations
	 * @param stackFrameAddresses
	 * @return 
	 */
	public static List<String> selectCallstacks(List<Node> stackFrameLocations, List<Node> stackFrameAddresses){
		List<String> userModeLocations = new ArrayList<String>();
		for (int index = 0; index < stackFrameLocations.size(); index++){
			String location = stackFrameLocations.get(index).getText();
			String address = stackFrameAddresses.get(index).getText();
			if (!address.startsWith("0xffff")){
				userModeLocations.add(location.toLowerCase());
			}
			
		}
		return userModeLocations;
	}
}
