package com.rectang.xsm.site;

import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.UserData;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 1.0
 */
public abstract class Page implements Serializable {
  private Site site;
  private HierarchicalPage parent;
  private String title;
  boolean hidden;

  public Page(Site site, HierarchicalPage parent, String title) {
    this(site, parent, title, false);
  }

  public Page(Site site, HierarchicalPage parent, String title, boolean hidden) {
    this.site = site;
    this.parent = parent;
    this.title = title;
    this.hidden = hidden;
  }

  public Site getSite() {
    return site;
  }

  public HierarchicalPage getParent() {
    return parent;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public boolean getHidden() {
    return hidden;
  }

  public void setHidden(boolean hide) {
    hidden = hide;
  }

  public String getFile() {
    return XSMDocument.encode(title);
  }

  public String toString() {
    return getClass().getName() + " [" + getTitle() + "]";
  }

  public String getPath() {
    String ret = "";
    if (parent != null)
      ret = parent.getPath();
    if (getTitle().equals("/"))
      return "/";
    if (ret.charAt(ret.length() - 1) == '/')
      return ret + getFile();
    return ret + "/" + getFile();
  }

  public boolean rename(String name) {
    if (name.charAt(0) == '/')
      return site.movePageTo(this, name);
    setTitle(name);
    return true;
  }

  // These require that the parent is a Hierarchical Page - which it must be
  // by definition :)

  public boolean delete() {
    return parent.removeSubPage(this);
  }

  public void moveToTop() {
    if (parent != null)
      parent.movePageToTop(this);
  }

  public void moveUp() {
    if (parent != null)
      parent.movePageUp(this);
  }

  public void moveDown() {
    if (parent != null)
      parent.movePageDown(this);
  }

  public void moveToBottom() {
    if (parent != null)
      parent.movePageToBottom(this);
  }

  public abstract boolean publish(UserData user);

  public abstract String getType();

  public String getLink() {
    return getSite().getPrefixUrl() + getPath() + "/";
  }

  public boolean isPublishable() {
    return false;
  }

  public boolean equals(Object page) {
    if (!(page instanceof Page))
      return false;

    return ((Page) page).getPath().equals(getPath());
  }
}
