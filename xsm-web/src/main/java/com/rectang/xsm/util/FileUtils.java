package com.rectang.xsm.util;

/**
 * TODO Document me!
 *
 * @author Andrew Williams
 * @version $Id: FileUtils.java 782 2009-04-01 21:31:53Z andy $
 * @since 1.0
 */
public class FileUtils {
  public static String getImageThumbnailName(String image) {
    if (image == null || image.equals("")) return "";
    return image.replace('.','_') + "_thumb.jpeg";
  }

  public static String getImagePreviewName(String image) {
    if (image == null || image.equals("")) return "";
    return image.replace('.','_') + "_preview.jpeg";
  }
}
