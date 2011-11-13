package com.rectang.xsm.pages.cms;

import com.rectang.xsm.AccessControl;
import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.site.HierarchicalPage;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.*;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.List;
import java.util.Vector;

/**
 * The main CMS pages parent
 *
 * @author Andrew Williams
 * @version $Id: Page.java 831 2011-09-25 12:59:18Z andy $
 * @since 2.0
 */
public abstract class Page extends XSMPage implements Secure {
  private List tabs;
  private boolean error = false;

  public Page() {
    tabs = new Vector();

    tabs.add("page-contents");
  }

  public void addTab(String tab) {
    tabs.add(tab);
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
      setResponsePage(getPageClass("error"));
      error = true;
      return;
    }
    if (getXSMPage() == null) {
      error("Page " + pageName + " does not exist");
      setResponsePage(getPageClass("error"));
      error = true;
      return;
    }

    add(new ListView("tabs", tabs) {
      protected void populateItem(ListItem listItem) {
        String tabId = (String) listItem.getModelObject();
        listItem.add(addWicketTab(tabId));
        listItem.setRenderBodyOnly(true);
      }
    });

    // TODO - figure what we should be doing with this new tab thingy
    add(addWicketTab("page-new", "tab-new").setVisible((getXSMPage() instanceof HierarchicalPage) && canEdit()));
    WebMarkupContainer space = new WebMarkupContainer("tab-new-spacer");
    space.setVisible((getXSMPage() instanceof HierarchicalPage) && canEdit());
    add(space);

    
    if (pageName == null) {
      error("No page selected");
    }
  }

  protected String getPageName() {
    return getPageParameters().getString("page");
  }

  public com.rectang.xsm.site.Page getXSMPage() {
    if (getPageName() == null)
      return null;

    return getXSMSession().getSite().getPage(getPageName());
  }

  private WebMarkupContainer addWicketTab(final String title) {
    return addWicketTab(title, "tab-cell");
  }

  private WebMarkupContainer addWicketTab(final String tabID, String id) {
    final Class thisClass = this.getClass();
    final Class page = getPageClass(tabID);
    WebMarkupContainer tabCell = new WebMarkupContainer(id);
    tabCell.add(new AttributeModifier("class", new Model() {
      public Object getObject() {
        if (thisClass.equals(page))
          return "xsm-editor-tab-active";

        return "xsm-editor-tab-inactive";
      }
    }));

    final Link tab = new BookmarkablePageLink("tab-link", page, getPageNameParams());
    tabCell.add(tab);

    Label label = new Label("tab-label",
        new StringResourceModel("tab." + tabID + ".title", tab, null));
    tab.add(label.setRenderBodyOnly(true));
    return tabCell;
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
}
