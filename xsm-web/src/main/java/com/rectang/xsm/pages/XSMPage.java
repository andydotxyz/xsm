package com.rectang.xsm.pages;

import com.rectang.xsm.*;
import com.rectang.xsm.pages.admin.xsm.Setup;
import com.rectang.xsm.site.Site;
import com.rectang.xsm.pages.admin.Upgrade;
import com.rectang.xsm.panels.ContentPanel;
import com.rectang.xsm.panels.LoginPanel;
import com.rectang.xsm.panels.XSMFeedbackPanel;
import org.codehaus.plexus.wicket.PlexusPageFactory;
import org.codehaus.plexus.wicket.PlexusWebPage;
import org.apache.wicket.*;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: XSMPage.java 832 2011-09-26 21:45:04Z andy $
 * @since 2.0
 */
public abstract class XSMPage extends PlexusWebPage {
  private Label title, titleHead;
  private HeaderContributor style, layout, theme;

  public XSMPage() {
    // test that the system is installed, if not then load the setup page to show the user what's wrong...
    if (!Config.isInstalled()) {
      if (this instanceof Setup) {
        return;
      } else {
        throw new RestartResponseAtInterceptPageException(Setup.class);
      }
    }

    if (this instanceof Secure) {
      UserData user = getXSMSession().getUser();
      if (user == null && !(this instanceof Register)) {
        if (!(this instanceof Login)) {
          if (this instanceof Logout) {
            PageParameters siteParams = new PageParameters();
            if (getPageParameters().getString("sitename") != null) {
              siteParams.add("sitename", getPageParameters().getString("sitename"));
            }

            setResponsePage(getPageClass("login"), siteParams);
          } else {
            PageParameters siteParams = new PageParameters();
            if (getPageParameters().getString("sitename") != null) {
              siteParams.add("sitename", getPageParameters().getString("sitename"));
            }

            Page page = ((PlexusPageFactory) getPageFactory()).getPage("login", siteParams);
            throw new RestartResponseAtInterceptPageException(page);
          }
        }
        return;
      }

      Site site = getXSMSession().getSite();
      if ( site != null ) {
        // check for site upgrades
        if (site.needsUpgrade() && !(this instanceof Upgrade)) {
          throw new RestartResponseAtInterceptPageException(getPageClass("upgrade"));
        }
      }

      Secure securePage = (Secure) this;
      if (securePage.getLevel() == AccessControl.ADMIN && (user == null || !user.isXSMAdmin() )) {
        throw new AuthorizationException("You must be an admin to access this page"){};
      }
      else if (securePage.getLevel() == AccessControl.MANAGER && (user == null || !user.isSiteAdmin() )) {
        throw new AuthorizationException("You must be a site admin to access this page"){};
      }
    }
  }

  protected void onBeforeRender() {
    super.onBeforeRender();
    UserData user = getXSMSession().getUser();

    if (style != null) {
      remove(style);
      remove(layout);
      remove(theme);
    }
    if (user != null) {
      title.getModel().setObject(user.getSite().getTitle());
      titleHead.getModel().setObject(user.getSite().getTitle() + " [edit]");
      layout = HeaderContributor.forCss(user.getSite().getRootUrl() + "/_theme/layout.css");
      style = HeaderContributor.forCss(user.getSite().getRootUrl() + "/_theme/style.css");
    } else if (getXSMSession().getSite() != null) {
      Site site = getXSMSession().getSite();
      if (site.exists()) {
        title.getModel().setObject(site.getTitle());
        titleHead.getModel().setObject(site.getTitle() + " [edit]");
        layout = HeaderContributor.forCss(site.getRootUrl() + "/_theme/layout.css");
        style = HeaderContributor.forCss(site.getRootUrl() + "/_theme/style.css");
      } else {
        title.getModel().setObject("XSM - eXtensible Site Manager");
        titleHead.getModel().setObject("XSM");
        layout = HeaderContributor.forCss(XSMApplication.class, "publish/layout/menu-left.css");
        style = HeaderContributor.forCss(XSMApplication.class, "publish/style/grey.css");
      }
    } else {
      title.getModel().setObject("XSM - eXtensible Site Manager");
      titleHead.getModel().setObject("XSM");
      layout = HeaderContributor.forCss(XSMApplication.class, "publish/layout/menu-left.css");
      style = HeaderContributor.forCss(XSMApplication.class, "publish/style/grey.css");
    }
    add(layout);
    add(style);

    // and add the xsm theme files which can override the site
    String themeStr = Theme.getTheme(user);
    add(theme = HeaderContributor.forCss(XSMApplication.class, "themes/" + themeStr + "/xsm-editor.css"));
  }

  public void layout() {
    UserData user = getXSMSession().getUser();

    if (getPageParameters().getString("sitename") != null) {
      ((XSMSession)getSession()).setRequestedSite(getPageParameters().getString("sitename"));
    }
    add(title = new Label("site-title", ""));
    titleHead = new Label("site-title-head", "");
    add(titleHead.setRenderBodyOnly(true));

    WebMarkupContainer container = new WebMarkupContainer("user-buttons");
    container.setVisible(getXSMSession().isUserLoggedIn());
    add(container);

    PluginLink link = new PluginLink("profilePlugin", getPageClass("profile"), "profile");
    container.add(link);
    link = new PluginLink("preferencesPlugin", getPageClass("preferences"), "preferences");
    container.add(link);
    link = new PluginLink("logoutPlugin", getPageClass("logout"), "logout");
    container.add(link);

    container = new WebMarkupContainer("admin-buttons");
    container.setVisible(user != null && user.isSiteAdmin());
    add(container);

    link = new PluginLink("settingsPlugin", getPageClass("site"), "settings");
    container.add(link);
    link = new PluginLink("themePlugin", getPageClass("theme"), "theme");
    container.add(link);
    link = new PluginLink("usersPlugin", getPageClass("users"), "users");
    container.add(link);
    link = new PluginLink("backupPlugin", getPageClass("backup"), "backup");
    container.add(link);
    link = new PluginLink("systemPlugin", getPageClass("system"), "system");
    container.add(link);

    container = new WebMarkupContainer("xsmadmin-buttons");
    container.setVisible(user != null && user.isSiteAdmin());
    add(container);

    link = new PluginLink("adminPlugin", getPageClass("admin"), "admin");
    container.add(link);

    add(new PluginLink("helpPlugin", getPageClass("help"), "help"));

    if (user == null) {
      String sitename = getPageParameters().getString("sitename");

      add(new LoginPanel("xsm-tree", sitename, getXSMSession()));
    } else {
      String page = getPageParameters().getString("page");

      add(new ContentPanel("xsm-tree", getXSMSession(), page));
    }

    add(new XSMFeedbackPanel("feedback"));

    // footer
    add(new Label("xsm-version", XSM.getConfig().getVersion()));
  }

  public XSMSession getXSMSession() {
    XSMSession sess = (XSMSession) getSession();

    return sess;
  }

  class PluginLink extends BookmarkablePageLink {
    public PluginLink(String id, Class link, String img) {
      super(id, link);

      add(new Image("pluginImage", new ResourceReference(this.getClass(),
          "buttons/" + img + ".png")));
    }
  }
}
