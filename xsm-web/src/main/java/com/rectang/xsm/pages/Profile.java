package com.rectang.xsm.pages;

import com.rectang.xsm.UserData;
import com.rectang.xsm.AccessControl;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.MarkupContainer;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: Profile.java 663 2007-10-04 22:50:25Z aje $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="profile"
 */
public class Profile extends XSMPage implements Secure {
  public int getLevel() {
    return AccessControl.MEMBER;
  }

  public void layout() {
    super.layout();

    UserData user = getXSMSession().getUser();

    add(new BookmarkablePageLink("edit-profile", getPageClass("edit-profile")));
    add(new BookmarkablePageLink("edit-password", getPageClass("edit-password")));

    setDefaultModel(new CompoundPropertyModel(user));
    add(new Label("name"));
    MarkupContainer link = new ExternalLink("email", user.getEmail());
    link.add(new Label("label", user.getEmail()));
    add(link);
    link = new ExternalLink("homepage", user.getHomepage());
    link.add(new Label("label", user.getHomepage()));
    add(link);

    add(new Label("avatar"));
    add(new Label("note"));
  }
}