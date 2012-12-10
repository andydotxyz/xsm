package com.rectang.xsm.pages.cms;

import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.io.XSMDocument;
import org.apache.wicket.PageParameters;

/**
 * The main CMS pages parent
 *
 * @author Andrew Williams
 * @version $Id: DocumentPage.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 */
public abstract class DocumentPage extends Page implements Secure {
  protected DocumentPage(PageParameters parameters) {
    super(parameters);

    addTab(View.class);
    addTab(Edit.class);
    addTab(Permissions.class);
    addTab(Status.class);
    addTab(Options.class);
    if (getXSMSession().getSite().getTechnologies().contains("apache")) {
      addTab(Security.class);
    }
  }

  public int getLevel() {
    return AccessControl.MEMBER;
  }


  public boolean canEdit() {
    return getXSMPage() != null && getDoc() != null &&
        getDoc().canEdit(getXSMSession().getUser());
  }

  protected com.rectang.xsm.site.DocumentPage getDocumentPage() {
    if (getPageName() == null)
      return null;

    return (com.rectang.xsm.site.DocumentPage) getXSMSession().getSite().getPage(getPageName());
  }

  protected XSMDocument getDoc() {
    if (getDocumentPage() == null)
      return null;

    return getDocumentPage().getXSMDocument();
  }
}
