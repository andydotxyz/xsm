package com.rectang.xsm.pages;

import com.rectang.xsm.UserData;
import com.rectang.xsm.Theme;
import com.rectang.xsm.AccessControl;
import com.rectang.xsm.wicket.LangDropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: EditPreferences.java 826 2011-09-25 12:17:36Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="edit-preferences"
 */
public class EditPreferences
    extends XSMPage implements Secure {
  public int getLevel() {
    return AccessControl.MEMBER;
  }

  public void layout() {
    super.layout();

    UserData user = getXSMSession().getUser();

    add(new EditPreferences.PreferencesForm("preferences", user));
  }

  class PreferencesForm extends Form
  {
    UserData user;
    public PreferencesForm(String id, UserData user) {
      super(id);
      this.user = user;

      setModel(new CompoundPropertyModel(user));

      add(new ThemeDropDownChoice("theme"));
      add(new EditorDropDownChoice("htmlEditor"));
//      add(new LangDropDownChoice("locale", getXSMSession()));
    }

    public void onSubmit() {
      if (user.getLocale() != null) {
        this.getSession().setLocale(new Locale(user.getLocale()));
      }

      if (user.save())
        this.setResponsePage(getPageClass("preferences"));
    }
  }
}

class ThemeDropDownChoice extends DropDownChoice {
  public ThemeDropDownChoice(String id) {
    super(id, Theme.listThemes());
  }
}

class EditorDropDownChoice extends DropDownChoice {
  static Map editors;

  static {
    editors = new LinkedHashMap();
    editors.put("tinymce", "TinyMCE - (default) An advanced WYSIWYG HTML editor");
    editors.put("textarea", "Textarea (advanced) - for editing the HTML source code manually");
  }

  public EditorDropDownChoice(String id) {
    super(id, new LinkedList(editors.keySet()));

    this.setChoiceRenderer(new EditorRenderer());
  }

  class EditorRenderer extends ChoiceRenderer {
    public Object getDisplayValue(Object object) {
      return editors.get(object);
    }
  }
}