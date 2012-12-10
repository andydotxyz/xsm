package com.rectang.xsm.pages.nav;

import com.rectang.xsm.pages.cms.Page;
import com.rectang.xsm.site.Site;
import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.site.HierarchicalPage;
import com.rectang.xsm.UserData;
import com.rectang.xsm.io.XSMDocument;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.PageParameters;

import java.io.File;

/**
 * The new tab for adding links to the site menu
 *
 * @author Andrew Williams
 * @version $Id: NewLink.java 831 2011-09-25 12:59:18Z andy $
 * @since 2.0
 */
public class NewLink extends Page {
  public NewLink(PageParameters parameters) {
    super(parameters);
  }

  public void layout() {
    super.layout();

    add(new NewLink.NewForm("newform"));
  }

  private class NewForm extends Form {
    private String name, url = "http://", level = "child";
    public NewForm(String id) {
      super(id);
      Site site = getXSMSession().getSite();
      UserData user = getXSMSession().getUser();

      add(new TextField("name", new PropertyModel(this, "name")).setRequired(true));

      add(new TextField("url", new PropertyModel(this, "url")).setRequired(true));

      RadioGroup grouping;
      add(grouping =
          new RadioGroup("levelgroup", new PropertyModel(this, "level")));

      boolean canAddSibling = false;
      if (getXSMPage().getParent() != null) {
        if (getXSMPage().getParent().equals(site.getRootPage())) {
          canAddSibling = user.isSiteAdmin();
        } else {
          if (getXSMPage().getParent() instanceof DocumentPage) {
            canAddSibling = ((DocumentPage) getXSMPage().getParent()).getXSMDocument().canEdit(user);
          } else {
            canAddSibling = user.isSiteAdmin();
          }
        }
      }
      boolean canAddChild;
      if (getXSMPage().equals(site.getRootPage())) {
        canAddChild = user.isSiteAdmin();
      } else {
        canAddChild = (getXSMPage() instanceof HierarchicalPage) && canEdit();
      }

      grouping.add(new Radio("siblinglevel", new Model("sibling")).setVisible(canAddSibling));
      grouping.add(new Label("siblingtext", new StringResourceModel("level.sibling", this, new Model(getXSMPage()))));
      grouping.add(new Radio("childlevel", new Model("child")).setVisible(canAddChild));
      grouping.add(new Label("childtext", new StringResourceModel("level.child", this, new Model(getXSMPage()))));
    }

    protected void onSubmit() {
      super.onSubmit();

      Site site = getXSMSession().getSite();
      String file = XSMDocument.encode(name);

      HierarchicalPage parent;
      if (level.equals("sibling")) {
        parent = getXSMPage().getParent();
      } else {
        if (!(getXSMPage() instanceof HierarchicalPage)) {
          getSession().error("Cannot add a child page to a non-heirarchical parent");
          return;
        }
        parent = (HierarchicalPage) getXSMPage();
      }

      if (file.charAt(0) == '_') { /* spaces already converted */
        warn("Pages names cannot begin with the characters _& ?+/\"'");
        return;
      }


      String newPath = new File(parent.getPath(), file).getPath();
      com.rectang.xsm.site.Page existing = site.getPage(newPath);
      if (existing != null) {
        warn("Page " + newPath + " already exists");
        return;
      }

      /* Add entry to contents for new page */
      com.rectang.xsm.site.LinkPage newLink =
          new com.rectang.xsm.site.LinkPage(site, parent, name, true);
      newLink.setLink(url);
      parent.addSubPage(newLink);
      if (!site.save()) {
        fatal("Error saving site, could not create new page");
        return;
      }

      PageParameters params = new PageParameters();
      params.add("page", newPath);
      setResponsePage(LinkView.class, params);
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getLevel() {
      return level;
    }

    public void setLevel(String level) {
      this.level = level;
    }
  }
}
