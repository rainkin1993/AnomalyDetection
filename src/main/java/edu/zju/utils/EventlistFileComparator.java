package edu.zju.utils;

import java.io.File;
import java.util.Comparator;

public class EventlistFileComparator implements Comparator<File> {

	@Override
	public int compare(File file1, File file2) {
		// TODO Auto-generated method stub
		String filePath1 = file1.getPath();
		String filePath2 = file2.getPath();
		int fileIndex1 = Integer.parseInt(filePath1.substring(filePath1.lastIndexOf("eventlist - ") + "eventlist - ".length()));
		int fileIndex2 = Integer.parseInt(filePath2.substring(filePath2.lastIndexOf("eventlist - ") + "eventlist - ".length()));
//		System.out.println(fileIndex1 + " " + fileIndex2);
		return fileIndex1 - fileIndex2;
	}
}