package com.aw.common.util;

import static org.junit.Assert.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import com.aw.common.util.SlidingWindow.Element;

public class SlidingWindowTest {

	//find all elements in a window
	@Test
	public void basicFind() throws Exception {

		//create a sliding window of 10 seconds with a 5 second tolerance - at time 15,000
		SetTimeSource timeSource = new SetTimeSource(Instant.ofEpochMilli(15000L));
		SlidingWindow<Long> window = new SlidingWindow<Long>(ChronoUnit.SECONDS, 10, Duration.ofSeconds(5), timeSource);

		//add some stuff
		window.add(0L, Instant.ofEpochMilli(1000L));
		window.add(1L, Instant.ofEpochMilli(2000L));
		window.add(2L, Instant.ofEpochMilli(3000L));

		assertEquals(3, window.getTotalCount());

		//ensure we can find what we need
		Collection<Element<Long>> found = window.find(3, Duration.ofSeconds(2), 0L);
		assertNotNull(found);
		assertEquals(3, found.size());

		//make sure they were consumed
		assertEquals(0, window.getTotalCount());

		//check properties
		assertEquals(ChronoUnit.SECONDS, window.getTimeUnit());
		assertEquals(10, window.getTimeUnitCount());
		assertEquals(Duration.ofSeconds(5), window.getTolerance());
		assertSame(timeSource, window.getTimeSource());

	}

	//find a range less than the elements in the window - make sure we get what we expect
	@Test
	public void limitedFind() throws Exception {

		//create a sliding window of 10 seconds with a 5 second tolerance - at time 15,000
		SlidingWindow<Long> window = new SlidingWindow<Long>(ChronoUnit.SECONDS, 10, Duration.ofSeconds(5), new SetTimeSource(Instant.ofEpochMilli(15000L)));

		//add some stuff
		window.add(0L, Instant.ofEpochMilli(1000L));
		window.add(1L, Instant.ofEpochMilli(2000L));
		window.add(2L, Instant.ofEpochMilli(3000L));

		assertEquals(3, window.getTotalCount());

		//ensure we can find what we need
		Collection<Element<Long>> found = window.find(2, Duration.ofSeconds(1), 0L);
		assertNotNull(found);
		assertEquals(2, found.size());

		Iterator<Element<Long>> iter = found.iterator();
		assertEquals(0L, iter.next().getData().longValue());
		assertEquals(1L, iter.next().getData().longValue());

		//make sure they were consumed
		assertEquals(1, window.getTotalCount());

	}

	//find a range less than the elements in the window - make sure we get what we expect - do this after a slide
	@Test
	public void slideLimitedFind() throws Exception {

		SetTimeSource time = new SetTimeSource(Instant.ofEpochMilli(15000L));

		//create a sliding window of 10 seconds with a 5 second tolerance - at time 15,000
		SlidingWindow<Long> window = new SlidingWindow<Long>(ChronoUnit.SECONDS, 10, Duration.ofSeconds(5), time);

		//add some stuff
		window.add(0L, Instant.ofEpochMilli(1000L));
		window.add(1L, Instant.ofEpochMilli(2000L));
		window.add(2L, Instant.ofEpochMilli(3000L));

		//move time forward
		time.setCurrentTime(Instant.ofEpochMilli(23000L));
		window.add(3L, Instant.ofEpochMilli(21000L));
		window.add(4L, Instant.ofEpochMilli(22000L));
		window.add(5L, Instant.ofEpochMilli(23000L));

		assertEquals(3, window.getTotalCount());

		//ensure we can find what we need
		Collection<Element<Long>> found = window.find(2, Duration.ofSeconds(1), 0L);
		assertNotNull(found);
		assertEquals(2, found.size());

		Iterator<Element<Long>> iter = found.iterator();
		assertEquals(3L, iter.next().getData().longValue());
		assertEquals(4L, iter.next().getData().longValue());

		//make sure they were consumed
		assertEquals(1, window.getTotalCount());

	}

	//find a range less than the elements in the window - make sure we get what we expect - do this after a slide
	@Test
	public void slideNoFind() throws Exception {

		SetTimeSource time = new SetTimeSource(Instant.ofEpochMilli(15000L));

		//create a sliding window of 10 seconds with a 5 second tolerance - at time 15,000
		SlidingWindow<Long> window = new SlidingWindow<Long>(ChronoUnit.SECONDS, 10, Duration.ofSeconds(5), time);

		//add some stuff
		window.add(0L, Instant.ofEpochMilli(1000L));
		window.add(1L, Instant.ofEpochMilli(2000L));
		window.add(2L, Instant.ofEpochMilli(3000L));

		//move time forward
		time.setCurrentTime(Instant.ofEpochMilli(23000L));
		window.add(3L, Instant.ofEpochMilli(21000L));
		window.add(4L, Instant.ofEpochMilli(22000L));
		window.add(5L, Instant.ofEpochMilli(23000L));

		assertEquals(3, window.getTotalCount());

		//ensure we can find what we need
		Collection<Element<Long>> found = window.find(4, Duration.ofSeconds(10), 0L);
		assertNull(found);

		//make sure they were consumed
		assertEquals(3, window.getTotalCount());

	}

	@Test
	public void simpleConstructor() {

		//create a sliding window of 10 seconds with a 5 second tolerance - at time 15,000
		SlidingWindow<Long> window = new SlidingWindow<Long>(ChronoUnit.SECONDS, 10);

		assertEquals(ChronoUnit.SECONDS, window.getTimeUnit());
		assertEquals(10, window.getTimeUnitCount());
		assertEquals(null, window.getTolerance());
		assertNotNull(window.getTimeSource());

	}

	@Test
	public void feedableTimeSource() {

		//create a sliding window of 10 seconds with a 5 second tolerance - at time 15,000
		SlidingWindow<Long> window = new SlidingWindow<Long>(ChronoUnit.SECONDS, 10);

		assertEquals(ChronoUnit.SECONDS, window.getTimeUnit());
		assertEquals(10, window.getTimeUnitCount());
		assertEquals(null, window.getTolerance());
		assertNotNull(window.getTimeSource());

		long time = System.currentTimeMillis() + 100000L;
		window.add(1L, Instant.ofEpochMilli(time));

		assertEquals(time, window.getTimeSource().nowMillis());

	}
}
