/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * Copyright 2018 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.aws;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;
import org.zeroturnaround.process.PidProcess;
import org.zeroturnaround.process.Processes;
import org.zeroturnaround.process.UnixProcess;

/**
 * Launches s3proxy in a separate JVM and creates shutdown hooks to gracefully kill the process.
 */
public class S3Proxy {
  private static String s3ProxyJar;
  private PidProcess s3ProxyProcess;

  static {
    /*
     * Find bender project version
     */
    final Properties properties = new Properties();
    try {
      properties.load(S3Proxy.class.getResourceAsStream("/project.properties"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String version = properties.getProperty("version");

    /*
     * Copy s3proxy shaded jar out of lib directory in this jar.
     */
    try {
      s3ProxyJar = LibExtracter.extract("lib/bender-shaded-s3proxy-" + version + ".jar");
    } catch (URISyntaxException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public S3Proxy() {}

  public void start(int port, String username, String pass)
      throws IOException, InterruptedException, InvalidExitValueException, TimeoutException {
    /*
     * Start s3proxy in a separate JVM
     */
    CountDownLatch ready = new CountDownLatch(1);
    Process process =
        new ProcessExecutor().command("java", "-cp", s3ProxyJar, "com.nextdoor.bender.s3proxy.Start",
            "" + port, username, pass).redirectOutput(new LogOutputStream() {
              @Override
              protected void processLine(String line) {
                System.out.println(line);

                /*
                 * Read output and set ready once s3proxy is fully started.
                 */
                if (line.contains("org.eclipse.jetty.server.Server - Started")) {
                  ready.countDown();
                }
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

    /*
     * JVM Shutdown handler
     */
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          ((UnixProcess) s3ProxyProcess).kill("TERM");
        } catch (IOException | InterruptedException e) {
          return;
        }
      }
    });

    /*
     * Wait until s3proxy is ready before returning.
     */
    ready.await(5, TimeUnit.SECONDS);
  }

  public void stop() throws IOException, InterruptedException, TimeoutException {
    if (this.s3ProxyProcess == null) {
      return;
    }

    try {
      ((UnixProcess) this.s3ProxyProcess).kill("TERM");
    } catch (IOException | InterruptedException e) {
      return;
    }
  }
}
