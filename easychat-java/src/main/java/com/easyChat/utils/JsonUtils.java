package com.easyChat.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import com.easyChat.enums.ResponseCodeEnum;
import com.easyChat.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    public static SerializerFeature[] FEATURES = new SerializerFeature[] {SerializerFeature.WriteMapNullValue};
    public static String convertObjectToJson(Object object) {
        return JSON.toJSONString(object, FEATURES);
    }

    public static <T> T convertJsonToObject(String json, Class<T> clazz) {
        try{
            return JSONObject.parseObject(json, clazz);
        }catch (Exception e){
            logger.error("convertJsonToObject异常,json:{}",json);
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }

    public static <T> List<T> convertJsonArrayToList(String json,Class<T> clazz) {
        try{
            return JSONArray.parseArray(json, clazz);
        }catch (Exception e){
            logger.error("convertJsonArrayToList,json:{}",json,e);
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }
}
