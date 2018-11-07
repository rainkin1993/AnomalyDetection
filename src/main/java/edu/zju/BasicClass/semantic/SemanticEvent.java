package edu.zju.BasicClass.semantic;

import java.util.List;

import org.dom4j.Element;

public class SemanticEvent {
	
	public String operation;
	public String path;
	public String detail;
	public String result;
	public String processName;
	public int pid;
	public int tid;
	public int ppid;
	public String time;
	
	public String semantic;
	
	public SemanticEvent(Element element, String semantic){
		this.operation = element.selectSingleNode("Operation").getText();
		this.path = element.selectSingleNode("Path").getText();
		this.detail = element.selectSingleNode("Detail").getText();
		this.result = element.selectSingleNode("Result").getText();
		this.processName = element.selectSingleNode("Process_Name").getText();
		this.pid = Integer.parseInt(element.selectSingleNode("PID").getText());
		this.tid = Integer.parseInt(element.selectSingleNode("TID").getText());
		this.ppid = Integer.parseInt(element.selectSingleNode("Parent_PID").getText());
		String time_of_day = element.selectSingleNode("Time_of_Day").getText();
		String data_time = element.selectSingleNode("Date___Time").getText();
		this.time = data_time.split(" ")[0] + " " + time_of_day;
		this.semantic = semantic;
		
	}

	@Override
	public String toString() {
		return "SemanticEvent ["
				+ "\noperation=" + operation 
				+ "\n, path=" + path 
				+ "\n, detail=" + detail 
				+ "\n, result=" + result
				+ "\n, processName=" + processName 
				+ "\n, pid=" + pid + "\n, tid=" + tid + "\n, ppid=" + ppid + "\n, time=" + time + "\n, semantic="
				+ semantic + "\n]";
	}
	
	
	
	

}
