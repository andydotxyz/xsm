package com.rectang.xsm.site;

import com.rectang.xsm.UserData;

/**
 * @author aje
 *
 * Represents a page in a site structure
 */
public class LinkPage extends Page {
  String link;

  public LinkPage(Site site, HierarchicalPage parent, String title) {
    this(site, parent, title, false);
  }

  public LinkPage(Site site, HierarchicalPage parent, String title, boolean hidden) {
    super(site, parent, title, hidden);
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public boolean publish(UserData user) {
    return true;
  }

  public String getType() {
    return "link";
  }
}
