package com.rectang.xsm.pages;

import com.rectang.xsm.UserData;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.site.Visitor;
import com.twmacinta.util.MD5;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.PropertyModel;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: EditPassword.java 816 2010-05-30 14:02:03Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="edit-password"
 */
public class EditPassword extends XSMPage implements Secure {
  public int getLevel() {
    return AccessControl.MEMBER;
  }

  public void layout() {
    super.layout();

      UserData user = EditProfile.getUserToEdit(getPageParameters(), getXSMSession().getUser());

    add(new PasswordForm("profile", user));
  }

  class PasswordForm extends Form
  {
    UserData user;
    String oldPassword, password, confirmPassword;

    public PasswordForm(String id, UserData user) {
      super(id);
      this.user = user;

      add(new PasswordTextField("oldPassword", new PropertyModel(this, "oldPassword")));
      add(new PasswordTextField("password", new PropertyModel(this, "password")));
      add(new PasswordTextField("confirmPassword", new PropertyModel(this, "confirmPassword")));
    }

    public void onSubmit() {
      MD5 md5 = new MD5(oldPassword);
      if (!md5.asHex().equals(user.getPassword())) {
        warn("Old password is not correct");
        return ;
      }

      if (password == null || confirmPassword == null ||
          password.equals("") || !password.equals(confirmPassword)) {
        warn("Passwords must match");
        return;
      }

      user.setPassword(password);

      if (user.save()) {
        if (user.getSite().getTechnologies().contains("apache")) {
          user.getSite().setVisitor(new Visitor(user.getUsername(), password));
        }

        this.setResponsePage(getPageClass("profile"));
      }
    }

    public String getOldPassword() {
      return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
      this.oldPassword = oldPassword;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getConfirmPassword() {
      return confirmPassword;
    }

    public void setConfirmPassword( String confirmPassword ) {
      this.confirmPassword = confirmPassword;
    }
  }
}
