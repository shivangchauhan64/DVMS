package com.DVMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.DVMS.config.MeetWithProperties;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(MeetWithProperties.class)
@EnableScheduling
public class DvmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(DvmsApplication.class, args);
	}

}
