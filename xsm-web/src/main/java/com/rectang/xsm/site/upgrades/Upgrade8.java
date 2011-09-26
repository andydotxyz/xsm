package com.rectang.xsm.site.upgrades;

import com.rectang.xsm.site.Site;
import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.site.Page;
import com.rectang.xsm.io.XSMDocument;

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

public class Upgrade8 implements UpgradeUnit {

  public int getFromVersion() {
    return 8;
  }

  public int getToVersion() {
    return 9;
  }

  public String getTitle() {
    return "Fix encoding of HTML entities";
  }

  public boolean upgrade(Site site) {
    Iterator pages = site.getAllPages();
    while (pages.hasNext()) {
      Page nextPage = (Page) pages.next();
      if (!(nextPage instanceof DocumentPage))
        continue;

      DocumentPage next = (DocumentPage) nextPage;

      try {
        XSMDocument doc = next.getXSMDocument();

        System.out.println("upgrade 8 - " + doc.getPath());
        Element root = doc.getContentElement();

        unescape(root);
        doc.save();
      } catch (Exception e) {
        System.err.println("Failed on file " + next.getPath() + e.getMessage());
        return false;
      }

    }
    return true;
  }

  private void unescape(Element root) {
    String value = root.getText();
    System.out.println(value);
    if (value.indexOf("&amp;") != -1) {
      root.setText(value.replaceAll("&amp;", "&"));
    }

    if (root.getChildren() != null && root.getChildren().size() > 0) {
      Iterator children = root.getChildren().iterator();

      while (children.hasNext()) {
        Element element = (Element) children.next();
        unescape(element);
      }
    }
  }
}
