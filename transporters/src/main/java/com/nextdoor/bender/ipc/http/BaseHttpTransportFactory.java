package com.nextdoor.bender.ipc.http;

public abstract class BaseHttpTransportFactory extends AbstractHttpTransportFactory {
  @Override
  public Class<HttpTransport> getChildClass() {
    return HttpTransport.class;
  }
}
