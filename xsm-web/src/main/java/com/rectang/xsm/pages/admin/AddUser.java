package com.rectang.xsm.pages.admin;

import com.rectang.xsm.pages.Register;
import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.*;
import com.rectang.xsm.io.RemoteDocument;
import com.rectang.xsm.site.Visitor;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * Page for adding a user to the site
 *
 * @author Andrew Williams
 * @version $Id: AddUser.java 823 2010-12-15 23:59:49Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="add-user"
 */
public class AddUser extends XSMPage implements Secure {

  public int getLevel() {
    return AccessControl.MANAGER;
  }

  protected com.rectang.xsm.site.Site getSite() {
    return getXSMSession().getSite();
  }

  public void layout() {
    super.layout();

    add(new UserForm("adduser"));
  }

  class UserForm extends Form {
    private String username, email, password, repeatPassword;

    public UserForm(String id) {
      super(id);
      setModel(new CompoundPropertyModel(this));

      PasswordTextField password, password2;
      add(new TextField("username").setRequired(true));
      add(new TextField("email").setRequired(true));
      add((password = new PasswordTextField("password")).setRequired(true));
      add((password2 = new PasswordTextField("repeatPassword")).setRequired(true));

      add(new EqualPasswordInputValidator(password, password2));
      add(new Button("add"));      
    }

    public void onSubmit() {
      if (username.indexOf(' ') > -1) {
        warn("Could not create user, username may not contain spaces");
        return;
      }
      RemoteDocument doc = RemoteDocument.getDoc(getSite(), "/members/" + username, true);
      if (doc.exists()) {
        warn("Could not create user, username " + username + " is already taken");
        return;
      }

      UserData newData = new UserData(username, getSite());

      newData.setEmail(email);
      newData.setPassword(password);
      newData.setSiteEditor(true);

      if (newData.save()) {
        if (getSite().getTechnologies().contains("apache")) {
          getSite().setVisitor(new Visitor(username, password));
        }
        getSession().info("Successfully added user " + username);

        if (AddUser.this instanceof Register) {
          PageParameters siteParams = new PageParameters();
          siteParams.add("sitename", getSite().getId());
          siteParams.add("page", getSite().getDefault());
          setResponsePage(getPageClass("page-view"), siteParams);
        } else {
          setResponsePage(getPageClass("users"));
        }
      } else {
        error("Could not save user file for " + username);
      }
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getRepeatPassword() {
      return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
      this.repeatPassword = repeatPassword;
    }
  }
}
