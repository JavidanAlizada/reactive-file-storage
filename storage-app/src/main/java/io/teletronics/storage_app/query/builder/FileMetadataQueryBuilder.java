package io.teletronics.storage_app.query.builder;

import io.teletronics.storage_app.query.FileMetadataQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static io.teletronics.storage_app.constants.FileVisibility.PRIVATE;
import static io.teletronics.storage_app.constants.FileVisibility.PUBLIC;
import static io.teletronics.storage_app.constants.QueryConstants.*;

@Component
public class FileMetadataQueryBuilder {

    public Mono<Query> createQueryForListingFilesWithFilters(FileMetadataQuery fileMetadataQuery) {
        return Mono.fromCallable(() -> {
            Query query = new Query();
            if (PRIVATE.getValue().equalsIgnoreCase(fileMetadataQuery.getVisibility())) {
                query.addCriteria(Criteria.where(USERNAME).is(fileMetadataQuery.getUsername()).and(VISIBILITY).is(PRIVATE.getValue()));
            } else {
                query.addCriteria(Criteria.where(VISIBILITY).is(PUBLIC.getValue()));
            }
            if (fileMetadataQuery.getTag() != null) {
                query.addCriteria(Criteria.where(TAGS).regex(fileMetadataQuery.getTag(), "i"));
            }
            Sort.Direction direction = fileMetadataQuery.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
            query.with(Sort.by(direction, fileMetadataQuery.getSortBy()));
            query.with(PageRequest.of(fileMetadataQuery.getPage(), fileMetadataQuery.getPageSize()));
            return query;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Query createQueryForSearchingByUsername(FileMetadataQuery fileMetadataQuery) {
        Query query = new Query();
        query.addCriteria(Criteria.where(USERNAME).is(fileMetadataQuery.getUsername()));
        return query;
    }


}
