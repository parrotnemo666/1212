 package com.dao;
import java.sql.*;

/**
 * 數據庫連接測試類
 * 用於驗證數據庫連接和基本操作
 */
public class DatabaseConnectionTest {
    // 數據庫連接配置
//    private static final String URL = "jdbc:sqlserver://172.16.45.213:1433;databaseName=TEST;encrypt=true;trustServerCertificate=true;";
//    private static final String USER = "sqlap";	
//    private static final String PASSWORD = "Ubot@1234";
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  private static final String URL = "jdbc:sqlserver://172.16.46.181:1433;databaseName=ExchangeHub;"
		+ "encrypt=true;trustServerCertificate=true;characterEncoding=UTF-8;";
private static final String USER = "sa";
private static final String PASSWORD = "Passw0rd";
    /**
     * 初始化數據庫驅動
     * 在靜態代碼塊中加載驅動，確保只加載一次
     */
    static {
        try {
            Class.forName(DRIVER);
            System.out.println("數據庫驅動加載成功");
        } catch (ClassNotFoundException e) {
            System.err.println("數據庫驅動加載失敗：" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 測試數據庫連接
     * 包含基本的連接測試和表結構驗證
     */
    public void testConnection() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("數據庫連接成功！");
            
            // 測試1：驗證數據庫版本
            testDatabaseVersion(conn);
            
            // 測試2：驗證表是否存在
            testTableExists(conn);
            
            // 測試3：驗證表結構
            testTableStructure(conn);
            
//            // 測試4：驗證索引
            testIndexes(conn);
            
        } catch (SQLException e) {
            System.err.println("數據庫連接失敗：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 測試數據庫版本信息
     */
    private void testDatabaseVersion(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT @@VERSION")) {
            if (rs.next()) {
                System.out.println("數據庫版本：" + rs.getString(1));
            }
        }
    }

    /**
     * 測試表是否存在
     */
    private void testTableExists(Connection conn) throws SQLException {
        String sql = """
            SELECT OBJECT_ID('exchange_transactions') AS TableId,
                   OBJECT_ID('dbo.exchange_transactions') AS TableIdWithSchema
        """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println("exchange_transactions 表存在狀態：" + 
                    (rs.getObject(1) != null ? "存在" : "不存在"));
            }
        }
    }

    /**
     * 測試表結構
     */
    private void testTableStructure(Connection conn) throws SQLException {
        String sql = """
            SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH
            FROM INFORMATION_SCHEMA.COLUMNS
            WHERE TABLE_NAME = 'exchange_transactions'
            ORDER BY ORDINAL_POSITION
        """;
        
        System.out.println("\n表結構信息：");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("列名：%-20s 類型：%-12s 長度：%s%n",
                    rs.getString("COLUMN_NAME"),
                    rs.getString("DATA_TYPE"),
                    rs.getString("CHARACTER_MAXIMUM_LENGTH"));
            }
        }
    }

    /**
     * 測試索引信息
     */
    private void testIndexes(Connection conn) throws SQLException {
        String sql = """
            SELECT i.name AS IndexName,
                   i.type_desc AS IndexType,
                   STRING_AGG(c.name, ', ') WITHIN GROUP (ORDER BY ic.key_ordinal) AS IndexColumns
            FROM sys.indexes i
            INNER JOIN sys.index_columns ic 
                ON i.object_id = ic.object_id AND i.index_id = ic.index_id
            INNER JOIN sys.columns c 
                ON ic.object_id = c.object_id AND ic.column_id = c.column_id
            WHERE OBJECT_NAME(i.object_id) = 'exchange_transactions'
            GROUP BY i.name, i.type_desc
        """;
        
        System.out.println("\n索引信息：");
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("索引名稱：%-30s 類型：%-20s 列：%s%n",
                    rs.getString("IndexName"),
                    rs.getString("IndexType"),
                    rs.getString("IndexColumns"));
            }
        }
    }

    /**
     * 主方法，運行測試
     */
    public static void main(String[] args) {
        DatabaseConnectionTest test = new DatabaseConnectionTest();
        test.testConnection();
    }
}

/*
表結構信息：
列名：id                   類型：bigint       長度：null
列名：name                 類型：varchar      長度：100
列名：id_number            類型：varchar      長度：20
列名：birth_date           類型：varchar      長度：10
列名：nationality          類型：varchar      長度：50
列名：residence_permit_issue_date 類型：varchar      長度：10
列名：residence_permit_expiry_date 類型：varchar      長度：10
列名：phone_number         類型：varchar      長度：20
列名：currency             類型：varchar      長度：10
列名：exchange_amount      類型：decimal      長度：null
列名：remittance_code      類型：varchar      長度：20
列名：transaction_description 類型：varchar      長度：200
列名：transaction_time     類型：datetime     長度：null
列名：transaction_number   類型：varchar      長度：50
列名：created_at           類型：datetime     長度：null
列名：updated_at           類型：datetime     長度：null
*/