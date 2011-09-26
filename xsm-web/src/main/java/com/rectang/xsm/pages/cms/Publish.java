package com.rectang.xsm.pages.cms;

import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;
import org.apache.wicket.PageParameters;

/**
 * Regenerate a page - A nasty hack until we can stop using jsp!
 * Not quite as bad as the site publish page, this one can redirect back once done.
 *
 * @author Andrew Williams
 * @version $Id: Publish.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="page-publish"
 */
public class Publish extends DocumentPage {

  public int getLevel() {
    return AccessControl.MEMBER;
  }

  public void layout() {
    super.layout();
    if (hasError()) return;

    getDocumentPage().publish(getXSMSession().getUser());

    String ret = getPageParameters().getString("return");

    if (ret != null) {
      PageParameters params = new PageParameters();
      params.add("page", getPageName());
      setResponsePage(getPageClass(ret), params);
    }
  }
}
