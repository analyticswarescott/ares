package com.aw.spark;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.aw.spark.correlation.GroupingTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    GroupingTest.class
})
public class IntegrationTests {
}
