import { app, shell, BrowserWindow, ipcMain, Menu, Tray } from 'electron'
import { join } from 'path'
import { electronApp, optimizer, is } from '@electron-toolkit/utils'
import icon from '../../resources/icon.png?asset'
const NODE_ENV = process.env.NODE_ENV
import {
  onLoginOrRegister, onLoginSuccess, winTitleOp,
  onSetLocalStore, onGetLocalStore, onLoadSessionData,
  onDelChatSession, onTopChatSession, onLoadChatMessage,
  onAddLocalMessage,onSetSessionSelect, onCreateCover,
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

} from './ipc'
import { saveWidnow } from './windowProxy'

const login_width = 300
const login_height = 362


const register_height = 478

function createWindow() {
  // Create the browser window.
  const mainWindow = new BrowserWindow({
    icon:icon,
    width: login_width,
    height: login_height,
    show: false,
    titleBarStyle: "hidden",//右上方 - [] X 工具条隐藏
    autoHideMenuBar: true,
    resizable: false,//不允许修改窗体大小
    frame: true,//：显示窗口边框和标题栏
    transparent: true,//控制窗口背景是否透明。
 

    webPreferences: {
      preload: join(__dirname, '../preload/index.js'),
      sandbox: false,
      contextIsolation: false
    }
  })

  //管理主窗口
  saveWidnow("main",mainWindow)
  //打开控制台
  if (NODE_ENV === 'development') {
    mainWindow.webContents.openDevTools()
  }

  mainWindow.on('ready-to-show', () => {
    mainWindow.show()
    mainWindow.setTitle('飞信')

  })

  mainWindow.webContents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url)
    return { action: 'deny' }
  })

  // HMR for renderer base on electron-vite cli.
  // Load the remote URL for development or the local html file for production.
  if (is.dev && process.env['ELECTRON_RENDERER_URL']) {
    mainWindow.loadURL(process.env['ELECTRON_RENDERER_URL'])
  } else {
    mainWindow.loadFile(join(__dirname, '../renderer/index.html'))
  }


  //托盘图标
  const tray = new Tray(icon)
  const contextMenu = [
    {
      label: '退出',
      click: function () {
        app.exit()
      }
    }
  ]

  const menu = Menu.buildFromTemplate(contextMenu)
  tray.setToolTip('飞信')
  tray.setContextMenu(menu)
  tray.on('click', () => {
    mainWindow.setSkipTaskbar(false)
    mainWindow.show()
  })

  //回调函数的调用
  onLoginOrRegister((isLogin) => {
    mainWindow.setResizable(true)
    if (isLogin) {
      mainWindow.setSize(login_width, login_height)
    } else {
      mainWindow.setSize(login_width, register_height)
    }
    mainWindow.setResizable(false)
  })

  onLoginSuccess((config) => {
    mainWindow.setResizable(true)
    mainWindow.setSize(800, 600)
    //居中
    mainWindow.center()
    //可以最大化
    mainWindow.setMaximizable(true)
    //最小大小
    mainWindow.setMinimumSize(800, 600)

    //TODO 管理后台，托盘操作
    if (config.admin) {
    }
    contextMenu.unshift({
      label: '用户:' + config.nickName, click: function () {

      }
    })
    tray.setContextMenu(Menu.buildFromTemplate(contextMenu))
  })

  winTitleOp((e, { action, data }) => {
    //获取当前窗口
    const webContents = e.sender
    const win = BrowserWindow.fromWebContents(webContents)

    switch (action) {
      case 'close': {
        if (data.closeType == 0) {
          win.close()
        }
        else {
          win.setSkipTaskbar(true)
          win.hide()
        }
        break;
      }
      case "minimize": {
        win.minimize()
        break;
      }
      case "maximize": {
        win.maximize()
        break;
      }
      case "unmaximize": {

        win.unmaximize()
        break;
      }
      case "top": {
        win.setAlwaysOnTop(data.top)
        break;
      }
    }
  })

  onSetLocalStore()
  onGetLocalStore()
  onLoadSessionData()
  onDelChatSession()
  onTopChatSession()
  onLoadChatMessage()
  onAddLocalMessage()
  onSetSessionSelect()
  onCreateCover()
  openNewWindow()
  onSaveAs()
  onSaveClipBoardFile()
  onLoadContactApply()
  onUpdateContactNoReadCount()
  onReloadChatSession()
  onGenKeys()
  onLoadShareKey()
  onLoadAESKey()
  onUpdateLastMessage()
  onAddLocalMessage4NoReadCount()
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  // Set app user model id for windows
  electronApp.setAppUserModelId('com.electron')

  // Default open or close DevTools by F12 in development
  // and ignore CommandOrControl + R in production.
  // see https://github.com/alex8088/electron-toolkit/tree/master/packages/utils
  app.on('browser-window-created', (_, window) => {
    optimizer.watchWindowShortcuts(window)
  })

  createWindow()

  app.on('activate', function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

// In this file you can include the rest of your app"s specific main process
// code. You can also put them in separate files and require them here.
