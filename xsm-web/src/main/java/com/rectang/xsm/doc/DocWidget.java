package com.rectang.xsm.doc;

import java.util.Properties;
import java.util.Enumeration;

/**
 * @author aje
 *
 * DocWidget - the base class for all renderable elements
 */
public class DocWidget extends DocElement {

  private Properties props = new Properties();
  
  public DocWidget(String name) {
    this(name, new Properties());
  }

  public DocWidget(String name, Properties props) {
    super(name);
    setProperties(props);
  }
  /**
   * @param prop the property key 
   * @return Returns the property specified.
   */
  public String getProperty(String prop) {
    return props.getProperty(prop);
  }
  /**
   * @param prop the property key
   * @param def the defualt to return if prop does not exist 
   * @return Returns the property specified.
   */
  public String getProperty(String prop, String def) {
    return props.getProperty(prop, def);
  }
  /**
   * @param prop The prop to set
   * @param val  The val to set the prop to
   * 
   * @return The previous property value, or null if there was no value
   */
  public String setProperty(String prop, String val) {
    return (String) props.setProperty(prop, val);
  }
  /**
   * Set all properties into this widget.
   * @param props The list of properties to set
   */
  public void setProperties(Properties props) {
    Enumeration keys = props.keys();
    while (keys.hasMoreElements()) {
      String next = (String) keys.nextElement();
      props.setProperty(next, props.getProperty(next));
    }
  }
}
