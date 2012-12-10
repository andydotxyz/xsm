package com.rectang.xsm.site.upgrades;

import com.rectang.xsm.site.Site;
import com.rectang.xsm.pages.XSMSession;
import com.rectang.xsm.XSM;

import org.apache.wicket.Session;

import org.headsupdev.support.java.FileUtil;

import java.io.File;
import java.io.IOException;

public class Upgrade6 implements UpgradeUnit {

  public int getFromVersion() {
    return 6;
  }

  public int getToVersion() {
    return 7;
  }

  public String getTitle() {
    return "Upgrade built in themes to new template system";
  }

  public boolean upgrade(Site site) {
    if (site.getLayout().equals("custom")) {
      Session.get().warn("This upgrade cannot migrate custom themes, they will need to be updated manually to use Velocity templates");

      if (site.getStylesheet().equals("custom")) {
        return true;
      }
    }

    // old defaults renamed
    if (site.getStylesheet().equals("default"))
      site.setStylesheet("light-blue");
    if (site.getLayout().equals("default"))
      site.setLayout("menu-left");

    // and change _ to - in the names
    if (site.getStylesheet().equals("simple_blue"))
      site.setStylesheet("simple-blue");
    if (site.getLayout().equals("one_column"))
      site.setLayout("one-column");

    // these themes were temporarily called "compliant"
    if (site.getStylesheet().equals("compliant"))
      site.setStylesheet("grey");

    if (site.getLayout().equals("compliant"))
      site.setLayout("menu-left");

    if (!site.getLayout().equals("custom")) {
      // move old custom files to backup and remove the XSM cache
      File oldLayout = new File(XSM.getConfig().getSiteTemplateDir(site), "layout.jsp");
      File backupLayout = new File(XSM.getConfig().getSiteTemplateDir(site), "layout.jsp-old");
      oldLayout.renameTo(backupLayout);

      File oldLayoutCache = new File(XSM.getConfig().getRootDir(), "/custom/" + site.getId());
      try {
        FileUtil.delete(oldLayoutCache, true);
      } catch (IOException e) {
        // no matter, will get cleared out on next restart
      }
    }

    // custom templates are likely to reference the wrong stylesheets now
    if (site.getLayout().equals("custom")) {
      Session.get().warn("You are using a custom template, you may need to update your stylesheet link from /style.css to /_theme/style.css");
    }
    if (site.getStylesheet().equals("custom")) {
      Session.get().warn("You are using a custom stylesheet, you will need to update your definition for the latest template structure");
    }
    site.save();

    // write the new theme files out to the site and update stylesheet links
    site.publish(((XSMSession) Session.get()).getUser());
    return true;
  }

}
