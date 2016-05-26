package com.aw.compute.referencedata.geo;

/**
 * A location on earth
 *
 *
 *
 */
public class GeoLocation {

	private GeoLocation() {
	}

	private GeoLocation(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public static GeoLocation forLocation(double lat, double lon) {
		return new GeoLocation(lat, lon);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lon);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeoLocation other = (GeoLocation) obj;
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
			return false;
		if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
			return false;
		return true;
	}

	public double getLat() { return this.lat;  }
	private double lat;

	public double getLon() { return this.lon;  }
	private double lon;

}
