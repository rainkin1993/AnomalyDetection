package edu.zju.BasicClass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnomalModelForOneApp {
	public Map<String, Set<List<String>>> operation2normalCallstacks = new HashMap<String, Set<List<String>>>();
	
	public AnomalModelForOneApp(Map<String, Set<List<String>>> operation2normalCallstacks) {
		super();
		this.operation2normalCallstacks = operation2normalCallstacks;
	}
	
	
	
	public AnomalModelForOneApp() {
		super();
	}


	public void addNormalOperationWithCallstack(String operation, List<String> userModeCallstacks){
		if (!operation2normalCallstacks.containsKey(operation)){
			operation2normalCallstacks.put(operation, new HashSet<List<String>>());
		}
		operation2normalCallstacks.get(operation).add(userModeCallstacks);
	}

	public boolean isAnomal(String operation, List<String> userModeCallstacks){
		boolean isAnomal = false;
		
		if (operation2normalCallstacks.containsKey(operation)){
			if (!operation2normalCallstacks.get(operation).contains(userModeCallstacks)){
				isAnomal = true;
			} else {
				// normal callstack and operation
			}
		} else {
			// ignore operation not exist
		}
		
		return isAnomal;
	}
}
