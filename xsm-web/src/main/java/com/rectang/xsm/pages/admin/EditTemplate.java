package com.rectang.xsm.pages.admin;

import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.XSM;
import com.rectang.xsm.UserData;
import com.rectang.xsm.wicket.StringFileModel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.codehaus.plexus.util.IOUtil;

import java.io.*;

/**
 * Page for editing the site template files
 *
 * @author Andrew Williams
 * @version $Id: EditTemplate.java 818 2010-05-30 14:04:21Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="edit-template"
 */
public class EditTemplate extends XSMPage implements Secure {

  public int getLevel() {
    return AccessControl.MANAGER;
  }

  public void layout() {
    super.layout();

    // we should not need a default, but for folk hacking URLs...
    String section = getPageParameters().getString("section", "layout");

    add(new Label("title", new StringResourceModel("title", this, null,
        new Object[] {new StringResourceModel(section, this, null)})));

    boolean exists = getCustomFile(section).exists();
    add(new TemplateForm("template", section, exists, getXSMSession().getUser()));
  }

  class TemplateForm extends Form {
    private UserData user;
    private String section;

    public TemplateForm(String id, final String section, boolean custom, UserData u) {
      super(id);
      this.user = u;
      this.section = section;
      final com.rectang.xsm.site.Site site = user.getSite();

      Button create = new Button("create") {
        public void onSubmit() {
          InputStream in = null;
          OutputStream out = null;
          try {
            in = getDefault(section, user.getSite());
            out = new FileOutputStream(getCustomFile(section));
            IOUtil.copy(in, out);

            if (section.equals("layout")) {
              site.setLayout("custom");
              site.save();
            } else if (section.equals("style")) {
              site.setStylesheet("custom");
              site.save();
            }

            // Here we need to redirect back to this page to refresh the models - not sure why...
            setResponsePage(getPageClass("edit-template"), getPageParameters());
          } catch (IOException e) {
            error("Unable to create custom copy of template " + section);
          } finally {
            if (in != null)
              IOUtil.close(in);
            if (out != null)
              IOUtil.close(out);
          }
        }
      };
      create.setDefaultFormProcessing(false);
      add(create);

      Button save = new Button("save");
      add(save);

      Button revert = new Button("revert");
      add(revert);

      Button delete = new Button("delete") {
        public void onSubmit() {
          if (getCustomFile(section).delete()) {
            if (section.equals("layout")) {
              site.setLayout("menu-left");
              site.save();
            } else if (section.equals("style")) {
              site.setStylesheet("grey");
              site.getPublishedDoc("style.css").delete();
              site.save();
            }

            if (section.equals("template")) {
              site.publish(user);
            } else {
              site.publishTheme();
            }
            // Here we need to redirect back to this page to refresh the models - not sure why...
            setResponsePage(getPageClass("edit-template"), getPageParameters());
          } else {
            error("Unable to delete custom template " + section);
          }
        }
      };
      delete.setDefaultFormProcessing(false);
      add(delete);

      if (custom) {
        add(new TextArea("customise",
            new StringFileModel(getCustomFile(section))));

        create.setVisible(false);
      } else {
        StringBuffer content = new StringBuffer();
        BufferedReader reader = null;
        try {
          reader = new BufferedReader(new InputStreamReader(getDefault(section, site)));

          String line = reader.readLine();
          while (line != null) {
            content.append(line);
            content.append('\n');

            line = reader.readLine();
          }
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          if (reader != null)
            IOUtil.close(reader);
        }

        TextArea area = new TextArea("customise", new Model(content.toString()));

        area.setEnabled(false);
        add(area);

        save.setVisible(false);
        revert.setVisible(false);
        delete.setVisible(false);
      }

      BookmarkablePageLink back;
      back = new BookmarkablePageLink("back", getPageClass("theme"));
      back.add(new Label("back-label", "Back to Theme page"));
      add(back);
    }


    protected void onSubmit() {
      super.onSubmit();

      if (section.equals("template")) {
        user.getSite().publish(user);
      } else {
        user.getSite().publishTheme();
      }
      // Here we need to redirect back to this page to refresh the models - not sure why...
      setResponsePage(getPageClass("edit-template"), getPageParameters());
    }
  }

  private String getCustomPath(String section) {
    if (section.equals("layout")) {
      return "/layout.css";
    } else if (section.equals("template")) {
      return "/publish.vm";
    }

    return "/style.css";
  }

  private com.rectang.io.File getCustomFile(String section) {
    File dir = new File(XSM.getConfig().getSiteTemplateDir(getXSMSession().getSite()));
    if (!dir.exists()) {
      dir.mkdir();
    }

    return new com.rectang.io.File(dir, getCustomPath(section));
  }

  private InputStream getDefault(String section, com.rectang.xsm.site.Site site) {
    String path = "publish.vm";
    if (section.equals("layout"))
      path = "layout/" + site.getLayout() + ".css";
    if (section.equals("style"))
      path = "style/" + site.getStylesheet() + ".css";

    return getClass().getClassLoader().getResourceAsStream(
            "/com/rectang/xsm/publish/" + path);
  }
}
