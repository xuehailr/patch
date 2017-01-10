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
/**
 *  * 标记清除 内存碎片 效率高
	 * 复制  内存浪费 
	 * 标记整理 效率低 
	 * serial  年轻代 单线程 stop the world  与cms收集器搭配 
	 * parnew 年轻代  多线程版本的serial stop the world 与cms收集器搭配
	 * parallal new 年轻代  多线程 吞吐量优先收集器 可设置吞吐量和最低停顿时间  与parallal old ,seral old搭配

	 * serial old 老年代  单线程 stop the world 
	 * parallal old 老年代  多线程 吞吐量优先收集器 可设置吞吐量和最低停顿时间
	 * cms 老年代  第一个并发垃圾收集器 收集过程包括四个阶段  初始标记  并发标记  重新标记  并发清除  缺点  :浮动垃圾  并发标记时会启动多个线程  降低应用的吞吐量.    
	 * G1 新一代垃圾收集器  将内存划分为若干块 根据最低停顿时间 优先收集垃圾最多的区域.
 * @author Administrator
 *
 */
public class PatchIncrement {
	private static String patchFolder;
	private static String patchResultFolder;
	private static String warFileName ;
	private static String count = "01";
	private static String patchName;
	private static boolean isConfig = false;
	public static void main(String[] args) throws Exception {
		if(args.length==2){
			patchFolder = args[0];
			warFileName = args[1];
		}else{
			patchFolder = "C:\\Users\\Administrator\\Desktop\\打包\\test\\bxloan-web";
			warFileName = "C:\\Users\\Administrator\\Desktop\\打包\\test\\bxloan.war";
		}
		warFileName = warFileName.replace("\\\\", File.separator);
		patchFolder = patchFolder.replace("\\\\", File.separator);
		System.out.println("patchFolder=="+patchFolder);
		System.out.println("warFileName=="+warFileName);
		count = readUserInput("please input version:");
		//isConfig = "1".equals(readUserInput("please input isConfig(打包配置文件,请输入1,否则0):"));
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
             System.out.println(prompt);  
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
		if(isConfig){
			return patchResultFolder+"_config_"+date+"_"+count+".tar.gz";
		}else{
			return patchResultFolder+"_"+date+"_"+count+".tar.gz";
		}
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
	private static  List<String> getChangeFileList() throws IOException{
		List<String> list = new ArrayList<String>();
		File f = new File(patchFolder);
		getSubFilePath(f,list);
		List l =  filterPath(list);
		if(!isConfig){
			if("y".equalsIgnoreCase(readUserInput("if commons.jar changes ? y or n"))){
				l.add(patchResultFolder+File.separator+"WEB-INF"+File.separator+"lib"+File.separator+"bxloan-commons-0.0.1-SNAPSHOT.jar");
			}
			if("y".equalsIgnoreCase(readUserInput("if dao.jar changes ? y or n"))){
				l.add(patchResultFolder+File.separator+"WEB-INF"+File.separator+"lib"+File.separator+"bxloan-dao-0.0.1-SNAPSHOT.jar");
			}
			if("y".equalsIgnoreCase(readUserInput("if service.jar changes ? y or n"))){
				l.add(patchResultFolder+File.separator+"WEB-INF"+File.separator+"lib"+File.separator+"bxloan-service-0.0.1-SNAPSHOT.jar");
			}
			//l.add(patchResultFolder+File.separator+"WEB-INF"+File.separator+"lib"+File.separator+"jcommon-1.0.21.jar");
			//l.add(patchResultFolder+File.separator+"WEB-INF"+File.separator+"lib"+File.separator+"jfreechart-1.0.17.jar");
		}
		return l;
	}
	private static List<String> filterPath(List<String> list){
		List<String> l = new ArrayList<String>();
		for(String f:list){
			String f2 = "";
			if(f.endsWith(".java")){
				String replaceStr =patchFolder+File.separator+"src"+File.separator+"main"+File.separator+"java"+File.separator;
				String str = patchResultFolder+File.separator+"WEB-INF"+File.separator+"classes"+File.separator;
				f2 = f.replace(replaceStr, str);
			}else if(f.contains(File.separator+"src"+File.separator+"main"+File.separator+"resources"+File.separator)){
				f2 = f.replace(patchFolder+File.separator+"src"+File.separator+"main"+File.separator+"resources", patchResultFolder+File.separator+"WEB-INF"+File.separator+"classes");
			}else{
				f2 = f.replace(patchFolder+File.separator+"src"+File.separator+"main"+File.separator+"webapp"+File.separator, patchResultFolder+File.separator);
			}
			l.add(f2);
		}
		return l;
	}
	private static void getSubFilePath(File folder ,List<String> list){
		File[] fs = folder.listFiles();
		for(File tf:fs){
			if(tf.isDirectory()){
				getSubFilePath(tf, list);
			}else{
				list.add(tf.getAbsolutePath());
			}
		}
	}
}
