package service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import test.MD5Util;
import utils.HttpUtils;
import utils.JsonUtils;

public class Test {
	public static void main(String[] args) {
		String url="http://127.0.0.1:8080/ResourceServer/res";
		String file="g:/【66影视www.66ys.cc】超验骇客HD中字1280高清.mp4";
		String saveFile="d:/【66影视www.66ys.cc】超验骇客HD中字1280高清.mp4";
//		Map<String,String> params=new HashMap<String, String>();
//		params.put("method", "getFileLength");
//		params.put("resourceid", file);
//		String r=HttpUtils.doPost(url, JsonUtils.encode2Str(params));
//		JsonNode result=JsonUtils.decode(r);
//		int threadCount=5;
//		if(result!=null&&result.get("code").asInt()==0){
//			long length=result.get("data").get("length").asLong();
//			System.out.println("file length:"+length);
//			List<long[]> list=spiltFile(length, threadCount);
//			Map<String,String> loadFileParams=new HashMap<String, String>();
//			loadFileParams.put("method", "downloadResource");
//			loadFileParams.put("resourceid", file);
//			System.out.println("开始下载文件");
//			for(long [] l:list){
//				System.out.println(l[0]+"-"+l[1]);
//				new Thread(new downloader(url, loadFileParams, l,saveFile)).start();
//			}
//		}else{
//			System.out.println("请求文件失败");
//		}
		
		
		try {
			//校验文件
			System.out.println(MD5Util.getFileMD5String(new File(file)));
			System.out.println(MD5Util.getFileMD5String(new File(saveFile)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static List<long[]> spiltFile(long fileLength, int size) {
		List<long[]> result = new ArrayList<long[]>();
		for (int i = 0; i < size; i++) {
			long begin = fileLength / size * (i);
			long end = fileLength / size * (i + 1);
			if (i == 4) {
				end = fileLength;
			}
			long[] temp = new long[] { begin, end };
			result.add(temp);
			System.out.println(i + "    " + begin + "-" + end);
		}
		return result;
	}
}

class downloader implements Runnable{
	private long[] block;
	private String url;
	private Map<String,String> params;
	private String file;
	public downloader(String url,Map<String,String> params,long[] block,String file) {
		this.url=url;
		this.params=params;
		this.block=block;
		this.file=file;
	}
	@Override
	public void run() {
		try {
			long begin=this.block[0];
			long end=this.block[1];
			HttpUtils.doPost(url, begin, end, JsonUtils.encode2Str(params), this.file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
	}
}

class fuck implements Runnable{

	@Override
	public void run() {
		Map<String,String> loadFileParams=new HashMap<String, String>();
		loadFileParams.put("method", "downloadResource");
		loadFileParams.put("resourceid", "g:/test.rmvb");
		HttpUtils.doPost("http://127.0.0.1:8080/ResourceServer/res", 0, 301704709, JsonUtils.encode2Str(loadFileParams), "d:/fuckadf");
	}
	
}