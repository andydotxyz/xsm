package com.rectang.xsm.panels;

import com.rectang.xsm.site.Site;
import com.rectang.xsm.pages.XSMSession;
import com.rectang.xsm.pages.nav.NewLink;
import com.rectang.xsm.pages.cms.New;
import com.rectang.xsm.wicket.ContentTreePanel;
import com.rectang.xsm.XSM;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;

/**
 * The contents tree panel
 *
 * @author Andrew Williams
 * @version $Id: ContentPanel.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 */
public class ContentPanel extends Panel {

  public ContentPanel(String id, XSMSession session, String page) {
    super(id);

    Site site = session.getSite();
    add(new ContentTreePanel("content", site.getRootPage(), page, "view"));

    PageParameters params = new PageParameters();
    params.add("page", site.getDefault());

    add(new BookmarkablePageLink("new-page-link", New.class, params));
    add(new BookmarkablePageLink("new-link-link", NewLink.class, params));
  }
}
