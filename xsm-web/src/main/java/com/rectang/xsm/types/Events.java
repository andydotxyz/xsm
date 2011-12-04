package com.rectang.xsm.types;

import com.rectang.xsm.*;
import com.rectang.xsm.util.HTMLUtils;
import com.rectang.xsm.doc.DocElement;
import com.rectang.xsm.doc.DocGroup;
import com.rectang.xsm.doc.DocList;
import com.rectang.xsm.doc.SupportedOption;
import com.rectang.xsm.io.PublishedFile;
import com.rectang.xsm.widget.DateTime;
import com.rectang.xsm.widget.HTMLTextArea;
import com.rectang.xsm.widget.Value;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Iterator;
import java.util.Vector;

import org.jdom.Element;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.Model;

/* TODO remove links "my events" when viewing a users page and remove "read more" if there is no more to read - maybe a  comments indicator too? */
public class Events extends DocGroup {

  public static final SupportedOption PAGE_LENGTH = new SupportedOption("PAGE_LENGTH",
      "The number of events to be displayed on the main events page", 10);
  public static final SupportedOption AUTHOR_PAGES = new SupportedOption("AUTHOR_PAGES",
      "Generage an events page for each author as well as the main page", false);
  public static final SupportedOption SUMMARY_LENGTH = new SupportedOption("SUMMARY_LENGTH",
      "The number of characters (approx) in an event summary", 500);

  private Vector options;

  /* FIXME - don't allow folk to save to items they cannot edit */
  public Events(java.lang.String name) {
    super(name, new Event("event"));
    
    options = new Vector();
    options.add(PAGE_LENGTH);
    options.add(AUTHOR_PAGES);
    options.add(SUMMARY_LENGTH);
  }

  public void view(Element node, StringBuffer s) {
    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();
    int i = 0;
    while (allChildren.hasNext() && i < PAGE_LENGTH.getInteger(getDoc())) {
      element.view((Element) allChildren.next(), s);
      s.append("<br />\n");
      i++;
    }
    /* FIXME maybe summarise the archive too? */
  }

  public WebMarkupContainer edit(String wicketId, Element node, String path) {
    Panel ret = new EventPanel(wicketId, node, path, getEditCount());

    return ret;
  }

  protected int getEditCount() {
    /* FIXME we need to allow editing of the archive */

    return 10;
  }

  public void publish(Element node, StringBuffer s) {
    int page_length = PAGE_LENGTH.getInteger(getDoc());

    /* publish front page */
    publishNNodes(node, page_length, s);
    /* publish user pages */
    boolean author_pages = AUTHOR_PAGES.getBoolean(getDoc());
    if (author_pages) {
      String author_dir = getPublishedPath() + File.separatorChar + "_authors"
          + File.separatorChar;
      (getSite().getPublishedDoc(author_dir)).mkdir();
      Vector authors = new Vector();
      Iterator events = node.getChildren("event").iterator();
      while (events.hasNext()) {
        Element next = (Element) events.next();
        String uidStr = next.getChild("uid").getText();
        if (uidStr != null && !uidStr.equals(""))
          if (!authors.contains(uidStr))
            authors.add(uidStr);
      }
      Iterator authorIter = authors.iterator();
      while (authorIter.hasNext()) {
        String author = (String) authorIter.next();
        publishNAuthorNodes(node, author, author_dir, page_length);
      }
    }

    /* publish each article */
    publishPermaNodes(node);
    
    List children = node.getChildren(element.getName());
    if (children.size() > page_length) {
      String archivePage = getPublishedPath() + "/archive.html";
      s.append("<p align=\"right\">more in the <a href=\"");
      s.append(getSite().getPrefixUrl() + archivePage + "\">archive</a></p>");

      PublishedFile archive = getSite().getPublishedDoc(archivePage);
      StringBuffer out = new StringBuffer();
      publishNNodes(node, 0, out);
      getDoc().publishContent(archive, out.toString(), getUser());
    }
  }

  private void publishNNodes(Element node, int n, StringBuffer s) {
    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();

    int counter = 0;
    if (n == 0) n = children.size();
    String index;
    int fakeIndex = 0;
    while (allChildren.hasNext() && counter++ < n) {
      Element next = (Element) allChildren.next();
      index = next.getAttributeValue("index");
      if (index == null || index.equals(""))
        index = "x" + fakeIndex++;

      ((Event) element).publish(next, true, index, s);
    }
  }

  private void publishNAuthorNodes(Element node, String author, String dir, int n) {
    StringBuffer content = new StringBuffer();
    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();
    int counter = 0;

    if (n == 0) n = children.size();
    String index;
    int fakeIndex = 0;
    while (allChildren.hasNext() && counter < n) {
      Element next = (Element) allChildren.next();
      index = next.getAttributeValue("index");
      if (index == null || index.equals(""))
        index = "x" + fakeIndex++;

      String uid = next.getChild("uid").getText();
      if (uid != null && uid.equals(author)) {
        ((Event) element).publish(next, true, index, content);
        counter++;
      }
    }    
    PublishedFile out = getSite().getPublishedDoc(dir + author + ".html");
    getDoc().publishContent(out, content.toString(), getUser());
  }

  private void publishPermaNodes(Element node) {
    Hashtable months = new Hashtable();
    Hashtable days = new Hashtable();
    Calendar lastDate = null, lastMonth = null;

    String dir = getPublishedPath() + File.separatorChar + "_events"
        + File.separatorChar;
    (getSite().getPublishedDoc(dir)).mkdir();

    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();
    String index;
    int fakeIndex = 0;
    while (allChildren.hasNext()) {
      Element next = (Element) allChildren.next();
      index = next.getAttributeValue("index");
      if (index == null || index.equals(""))
        index = "x" + fakeIndex++;

      StringBuffer content = new StringBuffer();
      element.publish(next, content);
      PublishedFile out = getSite().getPublishedDoc(dir + index + ".html");
      getDoc().publishContent(out, content.toString(), getUser());

      /* collate dates */
      Date theDate = DateTime.parseDateTime(next.getChild("start"));
      if (lastDate == null) {
        lastDate = Calendar.getInstance();
        lastDate.setTime(theDate);
        lastMonth = Calendar.getInstance();
        lastMonth.setTime(theDate);
        appendVectorInHashtable(months, theDate, next);
        appendVectorInHashtable(days, theDate, next);
      } else {
        Calendar thisDate = Calendar.getInstance();
        thisDate.setTime(theDate);
        if (thisDate.get(Calendar.YEAR) != lastDate.get(Calendar.YEAR) ||
            thisDate.get(Calendar.MONTH) != lastDate.get(Calendar.MONTH)) {
          lastMonth.setTime(theDate);
        }
        if (thisDate.get(Calendar.YEAR) != lastDate.get(Calendar.YEAR) ||
            thisDate.get(Calendar.MONTH) != lastDate.get(Calendar.MONTH) ||
            thisDate.get(Calendar.DAY_OF_YEAR) !=
            lastDate.get(Calendar.DAY_OF_YEAR)) {
          lastDate.setTime(theDate);
        }

        appendVectorInHashtable(months, lastMonth.getTime(), next);
        appendVectorInHashtable(days, lastDate.getTime(), next);
      }
    }

    Enumeration monthList = months.keys();
    while (monthList.hasMoreElements()) {
      Date date = (Date) monthList.nextElement();
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);

      StringBuffer content = new StringBuffer();
      Vector monthEvents = (Vector) months.get(date);
      for (int i = 0; i < monthEvents.size(); i++) {
        element.publish((Element) monthEvents.get(i), content);
      }
      PublishedFile out = getSite().getPublishedDoc(getPublishedPath() +
          "/_months/" + cal.get(Calendar.MONTH) + ".html");
      getDoc().publishContent(out, content.toString(), getUser());
    }

    Enumeration dayList = days.keys();
    while (dayList.hasMoreElements()) {
      Date date = (Date) dayList.nextElement();
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);

      StringBuffer content = new StringBuffer();
      Vector dayEvents = (Vector) days.get(date);
      for (int i = 0; i < dayEvents.size(); i++) {
        element.publish((Element) dayEvents.get(i), content);
      }
      PublishedFile out = getSite().getPublishedDoc(getPublishedPath() +
          "/_months/" + cal.get(Calendar.MONTH) + "/" +
          cal.get(Calendar.DAY_OF_MONTH) + ".html");
      getDoc().publishContent(out, content.toString(), getUser());
    }
  }

  private static void appendVectorInHashtable(Hashtable hash, Object key,
      Object append) {
    Vector vect = (Vector) hash.get(key);
    if (vect == null)
      vect = new Vector();
    vect.add(append);
    hash.put(key, vect);
  }

  public List getSupportedOptions() {
    Vector ret = new Vector();
    ret.addAll(options);
    ret.addAll(element.getSupportedOptions());
    return ret;
  }


  class EventPanel extends Panel {
    public EventPanel(final String wicketId, final Element node,
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
          listItem.add(add.setVisible(listItem.getIndex() == 0));
          add.add(new Image("add-icon", new ResourceReference(XSM.class,
                "icons/document-new.png")));
          listItem.add(new Label("add-label", new StringResourceModel("add", add, new Model(element))));

          Link delete = new Link("delete") {
            public void onClick() {
              //TODO add confirmation input
              delete(node, element.getName() + "@" + i);
            }
          };
          listItem.add(delete);
          delete.add(new Image("delete-icon", new ResourceReference(XSM.class,
                "icons/edit-delete.png")));

          listItem.add(element.edit("content", child, path + "/" + element.getName() + "@" + i));
        }
      }).setRenderBodyOnly(true);
    }
  }
}
  
class Event extends DocList {

  public Event(String name) {
    super(name, new DocElement[] {
        new com.rectang.xsm.widget.String("subject"),
        new DateTime("start"),
        new DateTime("stop"),
        new HTMLTextArea("body"),
        new Value("author", Value.FULLNAME),
        new Value("uid", Value.USERNAME),
        new Value("email", Value.EMAIL),
        new GalleryCommentList("comments") /* FIXME- have a central comments definition */
    });
  }

  public void view(Element root, StringBuffer s) {
    s.append("<table border=\"1\"><tr><td><b>");
    elements[0].view(root.getChild("subject"), s);
    s.append("</b> - ");
    elements[4].view(root.getChild("author"), s);
    s.append(" &lt;"); 
    elements[6].view(root.getChild("email"), s);
    s.append("&gt;");
    s.append(" (");
    elements[1].view(root.getChild("start"), s);
    s.append(" to ");
    elements[2].view(root.getChild("stop"), s);
    s.append(")</td></tr>\n");
    s.append("<tr><td><p>");
    elements[3].view(root.getChild("body"), s);
    s.append("</p><p>Comments:<br /><br />");
    elements[7].view(root.getChild("comments"), s);
    s.append("</td></tr></table>\n");
  }

  public void publish(Element root, StringBuffer s) {
    publish(root, false, String.valueOf(0), s);
  }

  public void publish(Element root, boolean summarise, String id, StringBuffer s) {
    s.append("<p class=\"xsm_news_title\"><b><a name=\"");
    s.append(root.getAttributeValue("index") + "\">");
    elements[0].view(root.getChild("subject"), s);
    s.append("</a></b> - ");
    elements[4].view(root.getChild("author"), s);
    if (Events.AUTHOR_PAGES.getBoolean(getDoc())) {
      String uid = root.getChildText("uid");
      if (uid != null & !uid.equals("")) {
        s.append(" [<a href=\"");
        s.append(getSite().getPrefixUrl() + getPublishedPath());
        s.append("/_authors/" + root.getChildText("uid") + ".html\">");
        s.append("All my events</a>]");
      }
    }

    Calendar start = Calendar.getInstance();
    start.setTime(DateTime.parseDateTime(root.getChild("start")));
    Calendar stop = Calendar.getInstance();
    stop.setTime(DateTime.parseDateTime(root.getChild("stop")));
    DateFormat format = DateFormat.getDateTimeInstance();
    format.setCalendar(start);

    String link = getSite().getPrefixUrl() + getPublishedPath() + "/_months/" + start.get(Calendar.MONTH);
    s.append(" (");
    s.append(new SimpleDateFormat("EE").format(start.getTime()));
    s.append(" <a href=\"" + link + "/" + start.get(Calendar.DATE) + ".html\">");
    s.append(start.get(Calendar.DATE));
    s.append("</a> <a href=\"" + link + ".html\">");
    s.append(new SimpleDateFormat("MMMM").format(start.getTime()));
    s.append("</a> ");
    s.append(start.get(Calendar.YEAR));
    s.append(" ");
    
    if (start.get(Calendar.YEAR) == stop.get(Calendar.YEAR) &&
        start.get(Calendar.DAY_OF_YEAR) == stop.get(Calendar.DAY_OF_YEAR)) {
      s.append(start.get(Calendar.HOUR_OF_DAY));
      s.append(":");
      s.append(start.get(Calendar.MINUTE));
      s.append(" - ");
      s.append(stop.get(Calendar.HOUR_OF_DAY));
      s.append(":");
      s.append(stop.get(Calendar.MINUTE));
    } else {
      s.append("for ");
      if (start.get(Calendar.YEAR) != stop.get(Calendar.YEAR)) {
        s.append(stop.get(Calendar.YEAR) - start.get(Calendar.YEAR));
        s.append(" years and");
      }
      s.append(stop.get(Calendar.DAY_OF_YEAR) - start.get(Calendar.DAY_OF_YEAR));
      s.append(" days");
    }
    s.append(")</p>\n");

    s.append("<div class=\"xsm_news_article\">");
    StringBuffer body = new StringBuffer();
    elements[3].publish(root.getChild("body"), body);
    if (summarise) {
      s.append(summarise(body.toString()));
      s.append(" <a href=\"" + getSite().getPrefixUrl());
      s.append(getPublishedPath() + "/_events/" + id + ".html\">Read more</a>");
    } else
      s.append(body);
    s.append("</div>\n");

    if (!summarise) {
      Element comments = root.getChild("comments");
      int commentCount = comments.getChildren("comment").size();
      if (commentCount > 0) {
        s.append("<div class=\"xsm_comments\"><p><b>Comments:</b></p>");
        elements[7].publish(comments, s);
        s.append("</div>");
      }
    }
  }

  public void destroy(Element root) {
    int index;
    try {
      index = Integer.parseInt(root.getAttributeValue("index"));
    } catch (NumberFormatException e) {
      // if there is no index we cannot just guess what page to delete
      return;
    }

    getSite().getPublishedDoc(getPublishedPath() + File.separatorChar
        + "_events" + File.separatorChar + index + ".html").delete();
  }

  private String summarise(String in) {
    return HTMLUtils.summarise(in, Events.SUMMARY_LENGTH.getInteger(getDoc()));
  }
}
