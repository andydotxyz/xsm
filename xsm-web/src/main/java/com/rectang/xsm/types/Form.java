package com.rectang.xsm.types;

import org.jdom.Element;

import com.rectang.xsm.doc.*;
import com.rectang.xsm.widget.HTMLTextArea;
import com.rectang.xsm.widget.ComboBox;
import com.rectang.xsm.widget.TextArea;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;

/**
 * A simple form type used to create "Contact Us" forms and simple feedback pages.
 *
 * @author Andrew Williams
 * @version $Id: Form.java 786 2009-04-02 22:25:55Z andy $
 * @since 1.2
 */
public class Form extends DocList implements PHPFile {
  public static final SupportedOption FROM_EMAIL = new SupportedOption("FROM_EMAIL",
    "who should the email appear to be sent from?", "");
  private Vector options;

  public Form(String name) {
    super(name, new DocElement[]{new com.rectang.xsm.widget.String("to"),
        new HTMLTextArea("intro"), new HTMLTextArea("response"),
        new DocGroup("fields", new FormField("field"))});

    options = new Vector();
    options.add(FROM_EMAIL);
  }

  public List getSupportedOptions() {
    Vector ret = new Vector();
    ret.addAll(options);
    for (int i = 0; i < elements.length; i++) {
      ret.addAll(elements[i].getSupportedOptions());
    }
    return ret;
  }

  public void view(Element root, StringBuffer s) {
    s.append("<p>Form will be submitted to ");
    elements[0].view(root.getChild("to"), s);
    s.append("</p>");

    elements[1].view(root.getChild("intro"), s);

    s.append("<table>");
    elements[3].view(root.getChild("fields"), s);
    s.append("</table>");
  }

  public void publish(Element root, StringBuffer s) {
    elements[1].publish(root.getChild("intro"), s);

    String headers = "";
    String from = FROM_EMAIL.getString(getDoc());
    if ( from != null && from.length() > 0 ) {
        headers = "From: " + from;
    }

    s.append("<?php if ($_POST[\"submit\"]) {\n$headers=\"");
    s.append(headers);
    s.append("\";\n$body=\'Form \"" + getDoc().getPage().getTitle() + "\" submitted as follows:\n\n\';\n");
    Iterator fields = root.getChild("fields").getChildren("field").iterator();
    while (fields.hasNext()) {
        Element field = (Element) fields.next();

        s.append("$body .= \"" );
        elements[0].publish(field.getChild("question"), s);
        s.append("\n\t \".$_POST[\"field" + field.getAttributeValue("index") + "\"].\"\n\";\n");
    }
    s.append("  mail('");
    elements[0].publish(root.getChild("to"), s);
    s.append("', 'Form \"" + getDoc().getPage().getTitle() + "\" submission results', $body, $headers);\n?>");
    elements[2].publish(root.getChild("response"), s);
    s.append("<?php } else { ?>\n");

    s.append("<form action=\"?\" method=\"post\" class=\"xsm_form\">");
    s.append("<table>");
    elements[3].publish(root.getChild("fields"), s);
    s.append("</table>");
    s.append("<input type=\"submit\" name=\"submit\"/></form>");
    s.append("<?php } ?>\n");
  }
}

class FormField extends DocList {
    public FormField(String name) {
      super(name, new DocElement[] {
          new com.rectang.xsm.widget.String("question"),
          new TextArea("description"),
          new ComboBox("type", Arrays.asList(new String[]{"text", "textarea", "checkbox"}))
      });
  }

  public void publish(Element node, StringBuffer s) {
    draw(node, s, true);
  }

  public void view(Element node, StringBuffer s) {
    draw(node, s, false);
  }

  private void draw(Element node, StringBuffer s, boolean enabled) {
    String enabledStr = "";
    if (!enabled) {
      enabledStr = "disabled=\"disabled\" ";
    }
    s.append("<tr><td class=\"xsm_form_question\">");
    elements[0].publish(node.getChild("question"), s);
    s.append("</td><td class=\"xsm_form_answer\">");
    String type = node.getChildText("type");
    if (type.equals("text")) {
      s.append("<input type=\"text\" name=\"field" + node.getAttributeValue("index") + "\" " + enabledStr + " />");
    } else if (type.equals("textarea")) {
      s.append("<textarea name=\"" + node.getAttributeValue("index") + "\"" + enabledStr + "></textarea>");
    } else if (type.equals("checkbox")) {
      s.append("<input type=\"checkbox\" name=\"field" + node.getAttributeValue("index") + "\" " + enabledStr + " />");
    }
    s.append("</td></tr>");

    String description = node.getChildText("description");
    if (description != null && description.length() > 0) {
      s.append("<tr><td colspan=\"2\" class=\"xsm_form_description\"><span>&nbsp;&nbsp;&nbsp;&nbsp;");
      elements[1].publish(node.getChild("description"), s);
      s.append("</span></td></tr>");
    }
  }
}
