package com.rectang.xsm.pages.admin;

import com.rectang.xsm.pages.EditProfile;
import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.UserData;
import com.rectang.xsm.XSM;
import com.rectang.xsm.site.Visitor;
import com.rectang.xsm.util.EmailUtils;
import com.rectang.xsm.wicket.DetachableUserDataModel;
import com.rectang.xsm.io.RemoteDocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;

import java.util.*;
import java.io.File;

/**
 * Page for managing the site's users (and groups in the future)
 *
 * TODO add confirmation for deleting users
 * TODO either add confirmation for, or disallow a user from deleting or removing admin from himself
 *
 * @author Andrew Williams
 * @version $Id: Users.java 816 2010-05-30 14:02:03Z andy $
 * @since 2.0
 */
public class Users extends XSMPage implements Secure {
  public Users(PageParameters parameters) {
    super(parameters);
  }

  public int getLevel() {
    return AccessControl.MANAGER;
  }

  public void layout() {
    super.layout();

    UserData user = getXSMSession().getUser();
    final com.rectang.xsm.site.Site site = getXSMSession().getSite();

    List columns = new ArrayList();
    columns.add(new PropertyColumn(new Model("Username"), "username", "username"));
    columns.add(new PropertyColumn(new Model("Name"), "name"));
    columns.add(new AbstractColumn(new Model("Status")) {

      public void populateItem(Item item, String componentId, IModel model) {
        UserData user = (UserData) model.getObject();

        String status = "";
        if (user.isXSMAdmin())
          status = "XSM Admin";
        else if (user.isSiteAdmin())
          status = "Site Admin";
        else if (user.isSiteEditor())
          status = "Site Editor";

        item.add(new Label(componentId, status));
      }
    });
    columns.add(new PropertyColumn(new Model("Email"), "email"));
    columns.add(new PropertyColumn(new Model("Last Login"), "lastLogin"));

    columns.add(new AbstractColumn(new Model("Actions")){
      public void populateItem(Item item, String componentId, IModel model) {
        item.add(new UsersActionPanel(componentId, ((UserData) model.getObject())));
      }
    });
    columns.add(new PropertyColumn(new Model("Notes"), "note"));

    add(new DefaultDataTable("users", columns, new UserDataProvider(site), 10));

    // Add a permissions table too
    columns = new ArrayList();
    columns.add(new PropertyColumn(new Model("Username"), "username", "username"));
    columns.add(new PropertyColumn(new Model("Name"), "name"));
    columns.add(new AbstractColumn(new Model("Site Admin")) {
      public void populateItem(Item item, String componentId, IModel model) {
        UserData user = (UserData) model.getObject();
        item.add(new PermissionPanel(componentId, user, "siteAdmin"));
      }
    });
    columns.add(new AbstractColumn(new Model("Site Editor")) {
      public void populateItem(Item item, String componentId, IModel model) {
        UserData user = (UserData) model.getObject();
        item.add(new PermissionPanel(componentId, user, "siteEditor"));
      }
    });

    add(new DefaultDataTable("permissions", columns, new UserDataProvider(site), 10));

    columns = new ArrayList();
    columns.add(new PropertyColumn(new Model("Username"), "username", "username"));
    columns.add(new AbstractColumn(new Model("Actions")){
      public void populateItem(Item item, String componentId, IModel model) {
        item.add(new VisitorActionPanel(componentId, ((Visitor) model.getObject()), site));
      }
    });
    add(new DefaultDataTable("visitors", columns, new VisitorDataProvider(site), 10)
        .setVisible(site.getTechnologies().contains("apache")));
  }

  public static List getUserList(com.rectang.xsm.site.Site site) {
    return getUserList(site, true);
  }

  public static List getUserList(com.rectang.xsm.site.Site site,
                                 boolean listXSMAdmins) {
    Vector users = new Vector();

    File[] userFiles = (RemoteDocument.getDoc(site, "/members", false)).listFiles();
    for (int i = 0; i < userFiles.length; i++) {
      String next = userFiles[i].getName();
      if (next.endsWith(".xml")) {
        String username = next.substring(0, next.length() - 4);
        users.add(new UserData(username, site, false));
      }
    }

    if (listXSMAdmins) {
      /* list XSM admins too */
      File[] adminFiles = new File(XSM.getConfig().getDataDir(), "admins").listFiles();

      if (adminFiles != null) {
        for (int i = 0; i < adminFiles.length; i++) {
          String next = adminFiles[i].getName();
          if (next.endsWith(".xml")) {
            String username = next.substring(0, next.length() - 4);
            users.add(new UserData(username, site, false));
          }
        }
      }
    }

    return users;
  }

  class UsersActionPanel extends Panel {
    private UserData user;
    public UsersActionPanel(String id, final UserData user) {
      super(id);
      this.user = user;
      String username = user.getUsername();
      boolean iAmXSMAdmin = getXSMSession().getUser().isXSMAdmin();

      WebMarkupContainer component;
      PageParameters params = new PageParameters();
      params.add("username", username);

      // don't allow non-XSMAdmins to edit XSMAdmin accounts
      BookmarkablePageLink edit = new BookmarkablePageLink("edit", EditProfile.class, params);
      if (user.isXSMAdmin() && !iAmXSMAdmin) {
        edit.setVisible(false);
      }
      add(edit);

      add(component = new Link("delete") {
        public void onClick() {
          if (user.isXSMAdmin()) {
            warn("Cannot delete an XSM admin user");
            return;
          }

          // TODO add a confirmation message
          RemoteDocument userFile = RemoteDocument.getDoc(user.getSite(),
              "/members/" + user.getUsername(), true);
          if (userFile.delete()) {
            if (user.getSite().getTechnologies().contains("apache")) {
              user.getSite().removeVisitor(new Visitor(user.getUsername()));
            }
            info("User " + user.getUsername() + " has been deleted");
          } else {
            warn("Could not delete user " + user.getUsername());
          }
        }
      });
      component.add(new Image("delete-icon", new ResourceReference(XSM.class,
          "icons/edit-delete.png")));
      // don't allow deletion of XSM Admins by non-XSMAdmins
      if (user.isXSMAdmin() && !iAmXSMAdmin) {
        component.setVisible(false);
      }

      add(component = new Link("reset") {
        public void onClick() {
          RemoteDocument doc = RemoteDocument.getDoc(user.getSite(),
              "/members/" + user.getUsername(), true);
          if (!doc.exists()) {
            warn("Cannot reset non-existant user " + user.getUsername() + ".");
          } else if (user.isXSMAdmin()) {
            warn("Cannot reset an XSM admin user");
          } else {

            UserData userData = new UserData(user.getUsername(), user.getSite(), false);
            String email = userData.getEmail();
            String newPassword = com.rectang.xsm.util.StringUtils.createPassword();
            userData.setPassword(newPassword);

            if (userData.save()) {
              if (user.getSite().getTechnologies().contains("apache")) {
                user.getSite().setVisitor(new Visitor(user.getUsername(), newPassword));
              }

              if (email != null && !email.equals("")) {
                String subject = "Your XSM password for site " + user.getSite().getId() + " has been reset";
                String body = "The XSM password for user " + user.getUsername() + " in site " + user.getSite().getId()
                    + " has been reset.\n\n"
                    + "Your new password is " + newPassword + " please change it after logging in\n"
                    + "Should you have any further problems please contact your site administrator\n";

                EmailUtils.emailTo(subject, body, email);
                info("Reset user " + user.getUsername() + "'s password");
              } else {
                info("Reset user " + user.getUsername() + "'s password to "
                    + newPassword + ".<br />\n"
                    + " Unfortunately their email address is blank so please"
                    + " let them know!");
              }
            }
          }
        }
      });
      component.add(new Image("reset-icon", new ResourceReference(XSM.class,
          "icons/view-refresh.png")));
      // don't allow resetting of an XSM Admin's passwords by non-XSMAdmins
      if (user.isXSMAdmin() && !iAmXSMAdmin) {
        component.setVisible(false);
      }
    }
  }

  class PermissionPanel extends Panel {
    public PermissionPanel(String id, final UserData user, final String property) {
      super(id);

      CheckBox permission;
      add(permission = new CheckBox("permission", new PropertyModel(user, property)) {
        // setting of the boolean on the user object will save the site data automatically :)

        protected boolean wantOnSelectionChangedNotifications() {
          return true;
        }
      });

      // XSM Admin accounts cannot have permissions switched off
      if (user.isXSMAdmin()) {
        permission.setEnabled(false);
      }
    }
  }

  class VisitorActionPanel extends Panel {
    private Visitor visitor;
    public VisitorActionPanel(String id, final Visitor visitor, final com.rectang.xsm.site.Site site) {
      super(id);
      this.visitor = visitor;
      final String username = visitor.getUsername();

      WebMarkupContainer component;
      PageParameters params = new PageParameters();
      params.add("username", username);

      add(component = new Link("delete") {
        public void onClick() {
          // TODO add a confirmation message

          site.removeVisitor(visitor);
          info("Visitor account " + visitor.getUsername() + " has been deleted");
        }
      });
      component.add(new Image("delete-icon", new ResourceReference(XSM.class,
          "icons/edit-delete.png")));

      add(component = new Link("reset") {
        public void onClick() {
          // TODO set password command
          String newPassword = com.rectang.xsm.util.StringUtils.createPassword();

          site.setVisitor(new Visitor(username, newPassword));
          info("Reset visitor " + visitor.getUsername() + "'s password to "
              + newPassword + ". Please pass this information to them!");
        }
      });
      component.add(new Image("reset-icon", new ResourceReference(XSM.class,
          "icons/view-refresh.png")));
    }
  }
}

class UserDataProvider extends SortableDataProvider {
  private com.rectang.xsm.site.Site site;

  public UserDataProvider(com.rectang.xsm.site.Site site) {
    this.site = site;
  }

  public Iterator iterator(int from, int to) {
    List userList = Users.getUserList(site);
    Collections.sort(userList);
    if (getSort() != null && getSort().isAscending())
      Collections.reverse(userList);

    return userList.subList(from, from + to).iterator();
  }

  public int size() {
    return Users.getUserList(site).size();
  }

  public IModel model(Object object) {
    return new DetachableUserDataModel((UserData) object);
  }
}

class VisitorDataProvider extends SortableDataProvider {
  private com.rectang.xsm.site.Site site;

  public VisitorDataProvider(com.rectang.xsm.site.Site site) {
    this.site = site;
  }

  public Iterator iterator(int from, int to) {
    List visitorList = site.getVisitors();
    Collections.sort(visitorList);
    if (getSort() != null && getSort().isAscending())
      Collections.reverse(visitorList);

    return visitorList.subList(from, from + to).iterator();
  }

  public int size() {
    return site.getVisitors().size();
  }

  public IModel model(Object object) {
    return new Model((Visitor) object);//new DetachableVisitorModel((Visitor) object);
  }
}
