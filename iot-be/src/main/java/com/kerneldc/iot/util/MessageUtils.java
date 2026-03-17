package com.kerneldc.iot.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.fge.jsonpatch.diff.JsonDiff;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageUtils {

	private final ObjectMapper objectMapper;
	
	private record JsonChange(
		    String path,
		    String operation,
		    Object oldValue,
		    Object newValue
		) {}
	private record JsonDiffResult(List<JsonChange> changes) {}
	

	public String diff(String oldMessage, String newMessage, String searchPath)throws JsonProcessingException {
		var diffResult = diff(oldMessage,newMessage);
		var filteredResult = filter(diffResult, searchPath);
		return toJsonString(filteredResult);
	}
	
	public JsonDiffResult diff(String oldJson, String newJson) throws JsonProcessingException {

        JsonNode oldNode = objectMapper.readTree(oldJson);
        JsonNode newNode = objectMapper.readTree(newJson);

        
        // To make class work, uncomment and include json-patch dependency
        JsonNode patch = null;//        JsonNode patch = JsonDiff.asJson(oldNode, newNode);
        

        List<JsonChange> changes = new ArrayList<>();

        for (JsonNode change : patch) {

            String op = change.get("op").asText();
            String path = change.get("path").asText();

            JsonNode oldValue = oldNode.at(path);
            JsonNode newValue = change.get("value");

            changes.add(new JsonChange(path.substring(1).replace("/", "."), op, oldValue, newValue));
        }

        return new JsonDiffResult(changes);
    }

    public JsonDiffResult filter(JsonDiffResult jsonDiffResult, String path) {
    	List<JsonChange> changes = new ArrayList<>();
    	for (var change: jsonDiffResult.changes()) {
    		if (StringUtils.equals(change.path() ,path)) {
    			changes.add(change);
    		}
    	}
    	return new JsonDiffResult(changes);
    }

    public String toJsonString(JsonDiffResult jsonDiffResult) throws JsonProcessingException {
    	return objectMapper.writeValueAsString(jsonDiffResult);
    }

}
