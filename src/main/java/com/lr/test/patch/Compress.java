package com.lr.test.patch;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

public class Compress
{
  String dirPath = null;
  String tarGzPath = null;
  FileOutputStream fOut = null;
  BufferedOutputStream bOut = null;
  GzipCompressorOutputStream gzOut = null;
  TarArchiveOutputStream tOut = null;
  
  public Compress(String dirPath, String tarGzPath)
  {
    this.dirPath = dirPath;
    this.tarGzPath = tarGzPath;
  }
  
  public static void main(String[] args)
    throws IOException
  {
    Compress c = new Compress("F:\\test\\bxloan-web", "F:\\test\\bxloan-web_20160704_00.tar.gz");
    c.createTarGZ();
    c.tarToGz();
  }
  
  public void createTarGZ()
    throws FileNotFoundException, IOException
  {
    String tarPath = this.tarGzPath.contains(".gz") ? this.tarGzPath.substring(0, this.tarGzPath.indexOf(".gz")) : this.tarGzPath;
    try
    {
      this.fOut = new FileOutputStream(new File(tarPath));
      this.bOut = new BufferedOutputStream(this.fOut);
      this.tOut = new TarArchiveOutputStream(this.bOut);
      System.out.println(tarPath);
      System.out.println(this.dirPath);
      addFileToTarGz(this.tOut, this.dirPath, "");
      this.tOut.finish();
      this.tOut.flush();
      this.tOut.close();
      this.bOut.close();
      this.fOut.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base)
    throws IOException
  {
    File f = new File(path);
    
    String entryName = base + f.getName();
    TarArchiveEntry tarEntry = new TarArchiveEntry(f, entryName);
    tOut.setLongFileMode(2);
    tOut.putArchiveEntry(tarEntry);
    if (f.isFile())
    {
      FileInputStream fos = new FileInputStream(f);
      IOUtils.copy(fos, tOut);
      tOut.closeArchiveEntry();
      fos.close();
    }
    else
    {
      tOut.closeArchiveEntry();
      File[] children = f.listFiles();
      if (children != null)
      {
        File[] arrayOfFile1;
        int j = (arrayOfFile1 = children).length;
        for (int i = 0; i < j; i++)
        {
          File child = arrayOfFile1[i];
          
          addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/");
        }
      }
    }
  }
  
  public void tarToGz(){ 
	  File tarf = null;
    try
    {
      String tarPath = this.tarGzPath.substring(0, this.tarGzPath.indexOf(".gz"));
      tarf= new File(tarPath);
      FileInputStream is = new FileInputStream(tarf);
      FileOutputStream fouts = new FileOutputStream(this.tarGzPath);
      
      GZIPOutputStream gos = new GZIPOutputStream(fouts);
      
      byte[] data = new byte[1024];
      int count;
      while ((count = is.read(data, 0, 1024)) != -1)
      {
        //int count;
        gos.write(data, 0, count);
      }
      is.close();
      gos.finish();
      gos.flush();
      gos.close();
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }finally{
    	if(tarf!=null){
    		tarf.deleteOnExit();
    	}
    }
  }
}
