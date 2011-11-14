package com.rectang.xsm.pages;

import com.rectang.xsm.*;
import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.pages.admin.Upgrade;
import com.rectang.xsm.pages.admin.xsm.Setup;
import com.rectang.xsm.panels.ContentPanel;
import com.rectang.xsm.panels.LoginPanel;
import com.rectang.xsm.panels.XSMFeedbackPanel;
import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.site.Site;
import com.rectang.xsm.wicket.VelocityPanel;
import org.apache.velocity.VelocityContext;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.DefaultMarkupResourceStreamProvider;
import org.apache.wicket.markup.IMarkupCacheKeyProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.codehaus.plexus.wicket.PlexusPageFactory;
import org.codehaus.plexus.wicket.PlexusWebPage;
import org.apache.wicket.*;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import java.io.StringWriter;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: XSMPage.java 832 2011-09-26 21:45:04Z andy $
 * @since 2.0
 */
public abstract class XSMPage extends PlexusWebPage implements IMarkupResourceStreamProvider, IMarkupCacheKeyProvider {
  private HeaderContributor style, layout;

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
      if (site != null) {
        // check for site upgrades
        if (site.needsUpgrade() && !(this instanceof Upgrade)) {
          throw new RestartResponseAtInterceptPageException(getPageClass("upgrade"));
        }
      }

      Secure securePage = (Secure) this;
      if (securePage.getLevel() == AccessControl.ADMIN && (user == null || !user.isXSMAdmin())) {
        throw new AuthorizationException("You must be an admin to access this page"){};
      }
      else if (securePage.getLevel() == AccessControl.MANAGER && (user == null || !user.isSiteAdmin())) {
        throw new AuthorizationException("You must be a site admin to access this page"){};
      }
    }
  }

  protected void onBeforeRender() {
    super.onBeforeRender();

    // provide a default style if no site can be found
    if (getXSMSession().getSite() == null) {
      add(style = HeaderContributor.forCss(XSMApplication.class, "publish/layout/menu-left.css"));
      add(layout = HeaderContributor.forCss(XSMApplication.class, "publish/style/grey.css"));
    } else if (getBehaviors().contains(style)) {
      remove(style);
      remove(layout);
    }
  }
        
  public IResourceStream getMarkupResourceStream(MarkupContainer markupContainer, Class aClass) {
    if (aClass.equals(XSMPage.class)) {
      return new StringResourceStream(evaluateVelocityTemplate());
    } else {
      return new DefaultMarkupResourceStreamProvider().getMarkupResourceStream(markupContainer, aClass);
    }
  }

  // TODO improve this "never cache" code with something that stores it until change needed...
  public String getCacheKey(MarkupContainer markupContainer, Class aClass) {
    return null;
  }

  /**
   * Evaluates the template and returns the result.
   * @return the result of evaluating the velocity template
   */
  private String evaluateVelocityTemplate() {
    XSMDocument doc = null;
    com.rectang.xsm.site.Page page = null;
    if (this instanceof com.rectang.xsm.pages.cms.Page) {
      page = ((com.rectang.xsm.pages.cms.Page) this).getXSMPage();
      if (page instanceof DocumentPage ) {
        doc = XSMDocument.getXSMDoc(((XSMSession) getSession()).getSite(), (DocumentPage) page);
      }
    }
    final Map<String, Object> map = Engine.getContext(doc, page, null, null, getXSMSession().getSite(),
        "<wicket:child></wicket:child>", getXSMSession().getUser());
    map.put("editing", "true");
    map.put("theme", Theme.getTheme(getXSMSession().getUser()));

    // create a Velocity context object using the model if set
    final VelocityContext ctx = new VelocityContext(map);

    // create a writer for capturing the Velocity output
    StringWriter writer = new StringWriter();

    try {
      // execute the velocity script and capture the output in writer
      Engine.process( getXSMSession().getSite(), ctx, writer );

      // replace the tag's body the Velocity output
      return writer.toString();
    } catch (Exception e){
      e.printStackTrace();
      // TODO handle displaying this error for debug
      return null;
    }
  }

  public void layout() {
    UserData user = getXSMSession().getUser();

    if (getPageParameters().getString("sitename") != null) {
      ((XSMSession)getSession()).setRequestedSite(getPageParameters().getString("sitename"));
    }

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
      add(new VelocityPanel("xsm-tree", "#publishedNav()"));
    } else {
      String page = getPageParameters().getString("page");

      add(new ContentPanel("xsm-tree", getXSMSession(), page));
    }

    add(new XSMFeedbackPanel("feedback"));
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
