package com.aw.common.util.es;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.WeekFields;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aw.common.exceptions.ConfigurationException;
import com.aw.common.tenant.Tenant;
import com.google.common.base.Preconditions;

/**
 * all of the defined analytics indexes within the platform
 *
 * the intention here is to have two ways of creating indexes for incoming data:
 *
 * - default - add incoming Data to index based on dg_time
 * - replay - add incoming Data to index based on current time
 *
 *
 *
 */
public enum ESKnownIndices {


	/**
	 * placeholder
	 */
	DUMMY,

	/**
	 * system errors, exceptions etc
	 */
	ERRORS,

	/**
	 * all incidents within the system
	 */
	INCIDENTS,

	/**
	 * events for writing to ES
	 */
	EVENTS_ES,

	/**
	 * events for writing to JDBC
	 */
	EVENTS_JDBC,

	/**
	 * alarms contain all event fields plus alarm fields
	 */
	ALARMS,

	/**
	 * system status information, i.e. health & performance
	 */
	STATUS;

	/**
	 * get indices for the given time period
	 *
	 * @param start
	 * @param end
	 * @return
	 */
	public void addIndices(Tenant tenant, Instant start, Instant end, List<String> indices) throws ConfigurationException {

		//truncate start
		Instant cur = start;
		ChronoUnit unit = tenant.getIndexTimeUnit();

		cur = truncate(cur, unit);

		while (!cur.isAfter(end)) {

			//build the index slice name for the current time
			String index = buildIndexFor(tenant, cur);

			indices.add(index);

			//move to next index
			cur = next(cur, tenant.getIndexTimeUnit());

		}

	}

	private Instant next(Instant current, ChronoUnit unit) {

		switch (unit) {
			case HOURS:
				return ZonedDateTime.ofInstant(current, ZoneId.of("UTC")).plusHours(1).toInstant();
			case DAYS:
				return ZonedDateTime.ofInstant(current, ZoneId.of("UTC")).plusDays(1).toInstant();
			case WEEKS:
				return ZonedDateTime.ofInstant(current, ZoneId.of("UTC")).plusWeeks(1).toInstant();
			case MONTHS:
				return ZonedDateTime.ofInstant(current, ZoneId.of("UTC")).plusMonths(1).toInstant();
			default: throw new UnsupportedOperationException("unsupported time unit: " + unit);
		}

	}

	/**
	 * get index names from start to end for the given tenant
	 *
	 * @param start the start time, inclusive
	 * @param end the end time, exclusive
	 * @return the index list covering start -> end
	 */
	public List<String> getIndices(Tenant tenant, Instant start, Instant end) throws Exception {

		List<String> ret = new ArrayList<String>();
		Instant cur = start;
		cur = truncate(cur, tenant.getIndexTimeUnit());
		while (cur.isBefore(end)) {

			ret.add(buildIndexFor(tenant, cur));

			cur = next(cur, tenant.getIndexTimeUnit());

		}

		return ret;

	}

	public String buildIndexFor(Tenant tenant, Instant time) throws ConfigurationException {

		ChronoUnit timeUnit = tenant.getIndexTimeUnit();

		Preconditions.checkState(FORMATS.containsKey(timeUnit), "unsupported time unit for elasticsearch index: " + timeUnit);

		//start with index name
		StringBuilder ret = new StringBuilder(name().toLowerCase());
		ret.append(SEPARATOR);

		//add tenant
		ret.append(tenant.getTenantID());
		ret.append(SEPARATOR);

		//get the formatter
		String timeSlice = FORMATS.get(timeUnit).format(time.atZone(ZoneOffset.UTC));

		//add time slice format
		ret.append(timeSlice);

		//return the resulting index name
		return ret.toString();

	}

	/**
	 * get name representing all indices
	 *
	 * @return all indices for this index
	 */
	public String getAllIndices(Tenant tenant) {
		return toPrefix(tenant) + "*";
	}

	Instant truncate(Instant cur, ChronoUnit unit) {
		switch (unit) {
			case WEEKS:
				LocalDateTime localDateTime = cur.atOffset(ZoneOffset.UTC).toLocalDateTime().truncatedTo(ChronoUnit.DAYS);
				int dayOfWeek = localDateTime.get(ChronoField.DAY_OF_WEEK);
				cur = localDateTime.minus(dayOfWeek, ChronoUnit.DAYS).toInstant(ZoneOffset.UTC);
				break;
			case MONTHS:
				cur = cur.atOffset(ZoneOffset.UTC).toLocalDateTime().truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1).toInstant(ZoneOffset.UTC);
				break;
			case YEARS:
				cur = cur.atOffset(ZoneOffset.UTC).toLocalDateTime().truncatedTo(ChronoUnit.DAYS).withDayOfYear(1).toInstant(ZoneOffset.UTC);
				break;
			default:
				//time units don't need any special handling
				cur = cur.truncatedTo(unit);
				break;
		}
		return cur;
	}

	/**
	 * returns whether the given index name belongs to the given tenant
	 *
	 * @param tenant
	 * @param index
	 * @return
	 */
	public boolean belongsTo(Tenant tenant, String index) {

		return index.startsWith(toPrefix(tenant));

	}

	/**
	 * get the earliest time handled by the given index
	 *
	 * @param tenant the tenant whose index is being examined
	 * @param indexName
	 * @return
	 */
	public Instant getEarliest(Tenant tenant, String indexName) throws ConfigurationException {

		ChronoUnit unit = tenant.getIndexTimeUnit();

		Instant ret = toInstant(tenant, unit, indexName);

		//get start of time unit
		return ret.atZone(ZoneId.of("UTC")).toInstant();

	}

	/**
	 * get the latest time handled by the given index
	 *
	 * @param tenant the tenant whose index is being examined
	 * @param indexName
	 * @return
	 */
	public Instant getLatest(Tenant tenant, String indexName) throws ConfigurationException {

		Instant ret = getEarliest(tenant, indexName);

		//start of next unit as latest is always exclusive
		return ret.atOffset(ZoneOffset.UTC).toLocalDateTime().plus(1, tenant.getIndexTimeUnit()).toInstant(ZoneOffset.UTC);


	}

	Instant toInstant(Tenant tenant, ChronoUnit unit, String indexName) throws ConfigurationException {

		//strip prefix
		String prefix = toPrefix(tenant);

		//make sure we have a correct index name
		Preconditions.checkState(indexName.length() > prefix.length(), "elasticsearch index " + indexName + " is shorter than prefix " + prefix + ", this is invalid");

		String datePart = indexName.substring(prefix.length());

		//get formatter and make sure we have one
		DateTimeFormatter formatter = FORMATS.get(unit);
		Preconditions.checkNotNull(formatter, "unsupported time unit for index: " + unit);

		TemporalAccessor accessor = null;
		LocalDateTime timestamp = null;
		switch (unit) {

			case DAYS:
				timestamp = LocalDate.parse(datePart, formatter).atStartOfDay();
				break;
			case WEEKS:
				accessor = formatter.parse(datePart);
				WeekFields weekFields = WeekFields.of(Locale.ENGLISH);
				Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				utc.setWeekDate(accessor.get(weekFields.weekBasedYear()), accessor.get(weekFields.weekOfWeekBasedYear()), 1);
				timestamp = utc.toInstant().truncatedTo(ChronoUnit.DAYS).atOffset(ZoneOffset.UTC).toLocalDateTime();
				break;
			case MONTHS:
				accessor = formatter.parse(datePart);
				timestamp = LocalDate.of(accessor.get(ChronoField.YEAR), accessor.get(ChronoField.MONTH_OF_YEAR), 1).atStartOfDay();
				break;
			case HOURS:
				timestamp = LocalDateTime.parse(datePart, formatter).truncatedTo(ChronoUnit.HOURS);
				break;
			default:
				throw new ConfigurationException("unsupported time unit for index: " + unit);

		}

		//parse
		Instant ret = timestamp.atOffset(ZoneOffset.UTC).toInstant();
		return ret;

	}

	public String toPrefix(Tenant tenant) {

		return name().toLowerCase() + "_" + tenant.getTenantID() + "_";

	}

	//create an unmodifiable, final map of time slice formats
	static final Map<ChronoUnit, DateTimeFormatter> FORMATS = Collections.unmodifiableMap(Stream.of(

		//hourly time slice
		new SimpleEntry<>(ChronoUnit.HOURS, DateTimeFormatter.ofPattern("yyyy_MM_dd_HH")),

		//daily time slice
		new SimpleEntry<>(ChronoUnit.DAYS, DateTimeFormatter.ofPattern("yyyy_MM_dd")),

		//weekly time slice
		new SimpleEntry<>(ChronoUnit.WEEKS, DateTimeFormatter.ofPattern("YYYY_ww")),

		//monthly time slice
		new SimpleEntry<>(ChronoUnit.MONTHS, DateTimeFormatter.ofPattern("yyyy_MM"))

		//convert to map
	).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

	private static final String SEPARATOR = "_";

	@Override
	public String toString() {
		return name().toLowerCase();
	}

}
