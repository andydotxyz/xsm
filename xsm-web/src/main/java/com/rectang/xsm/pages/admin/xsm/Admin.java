package com.rectang.xsm.pages.admin.xsm;

import com.rectang.xsm.AccessControl;
import com.rectang.xsm.XSM;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.site.Site;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Start of a admin page for managing the XSM instance, nothing much here yet.
 *
 * @author Andrew Williams
 * @version $Id: Admin.java 832 2011-09-26 21:45:04Z andy $
 * @since 2.0
 */
public class Admin extends XSMPage implements Secure {
  public Admin(PageParameters parameters) {
    super(parameters);
  }

  public int getLevel() {
    return AccessControl.ADMIN;
  }

  public void layout() {
    super.layout();

    File[] siteArray = new File(XSM.getConfig().getDataDir()).listFiles();
    List sites = Arrays.asList(siteArray);

    add(new ListView<File>("site", sites) {
      protected void populateItem(ListItem listItem) {
        File siteFile = ((File) listItem.getModelObject());
        Site site = new Site(siteFile.getName());

        if (!site.exists()) {
          listItem.setVisible(false);
          return;
        }

        long used = site.calculateSpaceUsage();
        int perc = 0;
        if (used > 0) {
          perc = (int)(((double) used / site.getQuota()) * 100);
        }

        listItem.add(new Label("id", site.getId()));
        listItem.add(new Label("name", site.getTitle()));
        listItem.add(new Label("used", String.valueOf(used)));
        listItem.add(new Label("quota", String.valueOf(site.getQuota())).setVisible(site.getQuota() > 0));
        listItem.add(new Label("perc", String.valueOf(perc)));
      }
    });
  }
}