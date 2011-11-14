package com.rectang.xsm.panels;

import com.rectang.xsm.XSM;
import com.rectang.xsm.pages.cms.View;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.Session;

import com.twmacinta.util.MD5;
import com.rectang.xsm.UserData;
import com.rectang.xsm.wicket.LangDropDownChoice;
import com.rectang.xsm.pages.XSMSession;
import com.rectang.xsm.pages.Dashboard;
import com.rectang.xsm.site.Site;
import org.apache.wicket.util.convert.ConversionException;

import java.util.Locale;

/**
 * Panel for handling user logins.
 * All validation (except the existance of a site and the authentication is
 * done by wicket.
 *
 * @author Andrew Williams
 * @version $Id: LoginPanel.java 827 2011-09-25 12:18:02Z andy $
 * @since 2.0
 */
public class LoginPanel extends Panel {

  private XSMSession session;
  private boolean dedicated = XSM.getConfig().isDedicated();

  public LoginPanel(String id, XSMSession session) {
    this(id, null, session);
  }

  public LoginPanel(String id, String sitename, XSMSession session) {
    super(id);
    this.session = session;

    add(new LoginForm("login", sitename));
  }

  class LoginForm extends Form {
    String username, password, sitename, lang;

    public LoginForm(String id, String sitename) {
      super(id);
      if (sitename != null)
        this.sitename = sitename;
      if (this.sitename == null && session.getSite() != null)
        this.sitename = session.getSite().getId();

      add(new TextField("username", new PropertyModel(this, "username"))
          .setRequired(true));
      add(new PasswordTextField("password", new PropertyModel(this, "password"))
          .setRequired(true));
      add(new TextField("sitename", new PropertyModel(this, "sitename")) {
        protected Object convertValue(String[] value) throws ConversionException {
          Object site = super.convertValue(value);

          // set the site based on the user input
          setSitename(site.toString());
          ((XSMSession)getSession()).setRequestedSite(site.toString());
          return site;
        }
      }.setRequired(!dedicated).setVisible(!dedicated));
//      add(new LangDropDownChoice("lang", new PropertyModel(this, "lang"), session, true));

      add(new Button("login", new ResourceModel("login.button")));
    }

    public void onSubmit() {
      if (!com.rectang.xsm.site.Site.getSiteList().contains(getSitename())) {
        getSession().warn("Error logging in - site \"" + getSitename()
            + "\" is not recognised");
        return;
      }

      UserData userData = new UserData(getUsername(), new Site(getSitename()));
      if (userData.getUsername() == null) {
        getSession().warn("Error logging in - username or password incorrect");
        return;
      }

      try {
        MD5 md5 = new MD5(getPassword());
        String passEnc = md5.asHex();
        if (passEnc.equals(userData.getPassword())) {
          session.setUser(userData);
          if (userData.getLocale() != null){
            Session.get().setLocale(new Locale(userData.getLocale()));
          }
          /* mark this as the last login time */
          userData.save();

          getSession().info("You have logged in to site " + getSitename()
              + " as " + userData.getUsername());

          /* if this fails we fall to the login page content */
          if (!continueToOriginalDestination()) {
            PageParameters params = new PageParameters();
            params.add("page", userData.getSite().getDefault());
            setResponsePage(View.class, params);
          }
        } else {
          getSession().warn("Error logging in - username or password incorrect");
        }
      } catch (Exception e) {
        e.printStackTrace();
        getSession().error("Error reading user file");
      }
    }

    public String getUsername() {
      return username;
    }

    public void setUsername( String username ) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword( String password ) {
      this.password = password;
    }

    public String getSitename() {
      return sitename;
    }

    public void setSitename( String sitename ) {
      this.sitename = sitename;
    }

    public String getLang() {
      return lang;
    }

    public void setLang( String lang ) {
      this.lang = lang;
    }
  }
}
