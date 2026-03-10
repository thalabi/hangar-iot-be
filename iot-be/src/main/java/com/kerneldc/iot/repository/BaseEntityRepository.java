package com.kerneldc.iot.repository;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import com.kerneldc.iot.domain.AbstractEntity;
import com.kerneldc.iot.domain.enums.IEntityEnum;

@NoRepositoryBean
public interface BaseEntityRepository<T extends AbstractEntity, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

	IEntityEnum canHandle();
	
}
