package com.lr.test.patch;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.tools.ant.util.DateUtils;

public class PatchIncrement {

	private static String patchResultFolder;
	private static String warFileName ;
	private static String count = "01";
	private static String version ;
	private static String revision;
	private static String patchName;
	private static String subPath = " https://172.16.49.100:8443/svn/jk/weidai/branches/bxloan-r" ;
	private static String commond ;
	public static void main(String[] args) throws Exception {
		if(args.length==2){
			warFileName = args[0];
			version = args[1];
		}else{
			warFileName = "C:"+File.separator+"Users"+File.separator+"Administrator"+File.separator+"Desktop"+File.separator+"打包";
			version = readUserInput("输入当前分支(如:1.5.255.1) : ");
		}
		warFileName = warFileName+File.separator+"bxloan.war";
		System.out.println("war file : "+warFileName);

		subPath += version;
		revision = readUserInput("修订号(如:54218) : ");
		commond = " cmd /c svn diff  -r "+revision+":head"+subPath+" --summarize ";

		count = readUserInput(" 今天第几个包 : ");

		reNameWarFile();
		patchName = getPatchName();
		List<String> retainList = getChangeFileList();
		System.out.println("打包文件个数为:"+retainList.size());
		File f = new File(patchResultFolder);
		removeNoChangeFile(f,retainList);
		 Compress c = new Compress(patchResultFolder, patchName);
	    c.createTarGZ();
	    c.tarToGz();
	    clean();
	
	}
	/* 
     * 读取用户输入 
     * 
     * @param prompt 提示文字 
     * @return 用户输入 
     * @throws IOException 如果读取失败 
     */  
    private static String readUserInput(String prompt) throws IOException {  
         //先定义接受用户输入的变量  
         String result;  
         do {  
              // 输出提示文字  
             System.out.print(prompt);  
             InputStreamReader is_reader = new InputStreamReader(System.in);  
             result = new BufferedReader(is_reader).readLine();  
         }while (isInvalid(result)); // 当用户输入无效的时候，反复提示要求用户输入  
         return result;  
    }
    /** 
     * 检查用户输入的内容是否无效 
     * 
     * @param str 用户输入的内容 
     * @return 如果用户输入的内容无效，则返回 true 
     */  
    private static boolean isInvalid(String str) {  
        return str.equals("");  
    } 
	private static void clean(){
		//delete folder bxloan
		deleteFolder(new File(patchResultFolder));
	}
	private static void deleteFolder(File folder){
		File[] fs = folder.listFiles();
		for(File f:fs){
			if(f.isDirectory()){
				deleteFolder(f);
			}else{
				f.delete();
			}
		}
		folder.delete();
	}
	
	private static String getPatchName(){
		String date = DateUtils.format(new Date(), "yyyyMMdd");
		return patchResultFolder+"_"+date+"_"+count+".tar.gz";
	}
	
	private static void reNameWarFile() throws Exception{
		if(warFileName.endsWith(".war")){
			patchResultFolder = warFileName.substring(0,warFileName.length()-4);
			File warf = new File(warFileName);
			String zipFileName = patchResultFolder+".zip";
			File zipFile = new File(zipFileName);
			warf.renameTo(zipFile);
			DeCompressUtil.deCompress(zipFileName, patchResultFolder);
			zipFile.renameTo(warf);
		}
	}
	private static void removeNoChangeFile(File folder,List<String> retainList ){
		File[] fs = folder.listFiles();
		for(File tf:fs){
			if(tf.isDirectory()){
				removeNoChangeFile(tf,retainList);
			}else{
				String path = tf.getAbsolutePath();
				if(path.endsWith(".class")){
					path = path.substring(0,path.length()-6);
					if(tf.getName().contains("$")){
						path = path.substring(0,path.lastIndexOf("$"));
					}
				}
				if(exist(path,retainList)){
					System.out.println(path);
				}else{
					tf.delete();
					//System.out.println(tf.delete()+"--"+path);;
				}
			}
		}
		if(folder.list().length==0){
			folder.delete();
		}
	}
	private static boolean exist(String path,List<String> retainList ){
		
		for(String t:retainList){
			if(t.startsWith(path)){
				return true;
			}
		}
		return false;
	}
	private static  List<String> getChangeFileList() throws IOException {
		List<String> list = getChangeList();

		List l =  filterPath(list);
		l.add(patchResultFolder+File.separator+"WEB-INF"+File.separator+"lib"+File.separator+"bxloan-commons-0.0.1-SNAPSHOT.jar");
		l.add(patchResultFolder+File.separator+"WEB-INF"+File.separator+"lib"+File.separator+"bxloan-dao-0.0.1-SNAPSHOT.jar");
		l.add(patchResultFolder+File.separator+"WEB-INF"+File.separator+"lib"+File.separator+"bxloan-service-0.0.1-SNAPSHOT.jar");
		return l;
	}
	private static List<String> filterPath(List<String> list){
		List<String> l = new ArrayList<String>();
		for(String f:list){
			String f2 = "";
			if(f.endsWith(".java")){
				String replaceStr =File.separator+"src"+File.separator+"main"+File.separator+"java"+File.separator;
				String str = patchResultFolder+File.separator+"WEB-INF"+File.separator+"classes"+File.separator;
				f2 = f.replace(replaceStr, str);
			}else if(f.contains(File.separator+"src"+File.separator+"main"+File.separator+"resources"+File.separator)){
				f2 = f.replace(File.separator+"src"+File.separator+"main"+File.separator+"resources", patchResultFolder+File.separator+"WEB-INF"+File.separator+"classes");
			}else{
				f2 = f.replace(File.separator+"src"+File.separator+"main"+File.separator+"webapp"+File.separator, patchResultFolder+File.separator);
			}
			l.add(f2);
		}
		return l;
	}
	private static List<String> getChangeList() throws IOException{


		Process process = Runtime.getRuntime().exec(commond);//svn diff  -r  54218 --summarize

		InputStream is = process.getInputStream();
		//is = process.getErrorStream();
		BufferedReader bf=new BufferedReader(new InputStreamReader(is,"GBK"));
		//最好在将字节流转换为字符流的时候 进行转码
		List<String> list = new ArrayList<String>();
		String line="";
		subPath = subPath.replace("/", File.separator);
		while((line=bf.readLine())!=null){
			line = line.replace("/", File.separator);
			if(!line.contains("bxloan-web")){
				continue;
			}
			if(line.contains("src"+File.separator+"main"+File.separator+"resources")){
				continue;
			}
			String web = line.substring(line.indexOf(subPath + File.separator + "bxloan-web"));
			web = web.replace(subPath+File.separator+"bxloan-web","");
			System.out.println(web);
			list.add(web);
		}
		return list;
	}
}
