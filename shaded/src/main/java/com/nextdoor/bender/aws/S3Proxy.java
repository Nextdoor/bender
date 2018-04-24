package com.nextdoor.bender.aws;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;
import org.zeroturnaround.process.PidProcess;
import org.zeroturnaround.process.Processes;
import org.zeroturnaround.process.UnixProcess;

public class S3Proxy {
  private final String s3ProxyJar;
  private PidProcess s3ProxyProcess;

  public S3Proxy() {
    try {
      s3ProxyJar = LibExtracter.extract("lib/bender-shaded-s3proxy-1.0.0-SNAPSHOT.jar");
    } catch (URISyntaxException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void start()
      throws IOException, InterruptedException, InvalidExitValueException, TimeoutException {
    /*
     * Start s3proxy in a separate jvm
     */
    Process process =
        new ProcessExecutor().command("java", "-cp", s3ProxyJar, "com.nextdoor.bender.aws.Start")
            .redirectOutput(new LogOutputStream() {
              @Override
              protected void processLine(String line) {
                System.out.println(line);
              }
            }).start().getProcess();
    this.s3ProxyProcess = Processes.newPidProcess(process);

    /*
     * Keepalive
     */
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          if (s3ProxyProcess != null) {
            s3ProxyProcess.waitFor();
          }
        } catch (InterruptedException e) {
          return;
        }
      }
    });
    t.start();

    Thread.sleep(1000);

    /*
     * JVM Shutdown handler
     */
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        System.out.println("shtudown hook");
        try {
          ((UnixProcess) s3ProxyProcess).kill("TERM");
        } catch (IOException | InterruptedException e) {
          return;
        }
      }
    });
  }

  public void stop() throws IOException, InterruptedException, TimeoutException {
    if (this.s3ProxyProcess == null) {
      return;
    }

    try {
      ((UnixProcess) s3ProxyProcess).kill("TERM");
    } catch (IOException | InterruptedException e) {
      return;
    }
  }
}
