package com.rectang.xsm.util;

import com.rectang.xsm.doc.DocElement;
import org.jdom.Element;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id: RenderUtils.java 731 2008-09-01 19:08:40Z aje $
 * @since 1.0
 */
public class RenderUtils {
  public static String publish(DocElement type, Element node) {
    StringBuffer ret = new StringBuffer();
    type.element.publish( node, ret );
    return ret.toString();
  }
}

