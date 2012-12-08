package com.rectang.xsm.pages.nav;

import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.site.HierarchicalPage;
import com.rectang.xsm.XSM;
import com.rectang.xsm.pages.cms.Page;

import java.util.List;
import java.util.Vector;

import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.AttributeModifier;

/**
 * The main CMS contents tab
 *
 * @author Andrew Williams
 * @version $Id: Contents.java 831 2011-09-25 12:59:18Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="page-contents"
 */
public class Contents extends Page {
  public void layout() {
    super.layout();

    WebMarkupContainer label;
    HierarchicalPage parent = getXSMPage().getParent();
    // check for the grandparent, as we don't want folk to get to the root parent
    if (parent.getParent() != null) {
      add(label = new WebMarkupContainer("parent"));
      PageParameters params = new PageParameters();
      params.add("page", parent.getPath());
      BookmarkablePageLink link = new BookmarkablePageLink("parentlink", getClass(), params);
      link.add(new Label("parentlabel", parent.getTitle()));
      label.add(link);
    } else {
      add((label = new WebMarkupContainer("parent")).setVisible(false));
    }

    List childPages;
    if (getXSMPage() instanceof HierarchicalPage) {
      childPages = ((HierarchicalPage) getXSMPage()).getSubPages();
    } else {
      childPages = new Vector();
    }
    add(new Label("childtext", new StringResourceModel("childpages", label,
        new Model(getXSMPage()))));
    add(new ListView("childpages", childPages) {
      protected void populateItem(ListItem listItem) {
        com.rectang.xsm.site.Page next =
            (com.rectang.xsm.site.Page) listItem.getModelObject();

        listItem.add(new AttributeModifier("class",
            new Model((listItem.getIndex() % 2 == 1)?"odd":"even")));
        generateListing(next, listItem);
      }
    });

    add(new Label("siblingtext", new StringResourceModel("siblingpages", label,
        new Model(getXSMPage().getParent()))));
    add(new ListView("siblingpages", getXSMPage().getParent().getSubPages()) {
      protected void populateItem(ListItem listItem) {
        com.rectang.xsm.site.Page next =
            (com.rectang.xsm.site.Page) listItem.getModelObject();

        listItem.add(new AttributeModifier("class",
            new Model((listItem.getIndex() % 2 == 1)?"odd":"even")));
        generateListing(next, listItem);
      }
    });
  }

  private void generateListing(final com.rectang.xsm.site.Page page, ListItem panel) {
    boolean canEdit;
    if (page instanceof DocumentPage)
      canEdit = ((DocumentPage) page).getXSMDocument().canEdit(getXSMSession().getUser());
    else
      canEdit = getXSMSession().getUser().isSiteAdmin();

    String nextTitle = page.getTitle();
    if (page.getHidden())
      nextTitle = "(" + nextTitle + ")";

    PageParameters subPage = new PageParameters();
    subPage.add("page", page.getPath());

    panel.add(new WebMarkupContainer("page-icon").add(new AttributeModifier("class", true, new Model() {
      public String getObject() {
        return "contenticon " + page.getType();
      }
    })));

    BookmarkablePageLink link = new BookmarkablePageLink("link", getClass(),
        subPage);
    link.add(new Label("label", nextTitle));
    panel.add(link);

    Link showHide = new Link("showhide"){
      public void onClick() {
        page.setHidden(!page.getHidden());
        getXSMSession().getSite().save();

        boolean publish = true;
        HierarchicalPage parent = page.getParent();
        while (parent != null) {
          if (parent.getHidden()) {
            publish = false;
            break;
          }

          parent = parent.getParent();
        }

        if (publish) {
          // TODO perhaps report status on failure?
          getXSMSession().getSite().publish(getXSMSession().getUser());
        }
      }
    };
    showHide.setVisible(canEdit);
    if (page.getHidden()) {
      showHide.add(new Label("label", new StringResourceModel("show", panel, null)));
    } else {
      showHide.add(new Label("label", new StringResourceModel("hide", panel, null)));
    }
    panel.add(showHide);

    Link top = new Link("top"){
      public void onClick() {
        page.moveToTop();
        getXSMSession().getSite().save();
        if (!page.getHidden()) {
          getXSMSession().getSite().publish(getXSMSession().getUser());
        }
        setResponsePage(getPageClass("page-contents"), getPageNameParams());
      }
    };
    top.setVisible(canEdit && panel.getIndex() > 0);
    panel.add(top);
    top.add(new Image("top-icon", new ResourceReference(XSM.class,
          "icons/go-top.png")));

    Link up = new Link("up"){
      public void onClick() {
        page.moveUp();
        getXSMSession().getSite().save();
        if (!page.getHidden()) {
          getXSMSession().getSite().publish(getXSMSession().getUser());
        }
        setResponsePage(getPageClass("page-contents"), getPageNameParams());
      }
    };
    up.setVisible(canEdit && panel.getIndex() > 0);
    panel.add(up);
    up.add(new Image("up-icon", new ResourceReference(XSM.class,
          "icons/go-up.png")));

    Link down = new Link("down"){
      public void onClick() {
        page.moveDown();
        getXSMSession().getSite().save();
        if (!page.getHidden()) {
          getXSMSession().getSite().publish(getXSMSession().getUser());
        }
        setResponsePage(getPageClass("page-contents"), getPageNameParams());
      }
    };
    down.setVisible(canEdit && panel.getIndex() < ((List) panel.getParent().getDefaultModelObject()).size() - 1);
    panel.add(down);
    down.add(new Image("down-icon", new ResourceReference(XSM.class,
          "icons/go-down.png")));

    Link bottom = new Link("bottom"){
      public void onClick() {
        page.moveToBottom();
        getXSMSession().getSite().save();
        if (!page.getHidden()) {
          getXSMSession().getSite().publish(getXSMSession().getUser());
        }
        setResponsePage(getPageClass("page-contents"), getPageNameParams());
      }
    };
    bottom.setVisible(canEdit && panel.getIndex() < ((List) panel.getParent().getDefaultModelObject()).size() - 1);
    panel.add(bottom);
    bottom.add(new Image("bottom-icon", new ResourceReference(XSM.class,
          "icons/go-bottom.png")));

    Link delete = new BookmarkablePageLink("delete", getPageClass("page-delete"),
        subPage);
    delete.setVisible(canEdit);
    panel.add(delete);
    delete.add(new Image("delete-icon", new ResourceReference(XSM.class,
          "icons/edit-delete.png")));

    Link rename = new BookmarkablePageLink("rename", getPageClass("page-rename"),
        subPage);
    rename.setVisible(canEdit);
    panel.add(rename);
    rename.add(new Image("rename-icon", new ResourceReference(XSM.class,
          "icons/rename.png")));    
  }
}
