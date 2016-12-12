package com.woting.crawler.scheme.utils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.woting.crawler.core.m3u8.persis.po.M3U8;
import com.woting.crawler.core.m3u8.persis.po.M3U8_ExtInfo;
import com.woting.crawler.core.m3u8.persis.po.M3U8_StreamInfo;



public class M3U8Utils {

	public static boolean M3u8Text(String urlstr) {
		try {
			URL url = new URL(urlstr);
			URLConnection hConnection = url.openConnection();
			hConnection.setConnectTimeout(10000);
			hConnection.setReadTimeout(10000);
			InputStream in = hConnection.getInputStream();
			int len = 0;
			String str = "";
			byte[] byt = new byte[1024 * 1024];
			while ((len = in.read(byt)) != -1) {
				str += new String(byt, 0, len);
			}
			M3U8 m3u8 = makeM3U8(str);
			if (m3u8.isOrNoEffective())
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static M3U8 makeM3U8(String str) {
		String[] sbyt = str.split("#");
		M3U8 m3u8 = new M3U8();
		M3U8_StreamInfo streamInfo = new M3U8_StreamInfo();
		List<M3U8_ExtInfo> extinfolist = new ArrayList<M3U8_ExtInfo>();
		for (String string : sbyt) {
			String[] extstr = string.split(":");
			if (extstr.length == 2)
				extstr[1] = extstr[1].replace("\r", "").replace("\n", "").trim();
			switch (extstr[0].trim()) {
			case "EXTM3U":
				m3u8.setExtm3u(true);
				break;
			case "EXT-X-VERSION":
				m3u8.setVersion(Integer.valueOf(extstr[1]));
				break;
			case "EXT-X-MEDIA-SEQUENCE":
				m3u8.setMediasequence(Long.valueOf(extstr[1]));
				break;
			case "EXT-X-TARGETDURATION":
				float fl = Float.valueOf(extstr[1]);
				long num = (long) fl * 1000;
				m3u8.setTargetduration(num);
				break;
			case "EXT-X-STREAM-INF":
				String value1 = extstr[1];
				String[] sname1 = value1.split(",");
				for (String s1 : sname1) {
					String[] s2 = s1.split("=");
					if (s2[1].contains("chunklist")) {
						s2[1] = s2[1].replace("\r", "").replace("\n", "");
						int begin = s2[1].indexOf("chunklist");
						s2[1] = s2[1].replace(s2[1].substring(begin, s2[1].length()), "").trim();
					}
					if (s2[0].equals("PROGRAM-ID"))
						streamInfo.setProgramid(Integer.valueOf(s2[1]));
					else if (s2[0].equals("BANDWIDTH"))
						streamInfo.setBandwidth(Long.valueOf(s2[1]));
					else if (s2[0].equals("CODECS"))
						streamInfo.setCodecs(s2[1].replace("\"", ""));
				}
				break;
			case "EXTINF":
				M3U8_ExtInfo extInfo = new M3U8_ExtInfo();
				if (extstr[1].contains(",")) {
					String[] sname2 = extstr[1].split(",");
					float f = Float.valueOf(sname2[0].replace(",", ""));
					long num1 = (long) f * 1000;
					extInfo.setDuration(num1);
					extInfo.setTitle(sname2[1]);
				} else {
					String title = extstr[1].replaceFirst(m3u8.getTargetduration() + "", "");
					extInfo.setDuration(m3u8.getTargetduration());
					extInfo.setTitle(title);
				}
				extinfolist.add(extInfo);
				break;
			default:
				break;
			}
			if (streamInfo != null)
				m3u8.setStreamInfo(streamInfo);
			if (extinfolist != null && extinfolist.size() > 0)
				m3u8.setExtinflist(extinfolist);
		}
		return m3u8;
	}
	
	public static void bubble_sort(long[] times)
    {
        for (int i = 0; i < times.length; i++)
        {
            for (int j = i; j < times.length; j++)
            {
                if (times[i] > times[j])
                {
                    long temp = times[i];
                    times[i] = times[j];
                    times[j] = temp;
                }
            }
        }
    }
}
