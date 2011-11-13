package com.rectang.xsm.site;

import com.rectang.xsm.UserData;

import java.util.Vector;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: HierarchicalPage.java 831 2011-09-25 12:59:18Z andy $
 * @since 1.0
 */
public class HierarchicalPage extends Page {
  private Vector subPages;

  public HierarchicalPage(Site site, HierarchicalPage parent, String title) {
    this(site, parent, title, false);
  }

  public HierarchicalPage(Site site, HierarchicalPage parent, String title, boolean hidden) {
    super(site, parent, title, hidden);
    subPages = new Vector();
  }

  public boolean addSubPage(Page sub) {
    return subPages.add(sub);
  }

  public boolean removeSubPage(Page sub) {
    return subPages.remove(sub);
  }

  public void addSubPages(List subs) {
    if (subs == null)
      return;
    subPages.addAll(subs);
  }

  public List getSubPages() {
    return subPages;
  }

  public boolean movePageToTop(Page child) {
    int index = subPages.indexOf(child);
    if (index == 0 || index == -1)
      return false;
    subPages.add(0, subPages.remove(index));
    return true;
  }

  public boolean movePageUp(Page child) {
    int index = subPages.indexOf(child);
    if (index == 0 || index == -1)
      return false;
    subPages.add(index - 1, subPages.remove(index));
    return true;
  }

  public boolean movePageDown(Page child) {
    int index = subPages.indexOf(child);
    if (index == subPages.size() - 1 || index == -1)
      return false;
    subPages.add(index + 1, subPages.remove(index));
    return true;
  }

  public boolean movePageToBottom(Page child) {
    int index = subPages.indexOf(child);
    if (index == subPages.size() - 1 || index == -1)
      return false;
    subPages.add(subPages.size() - 1, subPages.remove(index));
    return true;
  }

  public boolean publish(UserData user) {
    return true;
  }

  public String getType() {
    return "";
  }
}
