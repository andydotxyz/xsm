package com.rectang.xsm.pages.cms;

import com.rectang.xsm.AccessControl;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.pages.nav.Contents;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

/**
 * Page for summarising site management options
 *
 * @author Andrew Williams
 * @version $Id$
 * @since 2.0
 */
public class PageSettings extends DocumentPage implements Secure {
  public PageSettings(PageParameters parameters) {
    super(parameters);
  }

  public int getLevel() {
    return AccessControl.MEMBER;
  }

  public void layout() {
    super.layout();

    add(new BookmarkablePageLink("contents", Contents.class, getPageParameters()));
    add(new BookmarkablePageLink("permissions", Permissions.class, getPageParameters()));
    add(new BookmarkablePageLink("status", Status.class, getPageParameters()));
    add(new BookmarkablePageLink("options", Options.class, getPageParameters()));

    add(new BookmarkablePageLink("security", Security.class, getPageParameters()).setVisible(isApache()));

    add(new BookmarkablePageLink("new", New.class, getPageParameters()));
  }

  private boolean isApache() {
    return getXSMSession().getSite().getTechnologies().contains("apache");
  }
}
