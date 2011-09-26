package com.rectang.xsm.pages.admin;

import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.XSM;

import java.util.List;
import java.util.Vector;
import java.io.File;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * Page for editing the output site theme
 *
 * @author Andrew Williams
 * @version $Id: Theme.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="theme"
 */
public class Theme extends XSMPage implements Secure {

  public int getLevel() {
    return AccessControl.MANAGER;
  }

  public void layout() {
    super.layout();

    add(new ThemeForm("theme", getXSMSession().getSite()));
  }

  class ThemeForm extends Form {
    private com.rectang.xsm.site.Site site;
    public ThemeForm(String id, com.rectang.xsm.site.Site site) {
      super(id);
      this.site = site;

      setModel(new CompoundPropertyModel(site));

      add(new DropDownChoice("stylesheet", getStyles()));
      add(new DropDownChoice("layout", getLayouts()));
    }

    public void onSubmit() {
      if (site.save()) {
        site.publishTheme();
      } else {
        error("Unable to save site settings");
      }
    }
  }

  public List getStyles() {
    List ret = new Vector();
    ret.add("dashed");
    ret.add("grey");
    ret.add("light-blue");
    ret.add("simple-blue");
    ret.add("typewriter");

    com.rectang.xsm.site.Site site = getXSMSession().getSite();
    if (new File(XSM.getConfig().getSiteTemplateDir(site), "style.css").exists())
      ret.add("custom");

    return ret;
  }

  public List getLayouts() {
    List ret = new Vector();
    ret.add("menu-left");
    ret.add("menu-right");
    ret.add("news");
    ret.add("one-column");

    com.rectang.xsm.site.Site site = getXSMSession().getSite();
    if (new File(XSM.getConfig().getSiteTemplateDir(site), "layout.css").exists())
      ret.add("custom");

    return ret;
  }
}
