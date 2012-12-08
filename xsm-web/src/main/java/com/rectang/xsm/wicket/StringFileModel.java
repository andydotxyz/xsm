package com.rectang.xsm.wicket;

import org.apache.wicket.model.Model;
import org.apache.wicket.Session;

import java.io.*;

import org.codehaus.plexus.util.IOUtil;

/**
 * A wicket model that uses a file as the store.
 *
 * @author Andrew Williams
 * @version $Id: StringFileModel.java 674 2007-10-09 15:22:19Z aje $
 * @since 1.0
 */
public class StringFileModel extends Model {
  private File backing;

  public StringFileModel(File backing) {
    this.backing = backing;
  }

  public void setObject(Serializable object) {
    String content = (String) object;

    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(backing));
      writer.write(content);
    } catch (IOException e) {
      Session.get().error(e.getMessage());
    } finally {
      IOUtil.close(writer);
    }
  }

  public String getObject() {
    StringBuffer content = new StringBuffer();

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(backing));

      String line = reader.readLine();
      while (line != null) {
        content.append(line);
        content.append('\n');

        line = reader.readLine();
      }
    } catch (IOException e) {
      Session.get().error(e.getMessage());
    } finally {
      IOUtil.close(reader);
    }

    return content.toString();
  }
}
