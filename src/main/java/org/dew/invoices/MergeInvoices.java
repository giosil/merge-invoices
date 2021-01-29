package org.dew.invoices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.URL;

import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public 
class MergeInvoices 
{
  public static final String OUT_FILE = "_invoices.html";
  
  public static
  void main(String[] args)
  {
    String srcFolder = ".";
    
    if(args != null && args.length > 0) {
      srcFolder = args[0];
    }
    
    try {
      merge(srcFolder);
    }
    catch(Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }
  
  public static
  int merge(String srcFolder)
    throws Exception
  {
    if(srcFolder == null || srcFolder.length() == 0) {
      srcFolder = ".";
    }
    
    File fSrcfolder = new File(srcFolder);
    if(!fSrcfolder.exists()) {
      System.out.println("Source folder " + fSrcfolder + " not exists.");
      return -1;
    }
    
    File[] afFiles = fSrcfolder.listFiles();
    if(afFiles == null || afFiles.length == 0) {
      System.out.println("Source folder " + srcFolder + " contains no files.");
      return 0;
    }
    
    return merge(afFiles, srcFolder);
  }
  
  public static
  int merge(String srcFolder, String outFolder)
    throws Exception
  {
    if(srcFolder == null || srcFolder.length() == 0) {
      srcFolder = ".";
    }
    if(outFolder == null || outFolder.length() == 0) {
      outFolder = srcFolder;
    }
    
    File fSrcfolder = new File(srcFolder);
    if(!fSrcfolder.exists()) {
      System.out.println("Source folder " + fSrcfolder + " not exists.");
      return -1;
    }
    
    File fOutfolder = new File(outFolder);
    if(!fOutfolder.exists()) {
      System.out.println("Output folder " + fOutfolder + " not exists.");
      return -1;
    }
    
    File[] afFiles = fSrcfolder.listFiles();
    if(afFiles == null || afFiles.length == 0) {
      System.out.println("Source folder " + srcFolder + " contains no files.");
      return 0;
    }
    
    return merge(afFiles, outFolder);
  }
  
  public static
  int merge(List<File> listFiles, String outFolder)
    throws Exception
  {
    if(listFiles == null || listFiles.size() == 0) {
      return 0;
    }
    
    File[] afFiles = new File[listFiles.size()];
    for(int i = 0; i < listFiles.size(); i++) {
      afFiles[i] = listFiles.get(i);
    }
    
    return merge(afFiles, outFolder);
  }
  
  public static
  int merge(File[] afFiles, String outFolder)
    throws Exception
  {
    if(afFiles == null || afFiles.length == 0) {
      return 0;
    }
    
    if(outFolder == null || outFolder.length() == 0) {
      outFolder = ".";
    }
    
    File fOutfolder = new File(outFolder);
    if(!fOutfolder.exists()) {
      System.out.println("Output folder " + fOutfolder + " not exists.");
      return -1;
    }
    
    int count = 0;
    OutputStream out = null;
    for(int i = 0; i < afFiles.length; i++) {
      File file = afFiles[i];
      
      if(file == null || file.isDirectory()) continue;
      
      String fileName = file.getName();
      
      String ext = "";
      int iSepExt = fileName.lastIndexOf('.');
      if(iSepExt > 0) {
        ext = fileName.substring(iSepExt + 1);
      }
      if(!ext.equalsIgnoreCase("xml")) continue;
      
      byte[] content = readFile(file.getAbsolutePath());
      if(content == null || content.length < 10) continue;
      
      try {
        URL url = getStyleSheet(content);
        
        if(url == null) continue;
        
        Source xslSource = new StreamSource(url.toString());
        Source xmlSource = new StreamSource(new ByteArrayInputStream(content));
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(xslSource);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Result htmResult = new StreamResult(baos);
        transformer.transform(xmlSource, htmResult);
        byte[] result = baos.toByteArray();
        
        if(result != null && result.length > 10) {
          count++;
          
          String html = new String(result);
          if(count == 1) {
            int iEndBody = html.indexOf("</body>");
            if(iEndBody > 0) {
              html = html.substring(0, iEndBody);
              html += "<div style=\"page-break-before: always;\"></div>";
            }
          }
          else {
            int iStartBody = html.indexOf("<body>");
            int iEndBody   = html.indexOf("</body>");
            if(iStartBody > 0 && iEndBody > 0) {
              html = html.substring(iStartBody+6, iEndBody);
              html += "<div style=\"page-break-before: always;\"></div>";
            }
          }
          
          out = new FileOutputStream(outFolder + File.separator + OUT_FILE, count > 1);
          out.write(html.getBytes());
          out.close();
          
          System.out.println("Transform " + fileName + " -> OK");
        }
      }
      catch(Exception ex) {
        System.out.println("Transform " + fileName + ": " + ex);
      }
      finally {
        if(out != null) try { out.close(); } catch(Exception ex) {}
      }
    }
    
    if(count > 0) {
      out = new FileOutputStream(outFolder + File.separator + OUT_FILE, true);
      out.write("</body></html>".getBytes());
      out.close();
    }
    
    return count;
  }
  
  public static
  byte[] readFile(String sFile)
    throws Exception
  {
    int iFileSep = sFile.indexOf('/');
    if(iFileSep < 0) iFileSep = sFile.indexOf('\\');
    InputStream is = null;
    if(iFileSep < 0) {
      URL url = Thread.currentThread().getContextClassLoader().getResource(sFile);
      is = url.openStream();
    }
    else {
      is = new FileInputStream(sFile);
    }
    try {
      int n;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buff = new byte[1024];
      while((n = is.read(buff)) > 0) baos.write(buff, 0, n);
      return baos.toByteArray();
    }
    finally {
      if(is != null) try{ is.close(); } catch(Exception ex) {}
    }
  }
  
  public static
  URL getStyleSheet(byte[] content) 
  {
    if(content == null || content.length == 0) {
      return null;
    }
    String sTail = tail(content, 25);
    if(sTail == null || sTail.length() == 0) {
      return null;
    }
    if(sTail.endsWith("FatturaElettronica>")) {
      return Thread.currentThread().getContextClassLoader().getResource("fpa.xsl");
    }
    if(sTail.endsWith("RicevutaConsegna>")) {
      return Thread.currentThread().getContextClassLoader().getResource("frc.xsl");
    }
    if(sTail.endsWith("NotificaScarto>")) {
      return Thread.currentThread().getContextClassLoader().getResource("fns.xsl");
    }
    if(sTail.endsWith("RicevutaScarto>")) {
      return Thread.currentThread().getContextClassLoader().getResource("fns.xsl");
    }
    if(sTail.endsWith("MancataConsegna>")) {
      return Thread.currentThread().getContextClassLoader().getResource("fmc.xsl");
    }
    if(sTail.endsWith("ImpossibilitaRecapito>")) {
      return Thread.currentThread().getContextClassLoader().getResource("fmc.xsl");
    }
    return null;
  }
  
  public static
  String tail(byte[] content, int length)
  {
    if(content == null || content.length == 0) {
      return "";
    }
    
    // Tail (printable characters)
    byte[] buffer = new byte[length];
    int idx = 0;
    for(int i = 0; i < content.length; i++) {
      byte b = content[content.length - i - 1];
      if(b < 33 || b > 127) continue;
      
      buffer[idx] = b;
      idx++;
      if(idx >= length) break;
    }
    
    // Reverse
    byte[] result = new byte[idx];
    for(int i = 0; i < idx; i++) {
      result[i] = buffer[buffer.length - i - 1];
    }
    
    return new String(result);
  }
}
