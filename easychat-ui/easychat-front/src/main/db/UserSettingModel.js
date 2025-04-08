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
const os = require('os');
import store from '../store.js'

const userDir = 'D:'
// const fileFolder = userDir + (NODE_ENV === 'development' ? '/.easyChatDev/' : '/.easyChat/');
// if (!fs.existsSync(fileFolder)) {
//     fs.mkdirSync(fileFolder);
// }


const updateContactNoReadCount = ({ userId, noReadCount }) => {
    return new Promise(async (resolve, reject) => {
        let sql = null;
        if(noReadCount==0){
            resolve();
            return;
        }
        if (noReadCount) {
            sql = "update user_setting set contact_no_read = contact_no_read+? where user_id = ?";
        } else {
            noReadCount = 0;
            sql = "update user_setting set contact_no_read = ? where user_id = ?";
        }
        await run(sql, [noReadCount, userId]);
        resolve();
    })
}
const addUserSetting=async(userId,email)=>{

    let sql = "select max(server_port) server_port from user_setting";
    let {server_port} = await queryOne(sql, []);
    if(server_port == null){
        server_port=10240;
    }else{
        server_port++;
    }
    const sysSetting={
        localFileFolder:userDir + "\\.easyChat\\fileStorage\\",
    }
    sql = "select * from user_setting where user_id = ?";
    const userInfo = await queryOne(sql, [userId]);

    let resultServerPort = null
    let localFileFolder = sysSetting.localFileFolder + userId
    if(userInfo){
        await update("user_setting",{"email":email,},{"user_id":userId});
        resultServerPort = server_port;
        localFileFolder = JSON.parse(userInfo.sysSetting).localFileFolder+userId;
    }else{
        await insertOrIgnore("user_setting",{
            userId:userId,
            email:email,
            sysSetting:JSON.stringify(sysSetting),
            contactNoRead:0,
            serverPort:server_port
        })
        resultServerPort = server_port;
    }
    //TODO 启动本地服务
    store.setUserData("localServerPort",resultServerPort);
    store.setUserData("localFileFolder",localFileFolder);
}
export {
    updateContactNoReadCount,
    addUserSetting
}