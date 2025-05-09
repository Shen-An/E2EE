const fs = require('fs');
const fse = require('fs-extra');
const NODE_ENV = process.env.NODE_ENV;
const path = require('path');
const { app, ipcMain, shell } = require('electron');
const { exec } = require('child_process');
const FormData = require('form-data');//引入form-data模块（用于构建表单数据）
const axios = require('axios');//引入axios模块（用于发送http请求）
import store from "./store"
const moment = require('moment');
moment.locale('zh-cn', {});

import { selectByMessageId } from './db/ChatMessageModel'

const mkdirs = (dir) => {
    if (!fs.existsSync(dir)) {
        const parentDir = path.dirname(dir);
        if (parentDir !== dir) {
            mkdirs(parentDir);
        }
        fs.mkdirSync(dir);
    }
}
const saveFile2Local = (messageId, filePath, fileType) => {
    return new Promise(async(resolve, reject) => {
        let savePath = await getLocalFilePath("chat", false, messageId);
        fs.copyFileSync(filePath, savePath);
        resolve();
    });
}

const getLocalFilePath = (partType, showCover,fileId) => {
    return new Promise(async(resolve, reject) => {

        let localFolder = store.getUserData("localFileFolder");
        let localPath = null;
        if(partType == "avatar"){
            
        }else if(partType == "chat"){
            let messageInfo = await selectByMessageId(fileId);
            const month = moment(Number.parseInt(messageInfo.sendTime)).format('YYYYMM');
            localFolder = localFolder + "/" + month;
            if(!fs.existsSync(localFolder)){
                mkdirs(localFolder);
            }
            let fileSuffix = messageInfo.fileName;
            fileSuffix =  fileSuffix.substring(fileSuffix.lastIndexOf("."));
            localPath = localFolder + "/" + fileId + fileSuffix;
        }
        resolve(localPath);
    });
        
}

export {
    saveFile2Local
}