package com.woting.crawler.scheme.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;

import net.coobird.thumbnailator.Thumbnails;

public class FileUtils {

	public static boolean writeFile(String jsonstr, String path) {
		File file = createFile(path);
		try {
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file),"GBK");
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
	
	public static String readFile(String path) {
		String sb = "";
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
		return sb;
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
	
	public static String makeImgFile(String purpose, String imgpath) {
		String rootpath = "/opt/";// SystemCache.getCache(CrawlerConstants.APP_PATH).getContent()+"";
		if (purpose.equals("1")) { //用户头像处理
			String imgName = SequenceUUID.getPureUUID();
			String path = rootpath + "dataCenter/group03/";
			String filepath = path + imgName+".png";
			try {
				download(imgpath, imgName+".png", path);
                String img150path = path + "/" + imgName + ".150_150.png";
                String img300path = path + "/" + imgName + ".300_300.png";
                String img450path = path + "/" + imgName + ".450_450.png";
                Thumbnails.of(new File(filepath)).size(150, 150).toFile(img150path);
                Thumbnails.of(new File(filepath)).size(300, 300).toFile(img300path);
                Thumbnails.of(new File(filepath)).size(450, 450).toFile(img450path);
                return filepath.replace(rootpath, "http://www.wotingfm.com:908/CM/");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (purpose.equals("2")) { //内容图片处理
				String imgName = SequenceUUID.getPureUUID();
				String path = rootpath + "dataCenter/group03/";
				String filepath = path + imgName+".png";
				try {
					download(imgpath, imgName+".png", path);
					String img180path = path + "/" + imgName + ".180_180.png";
	                String img300path = path + "/" + imgName + ".300_300.png";
	                Thumbnails.of(new File(filepath)).size(180, 180).toFile(img180path);
	                Thumbnails.of(new File(filepath)).size(300, 300).toFile(img300path);
	                return filepath.replace(rootpath, "http://www.wotingfm.com:908/CM/");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				if (purpose.equals("3")) { //轮播图处理
					String imgName = SequenceUUID.getPureUUID();
					String path = rootpath + "dataCenter/group04/";
					String filepath = path + imgName+".png";
					try {
						download(imgpath, imgName+".png", path);
						String img1080_450path = path + "/" + imgName + ".1080_450.png";
		                Thumbnails.of(new File(filepath)).size(1080, 450).toFile(img1080_450path);
		                return filepath.replace(rootpath, "http://www.wotingfm.com:908/CM/");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
	
	public static void download(String urlString, String filename,String savePath) throws Exception {  
        // 构造URL
        URL url = new URL(urlString);
        // 打开连接
        URLConnection con = url.openConnection();
        //设置请求超时为5s
        con.setConnectTimeout(5*1000);
        // 输入流 
        InputStream is = con.getInputStream();
        // 1K的数据缓冲
        byte[] bs = new byte[1024];
        // 读取到的数据长度
        int len;
        // 输出的文件流
       File sf=new File(savePath);
       if(!sf.exists()){
           sf.mkdirs();
       }
       OutputStream os = new FileOutputStream(sf.getPath()+"/"+filename);
        // 开始读取
        while ((len = is.read(bs)) != -1) {
          os.write(bs, 0, len);
        }
        // 完毕，关闭所有链接
        os.close();
        is.close();
    }
}
