package com.example.cs304project.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
@CrossOrigin(origins = "*")
public class DiagnosticController {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticController.class);
    
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    
    @Autowired
    public DiagnosticController(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @GetMapping("/db-info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();
        try {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                
                // 基本数据库信息
                info.put("databaseProductName", metaData.getDatabaseProductName());
                info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
                info.put("driverName", metaData.getDriverName());
                info.put("driverVersion", metaData.getDriverVersion());
                info.put("url", metaData.getURL());
                info.put("username", metaData.getUserName());
                
                // 表信息
                List<Map<String, Object>> tables = new ArrayList<>();
                try (ResultSet rs = metaData.getTables(null, "public", null, new String[]{"TABLE"})) {
                    while (rs.next()) {
                        Map<String, Object> table = new HashMap<>();
                        String tableName = rs.getString("TABLE_NAME");
                        table.put("name", tableName);
                        
                        // 获取表的列信息
                        List<Map<String, Object>> columns = new ArrayList<>();
                        try (ResultSet columnRs = metaData.getColumns(null, "public", tableName, null)) {
                            while (columnRs.next()) {
                                Map<String, Object> column = new HashMap<>();
                                column.put("name", columnRs.getString("COLUMN_NAME"));
                                column.put("type", columnRs.getString("TYPE_NAME"));
                                column.put("size", columnRs.getInt("COLUMN_SIZE"));
                                column.put("nullable", columnRs.getBoolean("NULLABLE"));
                                columns.add(column);
                            }
                        }
                        table.put("columns", columns);
                        
                        // 获取表行数
                        try {
                            int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
                            table.put("rowCount", count);
                        } catch (Exception e) {
                            table.put("rowCount", -1);
                        }
                        
                        tables.add(table);
                    }
                }
                info.put("tables", tables);
            }
            
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            logger.error("Error getting database information", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(error);
        }
    }
} 