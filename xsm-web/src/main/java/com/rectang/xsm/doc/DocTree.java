package com.rectang.xsm.doc;

import java.util.List;
import java.util.Iterator;

import org.jdom.Element;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.ResourceReference;
import com.rectang.xsm.XSM;

public class DocTree extends DocGroup {
  
  public DocTree(String name, DocElement element) {
    super(name, element);
  }
  
  public void view(Element node, StringBuffer s) {
    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();
    while (allChildren.hasNext()) {
      Element next = (Element) allChildren.next();
      element.view(next, s);
      s.append("<br />\n");

      List subChildren = next.getChildren(element.getName());
      if (subChildren.size() > 0) {
        s.append("<blockquote>\n");
        view(next, s);
        s.append("</blockquote>\n");
      }
    }
  }
  
  public void publish(Element node, StringBuffer s) {
    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();
    while (allChildren.hasNext()) {
      Element next = (Element) allChildren.next();
      element.publish(next, s);
      s.append("<br />\n");

      List subChildren = next.getChildren(element.getName());
      if (subChildren.size() > 0) {
        s.append("<blockquote>\n");
        publish(next, s);
        s.append("</blockquote>\n");
      }
    }
  }

  public WebMarkupContainer edit(String wicketId, Element node, String path) {
    Panel ret = new TreePanel(wicketId, node, path);

    return ret;
  }

  public void destroy(Element node) {
    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();
    while (allChildren.hasNext()) {
      Element next = (Element) allChildren.next();

      List subChildren = next.getChildren(element.getName());
      if (subChildren.size() > 0) {
        destroy(next);
      }
      element.destroy(next);
    }
  }

  public void delete(Element node, String id) {
    String[] parts = id.split("/");
    Element parent = node;
    for (int i = 0; i < parts.length - 1; i++) {
      if (parts[i] == null || parts[i].equals(""))
        continue;
      
      String[] kidParts = parts[i].split("@");
      int pos;
      switch (kidParts.length) {
        case 2:
          if (!kidParts[0].equals(element.name))
            continue;
        case 1:
          pos = Integer.parseInt(kidParts[kidParts.length - 1]);
          break;
        default:
          return;
      }

      parent = (Element) parent.getChildren(element.name).get(pos);
    }
    String childName = parts[parts.length - 1];

    parts = childName.split("@");
    int pos;
    switch (parts.length) {
      case 2:
        if (!parts[0].equals(element.name))
          return;
      case 1:
        pos = Integer.parseInt(parts[parts.length - 1]);
        break;
      default:
        return;
    }

    Element oldNode = (Element) parent.getChildren(element.name).remove(pos);
    deleteKids(oldNode);
    element.destroy(oldNode);
  }

  private void deleteKids(Element node) {
    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();
    while (allChildren.hasNext()) {
      Element next = (Element) allChildren.next();

      List subChildren = next.getChildren(element.getName());
      if (subChildren.size() > 0) {
        deleteKids(next);
      }
      element.destroy(next);
    }
  }

  protected void move(Element node, String id, int to, boolean relative) {
    String[] parts = id.split("/");
    Element parent = node;
    for (int i = 0; i < parts.length - 1; i++) {
      if (parts[i] == null || parts[i].equals(""))
        continue;
      
      String[] kidParts = parts[i].split("@");
      int pos;
      switch (kidParts.length) {
        case 2:
          if (!kidParts[0].equals(element.name))
            continue;
        case 1:
          pos = Integer.parseInt(kidParts[kidParts.length - 1]);
          break;
        default:
          return;
      }

      parent = (Element) parent.getChildren(element.name).get(pos);
    }
    String childName = parts[parts.length - 1];

    parts = childName.split("@");
    int pos;
    switch (parts.length) {
      case 2:
        if (!parts[0].equals(element.name))
          return;
      case 1:
        pos = Integer.parseInt(parts[parts.length - 1]);
        break;
      default:
        return;
    }

    int toPos = to;
    if (relative) {
      toPos = pos + to;
    }

    List elements = parent.getChildren(element.name);
    if (toPos < 0 || toPos > elements.size() - 1)
      return;

    Element target = (Element) elements.remove(pos);
    elements.add(toPos, target);
  }

  class TreePanel extends Panel {
    public TreePanel(final String wicketId, final Element node, final String path) {
      super(wicketId);
      add(new Label("name", getName()));

      List children = node.getChildren(element.getName());
      add(new ListView<Element>("elements", children) {
        protected void populateItem(ListItem listItem) {
          final Element child = (Element) listItem.getModelObject();
          final int i = listItem.getIndex();

          Link top = new Link("top") {
            public void onClick() {
              top(node, element.getName() + "@" + i);
            }
          };
          listItem.add(top.setVisible(listItem.getIndex() > 0));
          top.add(new Image("top-icon", new ResourceReference(XSM.class,
                "icons/go-top.png")));

          Link up = new Link("up") {
            public void onClick() {
              up(node, element.getName() + "@" + i);
            }
          };
          listItem.add(up.setVisible(listItem.getIndex() > 0));
          up.add(new Image("up-icon", new ResourceReference(XSM.class,
                "icons/go-up.png")));

          Link delete = new Link("delete") {
            public void onClick() {
              //TODO add confirmation input
              delete(node, element.getName() + "@" + i);
            }
          };
          listItem.add(delete);
          delete.add(new Image("delete-icon", new ResourceReference(XSM.class,
                "icons/edit-delete.png")));

          Link down = new Link("down") {
            public void onClick() {
              down(node, element.getName() + "@" + i);
            }
          };
          listItem.add(down.setVisible(listItem.getIndex() < ((List) listItem.getParent().getDefaultModelObject()).size() - 1));
          down.add(new Image("down-icon", new ResourceReference(XSM.class,
                "icons/go-down.png")));

          Link bottom = new Link("bottom") {
            public void onClick() {
              bottom(node, element.getName() + "@" + i);
            }
          };
          bottom.add(new Image("bottom-icon", new ResourceReference(XSM.class,
                "icons/go-bottom.png")));
          listItem.add(bottom.setVisible(listItem.getIndex() < ((List) listItem.getParent().getDefaultModelObject()).size() - 1));
          
          listItem.add(element.edit("content", child, path + "/" + element.getName() + "@" + i));
          listItem.add(edit("recurse", child, path + "/" + element.getName() + "@" + i));
        }
      }).setRenderBodyOnly(true);

      Link add = new Link("add") {
        public void onClick() {
          Element child = new Element(element.getName());
          node.addContent(child);
          element.create(child);
        }
      };
      add(add);
      add.add(new Image("add-icon", new ResourceReference(XSM.class,
            "icons/document-new.png")));

      if (children.size() > 0)
        add(new Label("add-label", new StringResourceModel("add", add, new Model(element))));
      else
        add(new Label("add-label", new StringResourceModel("add-sub", add, new Model(element))));
    }
  }
}