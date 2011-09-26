package com.rectang.xsm.site.upgrades;

import com.rectang.xsm.site.*;

import com.rectang.xsm.io.XSMDocument;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

public class Upgrade2 implements UpgradeUnit {

  public int getFromVersion() {
    return 2;
  }

  public int getToVersion() {
    return 3;
  }

  public String getTitle() {
    return "Add metadata \"version\" to PreviewedFile pages";
  }

  public boolean upgrade(Site site) {
    Iterator pages = site.getAllPages();
    while (pages.hasNext()) {
      DocumentPage next = (DocumentPage) pages.next();
      
      try {
        XSMDocument doc = next.getXSMDocument();
        
        Element previewedFile = doc.getContentElement();
        if (!previewedFile.getName().equals("previewedfile"))
          continue;
        
        System.out.println("upgrade 2 - " + doc.getPath());

        List items = previewedFile.getChildren("item");
        if (items == null || items.size() == 0)
          continue;
        
        Iterator itemIter = items.iterator();
        while (itemIter.hasNext()) {
          Element nextItem = (Element) itemIter.next();
          int index = nextItem.indexOf(nextItem.getChild("title"));
          
          nextItem.addContent(++index, new Element("version"));
        }
        doc.save();
      } catch (Exception e) {
        System.err.println("Failed on file " + next.getPath() + e.getMessage());
        return false;
      }
      
    }
    return true;
  }

}
