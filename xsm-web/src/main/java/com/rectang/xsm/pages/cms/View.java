package com.rectang.xsm.pages.cms;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;

/**
 * The main CMS view tab
 *
 * @author Andrew Williams
 * @version $Id: View.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 */
public class View extends DocumentPage {
  public View(PageParameters parameters) {
    super(parameters);
  }

  public void layout() {
    super.layout();
    if (hasError()) return;

    getDoc().unlock(getXSMSession().getUser());

    add(new Label("content", getDoc().view("", getXSMSession().getUser()))
        .setEscapeModelStrings(false));
  }
}
