package com.rectang.xsm.io;

import java.io.*;

import com.rectang.xsm.site.Site;

public abstract class PublishedFile {

  protected String file;

  private Site site;

  public PublishedFile(Site site, String fileName) {
    this.site = site;
    setFile(fileName);
  }

  protected void setFile(String fileName) {
    if (fileName.charAt(0) != File.separatorChar)
      fileName = File.separatorChar + fileName;
    file = site.getRootDir() + fileName;
  }

  /**
   * This is the only reference to the underlying file, as there is no use in getting it's path etc, as
   * it may be remote to us.
   * @return The underlying files name
   */
  public String getFileName() {
    return (new File(file)).getName();
  }

  public Site getSite() {
    return site;
  }

  public abstract OutputStream getOutputStream() throws IOException;
  
  public abstract String toString();

  public abstract boolean exists();
  public abstract boolean isDirectory();
  public abstract long length();

  // FIXME do we ever need to recurively make dirs - surely we only ever need 1?
  public abstract boolean mkparentdirs();

  public abstract boolean mkdir();

//  public abstract File[] listFiles();

  public abstract boolean delete();
  public abstract boolean rename(String newName);

//  public abstract long getModifiedTime();

  /** as uploadFile(f, true); */
  public abstract void uploadFile(File f);
  
  public abstract void uploadFile(File f, boolean delete);

  public void moveFile(File file) {
    file.renameTo(new File(this.file));
  }
}
