package com.rectang.xsm.site.upgrades;

import com.rectang.xsm.site.*;

import com.rectang.xsm.io.XSMDocument;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

public class Upgrade3 implements UpgradeUnit {

  public int getFromVersion() {
    return 3;
  }

  public int getToVersion() {
    return 4;
  }

  public String getTitle() {
    return "Add comments to gallery images";
  }

  public boolean upgrade(Site site) {
    Iterator pages = site.getAllPages();
    while (pages.hasNext()) {
      DocumentPage next = (DocumentPage) pages.next();
      
      try {
        XSMDocument doc = next.getXSMDocument();
        
        Element gallery = doc.getContentElement();
        if (gallery.getName().equals("gallery")) {
          System.out.println("upgrade 3 (gallery) - " + doc.getPath());
          upgradeGallery(gallery, doc);
        } else if (gallery.getName().equals("book") || gallery.getName().equals("html")) {
          System.out.println("upgrade 3 (html / book) - " + doc.getPath());
          upgradeGallery(gallery.getChild("images"), doc);
        }

      } catch (Exception e) {
        System.err.println("Failed on file " + next.getPath() + e.getMessage());
        return false;
      }
      
    }
    return true;
  }

  private void upgradeGallery(Element gallery, XSMDocument doc) {
    List items = gallery.getChildren("image");
    if (items == null || items.size() == 0)
      return;
    
    Iterator itemIter = items.iterator();
    while (itemIter.hasNext()) {
      Element nextItem = (Element) itemIter.next();
      int index = nextItem.indexOf(nextItem.getChild("caption"));

      Element comments;
      nextItem.addContent(++index, comments = new Element("comments"));
      comments.setAttribute("next_index", "1");
    }
    doc.save();
  }
}
