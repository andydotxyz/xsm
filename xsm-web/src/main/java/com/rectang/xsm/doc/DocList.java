package com.rectang.xsm.doc;

import com.rectang.xsm.site.DocumentPage;
import com.rectang.xsm.io.XSMDocument;
import com.rectang.xsm.UserData;

import java.util.*;

import org.jdom.Element;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class DocList extends DocElement {
  
  public DocElement[] elements;
  
  public DocList(String name, DocElement[] elements) {
    super(name);
    this.elements = elements;
  }
  
  public void view(Element node, StringBuffer s) {
    for (int i = 0; i < elements.length; i++)
      if (elements[i] != null && node != null) { 
        elements[i].view(node.getChild(elements[i].getName()), s);
        s.append("<br />\n");
      }
  }
  
  public void publish(Element node, StringBuffer s) {
    for (int i = 0; i < elements.length; i++)
      if (elements[i] != null && node != null) { 
        elements[i].publish(node.getChild(elements[i].getName()), s);
        s.append("<br />\n");
      }
  }

  public WebMarkupContainer edit(String wicketId, final Element node, final String path) {
    return edit(wicketId, node, path, null);
  }

  public WebMarkupContainer edit(String wicketId, final Element node, final String path, final String hint) {
    Panel ret = new ListPanel(wicketId, node, path, hint);

    return ret;
  }
  
  public void create(Element node) {
    Element created;
    for (int i = 0; i < elements.length; i++) {
      node.addContent(created = new Element(elements[i].getName()));
      elements[i].create(created);
    }
  }

  public void destroy(Element node) {
    for (int i = 0; i < elements.length; i++)
      if (elements[i] != null && node != null) 
        elements[i].destroy(node.getChild(elements[i].getName()));
  }

  public DocElement getElement(String name) {
    for (int i = 0; i < elements.length; i++) {
      if (elements[i].getName().equals(name))
        return elements[i];
    }
    return null;
  }
  
  /* methods needed to copy root data to all children in the list */

  public void setUser(UserData user) {
   super.setUser(user);
   for (int i = 0; i < elements.length; i++) {
      elements[i].setUser(user);
    }
  }
  
  public void setPage(DocumentPage page) {
    super.setPage(page);
    for (int i = 0; i < elements.length; i++) {
      elements[i].setPage(page);
    }
  }
  
  public void setDoc(XSMDocument doc) {
    super.setDoc(doc);
    for (int i = 0; i < elements.length; i++) {
      elements[i].setDoc(doc);
    }
  }

  public List getSupportedOptions() {
    Vector ret = new Vector();
    for (int i = 0; i < elements.length; i++) {
      ret.addAll(elements[i].getSupportedOptions());
    }

    return ret;
  }

  class ListPanel extends Panel {
    public ListPanel(final String wicketId, final Element node, final String path, final String hint) {
      super(wicketId);
      add(new Label("hint", hint).setVisible(hint != null));

      // TODO don't reconstruct this all the time
      List elementList = Arrays.asList(elements);

      add(new ListView("elements", elementList) {
        protected void populateItem(ListItem listItem) {
          DocElement elem = (DocElement) listItem.getModelObject();
          if (elem == null || node == null) {
            listItem.add(new WebMarkupContainer("content"));
            listItem.setVisible(false);
            return;
          }

          listItem.add(elem.edit("content", node.getChild(elem.getName()),
              path + "/" + elem.getName() + "@" + listItem.getIndex()));
        }
      });
    }
  }
}