package com.rectang.xsm.pages.nav;

import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

/**
 * The main CMS view tab
 *
 * @author Andrew Williams
 * @version $Id: LinkEdit.java 818 2010-05-30 14:04:21Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="link-edit"
 */
public class LinkEdit extends LinkPage {

  public void layout() {
    super.layout();

    add(new EditLinkForm("editlink"));
  }

  class EditLinkForm extends Form {
    public EditLinkForm(String id) {
      super(id);

      add(new TextField("link", new PropertyModel(getXSMPage(), "link")));
    }


    protected void onSubmit() {
      super.onSubmit();
      getXSMSession().getSite().save();

      if (!getXSMPage().getHidden()) {
        getXSMSession().getSite().publish(getXSMSession().getUser());
      }
      setResponsePage(getPageClass("link-view"), getPageNameParams());
    }
  }
}
