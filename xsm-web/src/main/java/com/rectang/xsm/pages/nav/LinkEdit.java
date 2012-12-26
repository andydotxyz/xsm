package com.rectang.xsm.pages.nav;

import com.rectang.xsm.pages.XSMPage;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.PropertyModel;

/**
 * The main CMS view tab
 *
 * @author Andrew Williams
 * @version $Id: LinkEdit.java 818 2010-05-30 14:04:21Z andy $
 * @since 2.0
 */
public class LinkEdit extends LinkPage {
  EditLinkForm form;

  public LinkEdit(PageParameters parameters) {
    super(parameters);

    add(form = new EditLinkForm("editlink"));
  }

  public void layout() {
    super.layout();
  }

  class EditLinkForm extends Form {
    public EditLinkForm(String id) {
      super(id);

      add(new TextField<String>("link", new PropertyModel<String>(getXSMPage(), "link")));

      SubmitLink submit = new SubmitLink("saveButton");
      submit.add(new Image("saveImage", new ResourceReference(XSMPage.class, "buttons/save.png")));
      add(submit.setVisible(isCMSPageEditing()));
    }


    protected void onSubmit() {
      super.onSubmit();
      getXSMSession().getSite().save();

      if (!getXSMPage().getHidden()) {
        getXSMSession().getSite().publish(getXSMSession().getUser());
      }
      setResponsePage(LinkView.class, getPageNameParams());
    }
  }

  public Form getEditForm() {
    return form;
  }
}
