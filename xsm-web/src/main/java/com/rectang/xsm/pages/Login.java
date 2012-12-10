package com.rectang.xsm.pages;

import com.rectang.xsm.panels.LoginPanel;
import org.apache.wicket.PageParameters;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: Login.java 663 2007-10-04 22:50:25Z aje $
 * @since 2.0
 */
public class Login extends XSMPage {
  public Login(PageParameters parameters) {
    super(parameters);
  }

  @Override
  public void layout() {
    super.layout();
        
    String sitename = getPageParameters().getString("sitename");
    add( new LoginPanel( "login", sitename, getXSMSession() ) );
  }
}
