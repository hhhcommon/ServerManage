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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.spiritdata.framework.util.JsonUtils;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.oss.utils.OssUtils;
import com.woting.crawler.core.imagehash.persis.po.ImageHash;
import com.woting.crawler.core.imagehash.service.ImageHashService;
import com.woting.crawler.ext.SpringShell;

public class FileUtils {

	public static boolean writeFile(String jsonstr, String path) {
		File file = createFile(path);
		try {
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file), "GBK");
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

	public static boolean writeFile(String jsonstr, File file) {
		try {
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
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
	public static List<Map<String, Object>> readFileByJson(String path) {
		String sb = "";
		List<Map<String, Object>> l = new ArrayList<Map<String, Object>>();
		File file = new File(path);
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), "gbk");
			BufferedReader reader = new BufferedReader(read);
			String line;
			while ((line = reader.readLine()) != null) {
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
		if (!file.exists()) {
			return null;
		}
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), "utf-8");
			BufferedReader reader = new BufferedReader(read);
			String line;
			while ((line = reader.readLine()) != null) {
				sb += line;
			}
			read.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb;
	}

	public static String readFile(File file) {
		String sb = "";
		if (!file.exists()) {
			return null;
		}
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader reader = new BufferedReader(read);
			String line;
			while ((line = reader.readLine()) != null) {
				sb += line;
			}
			read.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb;
	}

	public static void writeContentInfo(String key, String jsonstr) {
		File file = FileUtils.createFile("/opt/dataCenter/contentinfo/" + key + ".json");
		FileUtils.writeFile(jsonstr, file);
	}

	public static File createFile(String path) {
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
		String tempfile = "/opt/tempfile/";
		String osstempfile = "tempfile/";
		Map<String, Object> m = null;
		if (purpose.equals("1")) { // 用户头像处理
			String ext = ".png";
			String imgName = SequenceUUID.getPureUUID();
			String path = "userimg/";
			String filepath = tempfile + imgName + ".png";
			String ossfilepath = osstempfile + imgName + ".png";
			try {
				download(imgpath, imgName + ".png", tempfile);
				if (!OssUtils.upLoadObject(ossfilepath, new File(filepath), true)) return null;
				OssUtils.makePictureType(ossfilepath, filepath, "png");
				synchronized (FileUtils.class) {
					m = compareImageHash(filepath, purpose, imgpath, imgName);
				}
				if (m.get("ISOK").equals("true")) {
					String imgPath = path + imgName + ext;
					String img150path = path + imgName + ".150_150" + ext;
					String img300path = path + imgName + ".300_300" + ext;
					String img450path = path + imgName + ".450_450" + ext;
					OssUtils.upLoadObject(imgPath, new File(filepath), true);
					OssUtils.makePictureResize(imgPath, img150path, 150);
					OssUtils.makePictureResize(imgPath, img300path, 300);
					OssUtils.makePictureResize(imgPath, img450path, 450);
					if (m.containsKey("HashCode")) {
						ImageHashService imageHashService = (ImageHashService) SpringShell.getBean("imageHashService");
						ImageHash imageHash = new ImageHash();
						imageHash.setId(purpose + "_" + m.get("HashCode").toString());
						imageHash.setIsValidate(1);
						imageHashService.updateImageHash(imageHash);
					}
					return "##userimg##" + imgName + ext;
				} else return m.get("Path").toString();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("图片出错路径:" + imgpath);
				return null;
			} finally {
				File file = new File(filepath);
				if (file.isFile() && file.exists()) file.delete();
				OssUtils.deleteObject(ossfilepath);
			}
		} else {
			if (purpose.equals("2")) { // 内容图片处理
				String ext = ".png";
				String imgName = SequenceUUID.getPureUUID();
				String path = "contentimg/";
				String filepath = tempfile + imgName + ".png";
				String ossfilepath = osstempfile + imgName + ".png";
				try {
					download(imgpath, imgName + ".png", tempfile);
					if (!OssUtils.upLoadObject(ossfilepath, new File(filepath), true)) return null;
					OssUtils.makePictureType(ossfilepath, filepath, "png");
					synchronized (FileUtils.class) {
						m = compareImageHash(filepath, purpose, imgpath, imgName);
					}
					if (m.get("ISOK").equals("true")) {
						String imgPath = path + imgName + ext;
						String img180path = path + imgName + ".180_180" + ext;
						String img300path = path + imgName + ".300_300" + ext;
						OssUtils.upLoadObject(imgPath, new File(filepath), true);
						OssUtils.makePictureResize(imgPath, img180path, 180);
						OssUtils.makePictureResize(imgPath, img300path, 300);
						if (m.containsKey("HashCode")) {
							ImageHashService imageHashService = (ImageHashService) SpringShell.getBean("imageHashService");
							ImageHash imageHash = new ImageHash();
							imageHash.setId(purpose + "_" + m.get("HashCode").toString());
							imageHash.setIsValidate(1);
							imageHashService.updateImageHash(imageHash);
						}
						return "##contentimg##" + imgName + ext;
					} else return m.get("Path").toString();
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("图片出错路径:" + imgpath);
					return null;
				} finally {
					File file = new File(filepath);
					if (file.isFile() && file.exists()) file.delete();
					OssUtils.deleteObject(ossfilepath);
				}
			} else {
				if (purpose.equals("3")) { // 轮播图处理
					String ext = ".png";
					String imgName = SequenceUUID.getPureUUID();
					String path = "contentimg/";
					String filepath = tempfile + imgName + ".png";
					String ossfilepath = osstempfile + imgName + ".png";
					try {
						download(imgpath, imgName + ".png", tempfile);
						if (!OssUtils.upLoadObject(ossfilepath, new File(filepath), true)) return null;
						OssUtils.makePictureType(ossfilepath, filepath, "png");
						String imgPath = path + imgName + ext;
						String img1080_450path = path + imgName + ".1080_450" + ext;
						OssUtils.upLoadObject(imgPath, new File(filepath), true);
						OssUtils.makePictureResize(imgPath, img1080_450path, 1080, 450);
						return "##contentimg##" + imgName + ext;
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println("图片出错路径:" + imgpath);
						return null;
					} finally {
						File file = new File(filepath);
						if (file.isFile() && file.exists()) file.delete();
						OssUtils.deleteObject(ossfilepath);
					}
				}
			}
		}
		return null;
	}

	public static void download(String urlString, String filename, String savePath) throws Exception {
		// 构造URL
		URL url = new URL(urlString);
		// 打开连接
		URLConnection con = url.openConnection();
		// 设置请求超时为500s
		con.setConnectTimeout(500 * 1000);
		con.setReadTimeout(500 * 1000);
		// 输入流
		InputStream is = null;
		try {
			is = con.getInputStream();
		} catch (Exception e) {}
		if (is==null) {
			int num = 10;
			while (num-- > 0) {
				try {
					Thread.sleep(50);
					is = con.getInputStream();
					if (is!=null) break;
				} catch (Exception e) {e.getMessage();}
			}
		}
		if (is==null) return;
		// 1K的数据缓冲
		byte[] bs = new byte[1024];
		// 读取到的数据长度
		int len;
		// 输出的文件流
		File sf = new File(savePath);
		if (!sf.exists()) {
			sf.mkdirs();
		}
		OutputStream os = new FileOutputStream(sf.getPath() + "/" + filename);
		// 开始读取
		while ((len = is.read(bs)) != -1) {
			os.write(bs, 0, len);
		}
		// 完毕，关闭所有链接
		os.close();
		is.close();
	}

	public static Map<String, Object> compareImageHash(String path, String purpose, String imgpath, String imgName) {
		Map<String, Object> map = new HashMap<>();
		try {
			ImageHashService imageHashService = (ImageHashService) SpringShell.getBean("imageHashService");
			String hashcode = ImageUtils.produceFingerPrint(path);
			ImageHash imageHash = imageHashService.getImageHash(purpose + "_" + hashcode);
			if (imageHash != null) {
				map.put("ISOK", "false");
				map.put("Path", imageHash.getImagePath());
			} else {
				map.put("ISOK", "true");
				map.put("HashCode", hashcode);
				imageHash = new ImageHash();
				imageHash.setId(purpose + "_" + hashcode);
				imageHash.setImagePath("##contentimg##" + imgName + ".png");
				imageHash.setImageSrcPath(imgpath);
				imageHash.setPurpose(purpose);
				imageHashService.insertImageHash(imageHash);
			}
		} catch (Exception e) {
			map.clear();
			map.put("ISOK", "true");
			return map;
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	public static void doingDB(File file, String albumId, Map<String, Object> mapall, String crawlerNum) {
		try {
			Map<String, Object> map = null;
			synchronized (file) {
				String str = readFile(file);
				if (str != null && str.length() > 32) {
					map = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
				} else
					map = new HashMap<>();
				Map<String, Object> dbmap = null;
				if (map.containsKey("doingDB")) {
					dbmap = (Map<String, Object>) map.get("doingDB");
				} else {
					dbmap = new HashMap<>();
					dbmap.put("CrawlerNum", crawlerNum);
					map.put("doingDB", dbmap);
				}
				dbmap.put(albumId, mapall.get(albumId));
				FileUtils.writeFile(JsonUtils.objToJson(map), file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static void didDB(File file, String albumId) {
		try {
			Map<String, Object> map = null;
			if (!file.exists()) {
				file.createNewFile();
			}
			synchronized (file) {
				String str = readFile(file);
				if (str != null && str.length() > 0) {
					map = (Map<String, Object>) JsonUtils.jsonToObj(str, Map.class);
				} else
					map = new HashMap<>();

				Map<String, Object> dbmap = null;
				if (map.containsKey("doingDB")) {
					dbmap = (Map<String, Object>) map.get("doingDB");
				} else {
					dbmap = new HashMap<>();
					map.put("doingDB", dbmap);
				}
				if (dbmap.containsKey(albumId)) {
					map.put(albumId, dbmap.get(albumId));
					dbmap.remove(albumId);
				}
				FileUtils.writeFile(JsonUtils.objToJson(map), file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
