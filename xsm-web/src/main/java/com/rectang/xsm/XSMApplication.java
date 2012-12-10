package com.rectang.xsm;

import com.rectang.xsm.pages.*;
import com.rectang.xsm.pages.Error;
import com.rectang.xsm.pages.admin.*;
import com.rectang.xsm.pages.admin.Publish;
import com.rectang.xsm.pages.admin.System;
import com.rectang.xsm.pages.admin.xsm.Admin;
import com.rectang.xsm.pages.admin.xsm.Setup;
import com.rectang.xsm.pages.cms.*;
import com.rectang.xsm.pages.nav.*;
import com.rectang.xsm.wicket.SiteThemeResource;
import org.apache.wicket.*;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadWebRequest;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: XSMApplication.java 822 2010-08-06 16:29:52Z andy $
 * @since 1.0
 */
public class XSMApplication extends WebApplication {

  protected void init() {
    this.getMarkupSettings().setStripWicketTags(true);

    // set error pages
    this.getApplicationSettings().setInternalErrorPage(com.rectang.xsm.pages.Error.class);
    this.getApplicationSettings().setAccessDeniedPage(com.rectang.xsm.pages.AccessDenied.class);
    this.getApplicationSettings().setPageExpiredErrorPage(com.rectang.xsm.pages.ErrorExpired.class);

    // Don't set this until we want to release...
    //this.getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE );

    if (XSM.getConfig() == null) {
      XSM.setConfig(Config.getInstance(this.getWicketFilter().getFilterConfig().getServletContext().getRealPath("/")));
    }

    mountFramework();
    mountPages();
    mountAdmin();
    mountXSMAdmin();
    mountCMS();
    mountResources();
  }

  private void mountFramework() {
    mountBookmarkablePage("denied", AccessDenied.class);
    mountBookmarkablePage("error", Error.class);
    mountBookmarkablePage("error404", Error404.class);
    mountBookmarkablePage("expired", ErrorExpired.class);
    mountBookmarkablePage("login", Login.class);
    mountBookmarkablePage("logout", Logout.class);
    mountBookmarkablePage("register", Register.class);
  }

  private void mountAdmin() {
    mountBookmarkablePage("add-user", AddUser.class);
    mountBookmarkablePage("add-visitor", AddVisitor.class);
    mountBookmarkablePage("backup", Backup.class);
    mountBookmarkablePage("edit-template", EditTemplate.class);
    mountBookmarkablePage("site-publish", Publish.class);
    mountBookmarkablePage("site", Site.class);
    mountBookmarkablePage("system", System.class);
    mountBookmarkablePage("theme", com.rectang.xsm.pages.admin.Theme.class);
    mountBookmarkablePage("upgrade", Upgrade.class);
    mountBookmarkablePage("users", Users.class);
  }

  private void mountXSMAdmin() {
    mountBookmarkablePage("admin", Admin.class);
    mountBookmarkablePage("setup", Setup.class);
  }

  private void mountPages() {
    mountBookmarkablePage("edit-password", EditPassword.class);
    mountBookmarkablePage("edit-preferences", EditPreferences.class);
    mountBookmarkablePage("edit-profile", EditProfile.class);
    mountBookmarkablePage("help", Help.class);
    mountBookmarkablePage("preferences", Preferences.class);
    mountBookmarkablePage("profile", Preferences.class);
  }

  private void mountCMS() {
    mountBookmarkablePage("page-edit", Edit.class);
    mountBookmarkablePage("page-imagelist", ImageList.class);
    mountBookmarkablePage("page-linklist", LinkList.class);
    mountBookmarkablePage("page-new", New.class);
    mountBookmarkablePage("page-options", Options.class);
    mountBookmarkablePage("page-permissions", Permissions.class);
    mountBookmarkablePage("page-security", Security.class);
    mountBookmarkablePage("page-status", Status.class);
    mountBookmarkablePage("page-view", View.class);

    mountBookmarkablePage("page-contents", Contents.class);
    mountBookmarkablePage("page-delete", Delete.class);
    mountBookmarkablePage("link-edit", LinkEdit.class);
    mountBookmarkablePage("link-view", LinkView.class);
    mountBookmarkablePage("link-new", NewLink.class);
    mountBookmarkablePage("page-rename", Rename.class);
  }

  private void mountResources() {
    mountSharedResource("sitetheme/style.css", new ResourceReference("sitestyle") {
      @Override
      protected Resource newResource() {
        return new SiteThemeResource("style");
      }
    }.getSharedResourceKey());
    mountSharedResource("sitetheme/layout.css", new ResourceReference("sitelayout") {
      @Override
      protected Resource newResource() {
        return new SiteThemeResource("layout");
      }
    }.getSharedResourceKey());
  }

  @Override
  public Class<? extends Page> getHomePage() {
    return Dashboard.class;
  }

  public Session newSession(Request request, Response response) {
    return new XSMSession(request);
  }

  public boolean isDeployment() {
    return false;
  }

  protected WebRequest newWebRequest(HttpServletRequest servletRequest) {
    return new UploadWebRequest(servletRequest);
  }

  public void initMount(String url, Class page) {
    mount(new QueryStringUrlCodingStrategy(url, page));
  }
}
