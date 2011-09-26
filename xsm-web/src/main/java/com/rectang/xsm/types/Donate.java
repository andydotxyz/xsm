package com.rectang.xsm.types;

import com.rectang.xsm.doc.DocList;
import com.rectang.xsm.doc.DocElement;
import com.rectang.xsm.doc.DocWidget;
import com.rectang.xsm.doc.SupportedOption;
import com.rectang.xsm.widget.TextArea;
import org.jdom.Element;
import org.apache.wicket.markup.html.WebMarkupContainer;

import java.util.Vector;
import java.util.List;

public class Donate extends DocList {

  public static final SupportedOption CURRENCY_CODE = new SupportedOption(
      "CURRENCY_CODE", "The currency code for your paypal account, i.e.GBP, EUR or USD", "GBP");
  public static final SupportedOption COUNTRY_CODE = new SupportedOption(
      "COUNTRY_CODE", "The country code for your paypal account, i.e.GB, FR or US", "GB");

  private Vector options;

  public Donate(String name) {
    super(name, new DocElement[]{new com.rectang.xsm.widget.String("account"),
        new com.rectang.xsm.widget.String("name"),
        new com.rectang.xsm.widget.String("amount"),
        new TextArea("header"), new TextArea("footer")});
    ((DocWidget) elements[3]).setProperty("rows", "6");
    ((DocWidget) elements[4]).setProperty("rows", "6");

    options = new Vector();
    options.add(CURRENCY_CODE);
    options.add(COUNTRY_CODE);
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
    elements[3].view(root.getChild("header"), s);

    s.append("<h4>Donation form \"");
    elements[1].view(root.getChild("name"), s);
    s.append("\" (to ");
    elements[0].view(root.getChild("account"), s);
    s.append(") will be published here.</h4>\n");
    elements[4].view(root.getChild("footer"), s);
  }

  public WebMarkupContainer edit(String wicketId, Element node, String path) {
    return super.edit(wicketId, node, path, "The header text appears before the donation form and the footer text after");
  }

  public void publish(Element root, StringBuffer s) {
    elements[3].publish(root.getChild("header"), s);

    s.append("<form action=\"https://www.paypal.com/cgi-bin/webscr\" method=\"post\">\n");
    s.append("  <input type=\"hidden\" name=\"cmd\" value=\"_xclick\" />\n");

    s.append("  <input type=\"hidden\" name=\"business\" value=\"");
    elements[0].publish(root.getChild("account"), s);
    s.append("\" />\n");

    s.append("  <input type=\"hidden\" name=\"item_name\" value=\"");
    elements[1].publish(root.getChild("name"), s);
    s.append("\" />\n");

    s.append("  <input type=\"hidden\" name=\"buyer_credit_promo_code\" value=\"\" />\n");
    s.append("  <input type=\"hidden\" name=\"buyer_credit_product_category\" value=\"\" />\n");
    s.append("  <input type=\"hidden\" name=\"buyer_credit_shipping_method\" value=\"\" />\n");
    s.append("  <input type=\"hidden\" name=\"buyer_credit_user_address_change\" value=\"\" />\n");
    s.append("  <input type=\"hidden\" name=\"no_shipping\" value=\"1\" />\n");
    s.append("  <textarea name=\"cn\" rows=\"7\" cols=\"50\">Donation description</textarea><br />\n");

    s.append("  <input type=\"hidden\" name=\"currency_code\" value=\"");
    s.append(CURRENCY_CODE.getString(getDoc()));
    s.append("\" />\n");

    s.append("  <input type=\"hidden\" name=\"lc\" value=\"");
    s.append(COUNTRY_CODE.getString(getDoc()));
    s.append("\" />\n");

    s.append("  <input type=\"hidden\" name=\"tax\" value=\"0\" />\n");
    s.append("  <input type=\"hidden\" name=\"bn\" value=\"PP-DonationsBF\" />\n");
    s.append("  <input type=\"text\" name=\"amount\" size=\"15\" value=\"");
    elements[2].publish(root.getChild("amount"), s);
    s.append("\"/>");
    s.append(CURRENCY_CODE.getString(getDoc()));
    s.append("<br />\n");
    s.append("  <input type=\"image\" src=\"https://www.paypal.com/en_US/i/btn/x-click-but21.gif\" border=\"0\" name=\"submit\" alt=\"Make payments with PayPal - it's fast, free and secure!\" />\n");
    s.append("  <img alt=\"\" border=\"0\" src=\"https://www.paypal.com/en_GB/i/scr/pixel.gif\" width=\"1\" height=\"1\" />\n");
    s.append("</form>\n");

    elements[4].publish(root.getChild("footer"), s);
  }
}
