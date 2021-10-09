package com.docusign.integration.docusign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.docusign.integration")
public class DocusignApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocusignApplication.class, args);
	}

}
