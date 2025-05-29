import { app, shell, BrowserWindow, ipcMain } from 'electron'
import { join } from 'path'
import { electronApp, optimizer, is } from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
const NODE_ENV = process.env.NODE_ENV
import store from './store'
import { initWs } from './wsClient'
import { addUserSetting } from './db/UserSettingModel'
import { selectUserSessionList, delChatSession, topChatSession, updateSessionInfo4Message, readAll } from './db/ChatSessionUserModel'
import { selectMessageList, saveMessage, updateMessage } from './db/ChatMessageModel'
import { saveFile2Local, createCover,saveAs,saveClipBoardFile } from './file'
import { delWindow, getWindow, saveWidnow } from './windowProxy'
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
    }else{
        newWindow.show()
        newWindow.setSkipTaskbar(false)
        newWindow.webContents.send('pageInitData', data)
    }
}

const onSaveAs=()=>{
    ipcMain.on("saveAs",async (e,data)=>{
       saveAs(data)
    })
}

const onSaveClipBoardFile =()=>{
    ipcMain.on("saveClipBoardFile",async (e,data)=>{
        const result = await saveClipBoardFile(data)
        e.sender.send("saveClipBoardFileCallback",result)
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
    onSaveClipBoardFile
}
