package com.aw.compute.referencedata.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

import com.aw.common.util.SetTimeSource;
import com.aw.util.ListMap;

public class GeoLocationLookupDataTest {

	@Test
	public void test() {

		TestSource source = new TestSource();

		SetTimeSource time = new SetTimeSource(Instant.now());

		//create a small test map
		GeoLocationLookupData data = new GeoLocationLookupData(time, source, Duration.ofMinutes(1), 2);

		GeoLocation loc1 = data.get("1.2.3.4");
		GeoLocation loc2 = data.get("2.3.4.5");
		GeoLocation loc3 = data.get("1.2.3.4"); //should not result in a call

		assertEquals(2, data.size());
		assertEquals(2, source.gets.size());
		assertTrue(source.gets.keySet().contains("1.2.3.4"));
		assertTrue(source.gets.keySet().contains("2.3.4.5"));
		assertEquals(1, source.gets.get("1.2.3.4").size());

		//now move time to cause staleness
		time.plus(Duration.ofSeconds(61));

		//this should result in another get for 1.2.3.4
		GeoLocation loc4 = data.get("1.2.3.4");
		assertEquals(2, source.gets.get("1.2.3.4").size());

		//finally check locations are correct
		assertEquals(GeoLocation.forLocation(1.2, 3.4), loc1);
		assertEquals(GeoLocation.forLocation(2.3, 4.5), loc2);
		assertEquals(GeoLocation.forLocation(1.2, 3.4), loc3);
		assertEquals(GeoLocation.forLocation(1.2, 3.4), loc4);

	}

	@Test
	public void hashcode() {

	}

	@Test
	public void equals() {

	}

	//creates bogus lat/lon based on the incoming ip(v4)'s octets
	private class TestSource implements GeoLocationSource {

		//keep track of gets
		private ListMap<String, GeoLocation> gets = new ListMap<String, GeoLocation>();

		@Override
		public GeoLocation get(String key) {

			//make it a float of the ip octets lat=1.2 lon=3.4
			String[] octets = key.split("\\.");

			GeoLocation ret = GeoLocation.forLocation(
								  Double.parseDouble(octets[0] + "." + octets[1]),
								  Double.parseDouble(octets[2] + "." + octets[3])
							  );

			gets.add(key, ret);

			return ret;

		}

	}

}
