package com.easyChat.redis;

import com.easyChat.constants.Constants;
import com.easyChat.entity.dto.SysSettingDto;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.utils.StringTools;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

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


    public void saveHeartBeat(String userId) {
        redisUtils.setex(REDIS_KEY_WS_USER_HEART_BEAT + userId,System.currentTimeMillis(),Constants.REDIS_KEY_EXPIRES_HEART_BEAT);
    }

    public void removeHeartBeat(String userId) {
        redisUtils.delete(REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }

    /**
     * redis 保存 token
     *
     * @param tokenUserInfoDto
     */
    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto) {
        //userId存token,token存 用户信息
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY);
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(), Constants.REDIS_KEY_EXPIRES_DAY);
    }

    public TokenUserInfoDto getTokenUserInfoDto(String token) {
        TokenUserInfoDto tokenUserInfoDto = (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
        return tokenUserInfoDto;
    }

    public TokenUserInfoDto getTokenUserInfoDtoByUserId(String userId) {
       String token =(String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + userId);
       return getTokenUserInfoDto(token);
    }

    public SysSettingDto getSysSetting() {
        SysSettingDto sysSettingDto = (SysSettingDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        sysSettingDto = sysSettingDto == null ? new SysSettingDto() : sysSettingDto;

        return sysSettingDto;
    }


    //清空联系人
    public void cleanUserContact(String userId) {
        redisUtils.delete(Constants.REDIS_KEY_USER_CONTACT + userId);
    }
    //批量添加联系人
    public void addUserContactBatch(String userId, List<String>contactIdList) {

        redisUtils.lpushAll(Constants.REDIS_KEY_USER_CONTACT+userId,contactIdList,Constants.REDIS_KEY_TOKEN_EXPIRES);
    }


    //添加联系人
    public void addUserContact(String userId, String contactId) {
        List<String> contactIdList = getUserContactList(userId);

        if(contactIdList.contains(contactId)) {
            return;
        }

        List <String> contactIdList1 = redisUtils.getQueueList(Constants.REDIS_KEY_USER_CONTACT+userId);
//        for(String contactId1 : contactIdList1) {
//            System.out.println("8881"+contactId1);
//        }
        redisUtils.lpush(Constants.REDIS_KEY_USER_CONTACT+userId,contactId,Constants.REDIS_KEY_TOKEN_EXPIRES);

    }

    public List<String> getUserContactList(String userId) {

        return  redisUtils.getQueueList(userId);
    }

    public void cleanUserTokenByUserId(String userId) {
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID+ userId);
        if(StringTools.isEmpty(token)) {
            return;
        }
        redisUtils.delete(Constants.REDIS_KEY_WS_TOKEN + token);
    }
}
