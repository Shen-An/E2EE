package com.easyChat.redis;

import com.easyChat.constants.Constants;
import com.easyChat.entity.dto.SysSettingDto;
import com.easyChat.entity.dto.TokenUserInfoDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.easyChat.constants.Constants.REDIS_KEY_WS_USER_HEART_BEAT;

@Component
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    /**
     * 获取心跳
     *
     * @param userId
     * @return
     */
    public Long getUserHeartBeat(String userId) {
        return (Long) redisUtils.get(REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }

    /**
     * redis 保存 token
     *
     * @param tokenUserInfoDto
     */
    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY);
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID + tokenUserInfoDto.getToken(), tokenUserInfoDto.getToken(), Constants.REDIS_KEY_EXPIRES_DAY);
    }

    public SysSettingDto getSysSetting() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        sysSettingDto = sysSettingDto == null ? new SysSettingDto() : sysSettingDto;

        return sysSettingDto;
    }

}
