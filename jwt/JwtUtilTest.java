//package com.utils.jwt;
//
//
//
//import java.time.LocalDateTime;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import com.model.member.User;
//import com.model.member.UserToken;
//import com.utils.jwt.JwtUtil;
//import com.exception.UtilException;
//
///**
// * JwtUtil 測試類
// */
//public class JwtUtilTest {
//    private static final Logger logger = LogManager.getLogger(JwtUtilTest.class);
//
//    public static void main(String[] args) {
//        JwtUtil jwtUtil = new JwtUtil();
//        
//        // 1. 測試正常流程
//        testNormalFlow(jwtUtil);
//        
//        // 2. 測試Token解析
//        testTokenParsing(jwtUtil);
//        
//        // 3. 測試Token過期
//        testTokenExpiration(jwtUtil);
//        
//        // 4. 測試錯誤情況
//        testErrorCases(jwtUtil);
//    }
//
//    /**
//     * 測試正常流程
//     */
//    private static void testNormalFlow(JwtUtil jwtUtil) {
//        System.out.println("===== 測試正常流程 =====");
//        try {
//            // 創建測試用戶
//            User testUser = createTestUser();
//            
//            // 生成Token
//            System.out.println("\n----- 生成Token -----");
//            UserToken userToken = jwtUtil.generateToken(testUser, "TestServer");
//            System.out.println("TokenId: " + userToken.getTokenId());
//            System.out.println("Token: " + userToken.getToken());
//            System.out.println("簽發時間: " + userToken.getIssuedAt());
//            System.out.println("過期時間: " + userToken.getExpiryDate());
//            
//            // 驗證Token
//            System.out.println("\n----- 驗證Token -----");
//            boolean isValid = jwtUtil.validateToken(userToken.getToken());
//            System.out.println("Token是否有效: " + isValid);
//            
//        } catch (UtilException e) {
//            System.err.println("錯誤代碼: " + e.getCode());
//            System.err.println("錯誤信息: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 測試Token解析
//     */
//    private static void testTokenParsing(JwtUtil jwtUtil) {
//        System.out.println("\n===== 測試Token解析 =====");
//        try {
//            User testUser = createTestUser();
//            UserToken userToken = jwtUtil.generateToken(testUser, "TestServer");
//            String token = userToken.getToken();
//            
//            // 提取用戶ID
//            System.out.println("\n----- 提取用戶ID -----");
//            Long userId = jwtUtil.extractUserId(token);
//            System.out.println("提取的用戶ID: " + userId);
//            System.out.println("是否匹配: " + userId.equals(testUser.getId()));
//            
//            // 提取TokenID
//            System.out.println("\n----- 提取TokenID -----");
//            String tokenId = jwtUtil.extractTokenId(token);
//            System.out.println("提取的TokenID: " + tokenId);
//            System.out.println("是否匹配: " + tokenId.equals(userToken.getTokenId()));
//            
//        } catch (UtilException e) {
//            System.err.println("錯誤代碼: " + e.getCode());
//            System.err.println("錯誤信息: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 測試Token過期
//     */
//    private static void testTokenExpiration(JwtUtil jwtUtil) {
//        System.out.println("\n===== 測試Token過期 =====");
//        try {
//            User testUser = createTestUser();
//            
//            // 生成一個快速過期的Token (1秒後過期)
//            System.out.println("\n----- 生成短期Token -----");
//            UserToken shortLivedToken = jwtUtil.generateTokenForTest(testUser, "TestServer", 1);
//            System.out.println("等待2秒...");
//            Thread.sleep(2000);
//            
//            // 驗證過期的Token
//            System.out.println("\n----- 驗證過期Token -----");
//            boolean isValid = jwtUtil.validateToken(shortLivedToken.getToken());
//            System.out.println("Token是否有效: " + isValid);
//            
//        } catch (UtilException e) {
//            System.err.println("錯誤代碼: " + e.getCode());
//            System.err.println("錯誤信息: " + e.getMessage());
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 測試錯誤情況
//     */
//    private static void testErrorCases(JwtUtil jwtUtil) {
//        System.out.println("\n===== 測試錯誤情況 =====");
//        
//        // 測試空Token
//        testInvalidToken(jwtUtil, null, "空Token");
//        testInvalidToken(jwtUtil, "", "空字符串Token");
//        testInvalidToken(jwtUtil, "   ", "空白Token");
//        
//        // 測試格式錯誤的Token
//        testInvalidToken(jwtUtil, "invalid.token", "格式錯誤的Token");
//        testInvalidToken(jwtUtil, "invalid.token.format", "無效格式Token");
//        
//        // 測試Token生成的錯誤情況
//        testTokenGeneration(jwtUtil, null, "TestServer", "空用戶");
//        testTokenGeneration(jwtUtil, createTestUser(), null, "空服務器名");
//    }
//
//    /**
//     * 測試無效Token
//     */
//    private static void testInvalidToken(JwtUtil jwtUtil, String token, String testCase) {
//        try {
//            System.out.println("\n----- 測試" + testCase + " -----");
//            System.out.println("Token: " + token);
//            jwtUtil.validateToken(token);
//            System.out.println("預期應該拋出異常！");
//        } catch (UtilException e) {
//            System.out.println("成功捕獲預期的異常：");
//            System.out.println("錯誤代碼: " + e.getCode());
//            System.out.println("錯誤信息: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 測試Token生成錯誤
//     */
//    private static void testTokenGeneration(JwtUtil jwtUtil, User user, String serverName, String testCase) {
//        try {
//            System.out.println("\n----- 測試" + testCase + " -----");
//            jwtUtil.generateToken(user, serverName);
//            System.out.println("預期應該拋出異常！");
//        } catch (UtilException e) {
//            System.out.println("成功捕獲預期的異常：");
//            System.out.println("錯誤代碼: " + e.getCode());
//            System.out.println("錯誤信息: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 創建測試用戶
//     */
//    private static User createTestUser() {
//        User user = new User();
//        user.setId(1L);
//        user.setUsername("testUser");
//        user.setPassword("password123");
//        user.setCreatedAt(LocalDateTime.now());
//        user.setUpdatedAt(LocalDateTime.now());
//        return user;
//    }
//}
//
//
////package com.utils.jwt;
////
////import com.model.member.User;
////import com.model.member.UserToken;
////import java.time.LocalDateTime;
////
/////**
//// * JWT Token 完整測試類
//// * 包含一般性測試和過期機制測試
//// */
////public class JwtUtilTest {
////    private static JwtUtil jwtUtil;
////    private static User testUser;
////    private static final String TEST_SERVER = "TestServer";
////
////    public static void main(String[] args) {
////        System.out.println("開始執行 JWT Token 完整測試");
////        System.out.println("====================================");
////
////        // 初始化測試環境
////        initialize();
////
////        // 執行所有測試
////        testNormalToken();           // 測試一般 Token
////        testTokenInformation();      // 測試 Token 信息提取
////        testInvalidTokens();         // 測試無效 Token
////        testTokenExpiration();       // 測試 Token 過期機制
////
////        System.out.println("\n====================================");
////        System.out.println("所有測試完成");
////    }
////
////    /**
////     * 初始化測試環境
////     */
////    private static void initialize() {
////        jwtUtil = new JwtUtil();
////        testUser = createTestUser();
////    }
////
////    /**
////     * 測試一般 Token 功能
////     */
////    private static void testNormalToken() {
////        printTestHeader("一般 Token 測試");
////
////        UserToken normalToken = jwtUtil.generateToken(testUser, TEST_SERVER);
////        System.out.println("Token 生成時間: " + normalToken.getIssuedAt());
////        System.out.println("Token 到期時間: " + normalToken.getExpiryDate());
////        
////        boolean isValid = jwtUtil.validateToken(normalToken.getToken());
////        System.out.println("Token 驗證結果: " + (isValid ? "有效" : "無效"));
////
////        // 測試被篡改的 Token
////        String tamperedToken = normalToken.getToken().substring(0, normalToken.getToken().length() - 10) + "modified";
////        boolean isTamperedValid = jwtUtil.validateToken(tamperedToken);
////        System.out.println("篡改的 Token 驗證結果: " + (isTamperedValid ? "有效" : "無效"));
////    }
////
////    /**
////     * 測試 Token 信息提取
////     */
////    private static void testTokenInformation() {
////        printTestHeader("Token 信息提取測試");
////
////        UserToken token = jwtUtil.generateToken(testUser, TEST_SERVER);
////        
////        Long userId = jwtUtil.extractUserId(token.getToken());
////        String tokenId = jwtUtil.extractTokenId(token.getToken());
////        
////        System.out.println("提取的用戶 ID: " + userId);
////        System.out.println("預期的用戶 ID: " + testUser.getId());
////        System.out.println("ID 匹配結果: " + testUser.getId().equals(userId));
////        
////        System.out.println("提取的 Token ID: " + tokenId);
////        System.out.println("預期的 Token ID: " + token.getTokenId());
////        System.out.println("Token ID 匹配結果: " + token.getTokenId().equals(tokenId));
////    }
////
////    /**
////     * 測試無效 Token
////     */
////    private static void testInvalidTokens() {
////        printTestHeader("無效 Token 測試");
////
////        // 測試 null Token
////        System.out.println("1. 測試 null Token");
////        System.out.println("驗證結果: " + jwtUtil.validateToken(null));
////
////        // 測試空字串 Token
////        System.out.println("\n2. 測試空字串 Token");
////        System.out.println("驗證結果: " + jwtUtil.validateToken(""));
////
////        // 測試格式錯誤的 Token
////        System.out.println("\n3. 測試格式錯誤的 Token");
////        System.out.println("驗證結果: " + jwtUtil.validateToken("invalid.token.format"));
////    }
////
////    /**
////     * 測試 Token 過期機制
////     */
////    private static void testTokenExpiration() {
////        printTestHeader("Token 過期機制測試");
////
////        try {
////            // 生成一個 5 秒後過期的 Token
////            UserToken shortLivedToken = jwtUtil.generateTokenForTest(testUser, TEST_SERVER, 5);
////            String token = shortLivedToken.getToken();
////            
////            System.out.println("Token 生成時間: " + shortLivedToken.getIssuedAt());
////            System.out.println("Token 過期時間: " + shortLivedToken.getExpiryDate());
////            
////            // 立即驗證
////            System.out.println("\n1. 立即驗證");
////            printTokenStatus(token);
////            
////            // 等待 3 秒
////            System.out.println("\n等待 3 秒...");
////            Thread.sleep(3000);
////            
////            // 3 秒後驗證
////            System.out.println("\n2. 3秒後驗證");
////            printTokenStatus(token);
////            
////            // 再等待 3 秒
////            System.out.println("\n等待再 3 秒...");
////            Thread.sleep(3000);
////            
////            // 6 秒後驗證（應該已過期）
////            System.out.println("\n3. 6秒後驗證");
////            printTokenStatus(token);
////            
////        } catch (InterruptedException e) {
////            System.out.println("測試被中斷: " + e.getMessage());
////        }
////    }
////
////    /**
////     * 輔助方法：創建測試用戶
////     */
////    private static User createTestUser() {
////        User user = new User();
////        user.setId(1L);
////        user.setUsername("testUser");
////        user.setPassword("testPassword");
////        user.setCreatedAt(LocalDateTime.now());
////        user.setUpdatedAt(LocalDateTime.now());
////        return user;
////    }
////
////    /**
////     * 輔助方法：打印測試標題
////     */
////    private static void printTestHeader(String title) {
////        System.out.println("\n=== " + title + " ===");
////    }
////
////    /**
////     * 輔助方法：打印 Token 狀態
////     */
////    private static void printTokenStatus(String token) {
////        System.out.println("Token 是否有效: " + jwtUtil.validateToken(token));
//////        System.out.println("Token 是否過期: " + jwtUtil.isTokenExpired(token));
////        
////        // 嘗試提取信息
////        Long userId = jwtUtil.extractUserId(token);
////        String tokenId = jwtUtil.extractTokenId(token);
////        System.out.println("能否提取用戶 ID: " + (userId != null));
////        System.out.println("能否提取 Token ID: " + (tokenId != null));
////    }
////}