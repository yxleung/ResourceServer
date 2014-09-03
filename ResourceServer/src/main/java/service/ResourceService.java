package service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import utils.ContentUtils;
import utils.FileUtils;
import utils.HttpUtils;
import utils.JsonUtils;
import utils.RespUtils;
import utils.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import config.Config;

public class ResourceService {
	private static String url = "http://127.0.0.1:4002";
	
	public static boolean hasThisResource(int playerid,int resourceid){
		try {
			Map<String,String> requestProperty=new HashMap<String, String>();
			requestProperty.put("deviceid", "resourceserver");
			requestProperty.put("protocol", "0x02");
			requestProperty.put("playerid", playerid+"");
			
			Map<String,Object> body = new HashMap<String, Object>();
			body.put("resourceid", resourceid);
			String data=JsonUtils.encode2Str(body);
			String result=HttpUtils.doPost(url, requestProperty,data);
			if(StringUtils.isNotBlank(result)){
				JsonNode node=JsonUtils.decode(result);
				int code=node.get("code").asInt();
				if(code==0){
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public static void downloadResource(HttpServletRequest req,
			HttpServletResponse resp, JsonNode reqData) throws IOException {
		System.out.println("downloadResource request");
		int resourceid=JsonUtils.getInt("resourceid", reqData);
		int playerid=JsonUtils.getInt("playerid",reqData);
		if(resourceid==-1||playerid==-1){
			RespUtils.commonResp(resp, 1,"Bad request.");
			return;
		}
		
		boolean isAuth=hasThisResource(playerid, resourceid);
		if(!isAuth){
			RespUtils.commonResp(resp, 2,"Did not has this resource.");
			return;
		}
		
		try {
			File file=new File(Config.RESOURCE_DIR+resourceid);
			RandomAccessFile readFile =new RandomAccessFile(file, "r");
			resp.reset();
			resp.setHeader("Accept-Ranges", "bytes");
			resp.addHeader("Content-Disposition","attachment; filename=\"" + file.getName() + "\"");
			resp.setContentType(ContentUtils.getContentType(file.getName()));
			int bufferSize = 1024;
			long begin = 0;
			long end = 0;
			long fileLength = readFile.length();
			long contentLength = 0;
			String rangeBytes = req.getHeader("Range");
			if (StringUtils.isNotBlank(rangeBytes)) {
				rangeBytes = rangeBytes.trim();
				rangeBytes = rangeBytes.replaceAll("bytes=", "");
				String[] range = rangeBytes.split("-");
				if (range.length == 1) {// bytes=969998336-
					begin = Long.valueOf(range[0].trim());
					end = fileLength;
					contentLength = end - begin;
				} else if (range.length == 2) {
					begin = Long.valueOf(range[0].trim());
					end = Long.valueOf(range[1].trim());
					if(begin<end){
						contentLength = end - begin;
					}else{
						begin=0;
						end=fileLength;
						contentLength = end - begin;
					}
				}
			} else {
				begin=0;
				end=fileLength;
				contentLength = end - begin;
			}
			String contentRange = new StringBuffer("bytes ").append(begin)
					.append("-").append(end-1).append("/").append(fileLength)
					.toString();
			resp.setHeader("Content-Range", contentRange);
			resp.setStatus(javax.servlet.http.HttpServletResponse.SC_PARTIAL_CONTENT);
			resp.addHeader("Content-Length", String.valueOf(contentLength)); 
			
			OutputStream os = resp.getOutputStream();
			OutputStream out = new BufferedOutputStream(os);
			byte b[] = new byte[bufferSize];
			
			readFile.seek(begin);
			while (begin < end) {
				int len = 0;
				if (begin + bufferSize < end){
					len = readFile.read(b);
				} else {
					len = readFile.read(b, 0,(int) (end - begin));
				}
				out.write(b, 0, len);
				begin += len;
			}
			if(out!=null){
				out.flush();
				out.close();
			}
			if(os!=null)
				os.close();
			if(readFile!=null)
				readFile.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			resp.setStatus(400);
		}
	}
	
	public static void getFileLength(HttpServletRequest req,
			HttpServletResponse resp, JsonNode reqData){
		RandomAccessFile reader=null;
		try {
			int resourceid=JsonUtils.getInt("resourceid", reqData);
			int playerid=JsonUtils.getInt("playerid",reqData);
			if(resourceid==-1||playerid==-1){
				RespUtils.commonResp(resp, 1,"Bad request.");
				return;
			}
			
			boolean isAuth=hasThisResource(playerid, resourceid);
			if(!isAuth){
				RespUtils.commonResp(resp, 2,"Did not has this resource.");
				return;
			}
			File file=new File(Config.RESOURCE_DIR+resourceid);
			reader = new RandomAccessFile(file, "r");
			long length = reader.length();
			String md5=FileUtils.getFileMD5String(file);
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("length", length);
			result.put("md5", md5);
			RespUtils.jsonResp(resp, 0, result);
		} catch (Exception e) {
			e.printStackTrace();
			RespUtils.commonResp(resp, 3,"can not find resource");
		}finally{
			if(reader!=null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}