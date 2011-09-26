package com.rectang.xsm.io;

import java.io.*;
import java.util.Hashtable;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.rectang.xsm.XSM;

import com.rectang.xsm.site.Site;

public class FtpPublishedFile extends PublishedFile {

  private static int TIMEOUT = 60000;
  
  public FtpPublishedFile(Site site, String fileName) {
    super(site, fileName);
    
  }

  public OutputStream getOutputStream() throws IOException {
    File tmp = XSM.getTempFile();
      
    return new OutputStreamWrapper(tmp);
  }
  
  public String toString() {
    return "Ftp file: " + getSite().getRemoteHost() + ":" + file;
  }

  public boolean exists() {
    return exists(file);
  }

  private boolean exists(String f) {
    FTPClient ftpChannel = getFTPChannel(getSite());
    try {
      Object[] ret = ftpChannel.listNames(f);
      return ret != null && ret.length > 0; 
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean mkparentdirs() {
    String parent = (new File(file)).getParentFile().getPath();
    return mkdir(parent); /* this checks for existance */
  }

  public boolean mkdir() {
    return mkdir(file);
  }

  private boolean mkdir(String f) {
    if (exists(f))
      return true;
    FTPClient ftpChannel = getFTPChannel(getSite());
    try {
      File parentFile = (new File(f)).getParentFile();
      if (parentFile != null) {
        String parent = parentFile.getPath();
        mkdir(parent); /* this checks for existance */
      }

      return ftpChannel.makeDirectory(f);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public boolean delete() {
    return deleteDir(file);
  }
  
  private boolean delete(String rmFile) {
    FTPClient ftpChannel = getFTPChannel(getSite());
    try {
      ftpChannel.deleteFile(rmFile);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private boolean deleteDir(String path) {
    FTPClient ftpChannel = getFTPChannel(getSite());
    if (!exists(path))
      return true;

    String[] files = null;
    try {
      files = ftpChannel.listNames(path);
    } catch (Exception e) {
      /* fall through to deleting the path */
    }
    if (files != null && files.length != 0) {
      for (int i = 0; i < files.length; i++) {
        String next = files[i].trim();
        int pos = next.lastIndexOf(' ');

        String name = next.substring(pos + 1);
        if (name.equals(".") || name.equals(".."))
          continue;

        String subPath = path + "/" + name;
        if (isDirectory(subPath)) {
          deleteDir(subPath);
        } else {
          delete(subPath);
        }
      }
    }

    return delete(path);
  }

  public boolean isDirectory() {
    return isDirectory(file);
  }

  private boolean isDirectory(String path) {
    FTPClient ftpChannel = getFTPChannel(getSite());
    
    try {
      String[] list = ftpChannel.listNames(path);
      if (list == null || (list.length == 1  /* 1 entry for non-directory */
          && list[0].trim().endsWith( /* if 1 entry make sure it is a file */
              ((new File(path)).getParentFile().getName())))) {
        return false;
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public long length() {
    return length(file);
  }

  private long length(String path) {
    FTPClient ftpChannel = getFTPChannel(getSite());

    try {
      FTPFile[] list = ftpChannel.listFiles(path);
      if (list == null || (list.length < 1)) {
        return 0;
      }
      return list[0].getSize();
    } catch (Exception e) {
      return 0;
    }
  }

  public boolean rename(String newName) {
    FTPClient ftpChannel = getFTPChannel(getSite());
    try {
      String oldPath = file;
      setFile(newName);
      
      mkparentdirs();
      ftpChannel.rename(oldPath, file);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  public void uploadFile(File f) {
    uploadFile(f, true);
  }

  public void uploadFile(File f, boolean delete) {
    try {
      if (!f.exists())
        return;

      mkparentdirs();
      FTPClient ftpChannel = getFTPChannel(getSite());
      try {
        FileInputStream fis = new FileInputStream(f);
        ftpChannel.storeFile(file, fis);
        fis.close();
        
        if (delete)
          f.delete();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } catch (Exception io) {
      io.printStackTrace();
    }
  }

  class OutputStreamWrapper extends FileOutputStream {
    private File f;
    public OutputStreamWrapper(File f) throws IOException {
      super(f);
      this.f = f;
    }
    
    public void close() throws IOException {
      super.close();
      uploadFile(f, true);
    }
  }

  private static Hashtable channelHash = new Hashtable();
  public static FTPClient getFTPChannel(Site site) {
    TimeoutChannel chan = (TimeoutChannel) channelHash.get(site);
    
    if (chan != null) {
      if (chan.timeout < System.currentTimeMillis()) {
        try {
          chan.channel.disconnect();
          chan.channel.quit();
        } catch (Exception e) {
          /* we ignore these */
        }
        chan = null;
      }
    }
    if (chan == null) {
      try {
        FTPClient cli = new FTPClient();
        cli.connect(site.getRemoteHost());
        cli.login(site.getRemoteUser(), site.getRemotePassword());
        cli.enterLocalPassiveMode();
        
        cli.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        chan = new TimeoutChannel(cli, System.currentTimeMillis() + TIMEOUT);

        channelHash.put(site, chan);
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }
    return chan.channel;
  }

  private static class TimeoutChannel {
    protected long timeout;
    protected FTPClient channel;
    
    public TimeoutChannel(FTPClient chan, long time) {
      this.channel = chan;
      this.timeout = time;
    }
  }

}