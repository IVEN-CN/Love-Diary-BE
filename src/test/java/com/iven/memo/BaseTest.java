package com.iven.memo;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

@SpringBootTest(properties = {
    "spring.data.redis.host=localhost",
    "spring.data.redis.port=6379"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BaseTest {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    private Flyway flyway;
    @Autowired
    protected ObjectMapper objectMapper;

    @AfterEach
    void clean() {
        // 清空数据库并重新执行迁移，保证每个测试运行在干净的 schema 上
        flyway.clean();
        flyway.migrate();
    }
}
