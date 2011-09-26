package com.rectang.xsm.types;

import com.rectang.xsm.doc.DocElement;
import com.rectang.xsm.doc.DocGroup;
import com.rectang.xsm.doc.DocList;
import com.rectang.xsm.widget.String;
import com.rectang.xsm.widget.File;

import java.util.Hashtable;

import org.jdom.Element;

public class Files extends DocGroup {

  public Files(java.lang.String name) {
    super(name, new FileCategory("category"));
  }

}

/* TODO - this code is almost the same as a LinkCategory - make it shared? */
class FileCategory extends DocList {

  public FileCategory(java.lang.String name) {
    super(name, new DocElement[] {new String("title"),
        new DocGroup("files", new FilesItem("file"))});
  }
  
  public void view(Element node, StringBuffer s) {
    s.append("<h2>");
    elements[0].view(node.getChild("title"), s);
    s.append("</h2>");
    elements[1].view(node.getChild("files"), s);
  }
  
  public void publish(Element node, StringBuffer s) {
    s.append("<h2>");
    elements[0].publish(node.getChild("title"), s);
    s.append("</h2>");
    elements[1].publish(node.getChild("files"), s);
  }
  
}

class FilesItem extends DocList {

  public FilesItem(java.lang.String name) {
    super(name, new DocElement[] {
        new File("path"),
        new String("caption")
    });
  }

  public void view(Element node, StringBuffer s) {
    java.lang.String caption = node.getChildText("caption");
    if (caption == null || caption.equals(""))
      caption = node.getChildText("path");
    if (caption == null || caption.equals(""))
      caption = "(no file)";
      
    s.append(caption);
  }

  public void publish(Element node, StringBuffer s) {
    java.lang.String type = File.getFileExt(node.getChildText("path"));
    Hashtable hash = File.getIcons();
    if (!hash.containsKey(type))
      type = "misc";

    java.lang.String path = node.getChildText("path");
    java.lang.String caption = node.getChildText("caption");
    if ( path != null && path.length() > 0 ) {
      s.append("<img src=\"/icons/");
      s.append(hash.get(type));
      s.append("\" alt=\"");
      s.append(type.toUpperCase());
      s.append(" file icon\"/><a href=\"");
      s.append(getSite().getRootUrl());
      s.append(File.getPath(elements[0], path));
      s.append("\">");
      s.append(path);
      s.append("</a>");
    } else {
      s.append("(no file)");
    }
    if (caption != null && caption.length() > 0) {
      s.append(" - ");
      s.append(caption);
      s.append("\n");
    }
  }
}
