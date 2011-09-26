package com.rectang.xsm.types;

import com.rectang.xsm.doc.*;
import com.rectang.xsm.io.PublishedFile;
import com.rectang.xsm.widget.HTMLTextArea;

import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;

import org.jdom.Element;

public class Book extends DocList {

  public static final SupportedOption GEN_TABLES = new SupportedOption("GEN_TABLES",
      "Generate the \"List of images\" and \"List of files\" pages", false);

  public static final int DEPTH_CUTOFF = 4;
  int[] index = new int[DEPTH_CUTOFF];
  private Vector options;
  
  String prev = null;
  Vector contents = new Vector();

  public Book(java.lang.String name) {
    super(name, new DocElement[] {new DocTree("contents", new BookPage("page")),
        new Gallery("images"), new Files("files")
    });

    options = new Vector();
    options.add(GEN_TABLES);
  }

  /* view maybe split into pieces with contents */
/* TODO make a nice edit layout
  public void edit(Element node, String path, StringBuffer s) {
    s.append("<p>You can <a href=\"XSM?action=page.edit&page=" + getPath());
    s.append("&index=" + path + "/images@1\">edit</a> the books images<br>\n");
    s.append("or <a href=\"XSM?action=page.edit&page=" + getPath());
    s.append("&index=" + path + "/files@2\">edit</a> the books files<br>\n");
    
    if (node.getChild("contents").getChildren("page").size() == 0) {
      s.append("<a href=\"XSM?action=page.add&page=" + getPath());
      s.append("&index=" + path + "/contents@0&addnode=page@0\">create");
      s.append("</a> your books first page<br>\n");
    } else
      editKids(node.getChild("contents"), path + "/contents@0", path, "", s);
    s.append("</p>");
  }
*/
  /* FIXME - need to insert some deletion code to remove stale page .htmls
   * how to do this? do we remove the page that was deleted and ripple? */
/* TODO make a nice edit layout
  private void editKids(Element node, String path, String root, String rel,
      StringBuffer s) {
    if (node.getChildren("page").size() == 0)
      return;
    Iterator children = node.getChildren("page").iterator();
    s.append("<table border=\"0\">");
    int counter = 0;
    while (children.hasNext()) {
      Element next = (Element) children.next();

      String rootLink = "XSM?page=" + getPath() + "&index=" + root + "/contents@0&viewindex=" + root;
      String movelink = rootLink + "&movenode=" + rel + "/page@" + counter + "&action=page.";
      s.append("<tr><td>");
      if (counter > 0) {
        s.append("<a href=\"" + movelink + "up\"><img src=\"icons/go-up.png\"");
        s.append("border=\"0\" alt=\"up\" title=\"up\"></a>");
      }
      s.append("<a href=\"" + rootLink + "&delnode=" + rel + "/page@" + counter);
      s.append("&action=page.delete\"><img src=\"icons/edit-delete.png\" border=\"0\"");
      s.append("alt=\"delete\" title=\"delete\"></a>\n");
      if (children.hasNext()) {
        s.append("<a href=\"" + movelink + "down\"><img src=\"icons/go-down.png\"");
        s.append("border=\"0\" alt=\"down\" title=\"down\"></a>");
      }
      s.append("</td><td><a href=\"XSM?action=page.edit&page=" + getPath());
      s.append("&index=" + path + "/page@" + counter + "\">");
      s.append("edit</a> " + next.getChildText("title"));
      
      if (next.getChildren("page").size() == 0) {
        s.append("<font size=\"-2\">\t\t(<a href=\"XSM?action=page.add&page=");
        s.append(getPath() + "&index=" + path + "/page@" + counter);
        s.append("&addnode=page@0" + "\">... add a sub level ...</a>)</font>\n");
      } else {
        s.append("</td></tr><tr><td>&nbsp;</td><td>");
        editKids(next, path + "/page@" + counter, root, rel + "/page@" + counter, s);
      }
      s.append("</td></tr>\n");
      counter++;
    }
    s.append("<tr><td><a href=\"XSM?action=page.add&page=" + getPath());
    s.append("&index=" + path + "&addnode=page@" + counter);
    s.append("\"><img src=\"icons/document-new.png\" border=\"0\"");
    s.append("alt=\"add page\" title=\"add page\"></a></td><td>&nbsp;</td></tr>\n");
    s.append("</table>");
  }
*/
  public void publish(Element node, StringBuffer s) {
    List nodes = node.getChild("contents").getChildren(elements[0].element
        .getName());
    if (nodes == null || nodes.size() == 0)
      return;

    String pageDir = getPath() + File.separatorChar + "_pages"
        + File.separatorChar;
    (getSite().getPublishedDoc(pageDir)).mkdir();

    s.append("<h2>Contents</h2>\n");
    generateContents(nodes.iterator(), "", 0, pageDir, s);
    s.append("<p>Or you can see the book in <a href=\"");
    s.append(getSite().getPrefixUrl() + pageDir);
    s.append("print.html\">1 page</a> (printer friendly).</p>");

    StringBuffer out;
    if (GEN_TABLES.getBoolean(getDoc())) {
      /* generate the gallery pages */
      PublishedFile galleryFile = getSite().getPublishedDoc(
          getPath() + File.separatorChar + "images.html");
      out = new StringBuffer();
      elements[1].publish(node.getChild("images"), out);
      getDoc().publishContent(galleryFile, out.toString(), getUser());

      /* generate the files page */
      PublishedFile filesFile = getSite().getPublishedDoc(
          getPath() + File.separatorChar + "files.html");
      out = new StringBuffer();
      elements[2].publish(node.getChild("files"), out);
      getDoc().publishContent(filesFile, out.toString(), getUser());
    }

    generateContents(nodes.iterator(), 0, null);
    generateView(nodes.iterator(), 0, pageDir, contents.iterator(), null);
    /* output a single-page (printer friendly) version */
    PublishedFile printFile = getSite().getPublishedDoc(pageDir
        + "print.html");
    out = new StringBuffer();
    generateWholeView(nodes.iterator(), 0, out);
    getDoc().publishContent(printFile, out.toString(), getUser());
  }

  private void generateView(Iterator pages, int level, String dir,
      Iterator contents, StringBuffer s) {
  	if (level < DEPTH_CUTOFF)
      index[level] = 0;
    if (!pages.hasNext())
      return;
    StringBuffer ret = new StringBuffer();
    while (pages.hasNext()) {
      Element next = (Element) pages.next();
      StringBuffer content = new StringBuffer();
      content.append("<h2>");
      if (level < DEPTH_CUTOFF) {
        index[level]++;
        content.append(getId(level) + " - ");
      }
      ((BookPage) ((DocTree) elements[0]).element).elements[0]
          .publish(next.getChild("title"), content);
      content.append("</h2>\n");
      ((BookPage) ((DocTree) elements[0]).element).elements[1]
          .publish(next.getChild("content"), content);

      /* add sub page links if the sub pages are not to be included */
      if (level < DEPTH_CUTOFF - 1) {
        if (next.getChildren("page").size() > 0) {
          content.append("\n<hr /><p class=\"footer\">Sub pages: <br />\n");
          Iterator subPages = next.getChildren("page").iterator();
          int counter = 0;
          while (subPages.hasNext()) {
            counter++;
            Element child = (Element) subPages.next();
            if (next == null)
              continue;
            String title = child.getChildText("title");
            content.append("<a href=\"" + getSite().getPrefixUrl());
            content.append(dir + getId(level) + "." + counter + ".html\">");
            content.append(title + "</a><br />\n");
          }
          content.append("</p>\n");
        }
      } else
        ret.append("<hr>\n" + content.toString());

      /* add the navigation links */
      ContentElement nav = (ContentElement) contents.next();
      content.append("<hr /><table width=\"100%\" border=\"0\"><tr>");
      content.append("<td width=\"33%\" align=\"center\">");
      if (nav.prev == null)
        content.append("&nbsp;");
      else {
        content.append("<a href=\"" + getSite().getPrefixUrl());
        content.append(getPath() + "/_pages/" + nav.prev + ".html\">Previous</a>");
      }
      content.append("</td><td width=\"34%\" align=\"center\">");
      if (level == 0) {
        content.append("<a href=\"" + getSite().getPrefixUrl());
        content.append(getPath() + "/index.html\">Contents</a>");
      } else {
        content.append("<a href=\"" + getSite().getPrefixUrl());
        content.append(dir + getId(level - 1) + ".html\">Up</a>");
      }
      content.append("</td><td width=\"33%\" align=\"center\">");
      if (nav.next == null)
        content.append("&nbsp;");
      else {
        content.append("<a href=\"" + getSite().getPrefixUrl());
        content.append(getPath() + "/_pages/" + nav.next + ".html\">Next</a>");
      }
      content.append("</td></tr></table>\n");

      StringBuffer sub = new StringBuffer();
      generateView(next.getChildren(((DocTree) elements[0]).element
          .getName()).iterator(), level + 1, dir, contents, sub);
      PublishedFile out = getSite().getPublishedDoc(dir + getId(level) + ".html");
      if (level < DEPTH_CUTOFF - 1) { // make our own page
        getDoc().publishContent(out, content.toString(), getUser());
      } else if (level == DEPTH_CUTOFF - 1) { // merge our page with subpages
        getDoc().publishContent(out, content.toString() + sub.toString(), getUser());
      } else // make no page, but pass our data up to be merged
        ret.append(sub);
    }
    if (level < DEPTH_CUTOFF)
      return;
    if (s != null)
      s.append(ret);
  }

  private class ContentElement {
    String prev, up, next;

    public ContentElement (String prev, String up, String next) {
      this.prev = prev;
      this.up = up;
      this.next = next;
    }
  }

  private void generateContents(Iterator pages, int level, String prev3) {
    if (level < DEPTH_CUTOFF)
      index[level] = 0;
    if (!pages.hasNext())
      return;

    while (pages.hasNext()) {
      Element next = (Element) pages.next();
      if (level < DEPTH_CUTOFF) {
        index[level]++;
      }

      /* calculate the navigation links */
      ContentElement elem = new ContentElement(prev, null, null);
      contents.add(elem);
      int thisPos = contents.indexOf(elem);
      if (thisPos > 0)
        ((ContentElement) contents.get(thisPos - 1)).next = getId(level);
      
      prev = getId(level);
      generateContents(next.getChildren(((DocTree) elements[0]).element
          .getName()).iterator(), level + 1, getId(level));
    }
  }

  private void generateWholeView(Iterator pages, int level, StringBuffer s) {
  	if (level < DEPTH_CUTOFF)
      index[level] = 0;
    if (!pages.hasNext())
      return;
    while (pages.hasNext()) {
      Element next = (Element) pages.next();
      s.append("<h2>");
      if (level < DEPTH_CUTOFF) {
        index[level]++;
        s.append("<a name=\"" + getId(level) + "\">" + getId(level) + "</a> - ");
      }
      ((BookPage) ((DocTree) elements[0]).element).elements[0]
          .publish(next.getChild("title"), s);
      s.append("</h2>\n");
      ((BookPage) ((DocTree) elements[0]).element).elements[1]
          .publish(next.getChild("content"), s);
      s.append("<hr>\n");
      generateWholeView(next.getChildren(((DocTree) elements[0]).element
          .getName()).iterator(), level + 1, s);
    }
  }

  private void generateContents(Iterator pages, String indent, int level,
      String dir, StringBuffer s) {
    if (level >= DEPTH_CUTOFF || !pages.hasNext())
      return;
    index[level] = 0;
    s.append("<ul>");
    if (level == 0 && GEN_TABLES.getBoolean(getDoc())) {
      s.append("<li><a href=\"" + getSite().getPrefixUrl());
      s.append(getPath() + "/images.html\">List of images</a></li>\n");
      s.append("<li><a href=\"" + getSite().getPrefixUrl());
      s.append(getPath() + "/files.html\">List of files</a></li>\n");
    }
    while (pages.hasNext()) {
      Element next = (Element) pages.next();
      index[level]++;

      s.append("<li><a href=\"" + getSite().getPrefixUrl() );
      s.append(dir + getId(level) + ".html\">" + getId(level) + " - ");
      s.append(next.getChild("title").getText() + "</a>\n");
      generateContents(next.getChildren(((DocTree) elements[0])
          .element.getName()).iterator(), indent + "&nbsp;&nbsp", level + 1,
          dir, s);
      s.append("</li>\n");
    }
    s.append("</ul>");
  }

  private String getId(int level) {
    if (level < 0)
      return "";
    int counter;
    String ret = "" + index[0];
    for (counter = 1; counter <= level && counter < DEPTH_CUTOFF; counter++)
      ret += "." + index[counter];
    return ret;
  }

  public List getSupportedOptions() {
    Vector ret = new Vector();
    ret.addAll(options);
    ret.addAll(element.getSupportedOptions());
    return ret;
  }
}

class BookPage extends DocList {
  
  public BookPage(java.lang.String name) {
    super(name, new DocElement[]{new com.rectang.xsm.widget.String("title"),
        new HTMLTextArea("content")});
  }
}