package com.rectang.xsm;

import javax.servlet.http.*;
import java.io.*;

public class XSM extends HttpServlet {

  private static Config config;

  public void init() {
    config = Config.getInstance(this.getServletContext().getRealPath("/"));
  }

  public static Config getConfig() {
    return config;
  }

  public static void setConfig(Config conf) {
    config = conf;
  }

  public static File getTempFile() {
    return getTempFile("");
  }

  public static File getTempFile(File name) {
    return getTempFile(name.getName());
  }

  private static int counter = 0;
  public static File getTempFile(String name) {
    String theName = "";
    if (name != null && !name.equals(""))
      theName = "_" + name;

    return new File(getConfig().getTmpDir() + "xsm_" + 
        String.valueOf(System.currentTimeMillis()) + String.valueOf(counter++) +
        theName);
  }
}

