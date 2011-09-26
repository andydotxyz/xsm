package com.rectang.xsm.doc;

import com.rectang.xsm.XSM;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import org.jdom.Element;

import com.rectang.xsm.io.RemoteDocument;
import com.rectang.xsm.site.Site;

//import org.jdom.Element;

/**
 * Type class for storing information about the various page templates in XSM. Useful accessors to the
 * entire list as well as the singleton accessor to each type.
 * 
 * @author aje
 */
public class Type implements Serializable {

  private static final String FILE = "config/types.xml";
  private static boolean loaded = false;

  private static List types = new LinkedList();
  private static Map typeMap = new HashMap();

  private String name, className, description;
  private boolean hidden;
  private List requires;

  /**
   * Construct a type object - internal only, represents one element from the types.xml file.
   * 
   * @param name        The type's name
   * @param className   The type's class
   * @param description The type's description
   * @param hidden      Whether the type is hidden
   * @param require     What technologies the type requires on the server
   */
  private Type(String name, String className, String description, boolean hidden, String require) {
    this.name = name;
    this.description = description;
    this.className = className;
    this.hidden = hidden;
    this.requires = com.rectang.xsm.util.StringUtils.stringToList(require);
  }

  /**
   * Load the types.xml into the list of types we need.
   */
  private static synchronized void load() {
    RemoteDocument doc = RemoteDocument.getDoc(
        (new File(XSM.getConfig().getRootDir(), FILE)).getPath());

    Iterator typeNodes = doc.getRootElement().getChildren("type").iterator();
    while (typeNodes.hasNext()) {
      Element typeNode = (Element) typeNodes.next();

      String name = typeNode.getAttributeValue("name");
      String hiddenStr = typeNode.getAttributeValue("hidden");
      boolean hidden = hiddenStr != null && !hiddenStr.equals("") &&
          Boolean.valueOf(hiddenStr).equals(Boolean.TRUE);
      Type type = new Type(name, typeNode.getAttributeValue("class"),
          typeNode.getChildText("description"), hidden, typeNode.getChildText("requires"));
      types.add(type);
      typeMap.put(name, type);
    }

    loaded = true;
  }

  /**
   * Get the name of this type (unique identifier)
   * 
   * @return The type's name
   */
  public String getName() {
    return name;
  }

  /**
   * Get the class for this type (must be in the classpath)
   * 
   * @return The type's class
   */
  public String getClassName() {
    return className;
  }

  /**
   * Get the description for this type
   * 
   * @return The type's description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Is this type a hidden type?
   * 
   * @return Whether this type should be hidden
   */
  public boolean isHidden() {
    return hidden;
  }

  /**
   * Is this the default type? (The one selected by default in the interfaces)
   * 
   * @return Should this be selected by default for type selection etc
   */
  public boolean isDefault() {
    return name.equals("html");
  }

  /**
   * Get the list of requirements for this type. The iteration is through
   * Strings matching those in Site.getTechnologies().
   * 
   * @return The requirements list for this type
   */
  public Iterator getRequirements() {
    return requires.iterator();
  }

  /**
   * Determine whether this type is supported by the site being passed.
   * This checks all Strings in getRequirements against those in
   * Site.getTechnologies(). If any requirement is not met we return false,
   * otherwise we return true.
   * 
   * @param site the site to test against using the getTechnologies() method
   * @return True if all requirements of this Type are met by the Site, false
   *         otherwise
   */
  public boolean isSupportedBy(Site site) {
    boolean ret = true;

    List provides = site.getTechnologies(); 
    Iterator requirements = getRequirements();
    while (requirements.hasNext()) {
      String requirement = (String) requirements.next();
      if (!provides.contains(requirement)) {
        ret = false;
        break;
      }
    }
    return ret;
  }

  /**
   * Get the list of all types present in types.xml
   * 
   * @return an Iterator of Types, representing all Types available
   */
  public static List /* Type */ listTypes() {
    if (!loaded)
      load();

    return types;
  }

  /**
   * Get a specific Type identified by <code>name</code>
   * 
   * @param name The identifier of the Type to load
   * @return The loaded Type, if it exists, null otherwise
   */
  public static Type getType(String name) {
    if (!loaded)
      load();

    return (Type) typeMap.get(name);
  }
}
