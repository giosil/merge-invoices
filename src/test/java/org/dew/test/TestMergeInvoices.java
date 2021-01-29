package org.dew.test;

import java.io.File;

import org.dew.invoices.MergeInvoices;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestMergeInvoices extends TestCase {

  public TestMergeInvoices(String testName) {
    super(testName);
  }
  
  public static Test suite() {
    return new TestSuite(TestMergeInvoices.class);
  }
  
  public void testApp() throws Exception {
    
    MergeInvoices.merge("./samples");
  
  }
  
  public static
  String getDesktop()
  {
    String sUserHome = System.getProperty("user.home");
    return sUserHome + File.separator + "Desktop";
  }
  
  public static
  String getDesktopPath(String sFileName)
  {
    String sUserHome = System.getProperty("user.home");
    return sUserHome + File.separator + "Desktop" + File.separator + sFileName;
  }
}