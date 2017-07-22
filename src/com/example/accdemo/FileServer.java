package com.example.accdemo;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;

import com.edroid.common.utils.FileUtils;
import com.edroid.common.utils.HttpUtils;
import com.edroid.common.utils.Logger;

/**
 * 文件服务器
 * 
 * @author Jianbin 2017-6-6
 *
 */
public class FileServer {
	static final Logger log = Logger.create(FileServer.class);
	
	private static final String PREFIX = "--", LINE_END = "\r\n";
	static final String CHARSET = "UTF-8";
	static final String HOST 	= your server;
	static final String URL 	= your server;
	

	private static void writeField(PrintWriter writer, String bund, String k, Object v) {
		writer.append(PREFIX).append(bund).append(LINE_END);
	    writer.append("Content-Disposition: form-data; name=\"").append(k).append("\"")
	        .append(LINE_END);
	    writer.append("Content-Type: text/plain; charset=")
	        .append(CHARSET)
	        .append(LINE_END);
	    writer.append(LINE_END);
	    writer.append(v.toString()).append(LINE_END);
	    writer.flush();
	}
	
	/**
	 * 文件上传
	 * 
	 * 表单字段：file; name
	 * 
	 * @param keepname 保留文件名存储，否则以文件MD5作为文件名
	 */
	public static PutRet putFile(String bucket, String bpath, String filepath, boolean keepname) {
		PutRet ret = new PutRet();
		
		log.i("putFile " + filepath + " -> " + bucket + "/" + bpath);
		
	    HttpURLConnection conn = null;
	    DataOutputStream dos = null;
	    DataInputStream inStream = null;
	    
	    String existingFileName = filepath;
	    String lineEnd = "\r\n";
	    String twoHyphens = "--";
	    String boundary = "*****";
	    
	    final int maxBufferSize = 2 * 1024 * 1024;
	    String urlString = URL;
	    long t0 = System.currentTimeMillis();
	    
	    try {
	        //------------------ CLIENT REQUEST
	        // open a URL connection to the Servlet
	        URL url = new URL(urlString);
	        // Open a HTTP connection to the URL
	        conn = (HttpURLConnection) url.openConnection();
	        // Allow Inputs
	        conn.setDoInput(true);
	        // Allow Outputs
	        conn.setDoOutput(true);
	        // Don't use a cached copy.
	        conn.setUseCaches(false);
	        // Use a post method.
	        conn.setRequestMethod("POST");
//	        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
	        conn.setRequestProperty("Connection", "Keep-Alive");
	        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//	        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//	        conn.setRequestProperty("Content-Length", "343847");
//	        conn.setRequestProperty("Host", "192.168.8.100:8090");
//	        conn.setRequestProperty("Origin", "192.168.8.180");
//	        conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
//	        conn.setRequestProperty("Referer", "http://localhost/hook2/upload.html");
	        
	        
	        PrintWriter pw = new PrintWriter(conn.getOutputStream());
	        
	        writeField(pw, boundary, "bucket", bucket);
			writeField(pw, boundary, "path", bpath);
			writeField(pw, boundary, "keepname", keepname);
	        
	        dos = new DataOutputStream(conn.getOutputStream());
	        dos.writeBytes(twoHyphens + boundary + lineEnd);
	        dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + existingFileName + "\"" + lineEnd);
	        dos.writeBytes(lineEnd);
	        
	        // create a buffer of maximum size
	        FileInputStream fis = new FileInputStream(new File(existingFileName));
	        int l = fis.available();
	        int bufferSize = Math.min(l, maxBufferSize);
	        byte[] buffer = new byte[bufferSize];
	        
	        // read file and write it into form...
	        int r = 0;
	        while((r = fis.read(buffer)) > 0) {
	            dos.write(buffer, 0, r);
	            dos.flush();
	        }

	        // send multipart form data necesssary after file data...
	        dos.writeBytes(lineEnd);
	        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	        // close streams
	        log.i("File is written");
	        fis.close();
	        dos.flush();

	        dos.close();
	    } catch (Exception ioe) {
	    	ioe.printStackTrace();
	    }

	    //------------------ read the SERVER RESPONSE
	    try {
	        inStream = new DataInputStream(conn.getInputStream());
	        
	        String s = HttpUtils.is2String(inStream, false);
			inStream.close();
			log.i("postFile ret=" + s);

			JSONObject jb = new JSONObject(s);
			if(jb.getInt("code") == 200) {
				ret.success = true;
				
				JSONObject data = jb.getJSONObject("data");
				ret.md5 = data.getString("md5");
				ret.path = data.getString("path");
				ret.size = data.getInt("size");
				ret.time = (int) (System.currentTimeMillis() - t0);
			} else {
				log.i("postFile err: " + jb.optString("msg"));
			}
	    } catch (Exception ioex) {
	    	ret.err = ioex.getMessage();
	    	ioex.printStackTrace();
	    }
	    
	    return ret;
	}
	
	public static PutRet putFile(String bucket, String bpath, String filepath) {
		return putFile(bucket, bpath, filepath, false);
	}
	
	public static class PutRet {
		public boolean success;
		public String err;
		public String path;
		public String md5;
		public int size;
		public int time;
		
		@Override
		public String toString() {
			if(success)
				return path + '|' + md5 + '|' + size + '|' + time + "ms speed=" + FileUtils.coverSize(Math.round(size / (time/1000f))) + "s";
			else
				return "fail|" + err;
		}
	}
	
	public static class GetRet {
		public boolean success;
		public String url;
		public String err;
		public long size;
		public int time;
		
		@Override
		public String toString() {
			if(success)
				return url + '|' + size + '|' + time + "ms speed=" + (size / (time/1000f)) + "s";
			else
				return "fail|" + err;
		}
	}
	
	/**
	 * 下载文件
	 * 
	 * @param path
	 * @param dst
	 * 
	 * @return
	 */
	public static GetRet getFile(String path, String dst) {
		GetRet ret = new GetRet();
		try {
			File f = new File(dst);
			if(f.exists())
				f.delete();
			
			ret.url = HOST + path;
			
			HttpURLConnection conn = (HttpURLConnection) new URL(ret.url).openConnection();
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
			FileOutputStream fos = new FileOutputStream(f);
			
			long t0 = System.currentTimeMillis();
			byte[] buf = new byte[1024];
			int read;
			while((read = bis.read(buf)) != -1) {
				fos.write(buf, 0, read);
			}
			bis.close();
			fos.flush();
			fos.close();
			conn.disconnect();
			
			ret.success = true;
			ret.time = (int) (System.currentTimeMillis() - t0);
			
			log.i("getFile url=" + ret.url + " ret=" + ret);
		} catch (Exception e) {
			ret.err = e.getMessage();
			e.printStackTrace();
		}
		
		return ret;
	}
}
