package com.rectang.xsm.pages;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: Welcome.java 670 2007-10-06 20:25:48Z aje $
 * @plexus.component role="org.apache.wicket.Page" role-hint="index"
 * @since 2.0
 */
public class Welcome extends XSMPage {
  public void layout() {
    super.layout();

    Label text = new Label("welcome.text");
    text.setModel(new StringResourceModel("welcome.text", text, null));
    text.setEscapeModelStrings(false);
    add(text);
  }
}
