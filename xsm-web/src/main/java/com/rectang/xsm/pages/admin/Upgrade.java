package com.rectang.xsm.pages.admin;

import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.site.upgrades.UpgradeUnit;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import java.util.Iterator;

/**
 * Page to handle the upgrading of old sites.
 *
 * @author Andrew Williams
 * @version $Id: Upgrade.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="upgrade"
 */
public class Upgrade extends XSMPage implements Secure {

  public int getLevel() {
    return AccessControl.MEMBER;
  }

  public void layout() {
    super.layout();
    com.rectang.xsm.site.Site site = getXSMSession().getSite();
    boolean runUpgrade = getPageParameters().getString("action", "").equals("run");

    if (runUpgrade) {
      StringBuffer status = new StringBuffer();
      boolean success = doUpgrade(site, status);

      Label intro = new Label("intro", status.toString());
      intro.setEscapeModelStrings(false);
      add(intro);
      Label statusLabel = new Label("status");
      if (success) {
        statusLabel.setModel(new StringResourceModel("intro.complete", statusLabel, null));
      } else {
        statusLabel.setModel(new StringResourceModel("intro.failed", statusLabel, null));
      }
      add(statusLabel);

      WebMarkupContainer upgrades = new WebMarkupContainer("run-upgrade");
      upgrades.setVisible(false);
      add(upgrades);
      return;
    }

    boolean needsUpgrade = site.needsUpgrade();
    boolean canUpgrade = getXSMSession().getUser().isSiteAdmin();

    Label intro = new Label("intro");
    if (!needsUpgrade) {
      intro.setModel(new StringResourceModel("intro.noupgrades", intro, null));
    } else {
      intro.setModel(new StringResourceModel("intro", intro, new Model(site)));
    }
    add(intro);

    Label status = new Label("status");
    if (needsUpgrade) {
      if (canUpgrade) {
        status.setModel(new StringResourceModel("intro.status.list", status, null));
      } else {
        status.setModel(new StringResourceModel("intro.status.noaccess", status, null));
      }
    } else {
      status = new Label("status", "");
      status.setVisible(false);
    }
    add(status);

    WebMarkupContainer upgrades = new WebMarkupContainer("run-upgrade");
    if (needsUpgrade && canUpgrade) {

      upgrades.add(new ListView("upgrade-items", com.rectang.xsm.site.Upgrade.listRequiredUpgrades(site)) {
        public void populateItem(final ListItem item) {
          UpgradeUnit unit = (UpgradeUnit) item.getModelObject();

          item.add(new Label("title", unit.getTitle()));
        }
      });


    } else {
      upgrades.setVisible(false);
    }
    add(upgrades);
  }

  private boolean doUpgrade(com.rectang.xsm.site.Site site, StringBuffer buffer) {
    buffer.append("<p>Upgrade started...<br />\n");
    Iterator upgrades = com.rectang.xsm.site.Upgrade.listRequiredUpgrades(site).iterator();

    while (upgrades.hasNext()) {
      UpgradeUnit upgrade = (UpgradeUnit) upgrades.next();

      buffer.append(upgrade.getTitle());
      buffer.append("... ");
      if (upgrade.upgrade(site)) {
        site.setVersion(upgrade.getToVersion());
        buffer.append("SUCCESS<br />\n");
      } else {
        buffer.append("FAILED!!!<br /><br />\n");
        buffer.append("Upgrade failed, please contact your XSM administrator");
        buffer.append(" version stuck at <font color=\"red\">");
        buffer.append(site.getVersion());
        buffer.append("</font><br />\n");
        break;
      }
    }

    return site.save();
  }
}
