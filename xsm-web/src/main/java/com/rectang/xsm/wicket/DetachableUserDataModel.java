package com.rectang.xsm.wicket;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.Component;
import com.rectang.xsm.UserData;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: DetachableUserDataModel.java 663 2007-10-04 22:50:25Z aje $
 * @since 1.0
 */
public class DetachableUserDataModel extends LoadableDetachableModel {
  private transient UserData user;
  private String username;
  private com.rectang.xsm.site.Site site;

  public DetachableUserDataModel(String username, com.rectang.xsm.site.Site site) {
    this.username = username;
    this.site = site;
  }

  public DetachableUserDataModel(UserData user) {
    this(user.getUsername(), user.getSite());
    this.user = user;
  }

  protected Object load() {
    return new UserData(username, site, false);
  }

  public Object getObject() {
    return user;
  }


  public int hashCode() {
    return username.hashCode();
  }

  public boolean equals(Object object) {
    if (object == this)
      return true;

    if (object == null)
      return false;

    return (object instanceof DetachableUserDataModel &&
        ((DetachableUserDataModel) object).username.equals(username));
  }
}
