package com.rectang.xsm;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.io.Serializable;
import java.io.File;

import org.jdom.Element;

import com.rectang.xsm.io.RemoteDocument;
import com.rectang.xsm.site.Site;
import com.twmacinta.util.MD5;

public class UserData implements Serializable, Comparable {

  private String name;
  private String email;
  private String homepage;
  private String avatar;
  private String theme;
  private String htmlEditor;
  private String locale;
  private String note;
  
  private String username;
  private String password;
  private Site site;
  
  private Date lastLogin = null;
  private transient Date thisLogin = null, modified = null;
  private boolean firstLogin = false;
  private transient boolean xsmAdmin = false;
  private transient SimpleDateFormat formatter = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss ZZZZ");

  public UserData(String username, Site site) {
    this(username, site, true);
  }

  public UserData(String username, Site site, boolean login) {
    this.username = username;
    this.site = site; 
    load(login);
  }
  
  public boolean load(boolean login) {
    File possibleAdmin = getAdminFile(username);

    RemoteDocument userDoc;
    if (possibleAdmin.exists()) {
      userDoc = RemoteDocument.getDoc(possibleAdmin.getAbsolutePath());
      xsmAdmin = true;
    } else {
      userDoc = RemoteDocument.getDoc(site, "/members/" + username, true);
    }

    Element root = userDoc.getRootElement();
    modified = new Date(userDoc.getModifiedTime());
    if (root == null)
      return false;
      
    /* TODO non-null all of these I think (not just htmlEditor and lastLogin */
    name = root.getChildText("fullname");
    theme = root.getChildText("theme");
    htmlEditor = root.getChildText("htmlEditor");
    if (htmlEditor == null)
      htmlEditor = "";
    email = root.getChildText("email");
    homepage = root.getChildText("web");
    avatar = root.getChildText("pic");
    password = root.getChildText("password");
    locale = root.getChildText("locale");
    note = root.getChildText("note");
    if (note == null)
      note = "";

    /* if we call load again do not reload this */
    if (lastLogin == null) {
      try {
        lastLogin = formatter.parse(root.getChildText("lastlogin"));
      } catch (Exception e) {
        /* only call if we are loggin in */
        if (login) {
          lastLogin = new Date();
          firstLogin = true;
        }
      }
      /* only call if we are loggin in */
      if (login)
        thisLogin = new Date();
    }

    return true;
  }
  
  /**
   * @return Returns the avatar.
   */
  public String getAvatar() {
    return avatar;
  }
  /**
   * @param avatar The avatar to set.
   */
  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }
  /**
   * @return Returns the email.
   */
  public String getEmail() {
    return email;
  }
  /**
   * @param email The email to set.
   */
  public void setEmail(String email) {
    this.email = email;
  }
  /**
   * @return Returns the homepage.
   */
  public String getHomepage() {
    return homepage;
  }
  /**
   * @param homepage The homepage to set.
   */
  public void setHomepage(String homepage) {
    this.homepage = homepage;
  }
  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }
  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }
  /**
   * @return Returns the theme.
   */
  public String getTheme() {
    return theme;
  }
  /**
   * @param theme The theme to set.
   */
  public void setTheme(String theme) {
    this.theme = theme;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password The password to set.
   */
  public void setPassword(String password) {
    MD5 md5 = new MD5(password);
    this.password = md5.asHex();
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  /**
   * Get the current user's username
   * 
   * @return The current user's username.
   */
  public String getUsername() {
    return username;
  }

  /**
   * Get the time of the last login for this user.
   * If this is their first login it will equal (new Date()) at the
   * instantiation of this object.
   * 
   * @return The last login date of this user
   */
  public Date getLastLogin() {
    return lastLogin;
  }

  /**
   * Get the time that this user's data was last modified. If the user has
   * never logged in (getLastLogin() == null) this is the creation time of the
   * user data.
   * 
   * @return The time this user's data was last modified.
   */
  public Date getModified() {
    return modified;
  }

  /**
   * Has this user logged in before?
   * 
   * @return true if this is the first login, false otherwise
   */
  public boolean isFirstLogin() {
    return firstLogin;
  }

  public boolean save() {
    RemoteDocument userDoc;
    if (xsmAdmin) {
      userDoc = RemoteDocument.getDoc(getAdminFile(username).getAbsolutePath());
      xsmAdmin = true;
    } else {
      userDoc = RemoteDocument.getDoc(site, "/members/" + username, true);
    }

    try {
      Element userRoot = userDoc.getRootElement();
      if (userRoot == null) {
        Element newRoot = new Element("member");
        userDoc.setRootElement(newRoot);
        userRoot = newRoot;
      }
      userRoot.removeContent();
      userRoot.addContent(new Element("fullname").setText(name));
      userRoot.addContent(new Element("theme").setText(theme));
      userRoot.addContent(new Element("htmlEditor").setText(htmlEditor));
      userRoot.addContent(new Element("email").setText(email));
      userRoot.addContent(new Element("web").setText(homepage));
      userRoot.addContent(new Element("pic").setText(avatar));
      userRoot.addContent(new Element("password").setText(password));
      userRoot.addContent(new Element("locale").setText(locale));
      userRoot.addContent(new Element("note").setText(note));
      
      if (thisLogin != null)
        userRoot.addContent(new Element("lastlogin").setText(
            formatter.format(thisLogin)));
      return userDoc.save();
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public boolean isSiteAdmin() {
    return isUserSiteAdmin(getUsername(), site);
  }

  public void setSiteAdmin(boolean admin) {
    if (admin) {
      site.addAdmin(username);
    } else {
      site.delAdmin(username);
    }

    site.save();
  }

  public boolean isUserSiteAdmin(String username) {
    return isUserSiteAdmin(username, site);
  }

  public static boolean isUserSiteAdmin(String username, Site site) {
    return (site.getAdmins().contains(username)
        || isUserXSMAdmin(username));
  }

  public boolean isSiteEditor() {
    return isUserSiteEditor(getUsername(), site);
  }

  public void setSiteEditor(boolean editor) {
    if (editor) {
      site.addEditor(username);
    } else {
      site.delEditor(username);
    }

    site.save();
  }

  public boolean isUserSiteEditor(String username) {
    return isUserSiteEditor(username, site);
  }

  public static boolean isUserSiteEditor(String username, Site site) {
    return site.getEditors().contains(username);
  }

  /**
   * @return Returns the users chosen htmlEditor.
   */
  public String getHtmlEditor() {
    return htmlEditor;
  }

  /**
   * @param htmlEditor The htmlEditor to set.
   */
  public void setHtmlEditor(String htmlEditor) {
    this.htmlEditor = htmlEditor;
  }

  public boolean isXSMAdmin() {
    return xsmAdmin;
  }

  public static boolean isUserXSMAdmin(String username) {
    return getAdminFile(username).exists();
  }

  private static File getAdminFile(String username) {
    return new File(XSM.getConfig().getDataDir(), "admins/" + username + ".xml");
  }

  public Site getSite() {
    return site;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String newNote) {
    note = newNote;
  }

  public boolean equals(Object o) {
    return o instanceof UserData && equals((UserData) o);
  }

  public boolean equals(UserData data) {
    return data.getUsername().equals(getUsername())
        && data.getSite().equals(getSite());
  }

  public int hashCode() {
    return (getUsername() + getSite().getId()).hashCode();
  }

  public int compareTo(Object o) {
    if (!(o instanceof UserData))
      return -1;

    return username.toLowerCase().compareTo(((UserData) o).getUsername().toLowerCase());
  }
}
