package com.rectang.xsm.types;

import org.jdom.Element;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.rectang.xsm.XSM;
import com.rectang.xsm.doc.*;
import com.rectang.xsm.widget.TextArea;

public class Login extends DocList {

  public Login(java.lang.String name) {
    super(name, new DocElement[]{new TextArea("header"),
        new TextArea("footer")});
    ((DocWidget) elements[0]).setProperty("rows", "6");
    ((DocWidget) elements[1]).setProperty("rows", "6");
  }

  public void view(Element root, StringBuffer s) {
    elements[0].view(root.getChild("header"), s);

    s.append("<h4>Login form will be published here.</h4>\n");
    elements[1].view(root.getChild("footer"), s);
  }

  public WebMarkupContainer edit(String wicketId, Element node, String path) {
    return super.edit(wicketId, node, path, "The header text appears before the login form and the footer text after");
  }
  
  public void publish(Element root, StringBuffer s) {
    elements[0].publish(root.getChild("header"), s);

    // long wicket-generated action param
    s.append("<form action=\"");
    s.append(XSM.getConfig().getUrl());
    s.append("login?wicket:interface=%3A59%3Axsm-tree%3Alogin%3A%3AIFormSubmitListener%3A%3A\"");
    s.append(" method=\"post\">\n");

    s.append("  <p>\n  <label for=\"xsm_username\">Username</label><br />\n");
    s.append("  <input type=\"text\" name=\"username\" id=\"xsm_username\" value=\"\" /><br />\n");
    s.append("  <label for=\"xsm_password\">Password</label><br />\n");
    s.append("  <input type=\"password\" name=\"password\" id=\"xsm_password\" /><br />\n");
    s.append("  <input type=\"hidden\" name=\"sitename\" id=\"xsm_sitename\" value=\"");
    s.append(getSite().getUniqueID());
    s.append("\" /><br />\n");
    s.append("  <input type=\"hidden\" name=\"action\" value=\"login\" />\n");
    s.append("  <input type=\"submit\" value=\"Log In\" />\n  </p>\n");
    s.append("</form>\n");

    if (getSite().canRegister()) {
      s.append("<p>Don't have an account? <a href=\"");
      s.append(XSM.getConfig().getUrl());
      s.append("register?sitename=");
      s.append(getSite().getUniqueID());
      s.append("\">Register</a> for one.</p>");
    }

    elements[1].publish(root.getChild("footer"), s);
  }
}
