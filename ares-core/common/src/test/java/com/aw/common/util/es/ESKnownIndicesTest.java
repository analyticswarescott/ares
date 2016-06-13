package com.aw.common.util.es;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.aw.common.tenant.Tenant;

public class ESKnownIndicesTest {

	private Tenant tenant;

	@Before
	public void before() {
		tenant = mock(Tenant.class);
		when(tenant.getTenantID()).thenReturn("1");
	}

	@Test
	public void addIndices_DAYS() throws Exception {

		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.DAYS);

		Instant start = _toInstant("2016_01_01", ChronoUnit.DAYS);
		Instant end = _toInstant("2016_01_05", ChronoUnit.DAYS);

		ESKnownIndices index = ESKnownIndices.STATUS;

		List<String> indices = new ArrayList<String>();
		index.addIndices(tenant, start, end, indices);

		assertEquals(5, indices.size());
		assertTrue(indices.contains("status_1_2016_01_01"));
		assertTrue(indices.contains("status_1_2016_01_02"));
		assertTrue(indices.contains("status_1_2016_01_03"));
		assertTrue(indices.contains("status_1_2016_01_04"));
		assertTrue(indices.contains("status_1_2016_01_05"));

	}

	@Test
	public void addIndices_HOURS() throws Exception {

		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.HOURS);

		Instant start = _toInstant("2015_12_31_23", ChronoUnit.HOURS);
		Instant end = _toInstant("2016_01_01_10", ChronoUnit.HOURS);

		ESKnownIndices index = ESKnownIndices.STATUS;

		List<String> indices = new ArrayList<String>();
		index.addIndices(tenant, start, end, indices);

		assertEquals(12, indices.size());
		assertTrue(indices.contains("status_1_2015_12_31_23"));
		assertTrue(indices.contains("status_1_2016_01_01_00"));
		assertTrue(indices.contains("status_1_2016_01_01_01"));
		assertTrue(indices.contains("status_1_2016_01_01_02"));
		assertTrue(indices.contains("status_1_2016_01_01_03"));
		assertTrue(indices.contains("status_1_2016_01_01_04"));
		assertTrue(indices.contains("status_1_2016_01_01_05"));
		assertTrue(indices.contains("status_1_2016_01_01_06"));
		assertTrue(indices.contains("status_1_2016_01_01_07"));
		assertTrue(indices.contains("status_1_2016_01_01_08"));
		assertTrue(indices.contains("status_1_2016_01_01_09"));
		assertTrue(indices.contains("status_1_2016_01_01_00"));

	}

	@Test
	public void addIndices_WEEKS() throws Exception {

		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.WEEKS);

		Instant start = LocalDateTime.of(2015, 12, 15, 23, 50).toInstant(ZoneOffset.UTC);
		Instant end = LocalDateTime.of(2015, 12, 28, 00, 01).toInstant(ZoneOffset.UTC);

		ESKnownIndices index = ESKnownIndices.STATUS;

		List<String> indices = new ArrayList<String>();
		index.addIndices(tenant, start, end, indices);

		assertEquals(3, indices.size());
		assertTrue(indices.contains("status_1_2015_51"));
		assertTrue(indices.contains("status_1_2015_52"));
		assertTrue(indices.contains("status_1_2016_01"));

	}

	@Test
	public void addIndices_MONTHS() throws Exception {

		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.MONTHS);

		Instant start = _toInstant("2015_10", ChronoUnit.MONTHS);
		Instant end = _toInstant("2016_02", ChronoUnit.MONTHS);

		ESKnownIndices index = ESKnownIndices.STATUS;

		List<String> indices = new ArrayList<String>();
		index.addIndices(tenant, start, end, indices);

		assertEquals(5, indices.size());
		assertTrue(indices.contains("status_1_2015_10"));
		assertTrue(indices.contains("status_1_2015_11"));
		assertTrue(indices.contains("status_1_2015_12"));
		assertTrue(indices.contains("status_1_2016_01"));
		assertTrue(indices.contains("status_1_2016_02"));

	}

	private Instant _toInstant(String strDate, ChronoUnit unit) throws Exception {

		DateTimeFormatter format = ESKnownIndices.FORMATS.get(unit);

		assertNotNull("unsupported time unit: " + unit, format);



		Instant ret = null;
		switch (unit) {
			case HOURS:
				ret = LocalDateTime.parse(strDate, format).truncatedTo(unit).toInstant(ZoneOffset.UTC); break;
			case WEEKS:
				TemporalAccessor accessor = format.parse(strDate);
				WeekFields weekFields = WeekFields.of(Locale.ENGLISH);
				Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				utc.setWeekDate(accessor.get(weekFields.weekBasedYear()), accessor.get(weekFields.weekOfWeekBasedYear()), 1);
				ret = utc.toInstant().truncatedTo(ChronoUnit.DAYS);
				break;
			case DAYS:
				ret = LocalDate.parse(strDate, format).atStartOfDay().toInstant(ZoneOffset.UTC); break;
			case MONTHS:
				ret = YearMonth.from(format.parse(strDate)).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC); break;
			default:
				throw new Exception("unsupported time unit: " + unit);
		}

		return ret;

	}

	@Test
	public void getEarliest_DAYS() throws Exception {

		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.DAYS);
		Instant earliest = ESKnownIndices.STATUS.getEarliest(tenant, "status_1_2016_02_17");
		assertEquals(earliest, _toInstant("2016_02_17", ChronoUnit.DAYS));

	}

	@Test
	public void getEarliest_WEEKS() throws Exception {

		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.WEEKS);
		Instant earliest = ESKnownIndices.STATUS.getEarliest(tenant, "status_1_2016_01");
		assertEquals(earliest, _toInstant("2015_12_27", ChronoUnit.DAYS));

	}
	@Test
	public void getEarliest_HOURS() throws Exception {

		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.HOURS);
		Instant earliest = ESKnownIndices.STATUS.getEarliest(tenant, "status_1_2016_02_17_14");
		assertEquals(earliest, _toInstant("2016_02_17_14", ChronoUnit.HOURS));

	}

	@Test
	public void toInstant() throws Exception {

		Instant expected =  LocalDate.of(2016, 02, 15).atStartOfDay().toInstant(ZoneOffset.UTC);
		assertEquals(expected, ESKnownIndices.STATUS.toInstant(tenant, ChronoUnit.DAYS, "status_1_2016_02_15"));

		expected =  LocalDate.of(2016, 01, 17).atStartOfDay().toInstant(ZoneOffset.UTC);
		assertEquals(expected, ESKnownIndices.STATUS.toInstant(tenant, ChronoUnit.WEEKS, "status_1_2016_04"));

		expected =  LocalDate.of(2015, 12, 27).atStartOfDay().toInstant(ZoneOffset.UTC);
		assertEquals(expected, ESKnownIndices.STATUS.toInstant(tenant, ChronoUnit.WEEKS, "status_1_2016_01"));

		expected =  LocalDate.of(2016, 12, 1).atStartOfDay().toInstant(ZoneOffset.UTC);
		assertEquals(expected, ESKnownIndices.STATUS.toInstant(tenant, ChronoUnit.MONTHS, "status_1_2016_12"));

		expected =  LocalDateTime.of(2016, 12, 1, 12, 0, 0).toInstant(ZoneOffset.UTC);
		assertEquals(expected, ESKnownIndices.STATUS.toInstant(tenant, ChronoUnit.HOURS, "status_1_2016_12_01_12"));

	}

	@Test
	public void getLatest_DAYS() throws Exception {

		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.DAYS);
		Instant latest = ESKnownIndices.STATUS.getLatest(tenant, "status_1_2016_02_17");
		assertEquals(latest, _toInstant("2016_02_18", ChronoUnit.DAYS));

	}

	@Test
	public void getLatest_WEEKS() throws Exception {

		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.WEEKS);
		Instant latest = ESKnownIndices.STATUS.getLatest(tenant, "status_1_2016_01");
		assertEquals(latest, _toInstant("2016_01_03", ChronoUnit.DAYS));

	}
	@Test
	public void getLatest_HOURS() throws Exception {

		when(tenant.getIndexTimeUnit()).thenReturn(ChronoUnit.HOURS);
		Instant latest = ESKnownIndices.STATUS.getLatest(tenant, "status_1_2016_02_17_14");
		assertEquals(latest, _toInstant("2016_02_17_15", ChronoUnit.HOURS));

	}

}
