package io.teletronics.storage_app.config;

import io.teletronics.storage_app.document.TagDocument;
import io.teletronics.storage_app.repository.TagRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MongoDBDatabaseTableInitializerConfig {
    private final TagRepository tagRepository;
    private final MongoTemplate mongoTemplate;
    private static final String TAG_FILE_PATH = "tags.txt";

    @Autowired
    public MongoDBDatabaseTableInitializerConfig(TagRepository tagRepository,
                                                 MongoTemplate mongoTemplate) {
        this.tagRepository = tagRepository;
        this.mongoTemplate = mongoTemplate;
    }

    private <T> boolean collectionExists(Class<T> clazz) {
        return this.mongoTemplate.collectionExists(clazz);
    }

    @PostConstruct
    public void initialize() {
        initializeTags();
    }

    private List<TagDocument> readTagsFromTxtFile() {
        Resource resource = new ClassPathResource(TAG_FILE_PATH);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(TagDocument::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Error reading tags.txt file", e);
            throw new RuntimeException("Failed to read tags.txt file", e);
        }
    }

    private void initializeTags() {
        if (collectionExists(TagDocument.class)) {
            return;
        }
        log.info("TagDocument data is migrating to Mongo...");

        List<TagDocument> tagDocumentList = readTagsFromTxtFile();
        tagRepository.insert(tagDocumentList).subscribe();
        log.info("TagDocument data is migrated to Mongo!");
    }
}

