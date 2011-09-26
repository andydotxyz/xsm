package com.rectang.xsm.types;

import java.util.List;
import java.util.Vector;

import org.jdom.Element;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.rectang.xsm.doc.*;

public class Embed extends DocList {

  public static final SupportedOption FRAME_HEIGHT = new SupportedOption(
      "FRAME_HEIGHT", "The height of the embedded frame in pixels", 400);
  public static final SupportedOption SHOW_BORDER = new SupportedOption(
      "SHOW_BORDER", "Show a border around the embedded frame", true);

  private Vector options;

  public Embed(java.lang.String name) {
    super(name, new DocElement[]{new com.rectang.xsm.widget.String("url")});

    options = new Vector();
    options.add(FRAME_HEIGHT);
    options.add(SHOW_BORDER);
  }

  public void view(Element root, StringBuffer s) {
    s.append("<p>[Page \"");
    elements[0].view(root.getChild("url"), s);
    s.append("\" will be embedded here]</p>\n");
  }

  public WebMarkupContainer edit(String wicketId, Element node, String path) {
    return super.edit(wicketId, node, path, "Enter the URL of the page to embed in this page");
  }

  public void publish(Element root, StringBuffer s) {
    s.append("<iframe src=\"");
    elements[0].publish(root.getChild("url"), s);
    s.append("\" width=\"100%\" height=\"" + FRAME_HEIGHT.getInteger(getDoc()));
    if (!SHOW_BORDER.getBoolean(getDoc()))
      s.append("\" frameborder=\"0");
    s.append("\">\n");
    s.append("<p>Your browser does not support internal frames.<br />");
    s.append("You can click to view the <a href=\"");
    elements[0].publish(root.getChild("url"), s);
    s.append("\">page</a></p>\n");
    s.append("</iframe>\n");
  }

  public List getSupportedOptions() {
    Vector ret = new Vector();
    ret.addAll(options);
    for (int i = 0; i < elements.length; i++) {
      ret.addAll(elements[i].getSupportedOptions());
    }
    return ret;
  }
}