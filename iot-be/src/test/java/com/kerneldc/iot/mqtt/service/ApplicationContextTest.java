package com.kerneldc.iot.mqtt.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.kerneldc.iot.domain.device.Device;
import com.kerneldc.iot.domain.deviceattributelog.DeviceAttributeLog.Change;

class ApplicationContextTest {
	
	@Test
	void testPushChanges_sameMinute() {
		var applicationContext = new ApplicationContext(null, null);
		
		var device = new Device();
		device.setName("test device");
		
		var timestamp1 = 1773871270463L; // 2026-03-18 18:01:10.463 -0400
		var change1 = new Change("linkquality", "57", "60");
		applicationContext.pushChanges(device, timestamp1, List.of(change1));
		
		
		var timestamp2 = 1773871270530L; // 2026-03-18 18:01:10.530 -0400
		var change2 = new Change("linkquality", "63", "57");
		applicationContext.pushChanges(device, timestamp2, List.of(change2));
		
		
		var deviceAttributeChanges = applicationContext.getDeviceAttributeChanges();
		assertThat(deviceAttributeChanges, notNullValue());
		assertThat(deviceAttributeChanges.size(), is(1));
		var changesDeque = deviceAttributeChanges.get(device);
		assertThat(changesDeque, notNullValue());
		assertThat(changesDeque.size(), is(1));
		assertThat(changesDeque.peekLast(), notNullValue());
		
		assertThat(changesDeque.peekLast().changes().size(), is(2));
	}

}
