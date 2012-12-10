package com.rectang.xsm.wicket;

import com.rectang.xsm.XSM;
import com.rectang.xsm.pages.XSMSession;
import com.rectang.xsm.site.Site;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.util.time.Time;

import org.headsupdev.support.java.IOUtil;

import java.io.*;

/**
 * Page that returns the requested style from the current site
 * <p/>
 * Created: 08/12/2012
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class SiteThemeResource extends DynamicWebResource {
  private final String style;

  public SiteThemeResource(String style) {
    this.style = style;
  }

  @Override
  protected ResourceState getResourceState() {
    return new ResourceState() {
      @Override
      public byte[] getData() {
        InputStream file = getFile();
        if (file == null) return new byte[0];

        return IOUtil.toString(file).getBytes();
      }

      @Override
      public String getContentType() {
        return "text/css";
      }

      @Override
      public Time lastModifiedTime() {
        // don't cache (for now...)
        // TODO get correct timing
        return Time.now();
      }

      private InputStream getFile() {
        Site site = ((XSMSession)Session.get()).getSite();
        if (site == null) return null;

        if (style.equals("style")) {
          return getStyleFile(site.getStylesheet());
        } else {
          return getStyleFile(site.getLayout());
        }
      }

      private InputStream getStyleFile(String id) {
        Site site = ((XSMSession)Session.get()).getSite();

        if (!id.equals("custom")) {
          String lookup = "/com/rectang/xsm/publish/" + style + "/" + id + ".css";
          return getClass().getClassLoader().getResourceAsStream(lookup);
        }

        // using a custom file so read it from the template directory
        File file = new File(XSM.getConfig().getSiteTemplateDir(site), "/" + style + ".css");

        if (file.exists()) {
          try {
            return new FileInputStream(file);
          } catch (FileNotFoundException e) {
            return null;
          }
        } else {
          return null;
        }
      }
    };
  }
}
