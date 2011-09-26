package com.rectang.xsm.widget;

import com.rectang.xsm.doc.DocWidget;

import java.io.Serializable;

import org.jdom.Element;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;

public class String extends DocWidget implements Serializable {
 
  public String(java.lang.String name) {
    super(name);
  }

  private java.lang.String escape(java.lang.String in) {
    java.lang.String lower = in.toLowerCase();

    if (lower.startsWith("<html>")) {
      if (lower.endsWith("</html>")) {
        return in.substring(6, in.length() - 7);
      } else {
        return in.substring(6);
      }
    } else {
      return in.replaceAll("&", "&amp;").replaceAll("\"", "&quot;")
          .replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
  }
  public void view(Element node, StringBuffer s) {
    s.append(escape(node.getValue()));
  }
  
  public void publish(Element node, StringBuffer s) {
    s.append(escape(node.getValue()));
  }

  public WebMarkupContainer edit(java.lang.String wicketId, Element node, java.lang.String path) {
    return new StringPanel(wicketId, node);
  }
  
  public void create(Element node) {
    node.setText("");
  }

  class StringPanel extends Panel {
    public StringPanel(java.lang.String id, Element node) {
      super(id);

      add(new Label("label", name));
      add(new TextField("value", new PropertyModel(node, "text")));
    }
  }
}