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
import com.nextdoor.bender.deserializer.FieldNotFoundException;
import com.nextdoor.bender.operation.EventOperation;
import com.nextdoor.bender.operation.OperationException;
import com.nextdoor.bender.operations.geo.GeoIpOperationConfig.GeoProperty;

public class GeoIpOperation implements EventOperation {
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
    String ipStr = null;

    /*
     * Get field containing an IP address
     */
    try {
      ipStr = ievent.getEventObj().getFieldAsString(this.pathToIpAddress);
    } catch (FieldNotFoundException e) {
      if (!this.required) {
        return ievent;
      }
      throw new OperationException("ip address field " + this.pathToIpAddress + " does not exist");
    }

    if (ipStr == null) {
      if (!this.required) {
        return ievent;
      }
      throw new OperationException("ip address field " + this.pathToIpAddress + " was null");
    }

    /*
     * Sometimes the field contains comma separated ip addresses (ie forwarded web requests). In
     * this case pick the first value in the list which is typically the user.
     */
    if (!ipStr.isEmpty() && ipStr.contains(",")) {
      ipStr = ipStr.split(",")[0];
    }

    InetAddress ipAddress = null;
    try {
      ipAddress = InetAddress.getByName(ipStr);
    } catch (UnknownHostException e) {
      if (!this.required) {
        return ievent;
      }
      throw new OperationException(e);
    }

    if (ipAddress == null) {
      if (!this.required) {
        return ievent;
      }
      throw new OperationException("ip address " + ipStr + " did not resolve");
    }

    CityResponse response = null;
    try {
      response = this.databaseReader.city(ipAddress);
    } catch (IOException | GeoIp2Exception e) {
      if (!this.required) {
        return ievent;
      }
      throw new OperationException(e);
    }

    HashMap<String, Object> geo = new HashMap<String, Object>(1);
    for (GeoProperty property : this.geoProperties) {
      switch (property) {
        case COUNTRY_NAME:
          if (response.getCountry() == null) {
            if (!this.required) {
              return ievent;
            }
            throw new OperationException("country returned null");
          }

          geo.put("country_name", response.getCountry().getName());
          break;
        case COUNTRY_ISO_CODE:
          if (response.getCountry() == null) {
            if (!this.required) {
              return ievent;
            }
            throw new OperationException("country returned null");
          }

          geo.put("country_iso_code", response.getCountry().getIsoCode());
          break;
        case SUBDIVISION_NAME:
          if (response.getMostSpecificSubdivision() == null) {
            if (!this.required) {
              return ievent;
            }
            throw new OperationException("MostSpecificSubdivision returned null");
          }

          geo.put("subdivision_name", response.getMostSpecificSubdivision().getName());
          break;
        case SUBDIVISION_ISO_CODE:
          if (response.getMostSpecificSubdivision() == null) {
            if (!this.required) {
              return ievent;
            }
            throw new OperationException("MostSpecificSubdivision returned null");
          }

          geo.put("subdivision_iso_code", response.getMostSpecificSubdivision().getIsoCode());
          break;
        case CITY_NAME:
          if (response.getCity() == null) {
            if (!this.required) {
              return ievent;
            }
            throw new OperationException("city returned null");
          }

          geo.put("city_name", response.getCity().getName());
          break;
        case POSTAL_CODE:
          if (response.getPostal() == null) {
            if (!this.required) {
              return ievent;
            }
            throw new OperationException("postal returned null");
          }

          geo.put("postal_code", response.getPostal().getCode());
          break;
        case LOCATION:
          if (response.getLocation() == null) {
            if (!this.required) {
              return ievent;
            }
            throw new OperationException("location returned null");
          }

          Double lat = response.getLocation().getLatitude();
          Double lon = response.getLocation().getLongitude();

          if (lat == null || lon == null) {
            if (!this.required) {
              return ievent;
            }
            throw new OperationException("error getting lat/lon");
          }

          HashMap<String, Object> location = new HashMap<String, Object>(2);
          location.put("lat", lat);
          location.put("lon", lon);
          geo.put("location", location);
          break;
      }
    }

    try {
      ievent.getEventObj().setField(this.destFieldName, geo);
    } catch (FieldNotFoundException e) {
      throw new OperationException(e);
    }

    return ievent;
  }
}
