package com.rectang.xsm.pages.admin;

import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.pages.XSMSession;
import org.apache.wicket.markup.html.basic.Label;

/**
 * Start of a system page for managing sites, just regeneration for now.
 *
 * @author Andrew Williams
 * @version $Id: System.java 663 2007-10-04 22:50:25Z aje $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="system"
 */
public class System extends XSMPage implements Secure {

  public int getLevel() {
    return AccessControl.MANAGER;
  }

  public void layout() {
    super.layout();

    com.rectang.xsm.site.Site site = ((XSMSession) getSession()).getUser().getSite();
    long used = site.calculateSpaceUsage();
    int perc = 0;
    if (used > 0) {
      perc = (int)(((double) used / site.getQuota()) * 100);
    }

    add(new Label("used", String.valueOf(used)));
    add(new Label("quota", String.valueOf(site.getQuota())).setVisible(site.getQuota() > 0));
    add(new Label("perc", String.valueOf(perc)));
  }
}