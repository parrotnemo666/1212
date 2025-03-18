package com.utils.jwt;

import com.model.member.User;
import com.model.member.UserToken;
import com.utils.keys.PemKeyManager;
import com.exception.ErrorCodes;
import com.exception.UtilException;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

/**
 * JWT Token 工具類
 * 負責處理每個用戶的 JWT Token 的生成、驗證、解析等操作
 */
public class JwtUtil {
    private static final Logger logger = LogManager.getLogger(JwtUtil.class);

    // Token 相關配置常量
    private static final long TOKEN_VALIDITY = 24 * 60 * 60 * 1000; // Token有效期：24小時
    private static final String ISSUER = "ExchangeHub";             // Token發行者
    private static final String TOKEN_TYPE = "JWT";                 // Token類型

    // 密鑰管理器和當前用戶信息
    private final PemKeyManager keyManager;
    private Long currentUserId;    // 當前處理的用戶ID
    private String currentKeyId;   // 當前使用的密鑰ID

    /**
     * 構造函數，初始化JWT工具類
     * @param userId 用戶ID
     * @param keyId 密鑰ID
     */
    public JwtUtil(Long userId, String keyId) {
        this.keyManager = PemKeyManager.getInstance();
        this.currentUserId = userId;
        this.currentKeyId = keyId;
    }
    
    public JwtUtil() {
        this.keyManager = PemKeyManager.getInstance();

    }

    /**
     * 更新當前處理的密鑰信息
     * @param userId 用戶ID
     * @param keyId 密鑰ID
     */
    public void setKeyInfo(Long userId, String keyId) {
        this.currentUserId = userId;
        this.currentKeyId = keyId;
    }

    /**
     * 生成新的JWT Token
     * @param user 用戶信息
     * @param serverName 服務器名稱
     * @return UserToken對象，包含所有Token相關信息
     */
    public UserToken generateToken(User user, String serverName) {
        // 參數驗證
        validateTokenGenerationParams(user, serverName);

        try {
            // 設置Token的時間相關信息
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiryDate = now.plusHours(24);
            String tokenId = UUID.randomUUID().toString();

            // 構建JWT claims（載荷）
            Claims claims = Jwts.claims();
            claims.setId(tokenId);                    // JWT的唯一標識符
            claims.setSubject(user.getUsername());    // 主題（用戶名）
            claims.setIssuer(ISSUER);                // 發行者
            claims.put("userId", user.getId());      // 用戶ID
            claims.put("type", TOKEN_TYPE);          // Token類型
            claims.put("serverName", serverName);    // 服務器名稱
            claims.put("keyId", currentKeyId);       // 使用的密鑰ID

            // 獲取用戶特定的私鑰
            PrivateKey privateKey = keyManager.loadUserPrivateKey(user.getId(), currentKeyId);
            if (privateKey == null) {
                throw new UtilException(
                    ErrorCodes.JWT_PRIVATE_KEY_MISSING,
                    "無法獲取用戶私鑰"
                );
            }

            // 生成JWT Token
            String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()))
                .setExpiration(Date.from(expiryDate.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();

            // 創建並返回UserToken對象
            UserToken userToken = new UserToken();
            userToken.setUserId(user.getId());
            userToken.setTokenId(tokenId);
            userToken.setToken(token);
            userToken.setServerName(serverName);
            userToken.setIssuedAt(now);
            userToken.setExpiryDate(expiryDate);
//          userToken.setKeyId(currentKeyId);   //先不要試試看

            return userToken;

        } catch (UtilException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Token生成失敗", e);
            throw new UtilException(
                ErrorCodes.JWT_GENERATION_ERROR,
                "Token生成過程發生錯誤"
            );
        }
    }

    /**
     * 驗證JWT Token的有效性
     * @param token JWT Token字符串
     * @return 如果token有效返回true
     */
    public boolean validateToken(String token) {
        // 基本檢查
        if (token == null || token.trim().isEmpty()) {
            throw new UtilException(
                ErrorCodes.JWT_TOKEN_EMPTY,
                "Token不能為空"
            );
        }

        // 檢查Token格式
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new UtilException(
                ErrorCodes.JWT_TOKEN_INVALID_FORMAT,
                "Token格式不正確"
            );
        }

        try {
            // 從Token中提取密鑰信息
            String keyId = extractKeyIdFromToken(token);
            Long userId = extractUserIdFromToken(token);
            
            // 獲取用戶特定的公鑰
            PublicKey publicKey = keyManager.loadUserPublicKey(userId, keyId);
            if (publicKey == null) {
                throw new UtilException(
                    ErrorCodes.JWT_PUBLIC_KEY_MISSING,
                    "無法獲取用戶公鑰"
                );
            }

            // 解析並驗證Token
            Jws<Claims> claims = parseToken(token, publicKey);
            
            // 驗證過期時間
            Date expiration = claims.getBody().getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                logger.warn("Token已過期: {}", token);
                return false;
            }

            // 驗證發行者
            if (!ISSUER.equals(claims.getBody().getIssuer())) {
                logger.warn("無效的Token發行者");
                return false;
            }

            // 驗證Token類型
            if (!TOKEN_TYPE.equals(claims.getBody().get("type", String.class))) {
                logger.warn("無效的Token類型");
                return false;
            }

            return true;

        } catch (SignatureException e) {
            logger.error("Token簽名無效", e);
            throw new UtilException(
                ErrorCodes.JWT_SIGNATURE_INVALID,
                "Token簽名無效"
            );
        } catch (ExpiredJwtException e) {
            logger.error("Token已過期", e);
            return false;
        } catch (Exception e) {
            logger.error("Token驗證失敗", e);
            throw new UtilException(
                ErrorCodes.JWT_VALIDATION_ERROR,
                "Token驗證過程發生錯誤"
            );
        }
    }

    
    public String extractKeyIdFromToken(String token) {
        try {
            // 使用未驗證的 claims 解析
            Claims claims = getUnverifiedClaims(token);
            
            // 直接從 claims 中提取 keyId
            String keyId = claims.get("keyId", String.class);
            
            if (keyId == null) {
                logger.warn("Token中未找到keyId");
                throw new UtilException(
                    ErrorCodes.JWT_KEYID_EXTRACTION_ERROR,
                    "無法從Token中提取密鑰ID"
                );
            }
            
            return keyId;
        } catch (Exception e) {
            logger.error("無法從Token中提取keyId", e);
            throw new UtilException(
                ErrorCodes.JWT_KEYID_EXTRACTION_ERROR,
                "無法從Token中提取密鑰ID"
            );
        }
    }

    public Long extractUserIdFromToken(String token) {
        try {
            // 使用未驗證的 claims 解析
            Claims claims = getUnverifiedClaims(token);
            
            // 直接從 claims 中提取 userId
            Object userIdObj = claims.get("userId");
            
            if (userIdObj == null) {
                logger.warn("Token中未找到userId");
                throw new UtilException(
                    ErrorCodes.JWT_USERID_EXTRACTION_ERROR,
                    "無法從Token中提取用戶ID"
                );
            }
            
            // 處理可能的類型轉換
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else {
                logger.warn("userId類型不正確: {}", userIdObj.getClass());
                throw new UtilException(
                    ErrorCodes.JWT_USERID_EXTRACTION_ERROR,
                    "用戶ID類型不正確"
                );
            }
        } catch (Exception e) {
            logger.error("無法從Token中提取userId", e);
            throw new UtilException(
                ErrorCodes.JWT_USERID_EXTRACTION_ERROR,
                "無法從Token中提取用戶ID"
            );
        }
    }
    
    /**
     * 解析JWT Token
     * @param token JWT Token字符串
     * @param publicKey 用於驗證的公鑰
     * @return 解析後的Claims對象
     */
    private Jws<Claims> parseToken(String token, PublicKey publicKey) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
        } catch (Exception e) {
            logger.error("Token解析失敗", e);
            throw new UtilException(
                ErrorCodes.JWT_PARSING_ERROR,
                "Token解析失敗"
            );
        }
    }

    /**
     * 檢查Token是否過期
     */
    public boolean isTokenExpired(String token) {
        try {
            String keyId = extractKeyIdFromToken(token);
            Long userId = extractUserIdFromToken(token);
            PublicKey publicKey = keyManager.loadUserPublicKey(userId, keyId);
            
            Jws<Claims> claims = parseToken(token, publicKey);
            Date expiration = claims.getBody().getExpiration();
            
            return expiration != null && expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            logger.error("檢查Token過期狀態時發生錯誤", e);
            throw new UtilException(
                ErrorCodes.TOKEN_VALIDATE_ERROR,
                "檢查Token過期狀態時發生錯誤"
            );
        }
    }

    /**
     * 驗證Token生成參數
     */
    private void validateTokenGenerationParams(User user, String serverName) {
        if (user == null) {
            throw new UtilException(
                ErrorCodes.JWT_USER_NULL,
                "用戶對象不能為空"
            );
        }
        if (user.getId() == null) {
            throw new UtilException(
                ErrorCodes.JWT_USERID_NULL,
                "用戶ID不能為空"
            );
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new UtilException(
                ErrorCodes.JWT_USERNAME_EMPTY,
                "用戶名不能為空"
            );
        }
        if (serverName == null || serverName.trim().isEmpty()) {
            throw new UtilException(
                ErrorCodes.JWT_SERVER_NAME_EMPTY,
                "服務器名稱不能為空"
            );
        }
    }
    /**
     * 從Token中提取Token ID
     * @param token JWT Token字符串
     * @return Token ID
     * @throws UtilException 當提取失敗時拋出
     */
    public String extractTokenId(String token) {
        try {
            // 驗證token基本格式
            if (token == null || token.trim().isEmpty()) {
                throw new UtilException(
                    ErrorCodes.JWT_TOKEN_EMPTY,
                    "Token不能為空"
                );
            }

            // 從token中提取claims
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(keyManager.loadUserPublicKey(currentUserId, currentKeyId))
                .build()
                .parseClaimsJws(token)
                .getBody();

            // 獲取 token ID (jti claim)
            String tokenId = claims.getId();
            if (tokenId == null) {
                throw new UtilException(
                    ErrorCodes.JWT_TOKENID_MISSING,
                    "Token中不包含ID信息"
                );
            }

            return tokenId;
            
        } catch (ExpiredJwtException e) {
            logger.error("Token已過期", e);
            throw new UtilException(
                ErrorCodes.JWT_TOKEN_EXPIRED,
                "Token已過期"
            );
        } catch (Exception e) {
            logger.error("無法從Token中提取Token ID", e);
            throw new UtilException(
                ErrorCodes.JWT_TOKENID_EXTRACTION_ERROR,
                "無法從Token中提取Token ID"
            );
        }
    }
    //直接解析token的內容
    public static Claims getUnverifiedClaims(String token) {
        // 使用 Jwts 提供的方法直接解析 token 的 payload
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new UtilException(
                ErrorCodes.JWT_TOKEN_INVALID_FORMAT,
                "Token格式不正確"
            );
        }
        
        try {
            // 直接解析 JWT payload，不驗證簽名
            return Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJwt(parts[0] + "." + parts[1] + ".")
                .getBody();
        } catch (Exception e) {
            throw new UtilException(
                ErrorCodes.JWT_PARSING_ERROR,
                "Token解析失敗"
            );
        }
    }
    /**
     * 生成測試用的短期Token
     * 僅用於測試目的
     */
    public UserToken generateTokenForTest(User user, String serverName, int secondsToExpire) {
        validateTokenGenerationParams(user, serverName);
        if (secondsToExpire <= 0) {
            throw new UtilException(
                ErrorCodes.JWT_INVALID_EXPIRY,
                "過期時間必須大於0秒"
            );
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiryDate = now.plusSeconds(secondsToExpire);
            return generateToken(user, serverName); // 使用基本的generateToken方法
        } catch (Exception e) {
            logger.error("測試Token生成失敗", e);
            throw new UtilException(
                ErrorCodes.JWT_GENERATION_ERROR,
                "測試Token生成過程發生錯誤"
            );
        }
    }
}
