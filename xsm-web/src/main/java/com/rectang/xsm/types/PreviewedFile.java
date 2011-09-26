package com.rectang.xsm.types;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.rectang.xsm.doc.*;
import com.rectang.xsm.io.PublishedFile;
import com.rectang.xsm.widget.*;
import com.rectang.xsm.widget.String;
import com.rectang.xsm.widget.File;

import org.jdom.Element;

public class PreviewedFile extends DocGroup {

  public static final SupportedOption PUBLISH_RSS = new SupportedOption(
      "PUBLISH_RSS", "Should an RSS feed for this page be published?", false);

  private Vector options;

  public PreviewedFile(java.lang.String name) {
    super(name, new PrevItem("item"));

    options = new Vector();
    options.add(PUBLISH_RSS);
  }

  public List getSupportedOptions() {
    Vector ret = new Vector();
    ret.addAll(options);
    ret.addAll(element.getSupportedOptions());
    return ret;
  }

  public void publish(Element node, StringBuffer s) {
    super.publish(node, s);

    if (PUBLISH_RSS.getBoolean(getDoc()))
      publishRSS(node);
  }

  protected static java.lang.String escape(java.lang.String in) {
    if (in == null)
      return "";
    return in.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
  }

  public void publishRSS(Element root) {
    PublishedFile rss = getSite().getPublishedDoc(getPath() +
        java.io.File.separatorChar + "feed.xml");
    try {
      OutputStream os = rss.getOutputStream();
      Writer out = new OutputStreamWriter(os);
      
      out.write("<?xml version=\"1.0\" ?>\n");
      out.write("<rss version=\"2.0\">\n");
      out.write("  <channel>\n");
      out.write("    <title>" + escape(getSite().getTitle()) + "</title>\n");
      out.write("    <link>" + escape(getSite().getRootUrl() + getPath())
          + "/feed.xml</link>\n");
      out.write("    <description>RSS generated from " + escape(
          getSite().getRootUrl() + getPath()) + "/</description>\n");
      out.write("    <generator>Rectang XSM</generator>\n");
      
      List elements = root.getChildren("item");
      Iterator all = elements.iterator();
      java.lang.String index;
      int fakeIndex = 0;
      while (all.hasNext()) {
        Element next = (Element) all.next();
        index = next.getAttributeValue("index");
        if (index == null || index.equals(""))
          index = "x" + fakeIndex++;
//        java.lang.String link = escape(site.getRootUrl()
//            + getPath() + "/_items/" + id + ".html");
        java.lang.String guid = getSite().getRootUrl() + getPath() + ".html#" + index;
        StringBuffer tmp = new StringBuffer();
        ((PrevItem) element).publishRSS(next, guid, tmp);
        out.write(tmp.toString());
      }

      out.write("  </channel>\n");
      out.write("</rss>\n");
      out.close();
      os.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
  
class PrevItem extends DocList {

  public PrevItem(java.lang.String name) {
    super(name, new DocElement[] {new String("title"), new String("version"),
        new String("author"), new String("email"), new Date("lastupdated"),
        new String("license"), new String("comment"),
        new TextArea("description"), new PrevItemData("miscdata"),
        new Image("preview"), new File("file")});
  }

  public void publish(Element node, StringBuffer s) {
    s.append("<table border=\"0\"><tr><td valign=\"top\">");
    java.lang.String file = node.getChild("file").getValue();
    if (file != null && !file.equals("")) {
      s.append("<a href=\"" + getSite().getPrefixUrl());
      s.append(getPath() + java.io.File.separatorChar + "_files");
      s.append(java.io.File.separatorChar + file + "\">");
    }
    s.append("<img border=\"0\" src=\"");
    s.append(getSite().getPrefixUrl() + getPath());
    s.append(java.io.File.separatorChar + "_images");
    s.append(java.io.File.separatorChar + node.getChild("preview").getValue());
    s.append("\" alt=\"");
    elements[0].publish(node.getChild("title"), s);
    s.append("\"/>");

    if (file != null && !file.equals(""))
      s.append("</a>");
    s.append("</td><td valign=\"top\">\n");

    addField("Title", "title", elements[0], node, s);
    addField("Version", "version", elements[1], node, s);
    java.lang.String name = node.getChildText("author");
    java.lang.String email = node.getChildText("email");
    if (name != null && !name.equals("")) {
      s.append("<b>Author:</b> ");
      if (email != null && !email.equals("")) {
        s.append("<a href=\"mailto:");
        s.append(email);
        s.append("\">");
      }
      s.append(name);
      if (email != null && !email.equals(""))
        s.append("</a>");
      s.append("<br />\n");
    }
    addField("Last Updated", "lastupdated", elements[4], node, s);
    addField("License", "license", elements[5], node, s);
    addField("Comment", "comment", elements[6], node, s);

    /* add the custom metadata */
    elements[8].publish(node.getChild("miscdata"), s);

    s.append("</td></tr><tr><td colspan=\"2\">");
    elements[7].publish(node.getChild("description"), s);
    s.append("</td></tr></table>");
  }

  public void publishRSS(Element root, java.lang.String guid, StringBuffer s) {
    StringBuffer tmp = new StringBuffer();

    s.append("<item>\n");
    elements[0].view(root.getChild("title"), tmp);
    s.append("  <title>" + PreviewedFile.escape(tmp.toString()) + "</title>\n");
    java.lang.String file = root.getChild("file").getValue();
    s.append("  <link>");
    s.append(PreviewedFile.escape(getSite().getRootUrl()
        + getPath() + java.io.File.separatorChar + "_files" +
        java.io.File.separatorChar + file));
    s.append("</link>\n");

    tmp = new StringBuffer();
    elements[3].view(root.getChild("email"), tmp);
    s.append("  <author>" + PreviewedFile.escape(tmp.toString()));
    tmp = new StringBuffer();
    elements[2].view(root.getChild("author"), tmp);
    s.append(" (" + PreviewedFile.escape(tmp.toString()) + ")</author>\n");

    tmp = new StringBuffer();
    elements[7].view(root.getChild("description"), tmp);
    s.append("  <description>" + PreviewedFile.escape(tmp.toString()) + "</description>\n");
    s.append("  <pubDate>");
    elements[4].view(root.getChild("lastupdated"), s);
    s.append("</pubDate>\n");
    s.append("<enclosure url=\"");
    java.lang.String preview = root.getChild("preview").getValue();
    s.append(PreviewedFile.escape(getSite().getRootUrl()
        + getPath() + java.io.File.separatorChar + "_images" +
        java.io.File.separatorChar + preview));
    /* FIXME - figure length and type */
    s.append("\" length=\"\" type=\"\"/>");
    s.append("  <guid isPermaLink=\"false\">" + guid + "</guid>\n");
    s.append("</item>\n");
  }

  /**
   * Add an item if its value is not empty
   * 
   * @param title         Item title
   * @param node          Name of the child node containing the data
   * @param docNode       The XSM DocElement that controls the rendering
   * @param domNodeParent The JDOMElement that contains the child we are reading
   * @return A String in the form "&lt;b&gt;Title&lt;/b&gt;:
   *         renderedValueOfChildNode&lt;br /&gt;\n" or "" if the value was
   *         empty
   */
  private static void addField(java.lang.String title, java.lang.String node,
      DocElement docNode, Element domNodeParent, StringBuffer s) {
    java.lang.String value = domNodeParent.getChildText(node);
    if (value == null || value.equals(""))
      return;
    s.append("<b>" + title + ":</b> ");
    docNode.publish(domNodeParent.getChild(node), s);
    s.append("<br />\n");
  }
}

/**
 * Storage of custom metadata elements 
 * 
 * @author aje
 */
class PrevItemData extends DocGroup {
  public PrevItemData(java.lang.String name) {
    super(name, new PrevItemDataPair("item"));
  }
}

/**
 * A custom metadata element
 * 
 * Publishes as "&lt;b&gt;name:&lt;b&gt;: value&lt;br /&gt;\n"
 * 
 * @author aje
 */
class PrevItemDataPair extends DocList {
  public PrevItemDataPair(java.lang.String name) {
    super(name, new DocElement[] {new String("name"), new String("value")});
  }

  public void publish(Element node, StringBuffer s) {
    java.lang.String value = node.getChildText("value");
    if (value == null || value.equals(""))
      return;
    s.append("<b>");
    elements[0].publish(node.getChild("name"), s);
    s.append(":</b> ");
    elements[1].publish(node.getChild("value"), s);
    s.append("<br />\n");
  }
}