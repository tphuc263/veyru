package share_app.tphucshareapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;

//@Configuration
@RequiredArgsConstructor
@Slf4j
public class MongoIndexConfig {

    private final MongoTemplate mongoTemplate;

//    @Bean
    public CommandLineRunner createIndexes() {
        return args -> {
            log.info("Synchronizing MongoDB indexes with database definitions...");

            createUserIndexes();
            createPhotoIndexes();
            createLikeIndexes();
            createCommentIndexes();
            createFollowIndexes();

            log.info("MongoDB indexes synchronized successfully!");
        };
    }

    private void createUserIndexes() {
        String collection = "users";

        ensureIndex(collection,
                new Index().on("username", Sort.Direction.ASC).unique());

        // auth require email unique
        ensureIndex(collection,
                new Index().on("email", Sort.Direction.ASC).unique());

        log.info("✓ User indexes synchronized");
    }

    private void createPhotoIndexes() {
        String collection = "photos";

        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new Document("user.userId", 1)
                                .append("createdAt", -1)
                ));

        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new Document("tags", 1)
                                .append("createdAt", -1)
                ));


        log.info("✓ Photo indexes synchronized");
    }

    private void createLikeIndexes() {
        String collection = "likes";

        ensureIndex(collection,
                new Index().on("photoId", Sort.Direction.ASC));


        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new Document("photoId", 1)
                                .append("userId", 1)
                ).unique());

        log.info("✓ Like indexes synchronized");
    }

    private void createCommentIndexes() {
        String collection = "comments";

        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new Document("photoId", 1)
                                .append("createdAt", -1)
                ));

        log.info("✓ Comment indexes synchronized");
    }

    private void createFollowIndexes() {
        String collection = "follows";

        ensureIndex(collection,
                new Index().on("followerId", Sort.Direction.ASC));

        ensureIndex(collection,
                new CompoundIndexDefinition(
                        new Document("followerId", 1)
                                .append("followingId", 1)
                ).unique());

        log.info("✓ Follow indexes synchronized");
    }

    /**
     * Helper method to safely create indexes
     */
    private void ensureIndex(String collection, IndexDefinition indexDefinition) {
        try {
            mongoTemplate.indexOps(collection).ensureIndex(indexDefinition);
        } catch (Exception e) {
            log.warn("Index creation failed for collection {}: {}", collection, e.getMessage());
        }
    }
}