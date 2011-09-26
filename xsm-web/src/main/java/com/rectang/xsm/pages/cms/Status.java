package com.rectang.xsm.pages.cms;

import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.UserData;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * The main CMS status tab
 *
 * @author Andrew Williams
 * @version $Id: Status.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="page-status"
 */
public class Status extends DocumentPage {

  public void layout() {
    super.layout();
    if (hasError()) return;

    XSMDocument xsmDoc = getDoc();

    Label locked = new Label("locked");
    if (xsmDoc.isLocked()) {
      locked.setModel(new StringResourceModel("locked.is", locked,
          new Model(new UserData(xsmDoc.getLocked(),
              getXSMSession().getSite(), false))));
    } else {
      locked.setModel(new StringResourceModel("locked.not", locked, null));
    }
    add(locked);

    List watcherList = xsmDoc.getWatchers();
    WebMarkupContainer watchers = new WebMarkupContainer("watchers");
    watchers.setVisible(watcherList.size() > 0);
    add(watchers);
    boolean watching = watcherList.contains(getXSMSession().getUser().getUsername());

    Label watched = new Label("watched");
    if (watcherList.size() == 0) {
      watched.setModel(new StringResourceModel("watched.not", watched, null));
      (new WebMarkupContainer("watchers")).setVisible(false);
    } else {
      watched.setModel(new StringResourceModel("watched.is", watched, null));

      watchers.add(new ListView("watcher", watcherList) {
        protected void populateItem(ListItem listItem) {
          listItem.add(new Label("username", (String) listItem.getModelObject()));
        }
      });
    }
    add(watched);

    // TODO fix the problem where we have to redirect to the same page on success
    Button watch = new Button("watch"){
      public void onSubmit() {
        UserData userdata = getXSMSession().getUser();
        List watchers = getDoc().getWatchers();
        if (watchers.contains(userdata.getUsername())) {
          warn("You are already watching page " + getDocumentPage().getPath());
        } else {
          watchers.add(userdata.getUsername());
          if (getDoc().setWatchers(watchers, userdata)) {
            setResponsePage(getPageClass("page-status"), getPageNameParams());
          } else {
            info("Failed to add user " + userdata.getUsername() +
                " to watchers of page " + getDocumentPage().getPath());
          }
        }
      }
    };
    Button unwatch = new Button("unwatch") {
      public void onSubmit() {
        UserData userdata = getXSMSession().getUser();

        List watchers = getDoc().getWatchers();
        if (!watchers.contains(userdata.getUsername()))
          warn("You are not watching page " + getDocumentPage().getPath());
        else {
          watchers.remove(userdata.getUsername());
          if (getDoc().setWatchers(watchers, userdata)) {
            setResponsePage(getPageClass("page-status"), getPageNameParams());
          } else {
            info("Failed to remove user " + userdata.getUsername() +
                " from watchers of page " + getDocumentPage().getPath());
          }
        }
      }
    };

    Form update = new Form("update");
    watch.setVisible(!watching);
    update.add(watch);
    unwatch.setVisible(watching);
    update.add(unwatch);
    add(update);
  }
}