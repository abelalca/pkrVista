package com.abp.pkr.pkrVista;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(exclude = { ErrorMvcAutoConfiguration.class })
public class PkrVistaApplication {

	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(PkrVistaApplication.class);
		builder.headless(false).run(args);
	}
}
