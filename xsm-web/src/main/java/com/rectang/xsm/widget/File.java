package com.rectang.xsm.widget;

import com.rectang.xsm.doc.DocWidget;
import com.rectang.xsm.io.*;
import com.rectang.xsm.XSM;

import java.util.Hashtable;
import java.io.Serializable;
import java.lang.*;

import org.jdom.Element;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

public class File extends DocWidget implements Serializable {
 
  private static Hashtable hash;

  static {
    hash = new Hashtable();
    hash.put("avi", "movie.gif");
    hash.put("c", "c.gif");
    hash.put("cfg", "text.gif");
    hash.put("conf", "text.gif");
    hash.put("class", "binary.gif");
    hash.put("deb", "deb.gif");
    hash.put("doc", "layout.gif");
    hash.put("dvi", "dvi.gif");
    hash.put("gz", "compressed.gif");
    hash.put("h", "h.gif");
    hash.put("htm", "layout.gif");
    hash.put("html", "layout.gif");
    hash.put("jar", "compressed.gif");
    hash.put("java", "text.gif"); // need a better java icon
    hash.put("jpeg", "image2.gif");
    hash.put("jpg", "image2.gif");
    hash.put("misc", "generic.gif");
    hash.put("mov", "movie.gif");
    hash.put("mp3", "sound2.gif");
    hash.put("ogg", "sound2.gif");
    hash.put("patch", "patch.gif");
    hash.put("pdf", "pdf.gif");
    hash.put("ppt", "portal.gif");
    hash.put("ps", "ps.gif");
    hash.put("png", "image2.gif");
    hash.put("tar", "tar.gif");
    hash.put("tex", "tex.gif");
    hash.put("txt", "text.gif");
    hash.put("xls", "image3.gif");
    hash.put("zip", "compressed.gif");
  }

  private java.lang.String[] exts;

  public File(java.lang.String name) {
    this(name, null);
  }

  public File(java.lang.String name, java.lang.String[] extensions) {
    super(name);

    exts = extensions;
  }

  public File setAcceptedExtensions(java.lang.String[] extensions) {
    exts = extensions;

    return this;
  }

  public void view(Element node, StringBuffer s) {
    if (node.getValue() == null || node.getValue().equals(""))
      s.append("(no file)\n");
    else
      s.append(node.getValue() + "\n");
  }
  
  public void publish(Element node, StringBuffer s) {
    java.lang.String name = node.getValue();
    if (name == null || name.equals("")) {
      s.append("(no file)\n");
      return;
    }

    java.lang.String type = getFileExt(name);
    if (!hash.containsKey(type))
      type = "misc";

    s.append("<img src=\"/icons/" + hash.get(type) + "\" /><a href=\"");
    s.append(getSite().getRootUrl() + getPath(node.getValue()));
    s.append("\">" + name + "</a>\n");
  }

  public WebMarkupContainer edit(java.lang.String wicketId, Element node, java.lang.String path) {
    return new FilePanel(wicketId, node, this);
  }

  public void create(Element node) {
    node.setText("");
  }

  public void destroy(Element node) {
    java.lang.String fileName = node.getText();
    if (fileName == null || fileName.equals(""))
      return;

    PublishedFile file = (getSite().getPublishedDoc(getPath(
        fileName)));
    if (file.exists())
      file.delete();
  }

  public static java.lang.String getFileExt(java.lang.String file) {
    int pos = file.lastIndexOf('.');
    java.lang.String type;
    if (pos == -1)
      type = file;
    else
      type = file.substring(pos + 1);
    return type;
  }
  
  public java.lang.String getPath(java.lang.String name) {
    return getPath(this, name);
  }
  
  public static java.lang.String getPath(com.rectang.xsm.doc.DocElement elem,
      java.lang.String name) {
    return elem.getPath() + java.io.File.separatorChar + "_files"
        + java.io.File.separatorChar + name;
  }
  
  public static Hashtable getIcons() {
    return hash;
  }

  /**
   * Nasty hack to account for the fileUpload returning paths in a different file system structure that we
   * cannot resolve as we could be on a different platform...
   * 
   * @param in A file path in any system style
   * @return The same path using our own dir separators
   */
  public static java.lang.String fixSeparators(java.lang.String in) {
    java.lang.String out;
    if (java.io.File.separatorChar == '/')
      out = in.replace('\\', '/');
    else
      out = in.replace('/', '\\');

    return out;
  }

  /**
   * Remove any unsuitable characters from a file name.
   * Also calls fixSeparators to ensure we only have a file name.
   *
   * @param name The file to get a sanitised name of
   * @return the sanitised file name
   */
  protected java.lang.String sanitiseName(java.lang.String fileName) {
    /* pass it through a "File" object to make sure we get only the name */
    java.lang.String name = (new java.io.File(fixSeparators(fileName))).getName();

    return name.replace('#', '_');
  }

  class FilePanel extends Panel {
    FileUploadField upload;

    public FilePanel(java.lang.String id, final Element node, final File type) {
      super(id);

      add(new Label("label", name));

      add(new Label("current", "using file \"" +
          (new java.io.File(node.getValue())).getName()
          + "\", to overwrite, upload another file:")
          .setVisible(!node.getText().equals("")));
      add(upload = new FileUploadField("value", new Model() {

        public void setObject(Serializable object) {
          FileUpload file = upload.getFileUpload();

          java.lang.String ext = getFileExt(file.getClientFileName()).toLowerCase();
          if (exts != null) {
            boolean found = false;

            for (int i = 0; i < exts.length; i++) {
              if (exts[i].equals(ext)) {
                found = true;
                break;
              }
            }

            if (!found) {
              error("File type " + ext + " is not supported");
              return;
            }
          }
          java.lang.String fileName = sanitiseName(file.getClientFileName());

          PublishedFile newFile = getSite().getPublishedDoc( type.getPath(fileName));
          java.lang.String oldName = node.getText();

          if (newFile.exists() && !oldName.equals(fileName)) {
            error("File " + fileName
                + " already exists, please rename your file and try uploading again");
            return;
          }

          /* remove the old file if it exists */
          if (oldName != null && !oldName.equals("")) {
            PublishedFile oldFile = getSite().getPublishedDoc(
                type.getPath(oldName));
            if (oldFile.exists())
              oldFile.delete();
            node.setText("");
          }

          node.setText(fileName);

          java.io.File tmpFile = XSM.getTempFile();
          try {
            file.writeTo(tmpFile);

            newFile.uploadFile(tmpFile, true);
          }
          catch (Exception e) {
            error("Unable to write file " + newFile.getFileName());
            e.printStackTrace();
          }
        }
      }));

      if (exts == null) {
        add(new Label("note", ""));
      } else {
        StringBuffer note = new StringBuffer("(file types ");
        for (int i = 0; i < exts.length; i++) {
          note.append(exts[i]);
          if (i < exts.length) {
            note.append(", ");
          }
        }
        note.append(" only)");

        add(new Label("note", note.toString()));
      }
    }
  }
}
