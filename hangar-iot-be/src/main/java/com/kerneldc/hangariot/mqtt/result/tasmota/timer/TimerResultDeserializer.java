package com.kerneldc.hangariot.mqtt.result.timer;

import java.io.IOException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.kerneldc.hangariot.mqtt.command.Timer;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TimerResultDeserializer extends StdDeserializer<TimerResult> {

	private static final long serialVersionUID = 1L;
	
	@Autowired
	private ObjectMapper objectMapper;

	protected TimerResultDeserializer() {
		this(null);
	}

	protected TimerResultDeserializer(Class<?> vc) {
		super(vc);
	}

	/**
	 * This deserializer will create a TimerResult on any field name that starts with string 'timer'
	 */
	@Override
	public TimerResult deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
		LOGGER.info("Begin ...");
		var timerResult = new TimerResult();
		JsonNode node = jp.getCodec().readTree(jp);
		timerResult.setTimestamp(node.get("timestamp").asLong());
		
		// convert iterator to stream
		Stream<String> fieldNameStream = StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(node.fieldNames(), Spliterator.ORDERED), false);
		
		Optional<String> timerFieldName = fieldNameStream.filter(fieldName -> StringUtils.startsWithIgnoreCase(fieldName, "timer")).findAny();
		if (timerFieldName.isEmpty()) {
			LOGGER.warn("Could not find field name [time??] when deserializing object");
			return timerResult;
		}
		timerResult.setTimerXX(objectMapper.treeToValue(node.get(timerFieldName.get()), Timer.class));
		
		LOGGER.info("End ...");
		return timerResult;
	}

}
