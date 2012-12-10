package com.rectang.xsm.pages.cms;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import com.rectang.xsm.UserData;
import com.rectang.xsm.site.Site;
import com.rectang.xsm.pages.admin.Users;

import java.util.List;

/**
 * The main CMS permissions tab
 *
 * @author Andrew Williams
 * @version $Id: Permissions.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 */
public class Permissions extends DocumentPage {
  public Permissions(PageParameters parameters) {
    super(parameters);
  }

  public void layout() {
    super.layout();
    if (hasError()) return;

    final UserData user = getXSMSession().getUser();
    Site site = getXSMSession().getSite();

    Label owner = new Label("owner");
    owner.setDefaultModel(new StringResourceModel("owner", owner,
            new Model(new UserData(getDoc().getOwner(), site, false))));
    add(owner);

    add(new ListView<UserData>("editors", Users.getUserList(site)){
      protected void populateItem(ListItem listItem) {
        final UserData next = (UserData) listItem.getModelObject();
        CheckBox edit = new CheckBox("edit"){
          public void onSelectionChanged() {
            super.onSelectionChanged();
            Boolean isEditor = (Boolean) this.getModelObject();

            List users = getDoc().getEditors();
            if (isEditor.equals(Boolean.TRUE))
              users.add(next.getUsername());
            else
              users.remove(next.getUsername());
            getDoc().setEditors(users, user);
          }

          protected boolean wantOnSelectionChangedNotifications() {
            return true;
          }
        };
        edit.setEnabled((user.isSiteAdmin() || getDoc().isOwner(user))
            && !(next.isSiteAdmin() || next.isSiteEditor()));
        edit.setModel(new Model(Boolean.valueOf(
            next.isSiteAdmin() || next.isSiteEditor() || getDoc().isOwner(next) || getDoc().getEditors().contains(next.getUsername()))));
        listItem.add(edit);

        listItem.add(new Label("username", next.getUsername()));
      }
    });
  }
}
