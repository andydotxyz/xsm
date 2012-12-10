package com.rectang.xsm.pages.cms;

import com.rectang.xsm.MetaData;
import com.rectang.xsm.site.Site;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.PageParameters;

import com.rectang.xsm.doc.SupportedOption;
import com.rectang.xsm.wicket.OptionPanel;
import org.apache.wicket.model.PropertyModel;

/**
 * The main CMS options tab
 *
 * @author Andrew Williams
 * @version $Id: Options.java 818 2010-05-30 14:04:21Z andy $
 * @since 2.0
 */
public class Options extends DocumentPage {
  private String oldPath;
  private MetaData metadata;

  public Options(PageParameters parameters) {
    super(parameters);
  }

  public void layout() {
    super.layout();
    if (hasError()) return;

    oldPath = getDocumentPage().getPublishedPath();
    metadata = getDoc().getMetadata();
    add(new OptionsForm("optionsform"));
  }

  private class OptionsForm extends Form {
    public OptionsForm(String id) {
      super(id);
      final boolean canEdit = getDoc().canEdit(getXSMSession().getUser());

      add(new TextField("title", new PropertyModel(metadata, "title")));
      add(new TextArea("description", new PropertyModel(metadata, "description")));

      add(new TextField("slug", new PropertyModel(this, "slug")));

      add(new ListView<SupportedOption>("options", getDoc().getSupportedOptions(getXSMSession().getUser())) {
        protected void populateItem(ListItem listItem) {
          SupportedOption option = (SupportedOption) listItem.getModelObject();

          // TODO if canEdit we display edit fields instead of the text
          // Make some SupportedOption wicket panels to drop in
          listItem.add(new Label("label", option.getDescription()));
          if (canEdit) {
            switch (option.getType()) {
              case SupportedOption.TYPE_BOOL:
                listItem.add(new OptionPanel.BooleanOption("option", option, getDoc()));
                break;
              case SupportedOption.TYPE_INT:
                listItem.add(new OptionPanel.IntegerOption("option", option, getDoc()));
                break;
              default:
                listItem.add(new OptionPanel.StringOption("option", option, getDoc()));
            }
          } else {
            switch (option.getType()) {
              case SupportedOption.TYPE_BOOL:
                listItem.add(new Label("option", option.getBoolean(getDoc())?"yes":"no"));
                break;
              case SupportedOption.TYPE_INT:
                listItem.add(new Label("option", String.valueOf(option.getInteger(getDoc()))));
                break;
              default:
                listItem.add(new Label("option", "\"" + option.getString(getDoc()) + "\""));
            }
          }
        }
      });

      Button save = new Button("save");
      save.setVisible(canEdit);
      add(save);

      Button reset = new Button("reset");
      reset.setVisible(canEdit);
      add(reset);
    }

    public String getSlug() {
      return getDocumentPage().getSlug();
    }
      
    public void setSlug(String slug) {
      getDocumentPage().setSlug(slug);
    }

    protected void onSubmit() {
      super.onSubmit();

      // TODO should we sanity check the slug (i.e. unique...)
      metadata.save();
      getDoc().save();
      if (!oldPath.equals(getDocumentPage().getPublishedPath())) {
          getDocumentPage().getSite().save();
        rename();

        oldPath = getDocumentPage().getPublishedPath();
        getDocumentPage().getSite().publish(getXSMSession().getUser());
      } else {
        getDoc().publish(getXSMSession().getUser());
      }
    }

    private void rename() {
      Site site = getDocumentPage().getSite();
      if (getXSMPage() instanceof com.rectang.xsm.site.DocumentPage) {
        // what to do if we fail?
        site.getPublishedDoc(oldPath).rename(getDocumentPage().getPublishedPath());
      }
    }
  }
}
