package com.rectang.xsm.wicket;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.Component;
import com.rectang.xsm.doc.SupportedOption;
import com.rectang.xsm.io.XSMDocument;

import java.io.Serializable;

/**
 * Wicket panels for the SupportedOption elements
 *
 * @author Andrew Williams
 * @version $Id: OptionPanel.java 663 2007-10-04 22:50:25Z aje $
 * @since 1.0
 */
public class OptionPanel {
  public static class BooleanOption extends Panel {
    private final transient XSMDocument doc;

    public BooleanOption(String id, final SupportedOption bool, XSMDocument d) {
      super(id);
      this.doc = d;

      add(new CheckBox("value", new Model() {
        public Object getObject() {
          return Boolean.valueOf(bool.getBoolean(doc));
        }

        public void setObject(Serializable object) {
          doc.setOption(bool.getName(), object.toString());
        }
      }));
    }
  }

  public static class IntegerOption extends Panel {
    private final transient XSMDocument doc;

    public IntegerOption(String id, final SupportedOption integ, XSMDocument d) {
      super(id);
      this.doc = d;

      add(new TextField("value", new Model() {
        public Object getObject() {
          return Integer.valueOf(integ.getInteger(doc));
        }

        public void setObject(Serializable object) {
          doc.setOption(integ.getName(), object.toString());
        }
      }));
    }
  }

  public static class StringOption extends Panel {
    private final transient XSMDocument doc;

    public StringOption(String id, final SupportedOption str, XSMDocument d) {
      super(id);
      this.doc = d;

      add(new TextField("value", new Model() {
        public Object getObject() {
          return str.getString(doc);
        }

        public void setObject(Serializable object) {
          doc.setOption(str.getName(), object.toString());
        }
      }));
    }
  }
}
