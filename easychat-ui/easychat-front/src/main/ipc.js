import { app, shell, BrowserWindow, ipcMain } from 'electron'
import { join } from 'path'
import { electronApp, optimizer, is } from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
const NODE_ENV = process.env.NODE_ENV
import store from './store'

//注册一个回调函数，当登录或注册时调用，传递一个布尔值参数，表示是登录还是注册
const onLoginOrRegister = (callback) => {
    ipcMain.on('loginOrRegister', (event, isLogin) => {
        callback(isLogin)
    })
}

const onLoginSuccess = (callback) => {
    ipcMain.on('openChat', (event, config) => {
        store.initUserId(config.userId)
        store.setUserData('token', config.token)
        //TODO 增加用户配置
        callback(config)
         //TODO 初始化ws连接

    })
}

const winTitleOp = (callback) => {
    ipcMain.on('winTitleOp', (event, data) => {
        console.log('winTitleOp',data)
        callback(event,data)
    })
}

const onSetLocalStore = () => {
    ipcMain.on('setLocalStore', (e, {key,value}) => {
        store.setData(key, value)
        // console.log(store.getData(key))
    })
}

const onGetLocalStore = () => {
    ipcMain.on('getLocalStore', (e, key) => {
        e.sender.send('getLocalStoreCallback', "主进程返回内容"+store.getData(key))
    })
}

export {
    onLoginOrRegister,
    onLoginSuccess,
    winTitleOp,
    onSetLocalStore,
    onGetLocalStore
}
