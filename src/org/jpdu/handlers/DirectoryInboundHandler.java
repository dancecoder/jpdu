package org.jpdu.handlers;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import org.jpdu.Convert;

import org.jpdu.pdu.PDUImpl;
import org.jpdu.pdu.TimeStamp;
import org.jpdu.pdu.WrongMessageTypeException;


public class DirectoryInboundHandler extends AbstractMessageHandler implements MessageAcceptor {

  private static final String CHARSET = "UTF-8";
  private static final String LOCKFILE_NAME = ".lock_write";
  private static final String PDUFILE_EXTENSION = ".pdu";
  
  private String storagePath = null;
  private FileLock storageLock = null;
  private FileSystem defaultFileSystem = null;
  private Charset charset = null;

  public DirectoryInboundHandler(String name) {
    super(name);
  }

  public String getStoragePath() {
    return storagePath;
  }

  public void setStoragePath(String value) {
    this.storagePath = value;
  }

  @Override
  public Result acceptMessage(PDUImpl message) {
    synchronized(this) {
      Result result;
      if (isStorageReady()) {
        try {
          writeMessage(message);
          result = Result.Accepted;
        } catch(IOException e) {
          System.out.print(e);
          result = Result.Failed;
        }        
      } else {
        result = Result.NotReady;
      }
      this.notify();
      return result;
    }
  }

  @Override
  public void run() {
    charset = Charset.forName(CHARSET);
    defaultFileSystem = FileSystems.getDefault();    
    Path lockPath = defaultFileSystem.getPath(storagePath, LOCKFILE_NAME);
    Set<StandardOpenOption> lockOptions = new HashSet<>();
    lockOptions.add(StandardOpenOption.WRITE);
    lockOptions.add(StandardOpenOption.CREATE_NEW);
    lockOptions.add(StandardOpenOption.DELETE_ON_CLOSE);
    try {
      try (FileChannel storageLockChannel = defaultFileSystem.provider().newFileChannel(lockPath, lockOptions))
      {
        try(FileLock lock = storageLockChannel.lock()) {
          this.storageLock = lock;
          while(!Thread.currentThread().isInterrupted()) {
            try {
              synchronized(this) {
                this.wait();
              }
            } catch(InterruptedException e) {
              System.out.println("DirectoryInboundHandler thread interrupted during wait, exiting");
              return;
            }
          }
          System.out.println("DirectoryInboundHandler thread interrupted, exiting");
        }
      }
    } catch (IOException ex) {
      System.out.println("DirectoryInboundHandler IOException, exiting. " + ex.getMessage());
    }
  }

  private void writeMessage(PDUImpl message) throws IOException {
    int counter = -1;
    String timestamp = Long.toString(System.currentTimeMillis());
    Path filepath;
    do {
      counter++;
      filepath = defaultFileSystem.getPath(storagePath, timestamp + Integer.toString(counter, 16) + PDUFILE_EXTENSION);
    } while (Files.exists(filepath));

    StringBuilder sb = new StringBuilder();
    sb.append("pdu: ");
    sb.append(Convert.bytesToHexString(message.getBytes()));
    sb.append('\n');
    try {
      String text = message.getUserData().getUserDataText();
      sb.append("text: ");
      sb.append(text);
      sb.append('\n');
    } catch (WrongMessageTypeException e) {
      // ignore
    }
    try {
      String orig = message.getOriginatingAddress().getNumber();
      sb.append("originator: ");
      sb.append(orig);
      sb.append('\n');
    } catch(WrongMessageTypeException e) {
      // ignore
    }

    try {
      TimeStamp ts = message.getServiceCentreTimeStamp();
      sb.append("sctimestamp: ");
      sb.append(ts.toString());
      sb.append('\n');
    } catch(WrongMessageTypeException e) {
      // ignore
    }
    
    byte[] octets = sb.toString().getBytes(charset);
    Files.write(filepath, octets, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);    
  }

  private boolean isStorageReady() {
    return storageLock != null && storageLock.isValid();
  }

}
