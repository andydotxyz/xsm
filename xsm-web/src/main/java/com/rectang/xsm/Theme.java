package com.rectang.xsm;

import java.util.List;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: Theme.java 745 2008-09-07 20:01:59Z aje $
 * @since 1.0
 */
public class Theme {
  private static List themes = new LinkedList();

  static {
    themes.add("men-at-work");
  }

  public static List listThemes() {
    return themes;
  }

  public static String getTheme(UserData user) {
    if (user != null) {
      String userTheme = user.getTheme();

      if (userTheme != null && themes.contains(userTheme))
        return userTheme;
    }

    String siteTheme = XSM.getConfig().getTheme();
    if (themes.contains(siteTheme))
      return siteTheme;

    return "men-at-work";
  }
}
