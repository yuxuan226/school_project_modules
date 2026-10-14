package com.example.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 邮件验证码Redis工具类
 */
@Component
public class EmailCodeRedisUtil {

    @Autowired
    @Qualifier("saTokenRedisTemplate") //指定我们的0号库
    private RedisTemplate<String, Object> redisTemplate;

    private static final String EMAIL_CODE_PREFIX = "email:code:";
    private static final String EMAIL_ATTEMPT_PREFIX = "email:attempt:";
    private static final long DEFAULT_CODE_EXPIRE = 5;
    private static final long DEFAULT_ATTEMPT_EXPIRE = 30;
    private static final String SENDING_PLACEHOLDER = "SENDING";

    public void createSendingPlaceholder(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, SENDING_PLACEHOLDER, 30, TimeUnit.SECONDS);
    }

    public void updatePlaceholderToCode(String email, String code) {
        String key = EMAIL_CODE_PREFIX + email;
        Object currentValue = redisTemplate.opsForValue().get(key);
        if (SENDING_PLACEHOLDER.equals(currentValue)) {
            redisTemplate.opsForValue().set(key, code, DEFAULT_CODE_EXPIRE, TimeUnit.MINUTES);
        } else {
            redisTemplate.opsForValue().set(key, code, DEFAULT_CODE_EXPIRE, TimeUnit.MINUTES);
        }
    }

    public boolean isSendingPlaceholder(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        Object value = redisTemplate.opsForValue().get(key);
        return SENDING_PLACEHOLDER.equals(value);
    }

    public void saveEmailCode(String email, String code) {
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, DEFAULT_CODE_EXPIRE, TimeUnit.MINUTES);
    }

    public void saveEmailCode(String email, String code, long expireMinutes) {
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, expireMinutes, TimeUnit.MINUTES);
    }

    public String getEmailCode(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        Object code = redisTemplate.opsForValue().get(key);
        if (SENDING_PLACEHOLDER.equals(code)) {
            return null;
        }
        return code != null ? code.toString() : null;
    }

    public boolean validateEmailCode(String email, String inputCode) {
        String savedCode = getEmailCode(email);
        if (savedCode == null) {
            return false;
        }
        if (savedCode.equals(inputCode)) {
            deleteEmailCode(email);
            clearAttemptCount(email);
            return true;
        } else {
            incrementAttemptCount(email);
            return false;
        }
    }

    public boolean validateEmailCodeWithoutDelete(String email, String inputCode) {
        String savedCode = getEmailCode(email);
        return savedCode != null && savedCode.equals(inputCode);
    }

    public void deleteEmailCode(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.delete(key);
    }

    public boolean hasValidCode(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null && !SENDING_PLACEHOLDER.equals(value);
    }

    public Long getExpireTime(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        if (isSendingPlaceholder(email)) {
            return -1L;
        }
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public Long incrementAttemptCount(String email) {
        String key = EMAIL_ATTEMPT_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, DEFAULT_ATTEMPT_EXPIRE, TimeUnit.MINUTES);
        }
        return count;
    }

    public Integer getAttemptCount(String email) {
        String key = EMAIL_ATTEMPT_PREFIX + email;
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count.toString()) : 0;
    }

    public void clearAttemptCount(String email) {
        String key = EMAIL_ATTEMPT_PREFIX + email;
        redisTemplate.delete(key);
    }

    public boolean isAttemptLimitExceeded(String email, int maxAttempts) {
        Integer attempts = getAttemptCount(email);
        return attempts >= maxAttempts;
    }

    public boolean isAttemptLimitExceeded(String email) {
        return isAttemptLimitExceeded(email, 5);
    }
}
