package com.rectang.xsm.pages.nav;

import com.rectang.xsm.io.RemoteDocument;
import com.rectang.xsm.site.HierarchicalPage;
import com.rectang.xsm.site.Site;
import com.rectang.xsm.pages.cms.Page;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;

import java.util.Iterator;

/**
 * The main CMS contents tab delete page
 *
 * @author Andrew Williams
 * @version $Id: Delete.java 831 2011-09-25 12:59:18Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="page-delete"
 */
public class Delete extends Page {

  public void layout() {
    super.layout();

    if (!canEdit()) {
      warn("You do not have permission to delete this page");
      setResponsePage(getPageClass("page-contents"), getPageNameParams());
      return;
    }

    add(new Label("confirm", getString("confirm", new Model(getXSMPage()))));
    add(new DeleteForm("deleteform"));
  }

  private class DeleteForm extends Form {
    public DeleteForm(String id) {
      super(id);

      add(new Button("yes") {
        public void onSubmit() {
          Site site = getXSMSession().getSite();

          HierarchicalPage parentPage = getXSMPage().getParent();
          String pagePath = getXSMPage().getPath();
          boolean wasHidden = getXSMPage().getHidden();
          /* try to delete the page from the site, then the page from the xsm
           * store then all of its sub pages from xsm store */
          boolean deleted = true;
          if (getXSMPage() instanceof com.rectang.xsm.site.DocumentPage) {
            deleted = ((com.rectang.xsm.site.DocumentPage) getXSMPage()).getXSMDocument().delete();
          }
          if (deleted && getXSMPage().delete()) {
            (RemoteDocument.getDoc(site, "/data" + pagePath, false)).delete(); /* rm dir */
            /* then delete the pages from the server */
            site.getPublishedDoc(pagePath).delete();
            site.save();
            getSession().info("Page " + pagePath + " deleted successfully");

            if (!wasHidden) {
              site.publish(getXSMSession().getUser());
            }
            HierarchicalPage requestedPage = parentPage;

            /* if there is no parent page just display the first... */
            if (requestedPage.equals(site.getRootPage())) {
              Iterator pages = site.getPages().iterator();
              if (pages.hasNext()) {
                // TODO fix - the first page in the site may not be a heirarchical page...
                requestedPage = (HierarchicalPage) pages.next();
              }
            }

            PageParameters newPage = new PageParameters();
            newPage.add("page", requestedPage.getPath());
            setResponsePage(getPageClass("page-contents"), newPage);

          } else {
            error("Failed to deleted page " + pagePath);
          }
        }
      });

      add(new Button("no") {
        public void onSubmit() {
          setResponsePage(getPageClass("page-contents"), getPageNameParams());
        }
      });
    }
  }
}
