package com.rectang.xsm.types;

import com.rectang.xsm.doc.*;

import com.rectang.xsm.widget.String;

import org.jdom.Element;

public class Links extends DocGroup {

  public Links(java.lang.String name) {
    super(name, new LinkCategory("category"));
  }

}

class LinkCategory extends DocList {

  public LinkCategory(java.lang.String name) {
    super(name, new DocElement[] {new String("title"),
        new DocGroup("links", new LinkItem("link"))});
  }
  
  public void view(Element node, StringBuffer s) {
    s.append("<h2>");
    elements[0].view(node.getChild("title"), s);
    s.append("</h2>");
    elements[1].view(node.getChild("links"), s);
  }
  
  public void publish(Element node, StringBuffer s) {
    s.append("<h2>");
    elements[0].publish(node.getChild("title"), s);
    s.append("</h2>");
    elements[1].publish(node.getChild("links"), s);
  }
  
}

class LinkItem extends DocList {

  public LinkItem(java.lang.String name) {
    super(name, new DocElement[] {new String("title"),
        new String("link")});
  }

  public void view(Element node, StringBuffer s) {
    s.append("<a href=\"" + getLink(node) + "\" target=\"_blank\">");
    s.append(node.getChildText("title") + "</a>");
  }

  public void publish(Element node, StringBuffer s) {
    s.append("<a href=\"" + getLink(node) + "\" target=\"_blank\">");
    s.append(node.getChildText("title") + "</a>");
  }

  private java.lang.String getLink(Element node) {
      java.lang.String link = node.getChildText("link");

      if (!link.contains("://")) {
          link = "http://" + link;
      }

      return link;
  }
}

