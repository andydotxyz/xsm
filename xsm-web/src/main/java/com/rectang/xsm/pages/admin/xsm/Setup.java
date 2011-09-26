package com.rectang.xsm.pages.admin.xsm;

import com.rectang.xsm.AccessControl;
import com.rectang.xsm.XSM;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.pages.XSMPage;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import java.util.*;

/**
 * A setup wizard that checks all system configurations
 *
 * @author Andrew Williams
 * @version $Id: Setup.java 833 2011-09-26 22:00:00Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="setup"
 */
public class Setup extends XSMPage implements Secure {

  static List setupTasks = new ArrayList();
  static {
    setupTasks.add(new SetupTask("Check /etc/xsm exists") {
      public Boolean run() {
        return new Boolean(new java.io.File("/etc/xsm").exists());
      }
    });
    setupTasks.add(new SetupTask("Check /etc/xsm is writable") {
      public Boolean run() {
        return new Boolean(new java.io.File("/etc/xsm").canWrite());
      }
    });

  }

  static abstract class SetupTask {
    private String description;

    protected SetupTask(String description) {
      this.description = description;
    }

    public abstract Boolean run();

    public String getDescription() {
      return description;
    }
  }

  public int getLevel() {
    return AccessControl.ADMIN;
  }

  public void layout() {
    super.layout();

    final List results = runSetup();

    add(new ListView("status-output", results) {
      protected void populateItem(ListItem listItem) {
        Boolean ok = (Boolean) listItem.getModelObject();
        SetupTask task = (SetupTask) setupTasks.get(listItem.getIndex());

        if (ok.equals(Boolean.TRUE)) {
          listItem.add(new Image("icon", new ResourceReference(XSM.class, "icons/emblem-default.png")));
        } else {
          listItem.add(new Image("icon", new ResourceReference(XSM.class, "icons/emblem-important.png")));
        }

        listItem.add(new Label("description", task.getDescription()));
      }

    });

    Boolean passed = (Boolean) results.get(results.size() - 1);
    if (passed.equals(Boolean.TRUE)) {
      add(new Image("icon", new ResourceReference(XSM.class, "icons/emblem-default.png")));
      add(new Label("summary", "OK - everything is fine!"));
    } else {
      add(new Image("icon", new ResourceReference(XSM.class, "icons/emblem-important.png")));
      add(new Label("summary", "Oh no, there seems to be a problem - please ensure you have extracted the supplied " +
              "xsm-demo-config.zip file to /etc/xsm and that it is writable by this server process"));
    }
  }

  protected List runSetup() {
    List ret = new ArrayList();

    for (int i = 0; i < setupTasks.size(); i++) {
      Boolean result = ((SetupTask) setupTasks.get(i)).run();
      ret.add(result);

      if (result.equals(Boolean.FALSE)) {
        break;
      }
    }


    return ret;
  }
}