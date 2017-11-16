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

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.aws.S3MockClientFactory;
import com.nextdoor.bender.operations.geo.GeoIpOperationConfig.GeoProperty;
import com.nextdoor.bender.operations.geo.GeoIpOperationTest.DummpyEvent;


public class GeoIpOperationFactoryTest {

  private S3MockClientFactory clientFactory;
  private AmazonS3Client client;
  private GeoIpOperationFactory opFactory;
  private static final String BUCKET = "mybucket";

  @Rule
  public TemporaryFolder tmpFolder = new TemporaryFolder();

  @Before
  public void setup() {
    /*
     * Patch the handler to use this test's factory which produces a mock client.
     */
    S3MockClientFactory f;
    try {
      f = new S3MockClientFactory(tmpFolder);
    } catch (Exception e) {
      throw new RuntimeException("unable to start s3proxy", e);
    }

    this.clientFactory = f;
    this.client = f.newInstance();

    /*
     * Populate S3
     */
    this.client.createBucket(BUCKET);
    InputStream is = this.getClass().getResourceAsStream("my-ip-data.mmdb");
    this.client.putObject(BUCKET, "my-ip-data.mmdb", is, new ObjectMetadata());

    this.opFactory = new GeoIpOperationFactory();
    this.opFactory.s3Factory = this.clientFactory;
  }

  @After
  public void teardown() {
    this.clientFactory.shutdown();
  }

  @Test
  public void testFactoryCreated() {
    GeoIpOperationConfig config = new GeoIpOperationConfig();
    config.setDstFieldName("test");
    config.setFailOnNotFound(false);
    config.setGeoLiteDb("s3://" + BUCKET + "/my-ip-data.mmdb");
    config.setSrcFieldName("ip_address");
    config.setGeoProperties(Arrays.asList(GeoProperty.COUNTRY_NAME, GeoProperty.COUNTRY_ISO_CODE,
        GeoProperty.SUBDIVISION_NAME, GeoProperty.SUBDIVISION_ISO_CODE, GeoProperty.CITY_NAME,
        GeoProperty.POSTAL_CODE, GeoProperty.LOCATION));
    this.opFactory.setConf(config);

    GeoIpOperation op = this.opFactory.newInstance();


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
    expected.put("test", expectedGeo);

    assertEquals(expected, ievent.getEventObj().getPayload());
  }
}
