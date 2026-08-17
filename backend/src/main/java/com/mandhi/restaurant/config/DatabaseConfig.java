package com.mandhi.restaurant.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String mysqlUrl = System.getenv("MYSQL_URL");
        if (mysqlUrl == null || mysqlUrl.isEmpty()) {
            mysqlUrl = System.getenv("DATABASE_URL");
        }
        
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String db = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        String jdbcUrl;
        String username = "root";
        String password = "";
        String driver = "com.mysql.cj.jdbc.Driver";

        if (mysqlUrl != null && !mysqlUrl.trim().isEmpty()) {
            String cleanUrl = mysqlUrl.trim();
            if (cleanUrl.startsWith("mysql://")) {
                cleanUrl = cleanUrl.substring(8);
            }
            
            if (cleanUrl.contains("@")) {
                String[] parts = cleanUrl.split("@");
                String creds = parts[0];
                String hostAndDb = parts[1];
                
                if (creds.contains(":")) {
                    String[] userPass = creds.split(":");
                    username = userPass[0];
                    password = userPass[1];
                } else {
                    username = creds;
                    password = "";
                }
                jdbcUrl = "jdbc:mysql://" + hostAndDb + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
            } else {
                jdbcUrl = "jdbc:mysql://" + cleanUrl + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
                if (user != null) username = user;
                if (pass != null) password = pass;
            }
        } else if (host != null && !host.trim().isEmpty()) {
            jdbcUrl = "jdbc:mysql://" + host.trim() + ":" + (port != null ? port.trim() : "3306") + "/" + (db != null ? db.trim() : "railway") + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
            if (user != null) username = user;
            if (pass != null) password = pass;
        } else {
            // Fallback to in-memory H2 if no Railway MySQL environment variables are present
            jdbcUrl = "jdbc:h2:mem:mandhi_db;DB_CLOSE_DELAY=-1;MODE=MySQL";
            username = "sa";
            password = "";
            driver = "org.h2.Driver";
        }

        return DataSourceBuilder.create()
                .driverClassName(driver)
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .build();
    }
}
