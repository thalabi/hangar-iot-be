package com.kerneldc.iot.utils;
import java.util.List;

import org.javers.core.JaversBuilder;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kerneldc.iot.domain.area.Area;
import com.kerneldc.iot.domain.device.Device;
import com.kerneldc.iot.domain.enums.BridgeEnum;
import com.kerneldc.iot.domain.enums.DeviceTypeEnum;
import com.kerneldc.iot.domain.zone.Zone;
import com.kerneldc.iot.mqtt.result.zigbee2mqtt.StateResult;
import com.kerneldc.iot.util.StateResultComparator;

class StateResultComparatorTest {
	private Device device1;
	private StateResultComparator stateResultComparator = new StateResultComparator(JaversBuilder.javers().build());

	{
		// 1. Create dependencies (Zone and Area)
		Zone zone = new Zone();
		zone.setId(1L); // Mapping zone_id: 1

		Area area = new Area();
		area.setId(1L); // Mapping area_id: 1

		// 2. Instantiate and populate the device1 object
		device1 = new Device();

		device1.setAddress("0x348d13fffec67189"); // This also sets the LogicalKeyHolder
		device1.setName("Master Bedroom-Lampshade");
		device1.setDescription("Lampshade");
		device1.setDeviceType(DeviceTypeEnum.PLUG);    // Mapping "PLUG" to Enum
		device1.setMake("Ikea");
		device1.setModel("Tretakt");
		device1.setLocation("On the center table");
		device1.setBridge(BridgeEnum.ZIGBEE2MQTT);    // Mapping "ZIGBEE2MQTT" to Enum
		device1.setPassive(false);
		device1.setIsManaged(true);
		device1.setVersion(0L);                        // From 'version' column (inherited from AbstractPersistableEntity)
		device1.setZone(zone);
		device1.setArea(area);

	}

	@Test
	void testDiff2() throws JsonProcessingException {
		
		var stateResult1 = new StateResult();
		
		stateResult1.setBattery(100);
		stateResult1.setBatteryLow(false);
		stateResult1.setLinkquality(80);
		stateResult1.setOccupancy(false);
		stateResult1.setState("ON");
		stateResult1.setTimestamp(10L);
		
		var satetResult2 = new StateResult();
		
		satetResult2.setBattery(100);
		satetResult2.setBatteryLow(false);
		satetResult2.setLinkquality(75);
		satetResult2.setOccupancy(false);
		satetResult2.setState("OFF");
		satetResult2.setTimestamp(0L);

		stateResultComparator.diff(stateResult1, satetResult2, List.of("linkquality","state"));
	}

}
