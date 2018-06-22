package edu.zju.BasicClass;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatisticsForOneApp {
	Map<List<String>, Set<String>> callstack2Path = new HashMap<List<String>, Set<String>>();

	public StatisticsForOneApp(Map<List<String>, Set<String>> callstack2Path) {
		super();
		this.callstack2Path = callstack2Path;
	}
	
	
	public StatisticsForOneApp() {
		// TODO Auto-generated constructor stub
	}


	public void addNewPathForCallstack(String path, List<String> userModeCallstacks){
		if (!callstack2Path.containsKey(userModeCallstacks)){
			callstack2Path.put(userModeCallstacks, new HashSet<String>());
		}
		callstack2Path.get(userModeCallstacks).add(path);
	}
}
