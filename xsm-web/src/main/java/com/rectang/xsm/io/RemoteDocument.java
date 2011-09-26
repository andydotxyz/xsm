package com.rectang.xsm.io;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.rectang.xsm.XSM;

import com.rectang.xsm.site.Site;

public class RemoteDocument extends File {

  protected static Hashtable documents = new Hashtable();
  private static EvictionThread evict = null;
  
  protected static SAXBuilder builder = new SAXBuilder();

  protected Element root;
  protected Document dom;

  public static RemoteDocument getDoc(String file) {
    if (documents.containsKey(file)) {
      TimedDocument ret = (TimedDocument) documents.get(file);

      if (ret.getModifiedTime() >= ret.getDoc().getModifiedTime())
        return ret.getDoc();

      documents.remove(file);
    }

    return new RemoteDocument(file);
  }

  public static RemoteDocument getDoc(Site site, String file) {
    return getDoc(calculateFileName(site, file, true));
  }

  public static RemoteDocument getDoc(Site site, String file, boolean makeXML) {
    return getDoc(calculateFileName(site, file, makeXML));
  }

  private RemoteDocument(String file) {
    super(file);
    if (evict == null) {
      evict = new EvictionThread();
      evict.start();
    }

    try {
      load(new FileInputStream(file));
    } catch (Exception e) {
      dom = new Document();
      root = null;
    }

    documents.put(file, new TimedDocument(this));
  }

  public static String calculateFileName(Site site, String file, boolean makeXML) {
    String fileName = file;
    if (fileName.charAt(0) != File.separatorChar)
      fileName = File.separatorChar + fileName;

    String theFile = XSM.getConfig().getDataDir() + site.getId() + fileName;
    if (makeXML && !theFile.endsWith(".xml"))
      theFile += ".xml";
    return theFile;
  }

  public void load(InputStream is) {
    try {
      synchronized(this) {
        dom = builder.build(is);
      }
      root = dom.getRootElement();
    } catch (Exception e) {
      dom = new Document();
      root = null;
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        /* nothing we can do */
      }
    }
  }
  
  public Element getRootElement() {
    return root;
  }

  public void setRootElement(Element root) {
    this.root = root;
    dom.setRootElement(root);
  }

  public static Element addContentAfterElement(Element parent, int at,
      Content child) {
    int id = 0, ptr = 0;
    List kids = parent.getContent();
    Iterator eachKid = kids.iterator();
    while(eachKid.hasNext() && ptr < at) {
      Content node = (Content) eachKid.next();
      if (node instanceof Element)
        ptr++;
      id++;
    }
    
    return parent.addContent(id, child);
  }
  
  public boolean save() {
    try {
      OutputStream out;
      out = new FileOutputStream(this);
      (new XMLOutputter(Format.getPrettyFormat() )).output(dom, out);
      out.close();

      documents.remove(getPath());
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public InputStream getInputStream() throws IOException {
    return new FileInputStream(this);
  }
  
  public OutputStream getOutputStream() throws IOException {
    return new FileOutputStream(this);
  }
  
  public String toString() {
    return "Remote document: " + getPath();
  }

  public boolean mkparentdirs() {
    if (getParentFile().exists())
      return true;
    return getParentFile().mkdirs();
  }

  public boolean delete() {
    boolean ret = true;
    if (isDirectory())
      ret = delete(listFiles());

    documents.remove(getPath());
    return ret && super.delete();
  }

  private static boolean delete(File[] files) {
    boolean ret = true;
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory())
        ret = ret && delete(files[i].listFiles());
      ret = ret && files[i].delete();
      documents.remove(files[i].getPath());
    }

    return ret;
  }

  public boolean rename(Site site, String newName, boolean makeXML) {
    return renameTo(new File(calculateFileName(site, newName, makeXML)));
  }

  public long getModifiedTime() {
    return lastModified();
  }

  class TimedDocument {
    private long stamp; // when we last accesed this document
    private RemoteDocument doc; // the document
    private long mtime; // last time the document was modified

    public TimedDocument(RemoteDocument doc) {
      this.doc = doc;
      this.stamp = System.currentTimeMillis(); // stamp it with the current time
      this.mtime = doc.getModifiedTime();
    }

    public RemoteDocument getDoc() {
      this.stamp = System.currentTimeMillis();

      return doc;
    }

    public long getTimestamp() {
      return stamp;
    }

    public long getModifiedTime() {
      return mtime;
    }
  }

  static class EvictionThread extends Thread {
    public static final long EVICT_TIMEOUT = 60 * 1000;
    public static final long EVICT_SLEEP = 5 * 60 * 1000;
    
    public void run() {
      while (true) {
        try {
          Thread.sleep(EVICT_SLEEP);
        } catch (InterruptedException timeout) {
          /* run the evacuation */
        }

        long expiry = System.currentTimeMillis() - EVICT_TIMEOUT;
        Iterator docs = documents.keySet().iterator();

        while (docs.hasNext()) {
          String nextKey = (String) docs.next();
          TimedDocument next = (TimedDocument) documents.get(nextKey);
        
          if (next.getTimestamp() < expiry) {
            docs.remove();
          }
        }
      }
    }
  }
}