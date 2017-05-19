package com.woting.cm.core.oss.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.spiritdata.framework.util.SequenceUUID;
import com.woting.cm.core.oss.persis.po.OssConfigPo;
import com.woting.crawler.ext.SpringShell;

public class OssUtils {
	
	/**
	 * OSS上传文件
	 * @param key 文件Object
	 * @param content 字符串
	 * @param isOrNoDelete 是否删除原文件
	 * @return
	 */
	public static boolean upLoadObject(String key, String content, boolean isOrNoDelete) {
		OSSClient ossClient = null;
		try {
			if (content!=null && key!=null) {
				OssConfigPo ossConfigPo = (OssConfigPo) SpringShell.getBean("ossconfig");
				if (ossConfigPo!=null) {
					ossClient = new OSSClient(ossConfigPo.getEndpoint(), ossConfigPo.getAccessKeyId(), ossConfigPo.getAccessKeySecret());
					if (ossClient!=null) {
						File file = createFile(ossConfigPo.getTempFile()+SequenceUUID.getPureUUID()+".json");
						writeFile(content, file);
						ObjectMetadata meta = new ObjectMetadata();
						meta.setContentLength(content.length());
						meta.setCacheControl("no-cache");
						meta.setHeader("Pragma", "no-cache");
						meta.setContentType(contentType(file.getName().substring(file.getName().lastIndexOf("."))));
						meta.setContentDisposition("inline;filename=" + file.getName());
						ossClient.putObject(ossConfigPo.getBucketName(), key, file, meta);
						if (ossClient.doesObjectExist(ossConfigPo.getBucketName(), key)) {
						    if (isOrNoDelete) deleteFile(file);
						    return true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (ossClient!=null) ossClient.shutdown();
		}
		return false;
	}
	
	/**
	 * OSS上传文件
	 * @param key 文件Object
	 * @param file 文件
	 * @param isOrNoDelete 是否删除原文件
	 * @return
	 */
	public static boolean upLoadObject(String key, File file, boolean isOrNoDelete) {
		OSSClient ossClient = null;
		try {
			if (file!=null && key!=null) {
				OssConfigPo ossConfigPo = (OssConfigPo) SpringShell.getBean("ossconfig");
				if (ossConfigPo!=null) {
					ossClient = new OSSClient(ossConfigPo.getEndpoint(), ossConfigPo.getAccessKeyId(), ossConfigPo.getAccessKeySecret());
					if (ossClient!=null) {
						ObjectMetadata meta = new ObjectMetadata();
						meta.setContentLength(file.getTotalSpace());
						meta.setCacheControl("no-cache");
						meta.setHeader("Pragma", "no-cache");
						meta.setContentType(contentType(file.getName().substring(file.getName().lastIndexOf("."))));  
						meta.setContentDisposition("inline;filename=" + file.getName()); 
						ossClient.putObject(ossConfigPo.getBucketName(), key, file, meta);
						if (ossClient.doesObjectExist(ossConfigPo.getBucketName(), key)) {
							if (isOrNoDelete) deleteFile(file);
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (ossClient!=null) ossClient.shutdown();
		}
		return false;
	}
	
	/**
	 * 上传网络流
	 * @param key 文件Object
	 * @param url 网络流地址
	 * @return
	 */
	public static boolean upLoadObject(String key, InputStream in) {
		OSSClient ossClient = null;
		try {
			if (in!=null && key!=null) {
				OssConfigPo ossConfigPo = (OssConfigPo) SpringShell.getBean("ossconfig");
				if (ossConfigPo!=null) {
					ossClient = new OSSClient(ossConfigPo.getEndpoint(), ossConfigPo.getAccessKeyId(), ossConfigPo.getAccessKeySecret());
					if (ossClient!=null) {
						ObjectMetadata meta = new ObjectMetadata();
						meta.setContentLength(in.available());
						meta.setCacheControl("no-cache");
						meta.setHeader("Pragma", "no-cache");
						meta.setContentType(contentType(key.substring(key.lastIndexOf("."))));
//						meta.setContentDisposition("inline;filename=");
						ossClient.putObject(ossConfigPo.getBucketName(), key, in, meta);
						if (ossClient.doesObjectExist(ossConfigPo.getBucketName(), key)) {
							in.close();
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (ossClient!=null) ossClient.shutdown();
		}
		return false;
	}
	
	/**
	 * 上传网络流
	 * @param key 文件Object
	 * @param url 网络流地址
	 * @return
	 */
	public static boolean upLoadObject(String key, String urlStr) {
		OSSClient ossClient = null;
		try {
			if (urlStr!=null && urlStr.length()>0 && key!=null) {
				OssConfigPo ossConfigPo = (OssConfigPo) SpringShell.getBean("ossconfig");
				if (ossConfigPo!=null) {
					ossClient = new OSSClient(ossConfigPo.getEndpoint(), ossConfigPo.getAccessKeyId(), ossConfigPo.getAccessKeySecret());
					if (ossClient!=null) {
						// 构造URL
						URL url = new URL(urlStr);
						// 打开连接
						URLConnection con = url.openConnection();
						// 设置请求超时为500s
						con.setConnectTimeout(500 * 1000);
						con.setReadTimeout(500 * 1000);
						
						// 输入流
						InputStream in = con.getInputStream();
						if (in != null) {
							ObjectMetadata meta = new ObjectMetadata();
							meta.setCacheControl("no-cache");
							meta.setHeader("Pragma", "no-cache");
							meta.setContentType(contentType(key.substring(key.lastIndexOf("."))));
							ossClient.putObject(ossConfigPo.getBucketName(), key, in, meta);
							if (ossClient.doesObjectExist(ossConfigPo.getBucketName(), key)) {
								in.close();
								return true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (ossClient!=null) ossClient.shutdown();
		}
		return false;
	}
	
	public static File getObjectToFile(String key) {
		OSSClient ossClient = null;
		try {
			OssConfigPo ossConfigPo = (OssConfigPo) SpringShell.getBean("ossconfig");
			if (ossConfigPo!=null) {
				ossClient = new OSSClient(ossConfigPo.getEndpoint(), ossConfigPo.getAccessKeyId(), ossConfigPo.getAccessKeySecret());
				if (ossClient!=null) {
					OSSObject ossObject = ossClient.getObject(ossConfigPo.getBucketName(), key);
					if (ossObject!=null) {
						InputStream in = ossObject.getObjectContent();					
						if (in!=null) {
							String name = key.substring(key.lastIndexOf("/"), key.length());
							File file = createFile(ossConfigPo.getTempFile()+name);
							if (writeFile(in, file)) {
								return file;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ossClient!=null) ossClient.shutdown();
		}
		return null;
	}
	
	public static String getObjectToString(String key) {
		OSSClient ossClient = null;
		try {
			OssConfigPo ossConfigPo = (OssConfigPo) SpringShell.getBean("ossconfig");
			if (ossConfigPo!=null) {
				ossClient = new OSSClient(ossConfigPo.getEndpoint(), ossConfigPo.getAccessKeyId(), ossConfigPo.getAccessKeySecret());
				if (ossClient!=null) {
					OSSObject ossObject = ossClient.getObject(ossConfigPo.getBucketName(), key);
					if (ossObject!=null) {
						InputStream in = ossObject.getObjectContent();					
						if (in!=null) {
							BufferedReader is = new BufferedReader(new InputStreamReader(in));
							StringBuffer buffer = new StringBuffer();
							String line = "";
							while ((line = is.readLine()) != null){
								buffer.append(line);
							}
							is.close();
							in.close();
							return buffer.toString();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ossClient!=null) ossClient.shutdown();
		}
		return null;
	}
	
	/**
	 * 判断文件是否存在
	 * @param key
	 * @return
	 */
	public static boolean exists(String key) {
		OSSClient ossClient = null;
		try {
			OssConfigPo ossConfigPo = (OssConfigPo) SpringShell.getBean("ossconfig");
			if (ossConfigPo!=null) {
				ossClient = new OSSClient(ossConfigPo.getEndpoint(), ossConfigPo.getAccessKeyId(), ossConfigPo.getAccessKeySecret());
				if (ossClient!=null) {
					return ossClient.doesObjectExist(ossConfigPo.getBucketName(), key);
				}
			}
		} catch (Exception e) {} finally {
			if (ossClient!=null) ossClient.shutdown();
		}
		return false;
	}
	
	/**
	 * 图片缩放
	 * @param key 图片Object
	 * @param ResizePer 缩放大小
	 * @return
	 * 默认进行格式转换，转为png格式
	 */
	public static boolean makePictureResize(String key,String newkey, int ResizePer) {
		OSSClient ossClient = null;
		try {
			OssConfigPo ossConfigPo = (OssConfigPo) SpringShell.getBean("ossconfig");
			if (ossConfigPo!=null) {
				ossClient = new OSSClient(ossConfigPo.getEndpoint(), ossConfigPo.getAccessKeyId(), ossConfigPo.getAccessKeySecret());
				if (ossClient!=null) {
					String style = "image/resize,m_lfit,w_"+ResizePer+",h_"+ResizePer+",limit_0/auto-orient,0";  
					GetObjectRequest request = new GetObjectRequest(ossConfigPo.getBucketName(), key);
					request.setProcess(style);
					OSSObject ossObject = ossClient.getObject(request);
					if (ossObject!=null) {
					    InputStream in = ossObject.getObjectContent();
						if (in!=null) {
							ObjectMetadata meta = new ObjectMetadata();
							meta.setCacheControl("no-cache");
							meta.setHeader("Pragma", "no-cache");
							meta.setContentType(contentType(key.substring(key.lastIndexOf("."))));
							ossClient.putObject(ossConfigPo.getBucketName(), newkey, in, meta);
							if (ossClient.doesObjectExist(ossConfigPo.getBucketName(), newkey)) {
								in.close();
								return true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			System.out.println("ac.wotingfm.com/"+key);
			e.printStackTrace();
			return false;
		} finally {
			if (ossClient!=null) ossClient.shutdown();
		}
		return false;
	}
	
	/**
	 * 
	 * @param key 图片Object
	 * @param WidhtResizePer 宽缩放大小
	 * @param HighResizePer 高缩放大小
	 * @return
	 */
	public static boolean makePictureResize(String key, String newkey, int WidhtResizePer, int HighResizePer) {
		OSSClient ossClient = null;
		try {
			OssConfigPo ossConfigPo = (OssConfigPo) SpringShell.getBean("ossconfig");
			if (ossConfigPo!=null) {
				ossClient = new OSSClient(ossConfigPo.getEndpoint(), ossConfigPo.getAccessKeyId(), ossConfigPo.getAccessKeySecret());
				if (ossClient!=null) {
					String style = "image/resize,m_lfit,w_"+WidhtResizePer+",h_"+HighResizePer+",limit_0/auto-orient,0/quality,q_100";  
					GetObjectRequest request = new GetObjectRequest(ossConfigPo.getBucketName(), key);
					request.setProcess(style);
					OSSObject ossObject = ossClient.getObject(request);
					if (ossObject!=null) {
					    InputStream in = ossObject.getObjectContent();
						if (in!=null) {
							ObjectMetadata meta = new ObjectMetadata();
							meta.setCacheControl("no-cache");
							meta.setHeader("Pragma", "no-cache");
							meta.setContentType(contentType(key.substring(key.lastIndexOf("."))));
							ossClient.putObject(ossConfigPo.getBucketName(), newkey, in, meta);
							if (ossClient.doesObjectExist(ossConfigPo.getBucketName(), newkey)) {
								in.close();
								return true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (ossClient!=null) ossClient.shutdown();
		}
		return false;
	}
	
	/**
	 * 图片格式转换到本地文件
	 * @param key
	 * @param newkey
	 * @param type 默认png
	 * @return
	 */
	public static void makePictureType(String key,String newkey, String type) {
		OSSClient ossClient = null;
		try {
			OssConfigPo ossConfigPo = (OssConfigPo) SpringShell.getBean("ossconfig");
			if (ossConfigPo!=null) {
				ossClient = new OSSClient(ossConfigPo.getEndpoint(), ossConfigPo.getAccessKeyId(), ossConfigPo.getAccessKeySecret());
				if (ossClient!=null) {
					if (type==null || type.length()==0 || type.equals("null")) type = "png";
					String style = "image/format,"+type;
					GetObjectRequest request = new GetObjectRequest(ossConfigPo.getBucketName(), key);
					request.setProcess(style);
					ossClient.getObject(request, new File(newkey));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ossClient!=null) ossClient.shutdown();
		}
	}
	
	public static void deleteObject(String key) {
		OSSClient ossClient = null;
		try {
			OssConfigPo ossConfigPo = (OssConfigPo) SpringShell.getBean("ossconfig");
			if (ossConfigPo!=null) {
				ossClient = new OSSClient(ossConfigPo.getEndpoint(), ossConfigPo.getAccessKeyId(), ossConfigPo.getAccessKeySecret());
				if (ossClient!=null) {
					ossClient.deleteObject(ossConfigPo.getBucketName(), key);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ossClient!=null) ossClient.shutdown();
		}
	}
	
	private static boolean writeFile(String jsonstr, File file) {
		try {
			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			BufferedWriter writer = new BufferedWriter(write);
			writer.write(jsonstr);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (file.exists()) return true;
		else return false;
	}
	
	private static boolean writeFile(InputStream is, File file) {
		try { //TODO
			// 1K的数据缓冲
			byte[] bs = new byte[1024];
			// 读取到的数据长度
			int len;
			// 输出的文件流
			if (!file.exists()) {
				file.mkdirs();
			}
			OutputStream os = new FileOutputStream(file.getName());
			// 开始读取
			while ((len = is.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			// 完毕，关闭所有链接
			os.close();
			is.close();
			
//			OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(file));//, "UTF-8"
//			BufferedWriter writer = new BufferedWriter(write);
//			BufferedReader in = new BufferedReader(new InputStreamReader(is));
//			StringBuffer buffer = new StringBuffer();
//			String line = "";
//			while ((line = in.readLine()) != null) {
//				buffer.append(line);
//			}
//			writer.write(buffer.toString());
//			writer.close();
//			is.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (file.exists()) return true;
		else return false;
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
	
	private static void deleteFile(File file) {
		if (file!=null && file.exists()) {
			file.delete();
		}
	}
	
	/**
     * 判断OSS服务文件上传时文件的contentType
     * @param FilenameExtension 文件后缀 
     * @return String
     */
     public static String contentType(String FilenameExtension){
    	if (FilenameExtension.contains(".")) FilenameExtension = FilenameExtension.replace(".", "");
    	FilenameExtension = FilenameExtension.toLowerCase();
        if(FilenameExtension.equals("BMP")||FilenameExtension.equals("bmp")){return "image/bmp";}  
        if(FilenameExtension.equals("GIF")||FilenameExtension.equals("gif")){return "image/gif";}  
        if(FilenameExtension.equals("JPEG")||FilenameExtension.equals("jpeg")||FilenameExtension.equals("JPG")||FilenameExtension.equals("jpg")||
           FilenameExtension.equals("PNG")||FilenameExtension.equals("png")){return "image/png";}  
        if(FilenameExtension.equals("HTML")||FilenameExtension.equals("html")){return "text/html";}
        if(FilenameExtension.equals("TXT")||FilenameExtension.equals("txt")){return "text/plain";}  
        if(FilenameExtension.equals("VSD")||FilenameExtension.equals("vsd")){return "application/vnd.visio";}  
        if(FilenameExtension.equals("PPTX")||FilenameExtension.equals("pptx")||
            FilenameExtension.equals("PPT")||FilenameExtension.equals("ppt")){return "application/vnd.ms-powerpoint";}  
        if(FilenameExtension.equals("DOCX")||FilenameExtension.equals("docx")||  
            FilenameExtension.equals("DOC")||FilenameExtension.equals("doc")){return "application/msword";}  
        if(FilenameExtension.equals("XML")||FilenameExtension.equals("xml")){return "text/xml";}  
        return "text/html";
     }
}
