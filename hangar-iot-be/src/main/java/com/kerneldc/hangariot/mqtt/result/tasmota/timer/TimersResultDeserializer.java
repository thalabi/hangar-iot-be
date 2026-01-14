package com.kerneldc.hangariot.mqtt.result.timer;

import java.io.IOException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.kerneldc.hangariot.mqtt.command.Timer;

@Component
public class TimersResultDeserializer extends StdDeserializer<TimersResult> {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private ObjectMapper objectMapper;

	protected TimersResultDeserializer() {
		this(null);
	}

	protected TimersResultDeserializer(Class<?> vc) {
		super(vc);
	}

	
	@Override
	public TimersResult deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		var timerResult = new TimersResult();
		JsonNode node = jp.getCodec().readTree(jp);
		timerResult.setTimers(getValue(node, "timers").asText());
		timerResult.getTimerArray()[0] = objectMapper.treeToValue(getValue(node, "timer1"), Timer.class);
		timerResult.getTimerArray()[1] = objectMapper.treeToValue(getValue(node, "timer2"), Timer.class);
		timerResult.getTimerArray()[2] = objectMapper.treeToValue(getValue(node, "timer3"), Timer.class);
		timerResult.getTimerArray()[3] = objectMapper.treeToValue(getValue(node, "timer4"), Timer.class);
		timerResult.getTimerArray()[4] = objectMapper.treeToValue(getValue(node, "timer5"), Timer.class);
		timerResult.getTimerArray()[5] = objectMapper.treeToValue(getValue(node, "timer6"), Timer.class);
		timerResult.getTimerArray()[6] = objectMapper.treeToValue(getValue(node, "timer7"), Timer.class);
		timerResult.getTimerArray()[7] = objectMapper.treeToValue(getValue(node, "timer8"), Timer.class);
		timerResult.getTimerArray()[8] = objectMapper.treeToValue(getValue(node, "timer9"), Timer.class);
		timerResult.getTimerArray()[9] = objectMapper.treeToValue(getValue(node, "timer10"), Timer.class);
		timerResult.getTimerArray()[10] = objectMapper.treeToValue(getValue(node, "timer11"), Timer.class);
		timerResult.getTimerArray()[11] = objectMapper.treeToValue(getValue(node, "timer12"), Timer.class);
		timerResult.getTimerArray()[12] = objectMapper.treeToValue(getValue(node, "timer13"), Timer.class);
		timerResult.getTimerArray()[13] = objectMapper.treeToValue(getValue(node, "timer14"), Timer.class);
		timerResult.getTimerArray()[14] = objectMapper.treeToValue(getValue(node, "timer15"), Timer.class);
		timerResult.getTimerArray()[15] = objectMapper.treeToValue(getValue(node, "timer16"), Timer.class);
		timerResult.setTimestamp(getValue(node, "timestamp").longValue());
		
		return timerResult;
	}

	/**
	 * Case insensitive get value of field name
	 * @param jsonNode
	 * @param lowerCaseFieldName
	 * @return the value of the matching field
	 */
	private JsonNode getValue(JsonNode jsonNode, String lowerCaseFieldName) {
		// convert iterator to stream
		var fieldNameStream = StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(jsonNode.fieldNames(), Spliterator.ORDERED), false);
		var matchingFieldName = fieldNameStream.filter(fieldName -> fieldName.equalsIgnoreCase(lowerCaseFieldName)).findAny().orElseThrow();
		return jsonNode.get(matchingFieldName);
	}
}
