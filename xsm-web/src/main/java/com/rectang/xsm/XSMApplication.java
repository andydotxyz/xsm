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
    initMount("denied", AccessDenied.class);
    initMount("error", Error.class);
    initMount("error404", Error404.class);
    initMount("expired", ErrorExpired.class);
    initMount("login", Login.class);
    initMount("logout", Logout.class);
    initMount("register", Register.class);
  }

  private void mountAdmin() {
    initMount("add-user", AddUser.class);
    initMount("add-visitor", AddVisitor.class);
    initMount("backup", Backup.class);
    initMount("edit-template", EditTemplate.class);
    initMount("site-publish", Publish.class);
    initMount("site", Site.class);
    initMount("system", System.class);
    initMount("theme", com.rectang.xsm.pages.admin.Theme.class);
    initMount("upgrade", Upgrade.class);
    initMount("users", Users.class);
  }

  private void mountXSMAdmin() {
    initMount("admin", Admin.class);
    initMount("setup", Setup.class);
  }

  private void mountPages() {
    initMount("edit-password", EditPassword.class);
    initMount("edit-preferences", EditPreferences.class);
    initMount("edit-profile", EditProfile.class);
    initMount("help", Help.class);
    initMount("preferences", Preferences.class);
    initMount("profile", Preferences.class);
  }

  private void mountCMS() {
    initMount("page-edit", Edit.class);
    initMount("page-imagelist", ImageList.class);
    initMount("page-linklist", LinkList.class);
    initMount("page-new", New.class);
    initMount("page-options", Options.class);
    initMount("page-permissions", Permissions.class);
    initMount("page-security", Security.class);
    initMount("page-status", Status.class);
    initMount("page-view", View.class);

    initMount("page-contents", Contents.class);
    initMount("page-delete", Delete.class);
    initMount("link-edit", LinkEdit.class);
    initMount("link-view", LinkView.class);
    initMount("link-new", NewLink.class);
    initMount("page-rename", Rename.class);
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

  @Override
  public String getConfigurationType() {
    return WebApplication.DEVELOPMENT;
  }

    protected WebRequest newWebRequest(HttpServletRequest servletRequest) {
    return new UploadWebRequest(servletRequest);
  }

  public void initMount(String url, Class page) {
    mount(new QueryStringUrlCodingStrategy(url, page));
  }
}
