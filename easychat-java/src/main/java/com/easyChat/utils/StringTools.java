package com.easyChat.utils;

import com.easyChat.constants.Constants;
import com.easyChat.enums.UserContactTypeEnum;
import com.easyChat.exception.BusinessException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class StringTools {

    public static void checkParam(Object param) throws BusinessException {
        try {
            Field[] fields = param.getClass().getDeclaredFields();
            boolean notEmpty = false;
            for (Field field : fields) {
                String methodName = "get" + StringTools.upperFirstLetter(field.getName());
                Method method = param.getClass().getMethod(methodName);
                Object object = method.invoke(param);
                if (object != null && object instanceof String && !StringTools.isEmpty(object.toString())
                        || object != null && (object instanceof String)) {
                    notEmpty = true;
                    break;
                }
            }
            if (!notEmpty) {
                throw new BusinessException("多参数更新，删除，必须有非空条件");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("校验参数是否为空失败");
        }
    }

    public static String upperFirstLetter(String field) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(field)) {
            return field;
        }
        return field.substring(0, 1).toUpperCase() + field.substring(1);
    }

    public static String lowerFirstLetter(String field) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(field)) {
            return field;
        }
        return field.substring(0, 1).toLowerCase() + field.substring(1);
    }

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    //随机生成用户、群组Id
    public static String getUserId(){
        return UserContactTypeEnum.USER.getPrefix()+getRandomNumber(Constants.LENGTH_11);
    }
    public static String getGroupId(){
        return UserContactTypeEnum.GROUP.getPrefix()+getRandomNumber(Constants.LENGTH_11);
    }


    //生成随机数字字符串
    public static String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);
    }

    //生成随机字符串
    public static String getRandomString(Integer count){
        return RandomStringUtils.random(count,true,true);
    }

    //md5加密密码
    public static String encodeMd5(String orignString){
        return StringTools.isEmpty(orignString)? null: DigestUtils.md5Hex(orignString);
    }

    public static String cleanHtmlTag(String content){
        if(isEmpty(content)){
            return content;
        }
        content = content.replace("<", "&lt;");
        content = content.replace("\r\n","<br>");
        content = content.replace("\n","<br>");
        return content;
    }
    public static final String getChatSessionId4User(String [] userIds){
        Arrays.sort(userIds);
        return encodeMd5(StringUtils.join(userIds,""));
    }
}
