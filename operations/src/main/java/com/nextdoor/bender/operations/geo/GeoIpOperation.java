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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.nextdoor.bender.InternalEvent;
import com.nextdoor.bender.operation.Operation;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operations.geo.GeoIpOperationConfig.GeoProperty;

public class GeoIpOperation implements Operation {
  private final String pathToIpAddress;
  private final String destFieldName;
  private final DatabaseReader databaseReader;
  private final List<GeoProperty> geoProperties;
  private final boolean required;

  public GeoIpOperation(String pathToIpAddress, String destFieldName, DatabaseReader databaseReader,
      List<GeoProperty> geoProperties, boolean required) {
    this.databaseReader = databaseReader;
    this.geoProperties = geoProperties;
    this.pathToIpAddress = pathToIpAddress;
    this.destFieldName = destFieldName;
    this.required = required;
  }



  @Override
  public InternalEvent perform(InternalEvent ievent) {

    String ipStr = ievent.getEventObj().getField(pathToIpAddress);
    if (ipStr == null) {
      if (!required) {
        return ievent;
      }
      throw new OperationException("ip address field " + this.pathToIpAddress + " was null");
    }

    InetAddress ipAddress = null;
    try {
      ipAddress = InetAddress.getByName(ipStr);
    } catch (UnknownHostException e) {
      if (!required) {
        return ievent;
      }
      throw new OperationException(e);
    }

    CityResponse response = null;
    try {
      response = databaseReader.city(ipAddress);
    } catch (IOException | GeoIp2Exception e) {
      if (!required) {
        return ievent;
      }
      throw new OperationException(e);
    }

    HashMap<String, Object> geo = new HashMap<String, Object>(1);
    for (GeoProperty property : this.geoProperties) {
      switch (property) {
        case COUNTRY_NAME:
          geo.put("country_name", response.getCountry().getName());
          break;
        case COUNTRY_ISO_CODE:
          geo.put("country_iso_code", response.getCountry().getIsoCode());
          break;
        case SUBDIVISION_NAME:
          geo.put("subdivision_name", response.getMostSpecificSubdivision().getName());
          break;
        case SUBDIVISION_ISO_CODE:
          geo.put("subdivision_iso_code", response.getMostSpecificSubdivision().getIsoCode());
          break;
        case CITY_NAME:
          geo.put("city_name", response.getCity().getName());
          break;
        case POSTAL_CODE:
          geo.put("postal_code", response.getPostal().getCode());
          break;
        case LOCATION:
          HashMap<String, Object> location = new HashMap<String, Object>(2);
          location.put("lat", new Double(response.getLocation().getLatitude()));
          location.put("lon", new Double(response.getLocation().getLongitude()));
          geo.put("location", location);
          break;
      }
    }

    ievent.getEventObj().setField(this.destFieldName, geo);

    return ievent;
  }
}
