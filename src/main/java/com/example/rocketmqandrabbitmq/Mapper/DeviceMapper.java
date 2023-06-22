package com.example.rocketmqandrabbitmq.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DeviceMapper {
    
    @Insert("INSERT INTO  mqtt_hello (message) VALUES (#{message})")
    void insertDeviceData(@Param("message") String message);
}
