import webSocket from 'ws';
const NODE_ENV = process.env.NODE_ENV;

import store from './store'
import {
    saveOrUpdateChatSessionBatch4Init, saveOrUpdate4Message,
    selectUserSessionByContactId
} from './db/ChatSessionUserModel.js'
import { saveMessage,saveMessageBatch, updateMessage } from './db/ChatMessageModel.js'
import { updateContactNoReadCount } from './db/UserSettingModel.js'
let ws = null;
let maxReconnectTimes = null;
let lockReconnect = false;


let wsUrl = null;

let sender = null;
let needReconnect = null;
const initWs = (config, _sender) => {
    wsUrl = `${NODE_ENV !== 'development' ? store.getData("proWsDomain") : store.getData("devWsDomain")}?token=${config.token}`;
    sender = _sender;
    needReconnect = true;
    maxReconnectTimes = 5;
    createWs();
}
const createWs = () => {
    if (wsUrl == null) {
        return;
    }
    ws = new webSocket(wsUrl);
    ws.onopen = function () {
        console.log("客户端连接成功");
        ws.send("heartBeat");
        maxReconnectTimes = 5;
    }
    //从服务器接收到信息的回调函数
    ws.onmessage = async function (e) {
        console.log("从服务器接收到信息", e.data);
        const message = JSON.parse(e.data);
        const messageType = message.messageType;
        switch (messageType) {
            case 0://ws连接成功
                //保存会话信息
                await saveOrUpdateChatSessionBatch4Init(message.extendData.chatSessionList);
                //保存消息
                await saveMessageBatch(message.extendData.chatMessageList);
                //更新联系人数量
                await updateContactNoReadCount({ userId: store.getUserId(), noReadCount: message.extendData.applyCount });
                //发送消息
                sender.send("receiveMessage", { messageType: message.messageType });
                break;
            case 2://文字
            case 5://图片视频
                //如果是自己发送的消息且是群聊，不处理
                if (message.sendUserId == store.getUserId() && message.contactType == 1) {
                    break;
                }
                const sessionInfo = {};

                if (message.extendData && typeof message.extendData === 'Object') {
                    Object.assign(sessionInfo, message.extendData);
                } else {
                    Object.assign(sessionInfo, message);
                    if (message.contactType == 0 && messageType != 1) {
                        sessionInfo.contactName = message.sendUserNickName;
                    }
                    sessionInfo.lastReceiveTime = message.sendTime;
                }
                await saveOrUpdate4Message(store.getUserData("currentSessionId"), sessionInfo);
                //写入本地消息
                await saveMessage(message);
                // TODO BUG
                const dbSessionInfo = await selectUserSessionByContactId(message.contactId);
                message.extendData = dbSessionInfo;
                sender.send("receiveMessage", message);
                break;
            //文件上传完成，需要更新消息状态，如图片/视频 状态改为1，代表发送完成。
            case 6:
                updateMessage({status:message.status},{messageId:message.messageId})
                sender.send("receiveMessage", message);
                break;
        }
    }
    ws.onclose = function () {
        console.log("客户端关闭");
        reconnnect();
    }
    ws.onerror = function () {
        console.log("客户端错误");
        reconnnect();
    }
    const reconnnect = () => {
        if (!needReconnect) {
            console.log("连接断开，不需要重连");
            return;
        }
        if (ws != null) {
            ws.close();
        }
        if (lockReconnect) {
            return;
        }
        lockReconnect = true;
        if (maxReconnectTimes > 0) {
            console.log("重连中...剩余重连次数" + maxReconnectTimes, new Date().getTime());
            maxReconnectTimes--;
            setTimeout(() => {
                createWs();
                lockReconnect = false;
            }, 5000);
        } else {
            console.log("重连失败");

        }
    }
    setInterval(() => {
        if (ws != null && ws.readyState == 1) {
            // console.log("发送心跳");
            ws.send("heartBeat");
        }
    }, 5000)
}
const closeWs = () => {
}

export {
    initWs
}