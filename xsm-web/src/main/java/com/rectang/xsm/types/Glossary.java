package com.rectang.xsm.types;

import java.util.List;
import java.util.Comparator;

import com.rectang.xsm.XSM;
import com.rectang.xsm.doc.DocElement;
import com.rectang.xsm.doc.DocGroup;
import com.rectang.xsm.doc.DocList;

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

public class Glossary extends DocGroup {

  static QSortAlgorithm sort;

  public Glossary(java.lang.String name) {
    super(name, new GlossaryItem("item"));

    sort = new QSortAlgorithm(compare);
  }

  public WebMarkupContainer edit(String wicketId, Element node, String path) {
    return new GlossaryPanel(wicketId, node, path);
  }

  public void publish(Element node, StringBuffer s) {
    List children = new java.util.Vector(node.getChildren("item"));
    node.removeChildren("item");
    sort.sort(children);
    node.addContent(children);

    super.publish(node, s);
  }

  static Comparator compare = new Comparator() {

    public int compare(Object o1, Object o2) {
      String o1term = ((Element) o1).getChildText("term");
      String o2term = ((Element) o2).getChildText("term");

      return String.CASE_INSENSITIVE_ORDER.compare(o1term, o2term);
    }    
  };

  class GlossaryPanel extends Panel {
    public GlossaryPanel(final String wicketId, final Element node, final String path) {
      super(wicketId);
      add(new Label("name", getName()));

      List children = node.getChildren(element.getName());
      add(new ListView("elements", children) {
        protected void populateItem(ListItem listItem) {
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
  
class GlossaryItem extends DocList {

  public GlossaryItem(String name) {
    super(name, new DocElement[] {
        new com.rectang.xsm.widget.String("term"),
        new com.rectang.xsm.widget.String("full-term"),
        new com.rectang.xsm.widget.TextArea("definition")
    });
  }

  public void view(Element root, StringBuffer s) {
    s.append("<p class=\"xsm_glossary_term\">");
    elements[0].view(root.getChild("term"), s);

    String full = root.getChildText("full-term");
    if (full != null && !full.equals("")) {
      s.append(" -- (");
      elements[1].view(root.getChild("full-term"), s);
      s.append(")");
    }
    s.append("<br /><blockquote>");
    elements[2].view(root.getChild("definition"), s);
    s.append("</blockquote></p>\n");
  }

  public void publish(Element root, StringBuffer s) {
    s.append("<p class=\"xsm_glossary_term\">");
    elements[0].view(root.getChild("term"), s);

    String full = root.getChildText("full-term");
    if (full != null && !full.equals("")) {
      s.append(" -- (");
      elements[1].view(root.getChild("full-term"), s);
      s.append(")");
    }
    s.append("</p><blockquote class=\"xsm_glossary_definition\">");
    elements[2].view(root.getChild("definition"), s);
    s.append("</blockquote>\n");
  }

}

/*
 * @(#)QSortAlgorithm.java  1.6f 95/01/31 James Gosling
 *
 * Copyright (c) 1994-1995 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
 * without fee is hereby granted. 
 * Please refer to the file http://java.sun.com/copy_trademarks.html
 * for further important copyright and trademark information and to
 * http://java.sun.com/licensing.html for further important licensing
 * information for the Java (tm) Technology.
 * 
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * 
 * THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
 * CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
 * PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
 * NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
 * SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
 * SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
 * PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES").  SUN
 * SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
 * HIGH RISK ACTIVITIES.
 */

/**
 * A quick sort demonstration algorithm
 * SortAlgorithm.java, Thu Oct 27 10:32:35 1994
 *
 * @author James Gosling
 * @version     1.6f, 31 Jan 1995
 */
/**
 * 19 Feb 1996: Fixed to avoid infinite loop discoved by Paul Haeberli.
 *              Misbehaviour expressed when the pivot element was not unique.
 *              -Jason Harrison
 *
 * 21 Jun 1996: Modified code based on comments from Paul Haeberli, and
 *              Peter Schweizer (Peter.Schweizer@mni.fh-giessen.de).  
 *              Used Daeron Meyer's (daeron@geom.umn.edu) code for the
 *              new pivoting code. - Jason Harrison
 *
 * 09 Jan 1998: Another set of bug fixes by Thomas Everth (everth@wave.co.nz)
 *              and John Brzustowski (jbrzusto@gpu.srv.ualberta.ca).
 * 
 * 05 Jan 2006: Adapted by Andrew Williams (andy@hndyande.co.uk) to modify List
 *              objects using a Comparator instead of int arrays comparing ints.
 */

class QSortAlgorithm {

  private Comparator comp;

  public QSortAlgorithm(Comparator c) {
    this.comp = c;
  }

  void sort(List a, int lo0, int hi0) {
    int lo = lo0;
    int hi = hi0;
    if (lo >= hi) {
      return;
    } else if( lo == hi - 1 ) {
      /*
       *  sort a two element list by swapping if necessary 
       */
      if (comp.compare(a.get(lo), a.get(hi)) > 0) {
        Object T = a.get(lo);
        a.set(lo, a.get(hi));
        a.set(hi, T);
      }
      return;
    }

    /*
     *  Pick a pivot and move it out of the way
     */
    Object pivot = a.get((lo + hi) / 2);

    a.set((lo + hi) / 2, a.get(hi));
    a.set(hi, pivot);

    while( lo < hi ) {
      /*
       *  Search forward from a[lo] until an element is found that
       *  is greater than the pivot or lo >= hi 
       */
      while (comp.compare(a.get(lo), pivot) <= 0 && lo < hi) {
        lo++;
      }

      /*
       *  Search backward from a[hi] until element is found that
       *  is less than the pivot, or lo >= hi
       */
      while (comp.compare(pivot, a.get(hi)) <= 0 && lo < hi ) {
        hi--;
      }

      /*
       *  Swap elements a[lo] and a[hi]
       */
      if( lo < hi ) {
        Object T = a.get(lo);
        a.set(lo, a.get(hi));
        a.set(hi, T);
      }
    }

    /*
     *  Put the median in the "center" of the list
     */
    a.set(hi0, a.get(hi));
    a.set(hi, pivot);

    /*
     *  Recursive calls, elements a[lo0] to a[lo-1] are less than or
     *  equal to pivot, elements a[hi+1] to a[hi0] are greater than
     *  pivot.
     */
    sort(a, lo0, lo-1);
    sort(a, hi+1, hi0);
  }

  void sort(List a) {
    sort(a, 0, a.size() - 1);
  }
}
