package com.rectang.xsm.pages.admin.xsm;

import com.rectang.xsm.AccessControl;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.pages.XSMPage;

/**
 * Start of a admin page for managing the XSM instance, nothing much here yet.
 *
 * @author Andrew Williams
 * @version $Id: Admin.java 832 2011-09-26 21:45:04Z andy $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="admin"
 */
public class Admin extends XSMPage implements Secure {

  public int getLevel() {
    return AccessControl.ADMIN;
  }

}