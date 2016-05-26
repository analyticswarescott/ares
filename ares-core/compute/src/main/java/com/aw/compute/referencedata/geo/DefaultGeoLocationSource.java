package com.aw.compute.referencedata.geo;

public class DefaultGeoLocationSource implements GeoLocationSource {

	@Override
	public GeoLocation get(String key) {

		//TODO: integrate with a geolocation data source
		return GeoLocation.forLocation(1, 2);

	}

}
