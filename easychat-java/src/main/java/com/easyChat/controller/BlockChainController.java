package com.easyChat.controller;

import com.easyChat.entity.po.ChatMessage;
import com.easyChat.entity.vo.ResponseVo;
import com.easyChat.service.ChatMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/blockChain")
public class BlockChainController extends ABaseController {

    @Resource
    private ChatMessageService chatMessageService;

    /**
     * 获取当天的聊天消息
     * @param date 日期时间戳（毫秒）
     * @return 包含聊天消息列表的响应对象
     */
    @RequestMapping("/getChatMessagesByDate")
    public ResponseVo getChatMessagesByDate(@RequestParam Long date) {
        List<ChatMessage> chatMessages = chatMessageService.findListByDate(date);
//        System.out.println(chatMessages);
        return getSuccessResponseVo(chatMessages);
    }
    @RequestMapping("/upchain")
    public ResponseVo markMessagesAsUpChain(@RequestParam("msgArr") List<String> msgArr) {
        try {
            // 调用服务层更新消息状态
            int updatedCount = chatMessageService.markMessagesAsUpChain(msgArr);
            return getSuccessResponseVo("成功标记 " + updatedCount + " 条消息为已上链");
        } catch (Exception e) {
            e.printStackTrace();
            return getServerErrorResponseVo("标记消息上链失败：" + e.getMessage());
        }
    }

}