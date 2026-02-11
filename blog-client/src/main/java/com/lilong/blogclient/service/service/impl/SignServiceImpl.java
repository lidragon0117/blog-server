package com.lilong.blogclient.service.service.impl;

import com.lilong.blog.constants.RedisConstants;
import com.lilong.blog.helper.helper.CurrentUserHelper;
import com.lilong.blog.utils.RedisUtil;
import com.lilong.blogclient.service.service.SignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * @author: lilong
 * @date: 2025/2/8
 * @description:
 */
@Service
@RequiredArgsConstructor
public class SignServiceImpl implements SignService {

    private final RedisUtil redisUtil;


    @Override
    public Boolean sign() {
        // 记录签到
        redisUtil.setBit(RedisConstants.USER_SIGN + CurrentUserHelper.getUserId().toString(), getOffset(), true);
        return Boolean.TRUE;
    }

    @Override
    public Boolean isSignedToday() {
        return redisUtil.getBit(RedisConstants.USER_SIGN + CurrentUserHelper.getUserId().toString(), getOffset());
    }

    @Override
    public Long getCumulativeSignDays() {
        return redisUtil.bitCount(RedisConstants.USER_SIGN + CurrentUserHelper.getUserId().toString(), 0, getOffset());
    }

    @Override
    public int getConsecutiveSignDays() {
        int consecutiveDays = 0;
        int maxConsecutiveDays = 0;

        long endOffset = getOffset();
        for (long offset = endOffset; offset >= 0; offset--) {
            boolean isSigned = redisUtil.getBit(RedisConstants.USER_SIGN + CurrentUserHelper.getUserId().toString(), offset);
            if (isSigned) {
                consecutiveDays++;
                maxConsecutiveDays = Math.max(maxConsecutiveDays, consecutiveDays);
            } else {
                break;
            }
        }

        return maxConsecutiveDays;
    }

    private static long getOffset() {
        // 起始日期（例如，2025年2月8日）
        LocalDate startDate = LocalDate.of(2025, 2, 8);
        LocalDate today = LocalDate.now();
        // 计算当前日期相对于起始日期的偏移量
        return ChronoUnit.DAYS.between(startDate, today);
    }
}
