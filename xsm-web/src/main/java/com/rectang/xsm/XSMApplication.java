package com.rectang.xsm;

import org.codehaus.plexus.wicket.PlexusWebApplication;

import java.io.File;
import java.io.IOException;

import org.apache.wicket.Session;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadWebRequest;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;
import com.rectang.xsm.pages.XSMSession;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: XSMApplication.java 822 2010-08-06 16:29:52Z andy $
 * @since 1.0
 *
 * @plexus.component role="org.apache.wicket.Application"
 */
public class XSMApplication extends PlexusWebApplication {

  protected void doInit() {
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
