package com.devoops.notification.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

//    @Value("${spring.mongodb.host:localhost}")
//    private String host;
//
//    @Value("${spring.mongodb.port:27017}")
//    private int port;
//
//    @Value("${spring.mongodb.database:notification_db}")
//    private String database;
//
//    @Value("${spring.mongodb.username:devoops}")
//    private String username;
//
//    @Value("${spring.mongodb.password:devoops}")
//    private String password;
//
//    @Value("${spring.mongodb.authentication-database:admin}")
//    private String authDatabase;
//
//    @Bean
//    public MongoClientSettings mongoClientSettings() {
//        String connectionString = String.format(
//                "mongodb://%s:%s@%s:%d/%s?authSource=%s",
//                username, password, host, port, database, authDatabase
//        );
//
//        return MongoClientSettings.builder()
//                .applyConnectionString(new ConnectionString(connectionString))
//                .uuidRepresentation(UuidRepresentation.STANDARD)
//                .build();
//    }
}
