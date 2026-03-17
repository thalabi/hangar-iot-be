package com.kerneldc.iot.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kerneldc.iot.util.MessageUtils;




class MessageUtilsTest {

	private static final String oldMessage = "{\"linkquality\":72,\"power_on_behavior\":null,\"state\":\"ON\",\"update\":{\"installed_version\":4110,\"latest_version\":4110,\"state\":\"idle\"}}";
	private static final String newMessage = "{\"linkquality\":772,\"state\":\"OFF\",\"update\":{\"installed_version\":4110,\"latest_version\":7777,\"state\":\"idle\"},\"newNode1\":\"newNode1Value\"}";
	
	@Disabled
	@Test
	void testDiff() throws JsonProcessingException {
		var messageUtils = new MessageUtils(new ObjectMapper());
		var diff = messageUtils.diff(oldMessage,
				newMessage);
		var diffString = messageUtils.toJsonString(diff);
		System.out.println(diffString);
		assertThat(diffString, not(StringUtils.EMPTY));
		
		var filteredChanges = messageUtils.filter(diff, "linkquality"); 
		var filteredChangesString = messageUtils.toJsonString(filteredChanges);
		System.out.println(filteredChangesString);
		assertThat(filteredChangesString.contains("linkquality"), is(true));
	}
	
}
