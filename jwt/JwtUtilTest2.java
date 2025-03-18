//package com.utils.jwt;
//
//import com.model.member.User;
//import com.model.member.UserToken;
//import java.time.LocalDateTime;
//import java.time.temporal.ChronoUnit;
//
///**
// * JWT Token 功能完整測試程式
// * 用於驗證 Token 的生成、驗證、解析等功能
// */
//public class JwtUtilTest2 {
//    private static final String TEST_SERVER = "TestServer";
//    
//    public static void main(String[] args) {
//        printTestHeader("JWT Token 功能測試");
//        
//        // 初始化測試環境
//        JwtUtil jwtUtil = new JwtUtil();
//        User testUser = createTestUser();
//        
//        // 執行所有測試案例
//        testTokenGeneration(jwtUtil, testUser);
//        testTokenValidation(jwtUtil, testUser);
//        testInvalidTokens(jwtUtil);
//        testTokenInformation(jwtUtil, testUser);
//        testTokenExpiration(jwtUtil, testUser);
//        
//        printTestFooter();
//    }
//    
//    private static void testTokenGeneration(JwtUtil jwtUtil, User user) {
//        printTestCase("Token 生成測試");
//        
//        try {
//            UserToken userToken = jwtUtil.generateToken(user, TEST_SERVER);
//            System.out.println("Token 生成結果:");
//            System.out.println("- Token ID: " + userToken.getTokenId());
//            System.out.println("- JWT Token: " + userToken.getToken());
//            System.out.println("- 伺服器名稱: " + userToken.getServerName());
//            System.out.println("- 發行時間: " + userToken.getIssuedAt());
//            System.out.println("- 到期時間: " + userToken.getExpiryDate());
//            
//            boolean isValid = jwtUtil.validateToken(userToken.getToken());
//            System.out.println("Token 驗證結果: " + (isValid ? "有效" : "無效"));
//            
//            assertTest("Token 生成測試", isValid);
//        } catch (Exception e) {
//            System.err.println("Token 生成測試失敗: " + e.getMessage());
//        }
//    }
//    
//    private static void testTokenValidation(JwtUtil jwtUtil, User user) {
//        printTestCase("Token 驗證測試");
//        
//        try {
//            UserToken userToken = jwtUtil.generateToken(user, TEST_SERVER);
//            boolean isValid = jwtUtil.validateToken(userToken.getToken());
//            
//            System.out.println("有效 Token 驗證結果: " + (isValid ? "通過" : "失敗"));
//            assertTest("有效 Token 驗證", isValid);
//            
//        } catch (Exception e) {
//            System.err.println("Token 驗證測試失敗: " + e.getMessage());
//        }
//    }
//    
//    private static void testInvalidTokens(JwtUtil jwtUtil) {
//        printTestCase("無效 Token 測試");
//        
//        // 測試空值
//        System.out.println("1. 測試 null Token");
//        assertTest("Null Token 測試", !jwtUtil.validateToken(null));
//        
//        // 測試空字串
//        System.out.println("2. 測試空字串 Token");
//        assertTest("空字串 Token 測試", !jwtUtil.validateToken(""));
//        
//        // 測試格式錯誤的 Token
//        System.out.println("3. 測試格式錯誤的 Token");
//        assertTest("格式錯誤 Token 測試", !jwtUtil.validateToken("invalid.token.format"));
//        
//        // 測試不完整的 Token
//        System.out.println("4. 測試不完整的 Token");
//        assertTest("不完整 Token 測試", !jwtUtil.validateToken("header.payload"));
//    }
//    
//    private static void testTokenInformation(JwtUtil jwtUtil, User user  ) {
//        printTestCase("Token 資訊提取測試");
//        
//        try {
//            UserToken userToken = jwtUtil.generateToken(user, TEST_SERVER);
//            String token = userToken.getToken();
//            
//            Long userId = jwtUtil.extractUserId(token);
//            String tokenId = jwtUtil.extractTokenId(token);
//            
//            System.out.println("提取的資訊:");
//            System.out.println("- 用戶 ID: " + userId);
//            System.out.println("- Token ID: " + tokenId);
//            
//            assertTest("用戶 ID 提取", userId.equals(user.getId()));
//            assertTest("Token ID 提取", tokenId.equals(userToken.getTokenId()));
//            
//        } catch (Exception e) {
//            System.err.println("Token 資訊提取測試失敗: " + e.getMessage());
//        }
//    }
//    
//    private static void testTokenExpiration(JwtUtil jwtUtil, User user) {
//        printTestCase("Token 過期測試");
//        
//        try {
//            UserToken userToken = jwtUtil.generateToken(user, TEST_SERVER);
//            String token = userToken.getToken();
//            
//            boolean isExpired = jwtUtil.isTokenExpired(token);
//            System.out.println("新生成的 Token 是否過期: " + (isExpired ? "是" : "否"));
//            assertTest("新 Token 過期檢查", !isExpired);
//            
//            // 檢查臨近過期時間
//            LocalDateTime expiryDate = userToken.getExpiryDate();
//            long hoursUntilExpiry = LocalDateTime.now().until(expiryDate, ChronoUnit.HOURS);
//            System.out.println("距離過期還有 " + hoursUntilExpiry + " 小時");
//            
//        } catch (Exception e) {
//            System.err.println("Token 過期測試失敗: " + e.getMessage());
//        }
//    }
//    
//    private static User createTestUser() {
//        User user = new User();
//        user.setId(1L);
//        user.setUsername("testUser");
//        user.setPassword("testPassword");
//        user.setCreatedAt(LocalDateTime.now());
//        user.setUpdatedAt(LocalDateTime.now());
//        return user;
//    }
//    
//    // 測試輔助方法
//    private static void printTestHeader(String title) {
//        System.out.println("\n====================================");
//        System.out.println("開始執行: " + title);
//        System.out.println("====================================");
//    }
//    
//    private static void printTestCase(String testName) {
//        System.out.println("\n------------------------------------");
//        System.out.println("執行測試: " + testName);
//        System.out.println("------------------------------------");
//    }
//    
//    private static void printTestFooter() {
//        System.out.println("\n====================================");
//        System.out.println("所有測試執行完成");
//        System.out.println("====================================\n");
//    }
//    
//    private static void assertTest(String testName, boolean condition) {
//        System.out.println(testName + ": " + (condition ? "通過" : "失敗"));
//    }
//}