package com.rectang.xsm.pages;

import com.rectang.xsm.UserData;
import com.rectang.xsm.AccessControl;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: Preferences.java 826 2011-09-25 12:17:36Z andy $
 * @since 2.0
 */
public class Preferences extends XSMPage implements Secure {
  public Preferences(PageParameters parameters) {
    super(parameters);
  }

  public int getLevel() {
    return AccessControl.MEMBER;
  }

  public void layout() {
    super.layout();

    UserData user = getXSMSession().getUser();

    add(new BookmarkablePageLink("edit", EditPreferences.class));

    setDefaultModel(new CompoundPropertyModel(user));

    add(new Label("theme"));
    add(new Label("htmlEditor"));

//    if (user.getLocale() == null || user.getLocale().length() == 0)
//      add(new Label("locale", "You have no chosen language."));
//    else
//      add(new Label("locale", "Your chosen language is " + user.getLocale() + "."));
  }
}
