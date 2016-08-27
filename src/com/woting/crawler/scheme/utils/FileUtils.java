package com.woting.crawler.scheme.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.spiritdata.framework.util.JsonUtils;

public class FileUtils {

	public static boolean writeFile(String jsonstr, String path) {
		File file = createFile(path);
		try {
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file));
			BufferedWriter writer = new BufferedWriter(write);
			writer.write(jsonstr);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (file.exists())
			return true;
		else
			return false;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> readFileByJson(String path){
		String sb = "";
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		File file = new File(path);
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file),"gbk");       
            BufferedReader reader=new BufferedReader(read);       
            String line;       
            while ((line = reader.readLine()) != null)   
            {
                sb += line;
            }
            read.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		l = (List<Map<String, Object>>) JsonUtils.jsonToObj(sb, List.class);
		return l;
	}
	
	private static File createFile(String path) {
		File file = new File(path);
		try {
			if (!file.exists()) {
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				} else {
					file.createNewFile();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
}
