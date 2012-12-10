package com.rectang.xsm.widget;

import org.jdom.Element;
import com.rectang.xsm.util.HTMLUtils;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.AttributeModifier;

public class HTMLTextArea extends TextArea {

  private boolean wysiwyg;

  public HTMLTextArea(java.lang.String name) {
    this(name, true);
  }

  public HTMLTextArea(java.lang.String name, boolean supportWYSIWYG) {
    super(name);
    setProperty("rows", "24");
    this.wysiwyg = supportWYSIWYG;
  }

  public void setSupportWYSIWYG(boolean support) {
    this.wysiwyg = support;
  }

  public void view(Element node, StringBuffer s) {
    s.append(HTMLUtils.toAbsoluteLinks(node.getValue(), getSite()));
  }
  
  public void publish(Element node, StringBuffer s) {
    s.append(node.getValue());
  }

  public WebMarkupContainer edit(java.lang.String wicketId, Element node, java.lang.String path) {
    return new HTMLTextAreaPanel(wicketId, node);
  }

  class HTMLTextAreaPanel extends Panel {
    public HTMLTextAreaPanel(java.lang.String id, Element node) {
      super(id);

      add(new org.apache.wicket.markup.html.form.TextArea("value", new PropertyModel(node, "text"))
          .add(new AttributeModifier("rows", new Model() {
        public java.lang.String getObject() {
          return getProperty("rows", "12");
        }
      })).add(new AttributeModifier("class", new Model() {
        public java.lang.String getObject() {
          if ( wysiwyg ) {
            return "mceEditor";
          }

          return "";
        }
      })));
    }
  }
}