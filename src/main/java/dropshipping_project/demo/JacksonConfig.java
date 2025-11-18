package dropshipping_project.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // <-- IMPORT THIS
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class JacksonConfig {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 1. Handle Hibernate Proxies (lazy loaded objects)
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        // 2. Handle Java 8 Date/Time types (LocalDateTime)
        mapper.registerModule(new JavaTimeModule());
        
        // Optional: Write dates as ISO-8601 strings instead of timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new MappingJackson2HttpMessageConverter(mapper);
    }
}