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
	
	/**
	 * two continuous events for the same thread, 
	 * if operation, path and time interval < xxx
	 * then these two events can be merged
	 * @param event
	 * @return
	 */
	public boolean canBeMerged(SemanticEvent event){
		boolean isMerged = false;
		
		double defaultInterval = 2; // 1 second
		
		if (this.operation.equals(event.operation)
				&& this.path.equals(event.path)
				&& this.pid == event.pid
				&& this.tid == event.tid){
			
			String[] firstTimeTmp = this.time.split(" ")[1].split(":");
			double firstTimeSeconds = Integer.parseInt(firstTimeTmp[0]) * 60 * 60 + Integer.parseInt(firstTimeTmp[1]) * 60 + Double.parseDouble(firstTimeTmp[2]);
			String[] secondTimeTmp = event.time.split(" ")[1].split(":");
			double secondTimeSeconds = Integer.parseInt(secondTimeTmp[0]) * 60 * 60 + Integer.parseInt(secondTimeTmp[1]) * 60 + Double.parseDouble(secondTimeTmp[2]);
			
			if (secondTimeSeconds - firstTimeSeconds <= defaultInterval)
				isMerged = true;
		}
		
		return isMerged;
		
			
	}
	
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
