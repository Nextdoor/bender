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
 * Copyright 2017 Nextdoor.com, Inc
 *
 */

package com.nextdoor.bender.operations.geo;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.deserializer.DeserializedEvent;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operations.geo.GeoIpOperationConfig.GeoProperty;

public class GeoIpOperationTest {
  GeoIpOperation op;

  public static class DummpyEvent implements DeserializedEvent {
    public Map<String, Object> payload = new HashMap<String, Object>();

    @Override
    public Object getPayload() {
      return payload;
    }

    @Override
    public String getField(String fieldName) {
      Object o = payload.get(fieldName);
      if (o == null) {
        return null;
      }

      return o.toString();
    }

    @Override
    public void setPayload(Object object) {
      this.payload = (Map<String, Object>) object;
    }

    @Override
    public void setField(String fieldName, Object value) {
      payload.put(fieldName, value);
    }
  }

  public GeoIpOperation setup(List<GeoProperty> geoProperties, boolean required)
      throws IOException {
    InputStream is = this.getClass().getResourceAsStream("my-ip-data.mmdb");
    DatabaseReader dr = new DatabaseReader.Builder(is).build();
    return new GeoIpOperation("ip_address", "geo_ip", dr, geoProperties, required);
  }

  @Test
  public void testUnkownIpPass() throws Throwable {
    GeoIpOperation op = setup(Arrays.asList(GeoProperty.LOCATION), false);

    DummpyEvent devent = new DummpyEvent();
    devent.setField("ip_address", "10.0.0.1");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);
  }

  @Test(expected = AddressNotFoundException.class)
  public void testUnkownIpRequired() throws Throwable {
    GeoIpOperation op = setup(Arrays.asList(GeoProperty.LOCATION), true);

    DummpyEvent devent = new DummpyEvent();
    devent.setField("ip_address", "10.0.0.1");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    try {
      op.perform(ievent);
    } catch (OperationException e) {
      throw e.getCause();
    }
  }

  @Test
  public void testInvalidIp() throws Throwable {
    GeoIpOperation op = setup(Arrays.asList(GeoProperty.LOCATION), false);

    DummpyEvent devent = new DummpyEvent();
    devent.setField("ip_address", "noanip");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);

  }

  @Test(expected = UnknownHostException.class)
  public void testInvalidIpRequired() throws Throwable {
    GeoIpOperation op = setup(Arrays.asList(GeoProperty.LOCATION), true);

    DummpyEvent devent = new DummpyEvent();
    devent.setField("ip_address", "noanip");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    try {
      op.perform(ievent);
    } catch (OperationException e) {
      throw e.getCause();
    }
  }

  @Test(expected = OperationException.class)
  public void testNullIpRequired() throws Throwable {
    GeoIpOperation op = setup(Arrays.asList(GeoProperty.LOCATION), true);

    DummpyEvent devent = new DummpyEvent();
    devent.setField("ip_address", null);

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);
  }

  @Test(expected = OperationException.class)
  public void testMissingField() throws Throwable {
    GeoIpOperation op = setup(Arrays.asList(GeoProperty.LOCATION), true);

    DummpyEvent devent = spy(new DummpyEvent());
    when(devent.getField("ip_address")).thenThrow(new NoSuchElementException(""));

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);
  }

  @Test
  public void testNullIp() throws Throwable {
    GeoIpOperation op = setup(Arrays.asList(GeoProperty.LOCATION), false);

    DummpyEvent devent = new DummpyEvent();
    devent.setField("ip_address", null);

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);
  }

  @Test
  public void testKnownIpLocation() throws Throwable {
    GeoIpOperation op = setup(Arrays.asList(GeoProperty.LOCATION), true);

    DummpyEvent devent = new DummpyEvent();
    devent.setField("ip_address", "5.5.5.5");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);

    HashMap<String, Object> expected = new HashMap<String, Object>();
    expected.put("ip_address", "5.5.5.5");
    HashMap<String, Object> expectedLoc = new HashMap<String, Object>();
    expectedLoc.put("lat", new Double("51.75"));
    expectedLoc.put("lon", new Double("2.25"));

    Map<String, Object> expectedGeo = new HashMap<String, Object>();
    expectedGeo.put("location", expectedLoc);
    expected.put("geo_ip", expectedGeo);

    assertEquals(expected, ievent.getEventObj().getPayload());
  }

  @Test
  public void testAllKnownFields() throws Throwable {
    GeoIpOperation op = setup(Arrays.asList(GeoProperty.COUNTRY_NAME, GeoProperty.COUNTRY_ISO_CODE,
        GeoProperty.SUBDIVISION_NAME, GeoProperty.SUBDIVISION_ISO_CODE, GeoProperty.CITY_NAME,
        GeoProperty.POSTAL_CODE, GeoProperty.LOCATION), true);

    DummpyEvent devent = new DummpyEvent();
    devent.setField("ip_address", "5.5.5.5");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);

    HashMap<String, Object> expected = new HashMap<String, Object>();
    expected.put("ip_address", "5.5.5.5");
    HashMap<String, Object> expectedLoc = new HashMap<String, Object>();
    expectedLoc.put("lat", new Double("51.75"));
    expectedLoc.put("lon", new Double("2.25"));

    Map<String, Object> expectedGeo = new HashMap<String, Object>();
    expectedGeo.put("location", expectedLoc);
    expectedGeo.put("country_name", "Eriador");
    expectedGeo.put("country_iso_code", "ER");
    expectedGeo.put("subdivision_name", "Rivendell");
    expectedGeo.put("subdivision_iso_code", "ENG");
    expectedGeo.put("city_name", "Rivendell");
    expectedGeo.put("postal_code", "1234");
    expected.put("geo_ip", expectedGeo);

    assertEquals(expected, ievent.getEventObj().getPayload());
  }

  @Test
  public void testIpList() throws Throwable {
    GeoIpOperation op = setup(Arrays.asList(GeoProperty.LOCATION), true);

    DummpyEvent devent = new DummpyEvent();
    devent.setField("ip_address", "5.5.5.5, 10.10.10.10");

    InternalEvent ievent = new InternalEvent("", null, 0);
    ievent.setEventObj(devent);

    op.perform(ievent);

    HashMap<String, Object> expected = new HashMap<String, Object>();
    expected.put("ip_address", "5.5.5.5, 10.10.10.10");
    HashMap<String, Object> expectedLoc = new HashMap<String, Object>();
    expectedLoc.put("lat", new Double("51.75"));
    expectedLoc.put("lon", new Double("2.25"));

    Map<String, Object> expectedGeo = new HashMap<String, Object>();
    expectedGeo.put("location", expectedLoc);
    expected.put("geo_ip", expectedGeo);

    assertEquals(expected, ievent.getEventObj().getPayload());
  }
}
