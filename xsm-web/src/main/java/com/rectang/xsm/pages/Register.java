package com.rectang.xsm.pages;

import com.rectang.xsm.AccessControl;
import com.rectang.xsm.pages.admin.AddUser;
import com.rectang.xsm.site.Site;

import org.apache.wicket.authorization.AuthorizationException;

/**
 * TODO document me
 *
 * @author Andrew Williams
 * @version $Id: Register.java 806 2010-05-26 21:55:04Z andy $
 * @since 1.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="register"
 */
public class Register extends AddUser {
  public int getLevel() {
    return AccessControl.ANON;
  }

  public Site getSite() {
    return new Site(getPageParameters().getString("sitename"));
  }

  public void layout() {
    if ( !getSite().canRegister() ) {
      throw new AuthorizationException("Registration is not enabled for this site"){};
    }

    super.layout();
  }
}
