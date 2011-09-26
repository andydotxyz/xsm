package com.rectang.xsm.pages;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.io.*;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.PageParameters;
import com.rectang.xsm.AccessControl;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Andrew Williams
 * @version $Id: Help.java 672 2007-10-06 21:42:14Z aje $
 * @since 2.0
 *
 * @plexus.component role="org.apache.wicket.Page" role-hint="help"
 */
public class Help extends XSMPage {
  private Element rootElement;
  private Element welcomeElement = null;

  public int getLevel() {
    return AccessControl.MEMBER;
  }

  public Help() throws Exception {
    Document doc = new SAXBuilder().build(
        getClass().getClassLoader().getResourceAsStream("/com/rectang/xsm/help/structure.xml"));
    rootElement = doc.getRootElement();

    Iterator kids = rootElement.getChildren("page").iterator();
    while (kids.hasNext()) {
      Element next = (Element) kids.next();
      if (next.getAttributeValue("section").equals("welcome")) {
        welcomeElement = next;
        break;
      }
    }

    if (welcomeElement == null)
      throw new Exception("structure.xml must define a page called \"welcome\".");
  }

  public void layout() {
    super.layout();

    add(new Label("sectionTitle", getTitle()));
    String content;
    try {
      content = getContent();
    } catch (FileNotFoundException e) {
      content = "<h2>Section Not Found</h2><p>Sorry, the section " +
          getSection() + " cannot be found</p>";
    }
    add(new Label("content", content).setEscapeModelStrings(false));

    /* tree */
    add(new ListView("helpPages", rootElement.getChildren("page")) {
      protected void populateItem(ListItem listItem) {
        Element page = (Element) listItem.getModelObject();

        WebMarkupContainer link;
        String remote = page.getAttributeValue("remote");
        if (remote != null) {
          link = new ExternalLink("helpPage", remote);
        } else {
          PageParameters params = new PageParameters();
          params.add("section", page.getAttributeValue("section"));

          link = new BookmarkablePageLink("helpPage", getPageClass("help"), params);
        }

        link.add(new Label("helpPageLabel", page.getChildText("title")));
        listItem.add(link);
      }
    });

    /* footer links */
    add(new ListView("childPages", getChildren()){
      protected void populateItem( ListItem listItem ) {
        HelpPage page = (HelpPage) listItem.getModelObject();

        PageParameters params = new PageParameters();
        params.add("section", page.getSection());

        Link link = new BookmarkablePageLink("childPage", getPageClass("help"), params);
        link.add(new Label("childPageLabel", page.getTitle()));
        listItem.add(link);
      }
    });
  }

  public String getSection() {
    String section = getPageParameters().getString("section", "welcome");

    /* check for the existance of the requested section */
    Element sectionElement = getSectionElement(section);
    if (sectionElement.equals(welcomeElement))
      return "welcome";

    return section;
  }

  public String getTitle() {
    try {
      return getSectionElement(getSection()).getChildText("title");
    } catch (Exception e) {
      return "Untitled";
    }
  }

  public String getContent() throws FileNotFoundException {
    StringBuffer buffer = new StringBuffer();
    String resource = "/com/rectang/xsm/help/" + getSection().replace('.', '/') + ".txt";

    InputStream input = this.getClass().getClassLoader().getResourceAsStream(resource);
    if (input == null) {
      throw new FileNotFoundException(getSection().replace('.', '/') + ".txt");
    }

    BufferedReader in = null;
    try {
      in = new BufferedReader(new InputStreamReader(input));

      String line;
      while ((line = in.readLine()) != null) {
        buffer.append(line);
        buffer.append('\n');
      }
    } catch ( IOException e) {
      e.printStackTrace();
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (IOException e) {
          /* ignore */
        }
      }
    }

    return buffer.toString();
  }

  public List getChildren() {
    Vector children;
    try {
      children = genKids();
    } catch (Exception e) {
      children = new Vector();
    }

    return children;
  }

  public Vector genKids() {
    String index = getSection();
    Element node = getSectionElement(getSection());

    Vector ret = new Vector();
    List kids = node.getChildren("page");
    if (kids != null) {
      Iterator subPages = kids.iterator();
      while (subPages.hasNext()) {
        Element next = (Element) subPages.next();
        ret.add(new HelpPage(next.getChildText("title"),
            index + "." + next.getAttributeValue("section")));
      }
    }
    return ret;
  }

  public Element getSectionElement(String section) {
    Element root = rootElement;

    String[] parts = section.split("\\.");
    for (int i = 0; i < parts.length; i++) {
      if (parts[i] == null || parts[i].equals(""))
        continue;

      Iterator kids = root.getChildren("page").iterator();
      boolean found = false;
      while (kids.hasNext()) {
        Element next = (Element) kids.next();
        if (next.getAttributeValue("section").equals(parts[i])) {
          root = next;
          found = true;
          break;
        }
      }
      if (!found)
        return welcomeElement;
    }

    return root;
  }

}

class HelpPage {
  private String section, title;

  public HelpPage(String title, String section) {
    this.title = title;
    this.section = section;
  }

  public String getTitle() {
    return title;
  }

  public String getSection() {
    return section;
  }
}