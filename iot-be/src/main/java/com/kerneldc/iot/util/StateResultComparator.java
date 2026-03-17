package com.kerneldc.iot.util;

import java.util.List;

import org.javers.core.Javers;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.springframework.stereotype.Service;

import com.kerneldc.iot.domain.deviceattributelog.DeviceAttributeLog.Change;
import com.kerneldc.iot.mqtt.result.AbstractBaseResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StateResultComparator {

	private final Javers javers;
	
	public List<Change> diff(AbstractBaseResult oldState, AbstractBaseResult newState, List<String> searchPropertyList) {
		
		Diff diff = javers.compare(oldState, newState);
		
		return 
				diff.getChangesByType(ValueChange.class).stream()
				.filter(c -> searchPropertyList.contains(c.getPropertyName()))
				.map(c -> new Change(c.getPropertyName(), c.getLeft(), c.getRight()))
				.toList();
	}
}
