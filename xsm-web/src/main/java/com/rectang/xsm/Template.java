package com.rectang.xsm;

public class Template {
  
  public static String container(String title, String body) {
    return startContainer(title) + body + endContainer();
  }

  public static String narrowContainer(String title, String body) {
    return startNarrowContainer(title) + body + endContainer();
  }

  public static String startContainer(String title) {
    String ret = "";
    ret += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"xsm-editor-message-wide\"><tr>\n";
    ret += "<th class=\"xsm-editor-message-title\">" +title + "</th>\n";
    ret += "</tr><tr>\n<td class=\"xsm-editor-message-main\">\n";
    ret += "<table border=\"0\" width=\"100%\"><tr><td>\n";
    return ret;
  }

  public static String startNarrowContainer(String title) {
    String ret = "";
    ret += "<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"xsm-editor-message\"><tr>\n";
    ret += "<th class=\"xsm-editor-message-title\">" +title + "</th>\n";
    ret += "</tr><tr>\n<td class=\"xsm-editor-message-main\">\n";
    ret += "<table border=\"0\" width=\"100%\"><tr><td>\n";
    return ret;
  }

  public static String endContainer() {
    return"\n</td></tr></table>\n</td>\n</tr></table>\n";
  }
}