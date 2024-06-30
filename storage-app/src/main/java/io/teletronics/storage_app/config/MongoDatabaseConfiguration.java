package io.teletronics.storage_app.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "io.teletronics.storage_app.repository")
public class MongoDatabaseConfiguration extends AbstractReactiveMongoConfiguration {

    private final String mongoUri;

    @Autowired
    public MongoDatabaseConfiguration(@Value("${spring.data.mongodb.uri}") String mongoUri) {
        this.mongoUri = mongoUri;
    }

    @Override
    protected String getDatabaseName() {
        return new ConnectionString(mongoUri).getDatabase();
    }

    @Override
    public MongoClient reactiveMongoClient() {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .readPreference(ReadPreference.primary())
                .build();

        return MongoClients.create(settings);
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() {
        return new ReactiveMongoTemplate(reactiveMongoClient(), getDatabaseName());
    }
}
