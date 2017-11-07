package org.jpdu.handlers;


import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jpdu.pdu.PDUImpl;
import org.jpdu.pdu.SmsSubmitBulder;


public class DirectoryOutboundHandler extends AbstractMessageEmitter {

  private static final String LOCKFILE_NAME = ".lock_read";
  
  private String storagePath = null;
  private FileSystem defaultFileSystem = null;
  private Path lockPath = null;

  public DirectoryOutboundHandler(String name) {
    super(name);
  }

  public String getStoragePath() {
    return storagePath;
  }

  public void setStoragePath(String value) {
    this.storagePath = value;
  }
  
  @Override
  public void run() {
    defaultFileSystem = FileSystems.getDefault();
    lockPath = defaultFileSystem.getPath(storagePath, LOCKFILE_NAME);
    Set<StandardOpenOption> lockOptions = new HashSet<>();
    lockOptions.add(StandardOpenOption.WRITE);
    lockOptions.add(StandardOpenOption.CREATE_NEW);
    lockOptions.add(StandardOpenOption.DELETE_ON_CLOSE);
    try {
      try (FileChannel storageLockChannel = defaultFileSystem.provider().newFileChannel(lockPath, lockOptions)) {
        try(FileLock lock = storageLockChannel.lock()) {
          try(WatchService watcher = defaultFileSystem.newWatchService()) {
            Path path = defaultFileSystem.getPath(storagePath);
            WatchKey key = path.register(watcher,  StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.OVERFLOW);
            handleWatchEvents(key);
            key.reset();
            while(!Thread.currentThread().isInterrupted() && lock.isValid()) {
              try {
                key = watcher.take();
                handleWatchEvents(key);
                key.reset();
              } catch(InterruptedException e) {
                System.out.println("DirectoryOutboundHandler thread interrupted during wait, exiting");
                return;
              }
            }
            System.out.println("DirectoryOutboundHandler thread interrupted, exiting");
          }
        }
      }
    } catch (IOException ex) {
      System.out.println("DirectoryOutboundHandler IOException, exiting. " + ex.getMessage());
    }
  }

  private void handleWatchEvents(WatchKey key) throws IOException {
    Path path = (Path)key.watchable();
    List<WatchEvent<?>> events =  key.pollEvents();
    for (WatchEvent<?> event : events) {
      if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
        Path fileName = (Path)event.context();
        Path file = path.resolve(fileName);
        handleFile(file);
      } else {
        System.out.println("Event: " + event.kind().name());
      }
    }
    DirectoryStream<Path> stream = Files.newDirectoryStream(path); // TODO: provide extension
    for (Path file : stream) {
      if (!file.equals(this.lockPath)) {
        handleFile(file);
      }      
    }
  }

  private void handleFile(Path file) {
    System.out.println("New inbound file: " + file.toString());
    try {
      byte[] octets = Files.readAllBytes(file);
      String text = new String(octets, "UTF-8");
      SmsSubmitBulder builder = new SmsSubmitBulder();
      builder.setRecipient("79043008412");
      builder.setText(text);
      for (PDUImpl pdu : builder.build()) {
        if (emitMessage(pdu)) {
          try {
            Files.delete(file);
          } catch(IOException e) {
            System.out.println("Unable delete file: " + file.toString());
          }
        }
      }
    } catch(IOException e) {
      System.out.println("Unable to read file: " + file.toString());
    }
  }

}
