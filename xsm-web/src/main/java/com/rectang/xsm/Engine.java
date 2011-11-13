package com.rectang.xsm;

import com.rectang.xsm.doc.DocElement;
import com.rectang.xsm.io.PublishedFile;
import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.site.Page;
import com.rectang.xsm.site.Site;
import com.rectang.xsm.types.Html;
import com.rectang.xsm.types.News;
import com.rectang.xsm.types.PHP;
import com.rectang.xsm.types.PreviewedFile;
import com.rectang.xsm.util.*;
import com.rectang.xsm.velocity.DateFormatter;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;

import java.io.File;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Helpers for the main Velocity engine configuration
 * <p/>
 * Created: 10/11/2011
 *
 * @author Andrew Williams
 * @since 2.0
 */
public class Engine {
  private static VelocityEngine velocity;
  private static Site fakeSite;

  static {
    initTemplates();
        
    fakeSite = new Site("") {
      @Override
      public String getTitle() {
        return "XSM - eXtensible Site Manager";
      }

      @Override
      public String getPrefixUrl() {
        return XSM.getConfig().getUrl().substring(0, XSM.getConfig().getUrl().length() - 1);
      }

      @Override
      public String getId() {
        return "xsm";
      }
    };
  }

  public static void initTemplates() {
    /* Initialise an engine that we will use for all document publishing */
    velocity = new VelocityEngine();
    /*  first, get and initialize an engine  */

    Map<String, String> properties = Engine.getProperties();
    for (String key : properties.keySet()) {
      velocity.setProperty(key, properties.get(key));
    }

    try {
      velocity.init();
    } catch (Exception e) {
      // TODO handle this error in a more visible way
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  public static VelocityEngine getVelocityEngine() {
    return velocity;
  }

  public static Map<String, String> getProperties() {
    HashMap<String, String> ret = new HashMap<String, String>();

    ret.put("resource.loader", "class,file");
    ret.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    ret.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
    ret.put("file.resource.loader.path", XSM.getConfig().getDataDir());
    ret.put("file.resource.loader.cache", "false");
    ret.put("velocimacro.library", "com/rectang/xsm/publish/macros.vm");
    ret.put("velocimacro.library.autoreload", "true");
    ret.put("velocimacro.permissions.allow.inline.to.replace.global", "true");

    ret.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
    ret.put("runtime.log.logsystem.log4j.category", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");

    return ret;
  }

  public static Map<String, Object> getContext(XSMDocument doc, Page page, DocElement rootType, PublishedFile pubFile,
                                               Site site, String content, UserData user) {
    MetaData metadata = null;
    if (doc != null) {
      metadata = doc.getMetadata();
    }
    if (site == null) {
      site = fakeSite;
    }

    HashMap<String, Object> ret = new HashMap<String, Object>();
    ret.put("page", page);
    ret.put("metadata", metadata);
    ret.put("user", user);
    ret.put("site", site);
    ret.put("config", XSM.getConfig());
    ret.put("type", rootType);
    ret.put("content", content);
  
    ret.put("pubFile", pubFile);
    ret.put("doc", doc);
  
    ret.put("dateFormatter", new DateFormatter());
    ret.put("htmlUtils", new HTMLUtils());
    ret.put("stringUtils", new StringUtils());
    ret.put("fileUtils", new FileUtils());
    ret.put("numberUtils", new NumberUtils());
    ret.put("renderUtils", new RenderUtils());
  
    boolean isWelcome = rootType instanceof Html && Html.WELCOME_PAGE.getBoolean(doc) ||
            rootType instanceof PHP && PHP.WELCOME_PAGE.getBoolean(doc);
    ret.put("isWelcome", isWelcome);
    boolean hasRss = rootType instanceof News ||
     (rootType instanceof PreviewedFile && PreviewedFile.PUBLISH_RSS.getBoolean(doc));
    ret.put("hasrss", hasRss);

    return ret;
  }

  public static org.apache.velocity.Template getTemplate(Site site) throws Exception {
    File customTemplate = new File(XSM.getConfig().getSiteTemplateDir(site), "publish.vm");

    org.apache.velocity.Template t;
    try {
      if (customTemplate.exists()) {
        t = Engine.getVelocityEngine().getTemplate(site.getId() + "/template/publish.vm");
      } else {
        t = Engine.getVelocityEngine().getTemplate("com/rectang/xsm/publish/publish.vm");
      }
    } catch ( NullPointerException e ) {
      // reset the template engine
      Engine.initTemplates();

      // try again
      if (customTemplate.exists()) {
        t = Engine.getVelocityEngine().getTemplate(site.getId() + "/template/publish.vm");
      } else {
        t = Engine.getVelocityEngine().getTemplate("com/rectang/xsm/publish/publish.vm");
      }
    }

    return t;
  }

  public static void process(Site site, Context context, Writer writer) throws Exception {
    org.apache.velocity.Template t = getTemplate(site);

    t.merge(context, writer);
  }
}
