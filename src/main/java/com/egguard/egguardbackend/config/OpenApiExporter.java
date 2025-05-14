package com.egguard.egguardbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Component
@Profile("dev")
public class OpenApiExporter {

    @Value("${spring.application.name}")
    private String appName;

    @EventListener(ApplicationReadyEvent.class)
    public void exportOpenAPI() throws IOException {
        String url = "http://localhost:8080/v3/api-docs"; // Default Springdoc API Docs URL
        RestTemplate restTemplate = new RestTemplate();

        // Fetch the OpenAPI spec as JSON from the Springdoc endpoint
        String jsonResponse = restTemplate.getForObject(url, String.class);

        if (jsonResponse != null) {
            convertJsonToYaml(jsonResponse);

            System.out.println("✅ OpenAPI spec exported to docs/openapi.yaml");
        } else {
            System.out.println("❌ Failed to fetch OpenAPI spec from /v3/api-docs");
        }
    }

    private void convertJsonToYaml(String jsonResponse) throws IOException {
        // Initialize Jackson ObjectMapper to convert JSON to Java Object
        ObjectMapper objectMapper = new ObjectMapper();

        // Convert JSON string to a Map (you can also use OpenAPI model classes, but Map works for simplicity)
        Object json = objectMapper.readValue(jsonResponse, Object.class);

        // Initialize SnakeYAML to convert the Map to YAML format
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        // Create the output file
        File outputFile = new File("docs/openapi.yaml");
        outputFile.getParentFile().mkdirs(); // Make sure directories exist

        // Write the YAML string to the file
        try (FileWriter fileWriter = new FileWriter(outputFile)) {
            yaml.dump(json, fileWriter);
        }
    }
}
