import {
    run,
    queryOne,
    queryAll,
    queryCount,
    insert,
    insertOrReplace,
    insertOrIgnore,
    update
} from './ADB.js'
import store from '../store.js'
const saveOrUpdateChatSessionBatch4Init = (chatSessionList) => {
    return new Promise(async (resolve, reject) => {
        try {
            for (let i = 0; i < chatSessionList.length; i++) {
                const sessionInfo = chatSessionList[i];
                sessionInfo.status = 1;
                let sessionData = await selectUserSessionByContactId(sessionInfo.contactId);
                if (sessionData) {
                    await updateChatSession(sessionInfo);
                } else {
                    await addChatSession(sessionInfo);
                }
            }
            resolve();
        } catch (error) {
            resolve();
        }
    })
}

const updateChatSession = (sessionInfo) => {
    const paramData = {
        userId: store.getUserId(),
        contactId: sessionInfo.contactId
    }
    const updateInfo = Object.assign({}, sessionInfo);
    updateInfo.userId = null;
    updateInfo.contactId = null;
    return update("chat_session_user", updateInfo, paramData);
}

const addChatSession = (sessionInfo) => {
    sessionInfo.userId = store.getUserId();
    insertOrIgnore("chat_session_user", sessionInfo);
}

const selectUserSessionByContactId = (contactId) => {
    let sql = "select * from chat_session_user where user_id = ? and contact_id= ? ";
    return queryOne(sql, [store.getUserId(), contactId])
}

//更新未读数
const updateNoReadCount = ({ contactId, noReadCount }) => {
    let sql = "update chat_session_user set no_read_count = no_read_count+? where user_id = ? and contact_id = ? ";
    return run(sql, [noReadCount, store.getUserId(), contactId])
}

const selectUserSessionList = () => {
    let sql = "select * from chat_session_user where user_id = ? and status = 1 ";
    return queryAll(sql, [store.getUserId()])
}

const delChatSession = (contactId) => {
    const paramData = {
        userId: store.getUserId(),
        contactId: contactId
    }
    const sessionInfo = {
        status: 0
    }
    return update("chat_session_user", sessionInfo, paramData);
}
const topChatSession = (contactId, topType) => {
    // console.log("topChatSession", contactId, topType)
    const paramData = {
        userId: store.getUserId(),
        contactId: contactId
    }
    const sessionInfo = {
        topType: topType
    }
    return update("chat_session_user", sessionInfo, paramData);
}

const updateSessionInfo4Message = async (currentSessionId, {
    sessionId,
    contactName,
    lastMessage,
    lastReceiveTime,
    contactId,
    memberCount,
}) => {
    const params = [lastMessage, lastReceiveTime];
    let sql = "update chat_session_user set last_message = ? ,last_receive_time = ? ,status =1";
    if (contactName) {
        sql += " ,contact_name = ?"
        params.push(contactName)
    }
    //成员数量
    if (memberCount != null) {
        sql += " ,member_count = ?"
        params.push(memberCount)
    }
    //未选中当前session增加未读消息数
    if (currentSessionId != sessionId) {
        sql += " ,no_read_count = no_read_count + 1"
    }
    sql += " where user_id = ? and contact_id = ?"
    params.push(store.getUserId())
    params.push(contactId)
    return run(sql, params)
}
//保存解密后的消息，不处理未读数
const updateSessionInfo4MessageNoReadCount = async (currentSessionId, {
    sessionId,
    contactName,
    lastMessage,
    lastReceiveTime,
    contactId,
    memberCount,
}) => {
    const params = [lastMessage, lastReceiveTime];
    let sql = "update chat_session_user set last_message = ? ,last_receive_time = ? ,status =1";
    if (contactName) {
        sql += " ,contact_name = ?"
        params.push(contactName)
    }
    //成员数量
    if (memberCount != null) {
        sql += " ,member_count = ?"
        params.push(memberCount)
    }
    sql += " where user_id = ? and contact_id = ?"
    params.push(store.getUserId())
    params.push(contactId)
    return run(sql, params)
}

const readAll = (contactId) => {
    let sql = "update chat_session_user set no_read_count = 0 where user_id = ? and contact_id = ? ";

    return run(sql, [store.getUserId(), contactId])
}

//保存或更新消息
const saveOrUpdate4Message = (currentSessionId, sessionInfo) => {
    return new Promise(async (resolve, reject) => {
        let sessionData = await selectUserSessionByContactId(sessionInfo.contactId);
        //如果是当前会话，不增加未读数
        if (sessionData) {
            updateSessionInfo4Message(currentSessionId,sessionInfo);
        } else {
            //如果是新会话，增加未读数
            sessionInfo.noReadCount = 1;
            await addChatSession(sessionInfo);
        }
        resolve();
    })

}

const updateGroupName = (contactId, groupName) => {
    const paramData = {
        userId: store.getUserId(),
        contactId: contactId
    }
    const sessionInfo = {
        contactName: groupName
    }
    return update("chat_session_user", sessionInfo, paramData);
}
const updateStatus =(contactId)=>{
    const paramData = {
        userId: store.getUserId(),
        contactId: contactId
    }
    const sessionInfo = {
        status:1
    }
    return update("chat_session_user", sessionInfo, paramData);
}
export {
    saveOrUpdateChatSessionBatch4Init,
    updateNoReadCount,
    selectUserSessionList,
    delChatSession,
    topChatSession,
    updateSessionInfo4Message,
    readAll,
    saveOrUpdate4Message,
    selectUserSessionByContactId,
    updateGroupName,
    updateStatus,
    updateSessionInfo4MessageNoReadCount
}