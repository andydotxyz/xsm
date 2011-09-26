package com.rectang.xsm.site;

import java.io.Serializable;

/**
 * Simple model for managing the visitor accounts for a site.
 *
 * @author Andrew Williams
 * @version $Id: Visitor.java 790 2009-04-14 21:39:47Z andy $
 * @since 2.0
 */
public class Visitor implements Serializable, Comparable {
  private String username, password;
  public Visitor(String username) {
    this(username, null);
  }

  public Visitor(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public boolean equals(Object o) {
    return o instanceof Visitor && equals((Visitor) o);
  }

  public boolean equals(Visitor v) {
    return v.getUsername().equals(username);
  }

  public int hashCode() {
    return username.hashCode();
  }

  public int compareTo(Object o) {
    if (!(o instanceof Visitor)) {
      return -1;
    }

    return username.compareTo(((Visitor) o).getUsername());
  }
}
