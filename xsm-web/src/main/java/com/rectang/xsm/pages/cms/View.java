package com.rectang.xsm.pages.cms;

import org.apache.wicket.markup.html.basic.Label;
import com.rectang.xsm.MetaData;

/**
 * The main CMS view tab
 *
 * @author Andrew Williams
 * @version $Id: View.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="page-view"
 */
public class View extends DocumentPage {

  public void layout() {
    super.layout();
    if (hasError()) return;

    getDoc().unlock(getXSMSession().getUser());

    MetaData meta = getDoc().getMetadata();
    add(new Label("title", meta.getTitle()).setEscapeModelStrings(false));
    add(new Label("description", meta.getDescription()).setEscapeModelStrings(false));

    add(new Label("content", getDoc().view("", getXSMSession().getUser()))
        .setEscapeModelStrings(false));
  }
}
