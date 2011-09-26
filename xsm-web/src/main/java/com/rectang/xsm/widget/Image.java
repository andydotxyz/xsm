package com.rectang.xsm.widget;

import com.rectang.xsm.XSM;

import com.rectang.xsm.io.PublishedFile;
import com.rectang.xsm.io.XSMDocument;
import com.rectang.io.TarFile;
import com.rectang.io.GZipFile;
import com.rectang.io.ZipFile;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.jdom.Element;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.AttributeModifier;

public class Image extends File implements Serializable {

  protected static HashSet hash, compHash;
  
  {
    hash = new HashSet();
    hash.add("jpeg");
    hash.add("jpg");
    hash.add("gif");
    hash.add("png");
    /* FIXME add all images java + web supports */

    compHash = new HashSet();
    compHash.add("gz");
    compHash.add("tar.gz");
    compHash.add("zip");
  }
  
  public Image(java.lang.String name) {
    super(name);
  }

  protected java.lang.String getViewURL(Element node) {
    return getPath(node.getValue());
  }

  public void view(Element node, StringBuffer s) {
    if (node.getValue() == null || node.getValue().equals(""))
      s.append("(no image)\n");
    else {
      s.append("<img src=\"" + getSite().getRootUrl());
      s.append(getViewURL(node) + "\" />\n");
    }
  }
  
  public void publish(Element node, StringBuffer s) {
    if (node.getValue() == null || node.getValue().equals(""))
      s.append("(no image)\n");
    else {
      s.append("<img src=\"" + getSite().getPrefixUrl());
      s.append(getPath(node.getValue()) + "\" />\n");
    }
  }

  public WebMarkupContainer edit(java.lang.String wicketId, Element node, java.lang.String path) {
    return new ImagePanel(wicketId, node, this);
  }

  public void destroy(Element node) {
    java.lang.String imageName = node.getText();
    if (imageName == null || imageName.equals(""))
      return;

    PublishedFile file = (getSite().getPublishedDoc(getPath(
        imageName)));
    if (file.exists())
      file.delete();
  }

  protected boolean addImagesRecurse(java.io.File file, Element node,
      boolean addNode) throws XSMDocument.GenerationException {
    if (file.isDirectory()) {
      java.io.File[] children = file.listFiles();
      for (int i = 0; i < children.length; i++) {
        if (addImagesRecurse(children[i], node, addNode)) {
          // only start adding nodes if we have hit a valid image upload
          addNode = true;
        }
      }

      file.delete();
      return false;
    }

    java.lang.String fileName = (new java.io.File(File.fixSeparators(file.getName()))).getName();
    // ignore non-image files and nasty OSX metadata
    if (!hash.contains(getFileExt(fileName)) || fileName.startsWith("._")) {
      file.delete();
      return false;
    }

    Element value = node;
    if (addNode) {
      /* FIXME - we need to hook into the node creation somehow!!! */
      java.lang.String type = ((Element) node.getDocument().getRootElement().getChild("data").getChildren().get(0)).getName();
      if (type.equals("gallery") || type.equals("book") ||
          type.equals("html") || type.equals("php")) {
        Element newNode = new Element("image");
        value = new Element("path");
        newNode.addContent(value);
        newNode.addContent(new Element("caption"));
        newNode.addContent(new Element("comments"));
        node.getParentElement().getParentElement().addContent(0, newNode);
      } else {
        /* FIXME - make the next_index stuff work again */
        value = new Element("image");
        node.getParentElement().addContent(0, value);
      }
    }

    PublishedFile newFile = getSite().getPublishedDoc(getPath(fileName));
    value.setText(fileName);

    processFile(fileName, file, newFile);
    file.delete();
    return true;
  }

  protected void clearOldFiles(Element node) {
    java.lang.String oldName = node.getText();

    if (oldName != null && !oldName.equals("")) {
      PublishedFile oldFile = getSite().getPublishedDoc(
          getPath(oldName));
      if (oldFile.exists())
        oldFile.delete();
      node.setText("");
    }
  }

  protected void processFile(java.lang.String fileName,
                             java.io.File in, PublishedFile pub)
      throws XSMDocument.GenerationException {
    pub.uploadFile(in);
  }

  public java.lang.String getPath(java.lang.String name) {
    return getPath(this, name);
  }

  public static java.lang.String getPath(com.rectang.xsm.doc.DocElement elem,
      java.lang.String name) {
    return elem.getPath() + java.io.File.separatorChar + "_images"
        + java.io.File.separatorChar + name;
  }

  class ImagePanel extends Panel {
    FileUploadField upload;

    public ImagePanel(java.lang.String id, final Element node, final Image type) {
      super(id);

      add(new Label("label", name));

      add(new WebMarkupContainer("current").add(new AttributeModifier("src",
          new Model(getSite().getRootUrl() +
              type.getViewURL(node)))).setVisible(!node.getText().equals("")));
      add(upload = new FileUploadField("value", new Model() {

        public void setObject(Serializable object) {
          FileUpload file = upload.getFileUpload();

          java.lang.String ext = getFileExt(file.getClientFileName()).toLowerCase();
          if (hash.contains(ext)) {
            java.lang.String fileName = sanitiseName(file.getClientFileName());

            PublishedFile newFile = getSite().getPublishedDoc(
                type.getPath(fileName));
            java.lang.String oldName = node.getText();

            if (newFile.exists() && !oldName.equals(fileName)) {
              error("File " + fileName
                  + " already exists, please rename your file and try uploading again");
              return;
            }

            /* remove the old images if they exist */
            clearOldFiles(node);

            node.setText(fileName);
            java.io.File tmpFile = XSM.getTempFile();

            try {
              file.writeTo(tmpFile);

              processFile(fileName, tmpFile, newFile);
            }
            catch (Exception e) {
              error("Unable to write file " + newFile.getFileName());
              e.printStackTrace();
            }
          } else if (compHash.contains(ext)) {
            java.io.File temp = XSM.getTempFile();
            temp.mkdirs();

            com.rectang.io.File zip = null;
            TarFile tar = null;
            try {
              if (ext.equals("gz")) {
                zip = new GZipFile(temp, "upload.gz");
                file.writeTo(zip);

                java.io.File maybeTar = ((GZipFile) zip).expand();
                zip.delete();

                if (file.getClientFileName().toLowerCase().endsWith(".tar.gz")) {
                  tar = new TarFile(maybeTar);
                  tar.expand();
                  tar.delete();
                }
              } else if (ext.equals("zip")) {
                zip = new ZipFile(temp, "upload.zip");
                file.writeTo(zip);
                ((ZipFile) zip).expand();
                zip.delete();
              }

              addImagesRecurse(temp, node, false);
            } catch (IOException e) {
              error("Error unpacking compressed file: " + e.getMessage());
            } catch (XSMDocument.GenerationException e) {
              error("Unable to write images from zip file ");
            } finally {
              if (tar != null && tar.exists())
                tar.delete();
              if (zip != null && zip.exists())
                zip.delete();
              if (temp.exists())
                temp.delete();
            }
          } else
            error("Image type " + type + " is not supported");
          }
        }));

      StringBuffer note = new StringBuffer("(image types ");
      Iterator types = hash.iterator();
      while (types.hasNext()) {
        java.lang.String next = (java.lang.String) types.next();
        note.append(next);
        if (types.hasNext()) {
          note.append(", ");
        }
      }
      note.append(" only) or you can upload a set of images in a compressed file (");
      types = compHash.iterator();
      while (types.hasNext()) {
        java.lang.String next = (java.lang.String) types.next();
        note.append(next);
        if (types.hasNext()) {
          note.append(", ");
        }
      }
      note.append(")");

      add(new Label("note", note.toString()));      
    }
  }
}
