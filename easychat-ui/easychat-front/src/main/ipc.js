import { app, shell, BrowserWindow, ipcMain } from 'electron'
import { join } from 'path'
import { electronApp, optimizer, is } from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
const NODE_ENV = process.env.NODE_ENV
import store from './store'
import { initWs } from './wsClient'
import { addUserSetting, selectSettingInfo, updateContactNoReadCount } from './db/UserSettingModel'
import {
    selectUserSessionList, delChatSession, topChatSession,
    updateSessionInfo4Message, readAll, updateStatus, saveOrUpdateChatSessionBatch4Init,
    saveOrUpdate4Message, updateSessionInfo4MessageNoReadCount
} from './db/ChatSessionUserModel'
import { selectMessageList, saveMessage, updateMessage, saveMessage4User } from './db/ChatMessageModel'
import { saveFile2Local, createCover, saveAs, saveClipBoardFile } from './file'
import { delWindow, getWindow, saveWidnow } from './windowProxy'

import { generateAndSaveECDHKeyPair, saveSharedSecretToFile, generateSharedSecret } from './GenKeys'
import { loadECDHFromPrivateKey } from './ReadShareKey'
import { deriveAESKey } from './AES'
import { execute, readDataFromFile } from './SPCGSwTT/UserKey'
import { computeHash } from './SPCGSwTT/computeHash'
import { exeComputeCommitScript, exeEncScript } from './SPCGSwTT/execPythonScript'
import { KZG, generatePolynomial } from './SPCGSwTT/lagrange'
const { load, cut } = require('@node-rs/jieba');

load();
//注册一个回调函数，当登录或注册时调用，传递一个布尔值参数，表示是登录还是注册
const onLoginOrRegister = (callback) => {
    ipcMain.on('loginOrRegister', (event, isLogin) => {
        callback(isLogin)
    })
}

const onLoginSuccess = (callback) => {
    ipcMain.on('openChat', (e, config) => {
        store.initUserId(config.userId)
        store.setUserData('token', config.token)
        //TODO 增加用户配置
        addUserSetting(config.userId, config.email)
        callback(config)
        //初始化ws连接
        initWs(config, e.sender)

    })
}

const winTitleOp = (callback) => {
    ipcMain.on('winTitleOp', (event, data) => {
        // console.log('winTitleOp',data)
        callback(event, data)
    })
}

const onSetLocalStore = () => {
    ipcMain.on('setLocalStore', (e, { key, value }) => {
        store.setData(key, value)
        // console.log(store.getData(key))
    })
}

const onGetLocalStore = () => {
    ipcMain.on('getLocalStore', (e, key) => {
        e.sender.send('getLocalStoreCallback', store.getData(key))
    })
}



const onLoadSessionData = () => {
    ipcMain.on('loadSessionData', async (e) => {
        const dataList = await selectUserSessionList()
        e.sender.send('loadSessionDataCallback', dataList)

    })
}

const onDelChatSession = () => {
    ipcMain.on('delChatSession', (e, contactId) => {
        delChatSession(contactId)
    })
}
const onTopChatSession = () => {
    ipcMain.on('topChatSession', (e, { contactId, topType }) => {
        console.log("ipc", contactId, topType)
        topChatSession(contactId, topType)
    })
}

const onLoadChatMessage = () => {
    ipcMain.on('loadChatMessage', async (e, data) => {
        const result = await selectMessageList(data)
        e.sender.send('loadChatMessageCallback', result)
    })
}

const onSetSessionSelect = () => {
    ipcMain.on('setSessionSelect', async (e, { contactId, sessionId }) => {
        if (sessionId) {
            store.setUserData("currentSessionId", sessionId)
            readAll(contactId)
        } else {
            store.deleteUserData("currentSessionId")
        }
    })
}

const onUpdateLastMessage = () => {
    ipcMain.on("updateLastMessage", async (e, sessionInfo) => {
        await saveOrUpdate4Message(store.getUserData("currentSessionId"), sessionInfo);
    })
}
const onAddLocalMessage = () => {
    ipcMain.on("addLocalMessage", async (e, data) => {
        await saveMessage(data)

        //保存文件
        if (data.messageType == 5) {

            await saveFile2Local(data.messageId, data.filePath, data.fileType)

            const updateInfo = {
                status: 1,
            }
            await updateMessage(updateInfo, { messageId: data.messageId })
        }

        //更新session
        data.lastReceiveTime = data.sendTime
        //TODO 更新会话
        updateSessionInfo4Message(store.getUserData("currentSessionId"), data)
        e.sender.send('addLocalCallback', { status: 1, messageId: data.messageId })
    })
}

const onAddLocalMessage4NoReadCount = () => {
    ipcMain.on("addLocalMessage4NoReadCount", async (e, data) => {
        await saveMessage4User(data)
        //更新session
        data.lastReceiveTime = data.sendTime
        //TODO 更新会话
        updateSessionInfo4MessageNoReadCount(store.getUserData("currentSessionId"), data)
    })
}
const onCreateCover = () => {
    ipcMain.on("createCover", async (e, localFilePath) => {
        const stream = await createCover(localFilePath)
        e.sender.send("createCoverCallback", stream)
    })

}

const openNewWindow = () => {
    ipcMain.on("newWindow", async (e, config) => {

        openWindow(config)
    })
}
const openWindow = ({ windowId, title = "EasyChat", path, width = 720, height = 540, data }) => {

    //传端口
    const localServerPort = store.getUserData("localServerPort")
    data.localServerPort = localServerPort

    let newWindow = getWindow(windowId);
    if (!newWindow) {
        newWindow = new BrowserWindow({
            icon: icon,
            width: width,
            height: height,
            fullscreenable: false,
            fullscreen: false,
            maximizable: false,
            titleBarStyle: "hidden",//右上方 - [] X 工具条隐藏
            autoHideMenuBar: true,
            resizable: false,//不允许修改窗体大小
            frame: true,//：显示窗口边框和标题栏
            transparent: true,//控制窗口背景是否透明。
            hasShadow: false,
            webPreferences: {
                preload: join(__dirname, '../preload/index.js'),
                sandbox: false,
                contextIsolation: false
            }
        })
        saveWidnow(windowId, newWindow)
        newWindow.setMinimumSize(600, 484)
        if (is.dev && process.env['ELECTRON_RENDERER_URL']) {
            newWindow.loadURL(`${process.env['ELECTRON_RENDERER_URL']}/index.html#${path}`)
        } else {
            newWindow.loadFile(join(__dirname, `../renderer/index.html`), { hash: `${path}` })
        }
        if (NODE_ENV === 'development') {
            newWindow.webContents.openDevTools()
        }
        newWindow.on('ready-to-show', () => {
            newWindow.setTitle(title)
            newWindow.show()
        })
        newWindow.on('close', () => {
            setTimeout(() => {
                newWindow.webContents.send('pageInitData', data)
            }, 500)
        })
        newWindow.on('closed', () => {
            delWindow(windowId)
        })
    } else {
        newWindow.show()
        newWindow.setSkipTaskbar(false)
        newWindow.webContents.send('pageInitData', data)
    }
}

const onSaveAs = () => {
    ipcMain.on("saveAs", async (e, data) => {
        saveAs(data)
    })
}

const onSaveClipBoardFile = () => {
    ipcMain.on("saveClipBoardFile", async (e, data) => {
        const result = await saveClipBoardFile(data)
        e.sender.send("saveClipBoardFileCallback", result)
    })
}

const onLoadContactApply = () => {
    ipcMain.on('loadContactApply', async (e) => {
        const userId = store.getUserId()
        let resp = await selectSettingInfo(userId)
        let contactNoRead = 0
        if (resp != null) {
            contactNoRead = resp.contactNoRead
        }
        console.log("contactNoRead", contactNoRead)

        e.sender.send("loadContactApplyCallback", contactNoRead)
    })
}

const onUpdateContactNoReadCount = () => {
    ipcMain.on("updateContactNoReadCount", async (e) => {
        updateContactNoReadCount({ userId: store.getUserId() })
    })

}

const onReloadChatSession = () => {
    ipcMain.on("reloadChatSession", async (e, { contactId }) => {
        await updateStatus(contactId)
        const chatSessionList = await selectUserSessionList()
        console.log(contactId, chatSessionList)
        e.sender.send("reloadChatSessionCallback", { contactId, chatSessionList })
    })
}

const onGenKeys = () => {
    ipcMain.on("genKeys", async (e, { email }) => {
        const edch = await generateAndSaveECDHKeyPair(email)
        const email_pk = edch.getPublicKey('hex')
        e.sender.send("genKeysPkCallback", { pk: email_pk })
    })
}

const onLoadShareKey = () => {
    ipcMain.on("loadShareKey", async (e, { pk: pk, email1: email1, email2: email2 }) => {
        //通信方的pk
        const edch = await loadECDHFromPrivateKey(email1)
        const sharedSecret = await generateSharedSecret(edch, pk)
        saveSharedSecretToFile(sharedSecret, email1, email2)
        e.sender.send("loadShareKeyCallback", { sharedSecret: sharedSecret })
    })
}

const onLoadAESKey = () => {
    ipcMain.on("loadAESKey", async (e, email1, email2) => {
        const AESKey = await deriveAESKey(email1, email2)
        e.sender.send("loadAESKeyCallback", { AESKey: AESKey })
    })
}

const onGenUserKey = () => {
    ipcMain.on("genUserKey", async (e, data) => {
        // console.log(readDataFromFile())
        if (!readDataFromFile()) {
            //如果是空的，就创建
            execute()
        }
        e.sender.send("genUserKeyCallback", readDataFromFile())//返回数据
    })
}
const onComputeHash = () => {
    ipcMain.on("computeHash", async (e, word) => {
        const hash = await computeHash(word)
        e.sender.send("computeHashCallback", hash)
    })
}

const dataForPython = () => {
    ipcMain.on("dataForPython", async (e, dataFromVue) => {
        console.log(`接收到的数据: ${dataFromVue}`);
        try {
            const result = await exeEncScript(JSON.parse(dataFromVue));
            console.log(`脚本输出: ${result}`);
            e.sender.send("dataForPythonCallback", result);
        } catch (error) {
            console.error(error);
        }
    });
}

const computeCommit = () => {
    ipcMain.on("computeCommit", async (e, data) => {
        try {
            console.log("接收到的数据1: ", data);
            const result = await exeComputeCommitScript(data);
            console.log(`脚本输出: ${result}`);
            e.sender.send("computeCommitCallback", result);
        } catch (error) {
            console.error(error);
        }
    });
}
const getUserPk = () => {
    ipcMain.on("getUserPk", async (e, data) => {
        const userKey = readDataFromFile()
        console.log("getUserPk", userKey.user.h)
        e.sender.send("getUserPkCallback", userKey.user.h)
    })
}

const segmentation = () => {
    ipcMain.on("segmentation", async (e, data) => {
        let res = cut(data, false);

        // 过滤掉标点符号
        const punctuationRegex = /[^\p{L}\p{N}]+/u;
        let filteredRes = res.filter(word => !punctuationRegex.test(word));
        e.sender.send("segmentationCallback", filteredRes);
    })

}
const userDir = 'D:'
const saveDir = userDir + "\\.easyChat\\fileStorage\\keys\\"//保存密钥的目录
const generateRandomLagrange = () => {
    ipcMain.on("generateRandomLagrangeWithKZGZK", async (e, data) => {
        //1.生成多项式   
        const originalCoefficients = generatePolynomial(2);
        const kzg = new KZG();
        // 2. 生成多项式承诺
        const commitment = kzg.commit(originalCoefficients);
        // 6. 使用KZG进行零知识证明
        console.log('\n=== KZG零知识证明验证 ===');
        const z = new Decimal(Math.random()).mul(1000).toFixed(0); // 随机生成一个z值
        const y = f0_original;

        // 生成证明
        const proof = kzg.prove(originalCoefficients, z);
        console.log('已生成证明');

        // 验证证明
        const isValid = kzg.verify(commitment, z, y, proof);
        console.log(`证明验证结果: ${isValid ? '有效' : '无效'}`);
        e.sender.send("generateRandomLagrangeWithKZGZKCallback", { commitment, proof, y, z, isValid });
    })
}

export {
    onLoginOrRegister,
    onLoginSuccess,
    winTitleOp,
    onSetLocalStore,
    onGetLocalStore,
    onLoadSessionData,
    onDelChatSession,
    onTopChatSession,
    onLoadChatMessage,
    onAddLocalMessage,
    onSetSessionSelect,
    onCreateCover,
    openNewWindow,
    onSaveAs,
    onSaveClipBoardFile,
    onLoadContactApply,
    onUpdateContactNoReadCount,
    onReloadChatSession,
    onGenKeys,
    onLoadShareKey,
    onLoadAESKey,
    onUpdateLastMessage,
    onAddLocalMessage4NoReadCount,
    onGenUserKey,
    onComputeHash,
    dataForPython,
    computeCommit,
    getUserPk,
    segmentation,
    openWindow,
    generateRandomLagrange,

}
