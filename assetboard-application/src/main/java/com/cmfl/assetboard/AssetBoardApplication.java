package com.cmfl.assetboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.cmfl.assetboard")
@EntityScan("com.cmfl.assetboard.dao.sql.entity")
@EnableJpaRepositories("com.cmfl.assetboard.dao.sql.repository")
@EnableScheduling
public class AssetBoardApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssetBoardApplication.class, args);
    }
}
