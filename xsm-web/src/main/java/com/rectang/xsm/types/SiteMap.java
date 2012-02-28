package com.rectang.xsm.types;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.rectang.xsm.site.HierarchicalPage;
import org.jdom.Element;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import com.rectang.xsm.MetaData;
import com.rectang.xsm.doc.*;
import com.rectang.xsm.site.Page;
import com.rectang.xsm.site.DocumentPage;

public class SiteMap extends DocElement {

  private SupportedOption SHOW_TITLES = new SupportedOption("SHOW_TITLES", 
      "Show page titles in the site map", true);

  private Vector options;

  public SiteMap(java.lang.String name) {
    super(name, null);

    options = new Vector();
    options.add(SHOW_TITLES);
  }

  public void view(Element root, StringBuffer s) {
    s.append("<h4>Site map will be generated when published.</h4>\n");
  }

  public WebMarkupContainer edit(String wicketId, Element node, String path) {
    return new SiteMapPanel(wicketId);
  }

  public void publish(Element root, StringBuffer s) {
    genTree(getSite().getPages().iterator(), getSite().getPrefixUrl(), "/", "", s);
  }

  public List getSupportedOptions() {
    return options;
  }

  public void genTree(Iterator pages, String preHref, String postHref,
      String indent, StringBuffer s) {
    if (pages == null)
      return;
    while (pages.hasNext()) {
      Page node = (Page) pages.next();
      String file = node.getPublishedPath();

      if (!node.getHidden()) {
        s.append(indent + "<a href=\"" + preHref + file + postHref 
            + "\">" + node.getTitle() + "</a>");
        if (SHOW_TITLES.getBoolean(getDoc()) && node instanceof DocumentPage) {
          MetaData meta = ((DocumentPage) node).getXSMDocument().getMetadata();
          s.append(" \"" + meta.getTitle() +  "\"");
        }
        s.append("<br />\n");

        if (node instanceof HierarchicalPage)
          if (((HierarchicalPage) node).getSubPages().size() > 0)
            genTree(((HierarchicalPage) node).getSubPages().iterator(), preHref, postHref,
                indent + "&nbsp;- ", s);
      }
    }
  }

  class SiteMapPanel extends Panel {
    public SiteMapPanel(String wicketId) {
      super(wicketId);

      add(new Label("name", getName()));
    }
  }
}
