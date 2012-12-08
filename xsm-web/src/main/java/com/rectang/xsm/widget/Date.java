package com.rectang.xsm.widget;

import com.rectang.xsm.doc.DocWidget;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.Serializable;
import java.lang.*;
import java.lang.String;

import org.jdom.Element;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.datetime.DateConverter;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;

public class Date extends DocWidget implements Serializable {

  private SimpleDateFormat store = new SimpleDateFormat("yyyy-MM-dd");
  private SimpleDateFormat format = new SimpleDateFormat("EEE, d MMMM yyyy");

  public Date(java.lang.String name) {
    super(name);
  }

  private synchronized java.util.Date parseDate(Element node) {
    if (node == null || node.getValue() == null || node.getValue().equals(""))
      return new java.util.Date();
    try {
      return store.parse(node.getValue());
    } catch (Exception e) {
      return new java.util.Date();
    }
  }

  private synchronized java.lang.String formatDate(java.util.Date date) {
    return store.format(date);
  }

  private synchronized java.lang.String renderDate(java.util.Date date) {
    return format.format(date);
  }
 
  public void view(Element node, StringBuffer s) {
    s.append(renderDate(parseDate(node)));
  }
  
  public void publish(Element node, StringBuffer s) {
    s.append(renderDate(parseDate(node)));
  }

  public WebMarkupContainer edit(java.lang.String wicketId, Element node, java.lang.String path) {
    return new DatePanel(wicketId, node);
  }

  protected SimpleDateFormat getStoreFormat() {
    return store;
  }

  public void create(Element node) {
    node.setText(formatDate(new java.util.Date()));
  }

  class DatePanel extends Panel {
    public DatePanel(java.lang.String id, final Element node) {
      super(id);

      // TODO methinks this could be improved to display in the user's locale
      final DateConverter conv = new DateConverter(true) {
        public String getDatePattern() {
          return getStoreFormat().toPattern();
        }

        protected DateTimeFormatter getFormat() {
          return DateTimeFormat.forPattern(getStoreFormat().toPattern());
        }
      };

      TextField<java.util.Date> date = DateTextField.withConverter("value", new Model<java.util.Date>() {
        public void setObject(java.util.Date object) {
          node.setText(getStoreFormat().format(object));
        }

        public java.util.Date getObject() {
          try {
            return getStoreFormat().parse(node.getText());
          } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
          }

          return null;
        }
      }, conv);
      add(new Label("label", name));
      add(date.add(new DatePicker()));
    }
  }
}