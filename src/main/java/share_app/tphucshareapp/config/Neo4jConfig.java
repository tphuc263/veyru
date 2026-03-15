package share_app.tphucshareapp.config;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Neo4j Graph Database Configuration
 */
@Configuration
public class Neo4jConfig {

    @Value("${spring.neo4j.uri:bolt://localhost:7687}")
    private String neo4jUri;

    @Value("${spring.neo4j.authentication.username:neo4j}")
    private String username;

    @Value("${spring.neo4j.authentication.password:password}")
    private String password;

    @Bean
    public Driver neo4jDriver() {
        return GraphDatabase.driver(neo4jUri, AuthTokens.basic(username, password));
    }

    @Bean
    public PlatformTransactionManager transactionManager(Driver driver,
                                                         DatabaseSelectionProvider databaseSelectionProvider) {
        return new Neo4jTransactionManager(driver, databaseSelectionProvider);
    }
}

