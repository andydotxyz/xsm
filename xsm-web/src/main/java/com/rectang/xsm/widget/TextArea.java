package com.rectang.xsm.widget;

import com.rectang.xsm.doc.DocWidget;

import java.io.Serializable;

import org.jdom.Element;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.AttributeModifier;

public class TextArea extends DocWidget implements Serializable {
 
  public TextArea(java.lang.String name) {
    super(name);
  }
  
  public void view(Element node, StringBuffer s) {
    s.append(node.getValue().replaceAll("\n", "<br />\n"));
  }
  
  public void publish(Element node, StringBuffer s) {
    s.append(node.getValue().replaceAll("\n", "<br />\n"));
  }

  public WebMarkupContainer edit(java.lang.String wicketId, Element node, java.lang.String path) {
    return new TextAreaPanel(wicketId, node);
  }

  public void create(Element node) {
    node.setText("");
  }

  class TextAreaPanel extends Panel {
    public TextAreaPanel(java.lang.String id, Element node) {
      super(id);

      add(new Label("label", name));
      add(new org.apache.wicket.markup.html.form.TextArea("value", new PropertyModel(node, "text"))
          .add(new AttributeModifier("rows", new Model() {
        public java.lang.String getObject() {
          return getProperty("rows", "12");
        }
      })));
    }
  }
}