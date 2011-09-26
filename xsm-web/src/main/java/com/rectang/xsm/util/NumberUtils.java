package com.rectang.xsm.util;

/**
 * A single place for lots of number parsing fun :)
 *
 * @author Andrew Williams
 * @version $Id: NumberUtils.java 781 2009-04-01 21:25:12Z andy $
 * @since 1.0
 */
public class NumberUtils {
  public static int parseInt(String string) {
    try {
      return Integer.parseInt(string);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  public static long parseLong(String string) {
    try {
      return Long.parseLong(string);
    } catch (NumberFormatException e) {
      return 0l;
    }
  }

  public static float parseFloat(String string) {
    try {
      return Float.parseFloat(string);
    } catch (NumberFormatException e) {
      return 0.0f;
    }
  }

  public static double parseDouble(String string) {
    try {
      return Double.parseDouble(string);
    } catch (NumberFormatException e) {
      return 0.0;
    }
  }
}
