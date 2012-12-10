package com.rectang.xsm.pages.nav;

import com.rectang.xsm.pages.cms.Page;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;
import org.apache.wicket.PageParameters;

/**
 * The main CMS pages parent
 *
 * @author Andrew Williams
 * @version $Id: LinkPage.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 */
public abstract class LinkPage extends Page implements Secure {
  protected LinkPage(PageParameters parameters) {
    super(parameters);

    addTab(LinkView.class);
    addTab(LinkEdit.class);
  }

  public int getLevel() {
    return AccessControl.MEMBER;
  }
}
