package com.nextdoor.bender.ipc.http2;

public abstract class BaseHttp2TransportFactory extends AbstractHttp2TransportFactory {
  @Override
  public Class<Http2Transport> getChildClass() {
    return Http2Transport.class;
  }
}
