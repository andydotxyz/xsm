package com.rectang.xsm.pages.admin;

import com.rectang.xsm.pages.XSMPage;
import com.rectang.xsm.pages.Secure;
import com.rectang.xsm.AccessControl;

/**
 * Start of a system page for managing sites, just regeneration for now.
 *
 * @author Andrew Williams
 * @version $Id: System.java 663 2007-10-04 22:50:25Z aje $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="system"
 */
public class System extends XSMPage implements Secure {

  public int getLevel() {
    return AccessControl.MANAGER;
  }

}