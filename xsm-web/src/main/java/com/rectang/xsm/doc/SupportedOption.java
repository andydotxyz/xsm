package com.rectang.xsm.doc;

import com.rectang.xsm.io.XSMDocument;

import java.io.Serializable;

/**
 * SupportedOption describes an option that the user can set to effect the output of a generated page.
 * 
 * @author aje
 */
public class SupportedOption implements Serializable {

  public static final int TYPE_INT = 1;
  public static final int TYPE_BOOL = 2;
  public static final int TYPE_STRING = 3;

  private String name, desc;
  private int type;
  private Object fallback;

  public SupportedOption(String name, String description, int type, Object fallback) {
    this.name = name;
    this.type = type;
    this.desc = description;
    this.fallback = fallback;
  }

  public SupportedOption(String name, String description, boolean fallback) {
    this(name, description, TYPE_BOOL, Boolean.valueOf(fallback));
  }

  public SupportedOption(String name, String description, int fallback) {
    this(name, description, TYPE_INT, new Integer(fallback));
  }

  public SupportedOption(String name, String description, String fallback) {
    this(name, description, TYPE_STRING, fallback);
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return desc;
  }

  public int getType() {
    return type;
  }

  public boolean getBoolean(XSMDocument container) {
    if (getType() != TYPE_BOOL)
      throw new IllegalStateException("Option is not a boolean");

    boolean ret = ((Boolean) fallback).booleanValue();
    String option = container.getOption(name);
    if (option != null && !option.equals(""))
      ret = Boolean.valueOf(option).booleanValue();

    return ret;
  }

  public int getInteger(XSMDocument container) {
    if (getType() != TYPE_INT)
      throw new IllegalStateException("Option is not an integer");

    int ret = ((Integer) fallback).intValue();
    String option = container.getOption(name);
    if (option != null && !option.equals(""))
      ret = Integer.parseInt(option);
    return ret;
  }


  public String getString(XSMDocument container) {
    if (getType() != TYPE_STRING)
      throw new IllegalStateException("Option is not an string");

    String ret = (String) fallback;
    String option = container.getOption(name);
    if (option != null && !option.equals(""))
      ret = option;
    return ret;
  }
}
