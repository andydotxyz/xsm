package com.rectang.xsm.pages.cms;

import com.rectang.xsm.Locale;
import com.rectang.xsm.UserData;
import com.rectang.xsm.XSM;
import com.rectang.xsm.site.Site;
import com.rectang.xsm.util.EmailUtils;
import com.rectang.xsm.io.XSMDocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.ajax.markup.html.form.upload.UploadProgressBar;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.model.CompoundPropertyModel;

import java.net.URLEncoder;
import java.util.Iterator;

/**
 * The main CMS edit tab
 *
 * @author Andrew Williams
 * @version $Id: Edit.java 818 2010-05-30 14:04:21Z andy $
 * @since 2.0
 */
public class Edit extends DocumentPage {
  public Edit(PageParameters parameters) {
    super(parameters);
  }

  public void layout() {
    super.layout();
    if (hasError()) return;

    if (!getDoc().lock(getXSMSession().getUser())) {
      getSession().warn("Could not lock file :(\n Perhaps you do not have suffcient permissions");

      setResponsePage(View.class, getPageParameters());
    }

    add(new Label("activateEditor", activateEditor()).setEscapeModelStrings(false)
        .setVisible(!getXSMSession().getUser().getHtmlEditor().equals("textarea")));

    add(new EditForm("editform", getDoc()));
  }

  private String activateEditor() {
    UserData user = getXSMSession().getUser();

    return "  tinyMCE.init({\n" +
    "    mode : \"textareas\",\n" +
    "    editor_selector : \"mceEditor\", \n" +
    "    theme : \"advanced\",\n" +
    "    theme_advanced_toolbar_location : \"top\",\n" +
    "    theme_advanced_statusbar_location : \"bottom\",\n" +
    "    skin : \"o2k7\",\n" +
    "    skin_variant : \"black\",\n" +
    "    content_css : \"" + XSM.getConfig().getUrl() + "sitetheme/style.css\",\n" +
    "    language : \"" + Locale.getString(user, "tinymce.lang") + "\",\n" +
    "    document_base_url : \"" + getDocumentPage().getLink() + "\",\n" +
    "    convert_urls : true,\n" +
    "    relative_urls : false,\n" +
    "    remove_linebreaks : \"false\",\n" +
    "    plugins : \"table,advimage,advlink,contextmenu,autosave,fullscreen,inlinepopups,style,insertdatetime,safari,visualchars,xhtmlxtras,searchreplace\", \n" +
    "    theme_advanced_buttons1_add : \"ins,del,abbr,acronym\", \n" +
    "    theme_advanced_buttons2_add : \"|,forecolor,backcolor,|,styleprops,|,insertdate,inserttime\", \n" +
    "    theme_advanced_buttons3_add_before : \"tablecontrols,|\", \n" +
    "    theme_advanced_buttons3_add : \"|,search,replace,|,visualchars,fullscreen\", \n" +
    "    theme_advanced_resizing : true, \n" +
    "    plugin_insertdate_dateFormat : \"%Y-%m-%d\",\n" +
    "    plugin_insertdate_timeFormat : \"%H:%M:%S\",\n" +
    "    external_image_list_url : \"" + XSM.getConfig().getUrl() + "page-imagelist?page="
        + URLEncoder.encode(getDocumentPage().getPath()) + "\",\n" +
    "    external_link_list_url : \"" + XSM.getConfig().getUrl() + "page-linklist/?page="
        + URLEncoder.encode(getDocumentPage().getPath()) + "\",\n" +
    "    valid_elements : \"*[*]\",\n" +
    "    extended_valid_elements : \"table[border=0|cellspacing|cellpadding|width|height|class],tr[class|rowspan|width|height],td[class|colspan|rowspan|width|height]\",\n" +
    "  });\n";
  }

  private class EditForm extends Form {
    private XSMDocument doc;
    private boolean publish = false;

    public EditForm(String id, XSMDocument doc) {
      super(id);
      this.setMultiPart(true);
      this.setMaxSize(Bytes.megabytes(50));
      
      this.doc = doc;

      setModel(new CompoundPropertyModel(doc));
      add(new UploadProgressBar("progress", this));

      add(new PublishButton("publish1"));
      add(new PublishButton("publish2"));
      add(getDoc().edit("content", "", getXSMSession().getUser()));
    }

    protected void onSubmit() {
      if (this.hasError()) {
        return;
      }

      Site site = getXSMSession().getSite();
      String pagePath = getDocumentPage().getPath();

      if (doc.save(getXSMSession().getUser())) {
        /* inform watching users of the update */
        Iterator watchers = doc.getWatchers().iterator();
        while (watchers.hasNext()) {
          UserData user = new UserData((String) watchers.next(), site, false);

          String email = user.getEmail();
          if (email != null && !email.equals("")) {
            EmailUtils.emailTo("XSM Page " + pagePath + " updated",
                "Page " + pagePath + " on site " + site.getId() +
                " has been updated.\n\n" +
                "We hope to contain the differences in future versions of XSM!",
                email);
          }
        }

        if (publish) {
          if (pagePath.equals(site.getNewsSource())) {
            // TODO perhaps report status on failure?
            site.publish(getXSMSession().getUser());
          } else {
            // TODO perhaps report status on failure?
            getDocumentPage().publish(getXSMSession().getUser());
          }

          setResponsePage(View.class, getPageNameParams());
        }
      } else {
        error("Failed to save page " + getDocumentPage().getTitle());
      }
    }

    class PublishButton extends Button {
      public PublishButton(String id) {
        super(id);
      }

      public void onSubmit() {
        publish = true;
      }
    }
  }
}
