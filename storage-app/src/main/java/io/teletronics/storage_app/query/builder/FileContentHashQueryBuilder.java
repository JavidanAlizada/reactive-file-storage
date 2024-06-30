package io.teletronics.storage_app.query.builder;

import io.teletronics.storage_app.query.FileContentHashQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import static io.teletronics.storage_app.constants.QueryConstants.FILE_HASH;
import static io.teletronics.storage_app.constants.QueryConstants.ID;

@Component
public class FileContentHashQueryBuilder {

    public Query createQuery(FileContentHashQuery fileContentHashQuery) {
        Query query = new Query();
        query.addCriteria(
                Criteria.where(FILE_HASH)
                        .is(fileContentHashQuery.getFileContentHash())
                        .and(ID)
                        .in(fileContentHashQuery.getFileContentHashIds()));
        return query;
    }
}
