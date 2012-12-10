package com.rectang.xsm.pages.admin;

import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.ResourceLink;

/**
 * Page for managing system backups
 *
 * @author Andrew Williams
 * @version $Id: Backup.java 802 2009-05-16 17:25:24Z andy $
 * @since 2.0
 */
public class Backup extends XSMPage implements Secure {
  public Backup(PageParameters parameters) {
    super(parameters);
  }

  public int getLevel() {
    return AccessControl.MANAGER;
  }

  public void layout() {
    super.layout();

    add(new ResourceLink("zip",
        new BackupResource(getXSMSession().getSite(), "zip")));
    add(new ResourceLink("tar.gz",
        new BackupResource(getXSMSession().getSite(), "tar.gz")));
  }
}
