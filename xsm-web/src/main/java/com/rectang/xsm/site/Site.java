/*
 * Created on Dec 10, 2004
 *
 */
package com.rectang.xsm.site;

import java.util.*;
import java.io.*;

import com.rectang.xsm.util.StreamGobbler;
import org.jdom.*;

import com.rectang.xsm.*;
import com.rectang.xsm.io.*;
import com.rectang.xsm.util.StringUtils;

import org.headsupdev.support.java.IOUtil;


/**
 * @author aje
 *
 * Maintain a cache of the site heirarchy and configuration.
 */
public class Site implements Serializable {

  private static final int VERSION = 11;

  private int version = 1; // all sites more recent than version 1 are tagged
  private String stylesheet, layout, index, id, news, login;
  private String title, description, keywords;
  private int type;
  private boolean register;
  private long mtime = 0, connMtime = 0;
  private List admins, editors, technologies;
  private String rootDir, rootUrl, prefixUrl;
  private HierarchicalPage rootPage;

  private long quota;
  private String quotaIncludes;

  private String remoteUser, remotePassword, remoteHost;
  private List visitors;

  public static final int LOCAL = 1;
  public static final int SSH = 2;
  public static final int FTP = 3;

  public Site(String site) {
    if (site == null || site.equals("")
        || !getSiteList().contains(site.toLowerCase()))
      return;
    id = site.toLowerCase();

    touch();
  }
  
  private void loadConnection() {
    RemoteDocument doc = RemoteDocument.getDoc(this, "/connection", true);
    connMtime = doc.getModifiedTime();

    try {
      Element root = doc.getRootElement();
      String typeStr = root.getAttributeValue("type");
      Element optional;
      if (typeStr != null && typeStr.equals("ssh")
          && ((optional = root.getChild("ssh")) != null)) {
        type = SSH;
        remoteUser = optional.getChildText("username");
        remoteHost = optional.getChildText("host");

      } else if (typeStr != null && typeStr.equals("ftp")
          && ((optional = root.getChild("ftp")) != null)) {
        type = FTP;
        remoteUser = optional.getChildText("username");
        remotePassword = optional.getChildText("password");
        remoteHost = optional.getChildText("host");

      } else
        type = LOCAL;
      rootDir = root.getChildText("rootDir");
      if (rootDir == null)
        throw new Exception("Corrupt site file - no rootDir"); 
      rootUrl = root.getChildText("rootUrl");
      if (rootUrl == null)
        throw new Exception("Corrupt site file - no rootUrl");       
      prefixUrl = root.getChildText("prefixUrl");
      if (prefixUrl == null)
        prefixUrl = ""; 

      String techString = root.getAttributeValue("provides");
      technologies = StringUtils.stringToList(techString);

      String quotaStr = root.getChildText("quota");
      if (quotaStr == null)
        quotaStr = "0";
      quota = Long.parseLong(quotaStr);
      quotaIncludes = root.getChildText("quotaIncludes");
    } catch (Exception e) {
      /* FIXME cannot error here */
      e.printStackTrace();
    }
  }

  private void touchConnection() {
    long newTime = (new File( RemoteDocument.calculateFileName(this, "/connection", true))).lastModified();
    if (newTime > connMtime)
      loadConnection();
  }

  private void load() {
    RemoteDocument doc = RemoteDocument.getDoc(this, "/site", true);
    mtime = doc.getModifiedTime();

    try {
      Element root = doc.getRootElement();
      try {
        version = Integer.parseInt(root.getAttributeValue("version"));
      } catch (NumberFormatException e) {
        /* use default site version (1) */
      }
      admins = StringUtils.stringToList(root.getAttributeValue("admin"));
      editors = StringUtils.stringToList(root.getAttributeValue("edit"));

      news = root.getAttributeValue("newsSource");
      if (news == null)
        news = "";
      login = root.getAttributeValue("login");
      if (login == null)
        login = "";
      register = notNull(root.getAttributeValue("register")).toLowerCase().equals("true");

      title = notNull(root.getChildText("title"));
      description = notNull(root.getChildText("description"));
      keywords = notNull(root.getChildText("keywords"));
      stylesheet = notNull(root.getChildText("stylesheet"));
      layout = notNull(root.getChildText("layout"));

      Element pages = root.getChild("pages");
      index = pages.getAttributeValue("default");
      this.rootPage = new HierarchicalPage(this, null, "/", false);
      rootPage.addSubPages(initPages(pages.getChildren(), rootPage));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void touch() {
    touchConnection();

    long newTime = (new File( RemoteDocument.calculateFileName(this, "/site", true))).lastModified();
    if (newTime > mtime)
      load();
  }

  public boolean save() {
    try {
      RemoteDocument doc = RemoteDocument.getDoc(this, "/site", true);
      Element site = new Element("xsmsite");
      site.setAttribute("version", "" + version);

      site.setAttribute("admin", StringUtils.listToString(getAdmins()));
      site.setAttribute("edit", StringUtils.listToString(getEditors()));
      site.setAttribute("login", login);
      site.setAttribute("register", String.valueOf(register));

      site.setAttribute("newsSource", news);
      site.addContent(new Element("title").setText(this.getTitle()));
      site.addContent(new Element("description").setText(this.getDescription()));
      site.addContent(new Element("keywords").setText(this.getKeywords()));
      site.addContent(new Element("stylesheet").setText(this.getStylesheet()));
      site.addContent(new Element("layout").setText(this.getLayout()));

      Element pages = new Element("pages");
      pages.addContent(savePages(this.getPages()));
      pages.setAttribute("default", this.getDefault());
      site.addContent(pages);
      doc.setRootElement(site);
      
      boolean ret = doc.save();
      /* TODO - do we really need this to avoid what seems to be an mtime
       * race condition */
      if (ret)
        load();
      return ret;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean publishTheme() {
    getPublishedDoc("_theme").mkdir();

    if (getStylesheet().equals("custom")) {
      File style = new File(XSM.getConfig().getSiteTemplateDir(this), "style.css");
      getPublishedDoc("_theme/style.css").uploadFile(style, false);
    } else {
      InputStream style = null;
      OutputStream out = null;
      try {
        style = getClass().getClassLoader().getResourceAsStream(
            "/com/rectang/xsm/publish/style/" + getStylesheet() + ".css");
        out = getPublishedDoc("_theme/style.css").getOutputStream();

        IOUtil.copyStream(style, out);
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      } finally {
        IOUtil.close(style);
        IOUtil.close(out);
      }
    }

    if (getLayout().equals("custom")) {
      File style = new File(XSM.getConfig().getSiteTemplateDir(this), "layout.css");
      getPublishedDoc("_theme/layout.css").uploadFile(style, false);
    } else {
      InputStream style = null;
      OutputStream out = null;
      try {
        style = getClass().getClassLoader().getResourceAsStream(
            "/com/rectang/xsm/publish/layout/" + getLayout() + ".css");
        out = getPublishedDoc("_theme/layout.css").getOutputStream();

        IOUtil.copyStream(style, out);
      } catch (IOException e) {
        e.printStackTrace();
        return false;
      } finally {
        IOUtil.close(style);
        IOUtil.close(out);
      }
    }

    return true;
  }

  public Map publish(UserData user) {
    Map results = new HashMap();
    results.put(" _theme", Boolean.valueOf(publishTheme()));
    publish_files(getPages(), user, results);

    return results;
  }

  private static void publish_files(List pageList, UserData user, Map results) {
    Iterator pages = pageList.iterator();
    while (pages.hasNext()) {
      Page next = (Page) pages.next();
      if (!next.isPublishable()) {
        continue;
      }

      Boolean ok = Boolean.FALSE;
      try {
        if (next.publish(user)) {
          ok = Boolean.TRUE;
        }
      } catch (Throwable e) {
        // TODO report this in the return...
//        ok = "ERROR " + e.getMessage();
        e.printStackTrace();
      }

      results.put(next.getPath(), ok);

      if (next instanceof HierarchicalPage)
        publish_files(((HierarchicalPage) next).getSubPages(), user, results);
    }
  }

  private Vector initPages(List pages, HierarchicalPage parent) {
    if (pages.size() == 0)
      return null;
    Vector ret = new Vector();
    Iterator list = pages.iterator();
    while (list.hasNext()) {
      Element next = (Element) list.next();
      String hiddenStr = next.getAttributeValue("hidden");
      boolean hidden = hiddenStr != null && hiddenStr.equals("true");


      Page newPage;
      if (next.getName().equals("page")) {

        newPage = new DocumentPage(this, parent, next.getChild("title").getText(),
            hidden);
      } else if (next.getName().equals("link")) {
        newPage = new LinkPage(this, parent, next.getChildText("title"), hidden);
        ((LinkPage) newPage).setLink(next.getAttributeValue("url"));
      } else if (next.getName().equals("title")) {
        continue; // ignore title elements, we are just interested in other nodes
      } else {
        System.err.println("Unknown Page type " + next.getName());
        continue;
      }

      if (newPage instanceof HierarchicalPage)
        ((HierarchicalPage) newPage).addSubPages(initPages(next.getChildren(),
            (HierarchicalPage) newPage));

      newPage.setSlug(next.getAttributeValue("slug"));
      ret.add(newPage);
    }
    return ret;
  }
  
  private List savePages(List pageList) {
    Iterator list = pageList.iterator();
    if (!list.hasNext())
      return null;
    ArrayList ret = new ArrayList();
    while (list.hasNext()) {
      Page next = (Page) list.next();

      Element newPage = null;
      if (next instanceof DocumentPage) {
        newPage = new Element("page");
      } else if (next instanceof LinkPage) {
        newPage = new Element("link");
        newPage.setAttribute("url", next.getLink());
      } else {
        System.err.println("Unable to save reference to page " + next.getTitle());
        continue;
      }
      newPage.setAttribute("hidden", String.valueOf(next.getHidden()));
      newPage.addContent(new Element("title").setText(next.getTitle()));
      if (next instanceof HierarchicalPage)
        newPage.addContent(savePages(((HierarchicalPage) next).getSubPages()));

      if (!next.getFile().equals(next.getSlug())) {
        newPage.setAttribute("slug", next.getSlug());
      }
      ret.add(newPage);
    }
    return ret;
  }

  public boolean exists() {
    return id != null;
  }

  private static String notNull(String in) {
    if (in == null)
      return "";
    return in;
  }

  public int getVersion() {
    return version;
  }

  public static int getCurrentVersion() {
    return VERSION;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public boolean needsUpgrade() {
    return getCurrentVersion() > getVersion();
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  /**
   * Get the type of site we are referencing. Currenly LOCAL or SSH.
   * @return the site type;
   */
  public int getType() {
    return type;
  }

  public String getStylesheet() {
    return stylesheet;
  }
  
  public String getLayout() {
    return layout;
  }
  
  public String getDefault() {
    return index;
  }
  
  public List getPages() {
    if (rootPage == null)
      return null;
    return rootPage.getSubPages();
  }
  
  public HierarchicalPage getRootPage() {
    return rootPage;
  }
  
  public Iterator getAllPages() {
    Vector pages = new Vector();
    
    addPages(pages, getRootPage().getSubPages());
    return pages.iterator();
  }

  private void addPages(List pages, List pageList) {
    Iterator addIter = pageList.iterator();

    while (addIter.hasNext()) {
      Page next = (Page) addIter.next();
      pages.add(next);

      if (next instanceof HierarchicalPage)
        addPages(pages, ((HierarchicalPage) next).getSubPages());
    }
  }

  public Page getPage(String path) {
    Page ret = this.rootPage;
    if (path == null || path.equals("") || path.equals("/"))
      return ret;
    
    String[] split = path.split("/");
    String searchPath = "";
    for (int i = 0; i < split.length; i++) {
      if (split[i].equals(""))
        continue;

      boolean found = false;
      if (ret instanceof HierarchicalPage) {
        searchPath += "/" + split[i];
        Iterator pages = ((HierarchicalPage) ret).getSubPages().iterator();
        while (pages.hasNext() && !found) {
          Page node = (Page) pages.next();
        
          if (node.getPath().equals(searchPath)) {
            ret = node;
            found = true;
          }
        }
      }
      if (!found)
        return null;
    }
    return ret;
  }  

  /**
   * Return a list of provided technologies for this server (php, apache e.g.).
   * The Objects are simply Strings.
   * 
   * @return Returns the site's technology list
   */
  public List getTechnologies() {
    return technologies;
  }

  /**
   * @return Returns the admin list
   */
  public List getAdmins() {
    return admins;
  }

  /**
   * @param admin The admin to add
   */
  public void addAdmin(String admin) {
    this.admins.add(admin);
  }

  /**
   * @param admin The admin to remove
   */
  public void delAdmin(String admin) {
    if (this.admins.contains(admin))
      this.admins.remove(admin);
  }

  /**
   * @return Returns the site's global editor list
   */
  public List getEditors() {
    return editors;
  }

  /**
   * @param editor The site's global editor to add
   */
  public void addEditor(String editor) {
    this.editors.add(editor);
  }

  /**
   * @param editor The site's global editor to remove
   */
  public void delEditor(String editor) {
    if (this.editors.contains(editor))
      this.editors.remove(editor);
  }

  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @param layout The layout to set.
   */
  public void setLayout(String layout) {
    this.layout = layout;
  }
  /**
   * @param stylesheet The stylesheet to set.
   */
  public void setStylesheet(String stylesheet) {
    this.stylesheet = stylesheet;
  }
  
  public void setDefault(String def) {
    index = def;
  }
  /**
   * @return Returns the rootDir.
   */
  public String getRootDir() {
    return rootDir;
  }
  
  public String getUniqueID() {
    return id;
  }
  
  /**
   * @return Returns the rootUrl.
   */
  public String getRootUrl() {
    return rootUrl;
  }

  /**
   * @return Returns the prefix url.
   */
  public String getPrefixUrl() {
    return prefixUrl;
  }

  public PublishedFile getPublishedDoc(String f) {
    if (type == SSH)
      return new SshPublishedFile(this, f);
    if (type == FTP) 
      return new FtpPublishedFile(this, f);
    return new LocalPublishedFile(this, f);
  }

  /**
   * @return Returns the page for the sites news source.
   */
  public String getNewsSource() {
    return news;
  }

  /**
   * The link to the login page (if one exists) if left blank will generate a
   * URL to the XSM instance.
   * 
   * @return Returns the page for the sites login screen.
   */
  public String getLogin() {
    return login;
  }

  /**
   * @param news The page for the sites news source to set.
   */
  public void setNewsSource(String news) {
    this.news = news;
  }

  /**
   * Return whether or not this site allows registration.
   *
   * @return if this site allows registrations
   */
  public boolean canRegister() {
    return register;
  }

  /**
   * @param canRegister whether or not we wish new users to register themselves
   */
  public void setCanRegister(boolean canRegister) {
    this.register = canRegister();
  }

  /**
   * @return Returns the remoteHost (if applicable).
   */
  public String getRemoteHost() {
    return remoteHost;
  }

  /**
   * @return Returns the remoteUser (if applicable).
   */
  public String getRemoteUser() {
    return remoteUser;
  }

  /**
   * @return Returns the remotePassword (if applicable).
   */
  public String getRemotePassword() {
    return remotePassword;
  }

  /**
   * @return Returns the description.
   */
  public String getDescription() {
    return description;
  }
  /**
   * @param description The description to set.
   */
  public void setDescription(String description) {
    this.description = description;
  }
  /**
   * @return Returns the keywords.
   */
  public String getKeywords() {
    return keywords;
  }
  /**
   * @param keywords The keywords to set.
   */
  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public long getQuota() {
    return quota;
  }

  public String getQuotaIncludes() {
    return quotaIncludes;
  }

  public static Vector getSiteList() {
    File[] list = new File(XSM.getConfig().getDataDir()).listFiles();

    Vector ret = new Vector();
    if (list != null) {
      for (int i = 0; i < list.length; i++) {
        if (new File(list[i], "site.xml").exists()) {
          ret.add(list[i].getName());
        }
      }
    }
    
    return ret;
  }

  public boolean movePageTo(Page source, String newLoc) {
    Page newParent = getPage((new File(newLoc)).getParent());
    Page oldParent = source.getParent();

    if (!(oldParent instanceof HierarchicalPage) ||
        !(newParent instanceof HierarchicalPage))
      return false;

    String newTitle = (new File(newLoc)).getName();
    if (!newTitle.equals(source.getTitle()))
      source.setTitle(newTitle);
    return ((HierarchicalPage) oldParent).removeSubPage(source) &&
        ((HierarchicalPage) newParent).addSubPage(source);
  }

  public boolean equals(Site site) {
    return site.getId().equals(getId());
  }

  public RemoteDocument getVisitorsFile() {
    return RemoteDocument.getDoc(this, "/htpasswd", false);
  }

  private void loadVisitors() {
    List ret = new LinkedList();

    File file = getVisitorsFile();
    if (file.exists()) {
      BufferedReader in = null;
      try {
        in = new BufferedReader(new FileReader(file));
        String line;
        while((line = in.readLine()) != null) {
          String username = line.substring(0, line.indexOf(':'));
          if (RemoteDocument.getDoc(this, "/members/" + username, true).exists()) {
            // this is a full account
            continue;
          }

          ret.add(new Visitor(username));
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            // ignore
          }
        }
      }
    }

    visitors = ret;
  }

  public List getVisitors() {
    if (visitors == null) {
      loadVisitors();
    }
    return visitors;
  }

  public Visitor getVisitor(String username) {
    Iterator iter = getVisitors().iterator();
    while (iter.hasNext()) {
      Visitor next = (Visitor) iter.next();
      if (next.getUsername().equals(username)) {
        return next;
      }
    }

    return null;
  }

  public void setVisitor(Visitor visitor) {
    String create = "";
    if (getVisitors().size() == 0) {
      create = "c";
    }

    Process p = null;
    try {
      p = Runtime.getRuntime().exec("htpasswd -mb" + create + " " + getVisitorsFile().getPath() + " " +
          visitor.getUsername() + " " + visitor.getPassword());
      p.waitFor();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      if (p != null) {
        try {
          p.getInputStream().close();
        } catch (Exception e) {}
        try {
          p.getErrorStream().close();
        } catch (Exception e) {}
        try {
          p.getOutputStream().close();
        } catch (Exception e) {}
      }
    }

    loadVisitors();
  }

  public void removeVisitor(Visitor visitor) {
    Process p = null;
    try {
      p = Runtime.getRuntime().exec("htpasswd -D " + getVisitorsFile().getPath() + " " + visitor.getUsername());
      p.waitFor();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      if (p != null) {
        try {
          p.getInputStream().close();
        } catch (Exception e) {}
       try {
          p.getErrorStream().close();
        } catch (Exception e) {}
        try {
          p.getOutputStream().close();
        } catch (Exception e) {}
      }
    }

    loadVisitors();
  }

  public long calculateSpaceUsage() {
    String folders = new File(XSM.getConfig().getDataDir(), getId()).getAbsolutePath();
    if (getType() == Site.LOCAL) {
      folders += " " + getRootDir();
    }
    if (getQuotaIncludes() != null) {
      folders += " " + getQuotaIncludes();
    }

    long used = 0;
    Process process = null;
    StreamGobbler out, err;
    try {
      process = Runtime.getRuntime().exec("du -cm " + folders);

      out = new StreamGobbler(process.getInputStream());
      err = new StreamGobbler(process.getErrorStream());

      out.start();
      err.start();
      process.waitFor();

      // check that our gobblers are finished...
      while ( !err.isComplete() || !out.isComplete() ) {
        try {
         Thread.sleep( 1000 );
        } catch (InterruptedException e) {
          // we were just trying to tidy up...
        }
      }

      String quotaLine = out.getLastLine();

      if (quotaLine != null) {
        String[] parts = quotaLine.trim().split("\t");
        if (parts.length >= 1) {
          used = Long.parseLong(parts[0]);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    } finally {
      try {
        process.getInputStream().close();
      } catch (Exception e) {}
      try {
        process.getOutputStream().close();
      } catch (Exception e) {}
      try {
        process.getErrorStream().close();
      } catch (Exception e) {}
    }

    return used;
  }
}
