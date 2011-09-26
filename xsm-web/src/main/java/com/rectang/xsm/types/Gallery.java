package com.rectang.xsm.types;

import java.util.*;

import org.jdom.Element;

import com.rectang.xsm.doc.DocElement;
import com.rectang.xsm.doc.DocGroup;
import com.rectang.xsm.doc.DocList;
import com.rectang.xsm.doc.SupportedOption;
import com.rectang.xsm.io.PublishedFile;
import com.rectang.xsm.io.XSMDocument;

import com.rectang.xsm.widget.PreviewedImage;

import com.rectang.xsm.site.*;
import com.rectang.xsm.util.FileUtils;

public class Gallery extends DocGroup {

  public static final SupportedOption THUMB_ROWS = new SupportedOption("THUMB_ROWS",
      "The number of rows on each thumbnail page", 3);
  public static final SupportedOption THUMB_COLS = new SupportedOption("THUMB_COLS",
      "The number of columns on each thumbnail page", 3);
  public static final SupportedOption NAV_POSITION = new SupportedOption("NAV_POSITION",
      "Display gallery navigation at the top, bottom or both", "both");
  private Vector options;

  public Gallery(java.lang.String name) {
    super(name, new GalleryItem("image"));

    options = new Vector();
    options.add(THUMB_ROWS);
    options.add(THUMB_COLS);
    options.add(NAV_POSITION);
  }

  /* FIXME - add a save + delete methods to remove the old preview pages if needed */

  /* TODO maybe tidy this up a tad ??? */
  private class VectorPair {
    public Vector pages, elements;

    public VectorPair(Vector a, Vector b) {
      this.pages = a;
      this.elements = b;
    }
  }

  private VectorPair getSubGalleries() {
    Vector pages = new Vector();
    Vector elements = new Vector();

    Iterator subPages = getPage().getSubPages().iterator();
    if (subPages == null || !subPages.hasNext())
      return new VectorPair(pages, elements);

    while (subPages.hasNext()) {
      Page page = (Page) subPages.next();
      if (!(page instanceof DocumentPage))
        continue;

      DocumentPage next = (DocumentPage) page;

      /* do not thumbnail hidden pages */
      if (next.getHidden())
        continue;

      try {
        Element elem = next.getXSMDocument().getContentElement();
        if (elem != null && elem.getName().equals("gallery")) {
          pages.add(next);
          elements.add(elem.getChild("image"));
        }
      } catch (Exception e) {
        /* don't add */
        e.printStackTrace();
      }
    }

    return new VectorPair(pages, elements);
  }

  public void view(Element node, StringBuffer s) {
    int galleryIndex = 0;
    VectorPair galleries = getSubGalleries();
    Iterator subGals = galleries.pages.iterator();
    while (subGals.hasNext()) {
      DocumentPage next = (DocumentPage) subGals.next();

      if (next != null) {
        s.append("<img src=\"");
        s.append(getSite().getRootUrl());
        s.append(next.getPath());
        s.append(java.io.File.separatorChar);
        s.append("_images");
        s.append(java.io.File.separatorChar);
        s.append(FileUtils.getImageThumbnailName(path((Element) galleries.elements.get(galleryIndex))));
        s.append("\" /><br />Gallery: ");
        s.append(next.getTitle());
        s.append("<br />\n<br />\n");
      } else {
        s.append("No images in gallery \"" + next.getTitle() + "\"<br />\n<br />\n");
      }
      galleryIndex++;
    }

    s.append("</p>\n");
    super.view(node, s);
  }

  public void publish(Element node, StringBuffer s) {
    List children = node.getChildren(element.getName());
    Iterator allChildren = children.iterator();
    VectorPair pair = getSubGalleries();
    Vector galleryPages = pair.pages;
    Vector galleryElements = pair.elements;
    Iterator subGalleries = galleryElements.iterator();

    String previewDir = getPreviewPath("");
    (getSite().getPublishedDoc(previewDir)).mkdir();

    int thumb_rows = THUMB_ROWS.getInteger(getDoc());
    int thumb_cols = THUMB_COLS.getInteger(getDoc());
    int thumb_cells = thumb_cols * thumb_rows;

    int counter = 0;
    int pageCounter = 0;
    int totalImages = children.size();
    int total = totalImages + galleryPages.size();
    int totalPages = (((total - (total % (thumb_cells))) / (thumb_cells)));
    if ((total % (thumb_cells)) != 0)
      totalPages++;
    
    PublishedFile file = null;
    StringBuffer content = new StringBuffer();
    StringBuffer page = new StringBuffer();
    Element before = null;
    int galleries = galleryPages.size();

    boolean hasNext = subGalleries.hasNext() || allChildren.hasNext();
    Element after = null;
    if (hasNext)
      if (subGalleries.hasNext())
        after = (Element) subGalleries.next();
      else
        after = (Element) allChildren.next();

    int galleryIndex = 0;
    Element[] nexts = new Element[thumb_cols];
    while (hasNext) {
      nexts[0] = after;
      for (int i = 1; i < thumb_cols; i++) {
        nexts[i] = null;
        if (subGalleries.hasNext())
          nexts[i] = (Element) subGalleries.next();
        else if (allChildren.hasNext())
          nexts[i] = (Element) allChildren.next();
      }

      hasNext = subGalleries.hasNext() || allChildren.hasNext();
      if (hasNext) {
        if (subGalleries.hasNext())
          after = (Element) subGalleries.next();
        else
          after = (Element) allChildren.next();
      } else
        after = null;

      if (counter % (thumb_cells) == 0) {
        page = new StringBuffer();
        page.append("<center>\n");
        page.append("<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\"");
        page.append(" width=\"100%\">\n");
      }
      page.append("<tr>\n");
      
      /* pics */
      for (int i = 0; i < thumb_cols; i++)
        generateImage(nexts[i], previewDir, (counter + i) < galleries,
            (DocumentPage) getVectorId(galleryPages, galleryIndex + i), page);
      page.append("</tr><tr>\n");
      
      /* captions */
      for (int i = 0; i < thumb_cols; i++)
        generateCaption(nexts[i], (counter + i) < galleries,
            (DocumentPage) getVectorId(galleryPages, galleryIndex + i), page);
      page.append("</tr>\n");
      
      page.append("<tr><td colspan=\"" + thumb_cols + "\">&nbsp;</td></tr>\n");

      /* preview pages */
      for (int i = 0; i < thumb_cols; i++)
        if ((counter + i) >= galleries) {
          Element prev = before;
          if (i > 0)
            prev = nexts[i - 1];
          Element next = after;
          if (i < thumb_cols - 1)
            next = nexts[i + 1];

            generatePreviewPage(nexts[i], prev, next, (counter + i) - galleries, totalImages, previewDir,
              pageCounter);
        }

      before = nexts[thumb_cols - 1];
      counter += thumb_cols;
      galleryIndex += thumb_cols;

      if ((counter / thumb_cols) % thumb_rows == 0 || !hasNext) {
        page.append("</table>\n</center>\n");

        String navPos = NAV_POSITION.getString(getDoc());
        if (navPos.equals("top") || navPos.equals("both"))
          publishThumbNav(pageCounter, hasNext, totalPages, content);
        content.append(page);
        if (navPos.equals("bottom") || navPos.equals("both"))
          publishThumbNav(pageCounter, hasNext, totalPages, content);
        if (pageCounter == 0)
          s.append(content);
        else
          getDoc().publishContent(file, content.toString(), getUser());
        file = getSite().getPublishedDoc(getPath() + "/_thumbs/"
            + ++pageCounter + ".html");
        content = new StringBuffer();
      }
    }
    if (content.length() != 0)
      getDoc().publishContent(file, content.toString(), getUser());

    String parentPath = (new java.io.File(getPath())).getParent();
    if (!parentPath.equals("/")) {
      HierarchicalPage parent = (HierarchicalPage) getSite().getPage(parentPath);

      if (parent instanceof DocumentPage) {
        XSMDocument parentDoc = ((DocumentPage) parent).getXSMDocument();
        if (parentDoc != null &&
            parentDoc.getContentElement().getName().equals("gallery")) {
          parentDoc.publish(getUser());
        }
      }
    }
  }

  private void publishThumbNav(int counter, boolean hasNext, int total,
      StringBuffer s) {
    String pStr = "";
    String nStr = "";
    if (counter > 0)
      if (counter == 1)
        pStr = "<a href=\"" + getSite().getPrefixUrl() + getPath() + "/index.html\">Previous</a>";
      else
        pStr = "<a href=\"" + getSite().getPrefixUrl() + getPath() + "/_thumbs/" + (counter - 1)
            + ".html\">Previous</a>";
    if (hasNext)
      nStr = "<a href=\"" + getSite().getPrefixUrl() + getPath() + "/_thumbs/" + (counter + 1) + ".html\">Next</a>";

    s.append("<p class=\"xsm_gallery_nav\">\n");
    s.append("<span class=\"xsm_gallery_nav_prev\">" + pStr + "</span>\n");
    s.append("<span class=\"xsm_gallery_nav_next\">" + nStr);
    s.append("</span>\n");
    s.append("<span class=\"xsm_gallery_nav_curr\">");
    s.append((counter + 1) + " of " + total + "</span>\n");
    s.append("</p>\n");
  }

  private Object getVectorId(Vector v, int id) {
    if (id >= v.size())
      return null;
    return v.get(id);
  }

  private void generateImage(Element node, String dir, boolean gallery,
      DocumentPage subGallery, StringBuffer s) {
    /* FIXME - rounding errors */
    int percent = 100 / THUMB_COLS.getInteger(getDoc());
    if (node == null) {
      s.append("<td class=\"xsm_gallery_thumb\" width=\"" + percent + "%\">&nbsp;</td>");
      return;
    }
    s.append("<td class=\"xsm_gallery_thumb\" align=\"center\" width=\"" + percent + "%\">");
    if (gallery) {
      s.append("<a href=\"" + getSite().getPrefixUrl());
      s.append(subGallery.getPath() + "/index.html\">");
    }
    else
      s.append(previewLink(node, dir));
    s.append("<img src=\"" + getSite().getPrefixUrl());
    if (gallery) {
      if (node == null)
        s.append("(no image)");
      else {
        s.append(subGallery.getPath() + java.io.File.separatorChar + "_images");
        s.append(java.io.File.separatorChar);
        s.append(FileUtils.getImageThumbnailName(path(node)));
      }
    } else
      s.append(com.rectang.xsm.widget.Image.getPath(this,
          FileUtils.getImageThumbnailName(path(node))));
    s.append("\" border=\"0\"/></a>\n</td>\n");
  }
  
  private String previewLink(Element node, String dir) {
    return "<a href=\"" + getSite().getPrefixUrl() + dir
        + path(node) + ".html\">";
  }
  
  private void generateCaption(Element node, boolean gallery, DocumentPage subGallery,
      StringBuffer s) {
    /* FIXME - rounding errors */
    int percent = 100 / THUMB_COLS.getInteger(getDoc());
    if (node == null) {
      s.append("<td class=\"xsm_gallery_caption\" width=\"" + percent + "%\">&nbsp;</td>");
      return;
    }
    s.append("<td class=\"xsm_gallery_caption\" width=\"" + percent + "%\">");
    s.append("<p>");
    if (gallery) {
      if (node != null) {
        s.append("Gallery: <a href=\"" + getSite().getPrefixUrl());
        s.append(subGallery.getPath() + "/index.html\">");
        s.append(subGallery.getTitle() + "</a>");
      }
    } else
      s.append(caption(node));
    int comments = node.getChild("comments").getChildren("comment").size();
    if (comments > 0)
      s.append("<br />" + comments + " comment" + ((comments != 1)?"s":""));
    s.append("</p></td>\n");
  }
  
  private void generatePreviewPage(Element node, Element before, Element after,
      int pos, int tot, String dir, int page) {
    if (node == null)
      return;

    PublishedFile out = getSite().getPublishedDoc(dir + path(node) + ".html");
    boolean full = PreviewedImage.KEEP_ORIG.getBoolean(getDoc());

    StringBuffer content = new StringBuffer();
    content.append("<div class=\"xsm_gallery_preview\">\n");

    String navPos = NAV_POSITION.getString(getDoc());
    if (navPos.equals("top") || navPos.equals("both"))
      publishPreviewNav(pos, tot, before, after, dir, content);
    if (full) {
      content.append("<a href=\"" + getSite().getPrefixUrl());
      content.append(getPath() + java.io.File.separatorChar + "_images");
      content.append(java.io.File.separatorChar + path(node) + "\" target=\"_blank\">");
    }
    content.append("<img border=\"0\"src=\"");
    content.append(getSite().getPrefixUrl());
    content.append(com.rectang.xsm.widget.Image.getPath(this,
            FileUtils.getImagePreviewName(path(node))) + "\">");
    if (full)
      content.append("</a>");
    content.append("\n<p>" + caption(node) + "</p>\n");

    if (navPos.equals("bottom") || navPos.equals("both"))
      publishPreviewNav(pos, tot, before, after, dir, content);
    content.append("<p class=\"xsm_gallery_nav_up\">");
    if (page == 0) {
      content.append("<a href=\"" + getSite().getPrefixUrl());
      content.append(getPath() + "/index.html\">Up</a>");
    } else {
      content.append("<a href=\"" + getSite().getPrefixUrl());
      content.append(getPath() + "/_thumbs/" + page + ".html\">Up</a>");
    }
    content.append("</p></div>\n");

    Element comments = node.getChild("comments");
    int commentCount = comments.getChildren("comment").size();
    if (commentCount > 0) {
      content.append("<p><b>Comments:</b></p>");
      ((GalleryItem) element).elements[2].publish(comments, content);
    }
    getDoc().publishContent(out, content.toString(), getUser());
  }

  private void publishPreviewNav(int pos, int tot, Element before,
      Element after, String dir, StringBuffer s) {
    String prev = "&nbsp;";
    if (pos > 0)
      prev = previewLink(before, dir) + "Previous</a>";
    String next = "&nbsp;";
    if (pos < tot -1)
      next = previewLink(after, dir) + "Next</a>";

    s.append("<p class=\"xsm_gallery_nav\">\n");
    s.append("<span class=\"xsm_gallery_nav_prev\">");
    s.append(prev + "</span>\n");
    s.append("<span class=\"xsm_gallery_nav_next\">");
    s.append(next + "</span>\n");
    s.append("<span class=\"xsm_gallery_nav_curr\">");
    s.append((pos + 1) + " of " + tot + "</span>\n");
    s.append("</p>\n");
  }

  private String path(Element node) {
    if (node == null)
      return "";
    return node.getChild("path").getValue();
  }

  private String caption(Element node) {
    if (node == null)
      return "";
    return node.getChild("caption").getValue();
  }

  protected java.lang.String getPreviewPath(java.lang.String name) {
    return getPath() + java.io.File.separatorChar + "_previews"
        + java.io.File.separatorChar + name;
  }

  public List getSupportedOptions() {
    Vector ret = new Vector();
    ret.addAll(options);
    ret.addAll(element.getSupportedOptions());
    return ret;
  }
}

class GalleryItem extends DocList {

  public GalleryItem(String name) {
    super(name, new DocElement[] {
        new GalleryPreviewedImage("path"),
        new com.rectang.xsm.widget.String("caption"),
        new GalleryCommentList("comments")
    });
  }

}

class GalleryPreviewedImage extends PreviewedImage {

  public GalleryPreviewedImage(java.lang.String name) {
    super(name);
  }

  public void destroy(Element node) {
    getSite().getPublishedDoc(getPath() + java.io.File.separatorChar + "_previews" + java.io.File.separatorChar
        + node.getText() + ".html").delete();
    super.destroy(node);
    
    /* FIXME here we need to trigger Gallery to check that it still needs all
     * of its thumbnail pages */
  }
}

class GalleryCommentList extends DocGroup {
  
  public GalleryCommentList(String name) {
    super(name, new GalleryComment("comment"));
  }

  public void publish(Element node, StringBuffer s) {
    Iterator children = node.getChildren("comment").iterator();
    while (children.hasNext()) {
      Element next = (Element) children.next();
      element.publish(next, s);
    }
  }
}

class GalleryComment extends DocList {
  
  public GalleryComment(String name) {
    super(name, new DocElement[] {
        new com.rectang.xsm.widget.TextArea("body"),
        new com.rectang.xsm.widget.Value("author", com.rectang.xsm.widget.Value.FULLNAME),
        new com.rectang.xsm.widget.Value("time", com.rectang.xsm.widget.Value.DATE)});
  }

  public void publish(Element node, StringBuffer s) {
    s.append("<p><b>");
    elements[1].publish(node.getChild("author"), s);
    s.append("</b> (");
    elements[2].publish(node.getChild("time"), s);
    s.append(")<br />");
    elements[0].publish(node.getChild("body"), s);
    s.append("</p>");
  }
}
