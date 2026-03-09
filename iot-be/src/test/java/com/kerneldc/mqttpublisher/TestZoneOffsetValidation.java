package com.kerneldc.mqttpublisher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class TestZoneOffsetValidation {

	@Test
	void test1() {
		var formatter = DateTimeFormatter.ofPattern("xxx");
		var offset1 = "-08:30";
		formatter.parse(offset1);
	}

	@Test
	void test2() {
		var formatter = DateTimeFormatter.ofPattern("xxx");
		var offset1 = "+08:30";
		formatter.parse(offset1);
	}


	Pattern pattern = Pattern.compile("([+|-])?(\\d{1,2})((:)(\\d\\d))?");
	
	@Test
	void test10() {
		var m = pattern.matcher("-5");
		assertThat(m.matches(), equalTo(true));
	}

	@Test
	void test11() {
		var m = pattern.matcher("-05");
		assertThat(m.matches(), equalTo(true));
	}

	@Test
	void test12() {
		var m = pattern.matcher("-05:30");
		assertThat(m.matches(), equalTo(true));
	}
	
	@Test
	void test13() {
		var m = pattern.matcher("-0530");
		assertThat(m.matches(), equalTo(false));
	}
	
	@Test
	void test14() {
		var m = pattern.matcher("-05:30");
		assertThat(m.matches(), equalTo(true));
		assertThat(m.group(1), equalTo("-"));
		assertThat(m.group(2), equalTo("05"));
		assertThat(m.group(5), equalTo("30"));
	}
	
	@Test
	void test15() {
		var m = pattern.matcher("-5:30");
		assertThat(m.matches(), equalTo(true));
		assertThat(m.group(1), equalTo("-"));
		assertThat(m.group(2), equalTo("5"));
		assertThat(m.group(5), equalTo("30"));
	}
	
	@Test
	void test16() {
		var m = pattern.matcher("+5:30");
		assertThat(m.matches(), equalTo(true));
		assertThat(m.group(1), equalTo("+"));
		assertThat(m.group(2), equalTo("5"));
		assertThat(m.group(5), equalTo("30"));
	}

	@Test
	void test17() {
		var m = pattern.matcher("5:30");
		assertThat(m.matches(), equalTo(true));
		assertThat(m.group(1), emptyOrNullString());
		assertThat(m.group(2), equalTo("5"));
		assertThat(m.group(5), equalTo("30"));
	}
}
