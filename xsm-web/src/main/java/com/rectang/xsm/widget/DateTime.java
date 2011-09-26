package com.rectang.xsm.widget;

import java.text.SimpleDateFormat;
import java.io.Serializable;

import org.jdom.Element;

public class DateTime extends Date implements Serializable {

  private static SimpleDateFormat store = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  private static SimpleDateFormat format = new SimpleDateFormat("EEE, d MMMM yyyy 'at' hh:mm aaa");

  public DateTime(java.lang.String name) {
    super(name);
  }

  public static synchronized java.util.Date parseDateTime(Element node) {
    if (node == null || node.getValue() == null || node.getValue().equals(""))
      return new java.util.Date();
    try {
      return store.parse(node.getValue());
    } catch (Exception e) {
      return new java.util.Date();
    }
  }

  public static synchronized java.lang.String formatDateTime(java.util.Date date) {
    return store.format(date);
  }

  private synchronized java.lang.String renderDateTime(java.util.Date date) {
    return format.format(date);
  }
 
  public void view(Element node, StringBuffer s) {
    s.append(renderDateTime(parseDateTime(node)));
  }
  
  public void publish(Element node, StringBuffer s) {
    s.append(renderDateTime(parseDateTime(node)));
  }

  protected SimpleDateFormat getStoreFormat() {
    return store;
  }

  public void create(Element node) {
    node.setText(formatDateTime(new java.util.Date()));
  }
}