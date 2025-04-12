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
const updateNoReadCount = ({contactId, noReadCount}) => {
    let sql = "update chat_session_user set no_read_count = no_read_count+? where user_id = ? and contact_id = ? ";
    return run(sql, [noReadCount, store.getUserId(), contactId])
}

const selectUserSessionList =()=>{
    let sql = "select * from chat_session_user where user_id = ? and status = 1 ";
    return queryAll(sql, [store.getUserId()])
}

const delChatSession = (contactId) => {
    const paramData = {
        userId: store.getUserId(),
        contactId: contactId
    }
    const sessionInfo={
        status:0
    }
    return update("chat_session_user", sessionInfo, paramData);
}
const topChatSession = (contactId,topType) => {
    console.log("topChatSession",contactId,topType)
    const paramData = {
        userId: store.getUserId(),
        contactId: contactId
    }
    const sessionInfo={
        topType:topType 
    }
    return update("chat_session_user", sessionInfo, paramData);
}
export {
    saveOrUpdateChatSessionBatch4Init,
    updateNoReadCount,
    selectUserSessionList,
    delChatSession,
    topChatSession
}