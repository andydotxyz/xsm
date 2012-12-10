package com.rectang.xsm.widget;

import com.rectang.xsm.UserData;
import com.rectang.xsm.doc.DocWidget;
import com.rectang.xsm.io.XSMDocument;

import java.util.Date;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.io.Serializable;

import org.jdom.Element;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.Component;

public class Value extends DocWidget implements Serializable {

  public static final int USERNAME = 1;
  public static final int FULLNAME = 2;
  public static final int EMAIL = 4;
  public static final int DATE = 3;
  
  private int valueType;
  private SimpleDateFormat formatter = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss ZZZZ");
  
  public Value(java.lang.String name) {
    this(name,0); 
  }
  public Value(java.lang.String name, int type) {
    super(name);
    valueType = type;
  }
  
  public void view(Element node, StringBuffer s) {
    s.append(node.getValue());
  }
  
  public void publish(Element node, StringBuffer s) {
    s.append(node.getValue());
  }
  
  public WebMarkupContainer edit(java.lang.String wicketId, Element node, java.lang.String path) {
    /* cannot edit values */
    return new ValuePanel(wicketId, name, node.getValue());
  }
  
  public void create(Element node) {
    UserData user = getUser();
    switch (valueType) {
      case USERNAME:
        node.setText(user.getUsername());
        break;
      case FULLNAME:
        node.setText(user.getName());
        break;
      case EMAIL:
        node.setText(user.getEmail());
        break;
      case DATE:
        node.setText(formatter.format(new Date()));
        break;
    }
  }

  class ValuePanel extends Panel {
    public ValuePanel(java.lang.String id, java.lang.String key, java.lang.String value) {
      super(id);

      add(new Label("key", key));
      add(new Label("value", value));
    }
  }
}