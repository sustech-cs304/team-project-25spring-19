package com.example.cs304project.config;

import com.example.cs304project.entity.User;
import com.example.cs304project.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DataInitializer(UserRepository userRepository, DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        logger.info("开始数据库初始化检查...");
        
        try {
            // 检查数据库连接
            logDatabaseInfo();
            
            // 检查并确保有默认用户
            ensureDefaultUser();
            
            // 检查代码聊天室相关表是否存在
            checkRequiredTables();
            
            logger.info("数据库初始化检查完成");
        } catch (Exception e) {
            logger.error("数据库初始化失败", e);
        }
    }
    
    private void logDatabaseInfo() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            logger.info("数据库连接成功: {}，驱动版本: {}", 
                    metaData.getDatabaseProductName(), 
                    metaData.getDatabaseProductVersion());
            
            // 列出所有表
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(null, "public", null, new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
            logger.info("数据库中的表: {}", tables);
        } catch (Exception e) {
            logger.error("获取数据库信息失败", e);
        }
    }
    
    private void ensureDefaultUser() {
        // 检查是否有用户ID为1的用户
        Optional<User> defaultUser = userRepository.findById(1L);
        
        if (defaultUser.isEmpty()) {
            logger.info("创建默认用户...");
            User user = new User();
            user.setUserName("admin");
            user.setEmail("admin@example.com");
            user.setPassword("admin123");  // 生产环境应加密
            user.setRole("ADMIN");
            user.setProfile("系统默认管理员");
            
            userRepository.save(user);
            logger.info("默认用户创建完成，ID: {}", user.getUserId());
        } else {
            logger.info("默认用户已存在: {}", defaultUser.get().getUserName());
        }
    }
    
    private void checkRequiredTables() {
        try {
            // 检查code_rooms表是否存在
            boolean codeRoomsExists = isTableExists("code_rooms");
            logger.info("code_rooms表是否存在: {}", codeRoomsExists);
            
            // 检查room_members表是否存在
            boolean roomMembersExists = isTableExists("room_members");
            logger.info("room_members表是否存在: {}", roomMembersExists);
            
            // 如果表不存在，尝试手动创建
            if (!codeRoomsExists) {
                logger.warn("code_rooms表不存在，尝试手动创建");
                try {
                    jdbcTemplate.execute(
                        "CREATE TABLE IF NOT EXISTS code_rooms (" +
                        "id SERIAL PRIMARY KEY, " +
                        "name VARCHAR(255) NOT NULL, " +
                        "language VARCHAR(100) NOT NULL, " +
                        "current_code TEXT, " +
                        "created_at TIMESTAMP, " +
                        "updated_at TIMESTAMP, " +
                        "owner_id BIGINT REFERENCES users(user_id))"
                    );
                    logger.info("手动创建code_rooms表成功");
                } catch (Exception e) {
                    logger.error("手动创建code_rooms表失败", e);
                }
            }
            
            if (!roomMembersExists) {
                logger.warn("room_members表不存在，尝试手动创建");
                try {
                    jdbcTemplate.execute(
                        "CREATE TABLE IF NOT EXISTS room_members (" +
                        "room_id BIGINT NOT NULL REFERENCES code_rooms(id), " +
                        "user_id BIGINT NOT NULL REFERENCES users(user_id), " +
                        "PRIMARY KEY (room_id, user_id))"
                    );
                    logger.info("手动创建room_members表成功");
                } catch (Exception e) {
                    logger.error("手动创建room_members表失败", e);
                }
            }
        } catch (Exception e) {
            logger.error("检查所需表失败", e);
        }
    }
    
    private boolean isTableExists(String tableName) {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName + " LIMIT 1", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 