package com.rectang.xsm.pages;

import com.rectang.xsm.UserData;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.pages.admin.Users;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.PageParameters;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: EditProfile.java 663 2007-10-04 22:50:25Z aje $
 * @since 2.0
 */
public class EditProfile extends XSMPage implements Secure {
  public EditProfile(PageParameters parameters) {
    super(parameters);
  }

  public int getLevel() {
    return AccessControl.MEMBER;
  }

  public void layout() {
    super.layout();

    UserData user = getUserToEdit(getPageParameters(), getXSMSession().getUser());

    add(new ProfileForm("profile", user));
  }

  class ProfileForm extends Form {
    UserData user;
    public ProfileForm(String id, UserData user) {
      super(id);
      this.user = user;

      setModel(new CompoundPropertyModel(user));
      add(new TextField("name"));
      add(new TextField("email"));
      add(new TextField("homepage"));
      add(new TextField("avatar"));

      WebMarkupContainer admin = new WebMarkupContainer("admin");
      admin.setVisible(getXSMSession().getUser().isSiteAdmin());
      admin.add(new TextField("note"));
      add(admin);
    }

    public void onSubmit() {
      if (user.save()) {
        if (user.getUsername().equals(getXSMSession().getUser().getUsername())) {
          // TODO make the userdata singleton, so this is not needed
          getXSMSession().setUser(user);
          this.setResponsePage(Profile.class);
        } else {
          this.setResponsePage(Users.class);
        }
      }
    }
  }

  public static UserData getUserToEdit(PageParameters params, UserData currentUser) {
    if (!currentUser.isSiteAdmin())
      return currentUser;

    if (params == null)
      return currentUser;

    String username = params.getString("username");
    if (username == null || username.trim().length() == 0)
      return currentUser;

    return new UserData(username, currentUser.getSite(), false);
  }
}
