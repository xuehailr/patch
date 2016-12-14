package com.lr.test.patch;

import java.io.File;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Expand;

public class DeCompressUtil {  
	private static void unzip(String sourceZip,String destDir) throws Exception{   
	    try{   
	        Project p = new Project();   
	        Expand e = new Expand();   
	        e.setProject(p);   
	        e.setSrc(new File(sourceZip));   
	        e.setOverwrite(false);   
	        e.setDest(new File(destDir));   
	        e.setEncoding("gbk");   
	        e.execute();   
	    }catch(Exception e){   
	        throw e;   
	    }   
	}   
 

	public static void deCompress(String sourceFile,String destDir) throws Exception{   
	    char lastChar = destDir.charAt(destDir.length()-1);   
	    if(lastChar!='/'&&lastChar!='\\'){   
	        destDir += File.separator;   
	    }   
	    String type = sourceFile.substring(sourceFile.lastIndexOf(".")+1);   
	    if(type.equals("zip")){   
	        DeCompressUtil.unzip(sourceFile, destDir);   
	     }else{   
	         throw new Exception("only zip");   
	     }   
	 }
} 