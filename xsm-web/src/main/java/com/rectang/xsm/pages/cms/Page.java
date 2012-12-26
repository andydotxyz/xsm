package com.rectang.xsm.pages.cms;

import com.rectang.xsm.AccessControl;
import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;

import org.apache.wicket.*;

/**
 * The main CMS pages parent
 *
 * @author Andrew Williams
 * @version $Id: Page.java 831 2011-09-25 12:59:18Z andy $
 * @since 2.0
 */
public abstract class Page extends XSMPage implements Secure {
  private boolean error = false;
  private String pageName;

  public Page(PageParameters parameters) {
    super(parameters);

    pageName = parameters.getString("page");
  }

  public int getLevel() {
    return AccessControl.MEMBER;
  }

  public boolean canEdit() {
    return getXSMSession().getUser().isSiteAdmin();
  }

  public void layout() {
    super.layout();
    // verify the page name is specified
    String pageName = getPageName();
    if (pageName == null) {
      error("You must specify a page");
      setResponsePage(com.rectang.xsm.pages.Error.class);
      error = true;
      return;
    }
    if (getXSMPage() == null) {
      error("Page " + pageName + " does not exist");
      setResponsePage(com.rectang.xsm.pages.Error.class);
      error = true;
      return;
    }

    if (pageName == null) {
      error("No page selected");
    }
  }

  protected String getPageName() {
    return pageName;
  }

  public com.rectang.xsm.site.Page getXSMPage() {
    if (getPageName() == null)
      return null;

    return getXSMSession().getSite().getPage(getPageName());
  }

  protected PageParameters getPageNameParams() {
    PageParameters params = new PageParameters();
    if (getPageName() != null) {
      params.add("page", getPageName());
    }

    return params;
  }

  protected boolean hasError() {
    return error;
  }

  @Override
  public Class<? extends Page> getCMSEditPage() {
    return Edit.class;
  }
}
