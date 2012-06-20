package com.rectang.xsm.doc;

import java.util.*;

import org.jdom.Element;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.ResourceReference;
import com.rectang.xsm.XSM;

public class DocGroup extends DocElement {

  public DocGroup(String name, DocElement element) {
    super(name, element);
  }

  public void view(Element node, StringBuffer s) {
    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();
    while (allChildren.hasNext()) {
      element.view((Element) allChildren.next(), s);
      s.append(getNewline());
    }
  }

  public void publish(Element node, StringBuffer s) {
    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();
    while (allChildren.hasNext()) {
      element.publish((Element) allChildren.next(), s);
      s.append(getNewline());
    }
  }

  public String getNewline() {
    return "<br />\n";
  }

  public WebMarkupContainer edit(String wicketId, Element node, String path) {
    Panel ret = new GroupPanel(wicketId, node, path, getEditCount());

    return ret;
  }

  protected int getEditCount() {
    return 0;
  }
  
  public void create(Element node) {
    /* do not create any more here - "name" is multi*/
  }

  public void destroy(Element node) {
    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();
    while (allChildren.hasNext())
      element.destroy((Element) allChildren.next());
  }

  public void top(Element node, String id) {
    move(node, id, 0);
  }

  public void up(Element node, String id) {
    move(node, id, -1, true);
  }

  public void down(Element node, String id) {
    move(node, id, +1, true);
  }

  public void bottom(Element node, String id) {
    move(node, id, node.getChildren(element.name).size() - 1);
  }

  public void delete(Element node, String id) {
    String[] parts = id.split("@");

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
    element.destroy((Element) node.getChildren(element.name).remove(pos));
  }

  protected void move(Element node, String id, int to) {
    move(node, id, to, false);
  }

  protected void move(Element node, String id, int to, boolean relative) {
    String[] parts = id.split("@");
    
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

    List elements = node.getChildren(element.name);
    if (toPos < 0 || toPos > elements.size() - 1)
      return;

    Element target = (Element) elements.remove(pos);
    elements.add(toPos, target);
  }

  public int getNextIndex(Element node) {
    try {
      return Integer.parseInt(node.getAttributeValue("next_index"));
    } catch (NumberFormatException e) {
      return 1;
    }
  }

  public void addChild(Element parent, int index) {
    Element child = new Element(element.getName());
    int nextIndex = getNextIndex(parent);
    parent.setAttribute("next_index", String.valueOf(nextIndex + 1));
    child.setAttribute("index", String.valueOf(nextIndex));

    parent.getChildren(element.getName()).add(index, child);
    element.create(child);
  }

  public void addChildAtTop(Element parent) {
    addChild(parent, 0);
  }

  public void addChildAtEnd(Element parent, int displayCount) {
    int id = parent.getChildren(element.getName()).size();
    if (displayCount > 0 && id > displayCount) {
      addChild(parent, displayCount);
    } else {
      addChild(parent, id);
    }
  }

  class GroupPanel extends Panel {
    public GroupPanel(final String wicketId, final Element node,
                      final String path, final int childCount) {
      super(wicketId);
      add(new Label("name", getName()));

      List children = node.getChildren(element.getName());
      add(new ListView("elements", children) {
        protected void populateItem(ListItem listItem) {
          // not at all glamarous - but using sublists currently breaks the insertion code
          if (childCount > 0 && listItem.getIndex() > childCount) {
            listItem.setVisible(false);
            return;
          }
          final Element child = (Element) listItem.getModelObject();
          final int i = listItem.getIndex();

          Link add = new Link("add") {
            public void onClick() {
              addChild(node, i);
            }
          };
          listItem.add(add);
          add.add(new Image("add-icon", new ResourceReference(XSM.class,
                "icons/document-new.png")));
          listItem.add(new Label("add-label", new StringResourceModel("add", add, new Model(element))));

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
          down.add(new Image("down-icon", new ResourceReference(XSM.class,
                "icons/go-down.png")));
          listItem.add(down.setVisible(listItem.getIndex() < ((List) listItem.getParent().getModelObject()).size() - 1));

          Link bottom = new Link("bottom") {
            public void onClick() {
              bottom(node, element.getName() + "@" + i);
            }
          };
          bottom.add(new Image("bottom-icon", new ResourceReference(XSM.class,
                "icons/go-bottom.png")));
          listItem.add(bottom.setVisible(listItem.getIndex() < ((List) listItem.getParent().getModelObject()).size() - 1));

          listItem.add(element.edit("content", child, path + "/" + element.getName() + "@" + i));
        }
      }).setRenderBodyOnly(true);

      Link add = new Link("add") {
        public void onClick() {
          addChildAtEnd(node, childCount);
        }
      };
      add(add);
      add.add(new Image("add-icon", new ResourceReference(XSM.class,
            "icons/document-new.png")));
      add(new Label("add-label", new StringResourceModel("add", add, new Model(element))));
    }
  }
}
