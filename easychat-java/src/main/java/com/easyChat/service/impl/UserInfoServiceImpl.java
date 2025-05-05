package com.easyChat.service.impl;

import com.easyChat.constants.Constants;
import com.easyChat.entity.config.AppConfig;
import com.easyChat.entity.dto.TokenUserInfoDto;
import com.easyChat.entity.po.UserContact;
import com.easyChat.entity.po.UserInfoBeauty;
import com.easyChat.entity.query.SimplePage;
import com.easyChat.entity.query.UserContactQuery;
import com.easyChat.entity.query.UserInfoBeautyQuery;
import com.easyChat.entity.vo.UserInfoVo;
import com.easyChat.enums.*;
import com.easyChat.entity.vo.PaginationResultVo;
import com.easyChat.entity.po.UserInfo;
import com.easyChat.entity.query.UserInfoQuery;
import com.easyChat.exception.BusinessException;
import com.easyChat.mappers.UserContactMapper;
import com.easyChat.mappers.UserInfoBeautyMapper;
import com.easyChat.mappers.UserInfoMapper;
import com.easyChat.redis.RedisComponent;
import com.easyChat.service.ChatSessionUserService;
import com.easyChat.service.UserContactService;
import com.easyChat.service.UserInfoService;
import com.easyChat.utils.CopyUtils;
import com.easyChat.utils.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:用户信息表Service
 * @author:Shen-An
 * @date:2025/01/17
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private UserInfoBeautyMapper<UserInfoBeauty, UserInfoBeautyQuery> userInfoBeautyMapper;

    @Resource
    private AppConfig appConfig;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private UserContactService userContactService;
    @Resource
    private UserContactMapper userContactMapper;
    @Resource
    private ChatSessionUserService chatSessionUserService;

    /**
     * 用户信息表根据条件查询列表
     */
    public List<UserInfo> findListByParam(UserInfoQuery query) {
        return this.userInfoMapper.selectList(query);
    }

    /**
     * 用户信息表根据条件查询数量
     */
    public Integer findCountByParam(UserInfoQuery query) {
        return this.userInfoMapper.selectCount(query);
    }

    /**
     * 分页查询
     */
    public PaginationResultVo<UserInfo> findListByPage(UserInfoQuery query) {
        Integer count = this.findCountByParam(query);
        Integer pageSize = query.getPageSize() == null ? PageSize.SIZE15.getSize() : query.getPageSize();
        SimplePage page = new SimplePage(query.getPageNo(), count, pageSize);
        query.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(query);
        PaginationResultVo<UserInfo> result = new PaginationResultVo(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或修改
     */
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserId查询
     */
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId更新
     */
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email查询
     */
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email更新
     */
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    /**
     * @param email
     * @param nickName
     * @param password
     * @throws BusinessException
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    //因为有操作两个表，必须同时成功或者失败，所以要加事务
    public void register(String email, String nickName, String password) throws BusinessException {
        Map<String, Object> res = new HashMap<>();

        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);

        if (null != userInfo) {
            throw new BusinessException("邮箱账号已经存在");
        }

        String userId = StringTools.getUserId();

        UserInfoBeauty beautyAccount = this.userInfoBeautyMapper.selectByEmail(email);
        Boolean useBeautyAccount = null != beautyAccount && BeautyAccountStatusEnum.NO_USE.getStatus().equals(beautyAccount.getStatus());
        //没有使用过靓号
        if (useBeautyAccount) {
            userId = UserContactTypeEnum.USER.getPrefix() + beautyAccount.getUserId();
        }
        Date curDate = new Date();
        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setNickName(nickName);
        userInfo.setEmail(email);
        userInfo.setPassword(StringTools.encodeMd5(password));
        userInfo.setCreateTime(curDate);
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setLastOffTime(curDate.getTime());
        userInfo.setJoinType(JoinTypeEnum.APPLY.getType());

        this.userInfoMapper.insert(userInfo);

        //把刚刚使用的靓号的状态设置为USEED
        if (useBeautyAccount) {
            UserInfoBeauty updateBeauty = new UserInfoBeauty();
            updateBeauty.setStatus(BeautyAccountStatusEnum.USEED.getStatus());
            this.userInfoBeautyMapper.updateById(updateBeauty, beautyAccount.getId());
        }

        // 创建机器人
        userContactService.addContact4Robot(userId);

    }


    @Override
    public UserInfoVo login(String email, String password) {

        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);

        if (null == userInfo || !userInfo.getPassword().equals(password)) {
            throw new BusinessException("账号或者密码不存在");
        }
        if (UserStatusEnum.DISABLE.equals(userInfo.getStatus())) {
            throw new BusinessException("账号已禁用");
        }
        // 查询联系人 放入redis
        UserContactQuery contactQuery = new UserContactQuery();
        contactQuery.setUserId(userInfo.getUserId());
        contactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        List<UserContact> list = this.userContactMapper.selectList(contactQuery);

        //只需要Id放入
        List<String> contactIdList = list.stream().map(item -> item.getContactId()).collect(Collectors.toList());
        if (!contactIdList.isEmpty()) {
            redisComponent.cleanUserContact(userInfo.getUserId());
        }
        redisComponent.addUserContactBatch(userInfo.getUserId(), contactIdList);


        //TODO 查询群组 放入redis
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(userInfo);

        //判断心跳是否存在---》存在则已经登录，
        Long lastHeartBeat = redisComponent.getUserHeartBeat(userInfo.getUserId());
        if (null != lastHeartBeat) {
            throw new BusinessException("账号已经在别处登录，请退出后登录");
        }

        //保存token，保存登录信息到redis中
        String token = StringTools.encodeMd5(tokenUserInfoDto.getUserId() + StringTools.encodeMd5(StringTools.getRandomString(Constants.LENGTH_20)));
        tokenUserInfoDto.setToken(token);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        UserInfoVo userInfoVo = CopyUtils.copy(userInfo, UserInfoVo.class);
        userInfoVo.setToken(tokenUserInfoDto.getToken());
        userInfoVo.setAdmin(tokenUserInfoDto.getAdmin());

        return userInfoVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        if (avatarFile != null) {
            String baseFoder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFile = new File(baseFoder + Constants.FILE_FOLDER_AVATAR_NAME);
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }
            String filePath = targetFile.getPath() + "/" + userInfo.getUserId() + Constants.IMAGE_SUFFIX;
            avatarFile.transferTo(new File(filePath));
            avatarCover.transferTo(new File(filePath + Constants.COVER_IMAGE_SUFFIX));
        }

        UserInfo dbInfo = this.userInfoMapper.selectByUserId(userInfo.getUserId());

        this.userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());
        String contactNameUpdate = null;
        if (!dbInfo.getNickName().equals(userInfo.getNickName())) {
            contactNameUpdate = userInfo.getNickName();
        }
        // 更新会话信息中的昵称信息
        if(contactNameUpdate == null) {
            return;
        }

        //更新token中的昵称
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDtoByUserId(userInfo.getUserId());
        tokenUserInfoDto.setNickName(contactNameUpdate);
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        chatSessionUserService.updateRedundanceInfo(contactNameUpdate,userInfo.getUserId());
    }

    private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo) {
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        tokenUserInfoDto.setUserId(userInfo.getUserId());
        tokenUserInfoDto.setNickName(userInfo.getNickName());

        String adminEmails = appConfig.getAdminEmails();
        if (!StringTools.isEmpty(adminEmails) && ArrayUtils.contains(adminEmails.split(","), userInfo.getEmail())) {
            tokenUserInfoDto.setAdmin(true);
        } else {
            tokenUserInfoDto.setAdmin(false);
        }
        return tokenUserInfoDto;
    }
//	public static void main(String[] args) {
//		System.out.println(UserContactTypeEnum.getByName("USER").getDescription());
//		System.out.println(StringTools.getUserId());
//	}


}