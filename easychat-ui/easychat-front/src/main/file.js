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

const cover_image_suffix = "_cover.png";
const image_suffix = ".png";

const ffprobePath = "/assets/ffprobe.exe";
const ffmepegPath = "/assets/ffmpeg.exe";

const getDomain = () => {
    // console.log("NODE_ENV"+NODE_ENV);
    // console.log("prodDomain"+store.getData("prodDomain"));
    // console.log("devDomain"+store.getData("devDomain"));
    return NODE_ENV !== 'development' ? store.getData("prodDomain") : store.getData("devDomain");
}

const mkdirs = (dir) => {
    if (!fs.existsSync(dir)) {
        const parentDir = path.dirname(dir);
        if (parentDir !== dir) {
            mkdirs(parentDir);
        }
        fs.mkdirSync(dir);
    }
}

//获取资源路径
const getResourcesPath = () => {
    //开发环境下，resourcesPath在app目录下
    let resourcesPath = app.getAppPath();
    //生产环境下，resourcesPath在exe目录+/resources下
    if (NODE_ENV !== 'development') {
        resourcesPath = path.dirname(app.getPath('exe') + "/resources");
    }
    return resourcesPath;
}
//获取ffprobe路径
const getFFprobePath = () => {
    return path.join(getResourcesPath(), ffprobePath);
}

//获取ffmpeg路径
const getFFmpegPath = () => {
    //path.join()方法用于连接路径
    return path.join(getResourcesPath(), ffmepegPath);
}

const execCommand = (command) => {
    return new Promise((resolve, reject) => {
        exec(command, (error, stdout, stderr) => {
            if (error) {
                console.error(`执行命令失败: ${error}`);
            }
            resolve(stdout);
        });
    });
}

const saveFile2Local = (messageId, filePath, fileType) => {
    return new Promise(async (resolve, reject) => {
        let ffmpegPath = getFFmpegPath();
        let savePath = await getLocalFilePath("chat", false, messageId);
        fs.copyFileSync(filePath, savePath);

        let coverPath = null;//封面路径

        //文件类型不等于2 ，则不是文件，是图片或者视频
        if (fileType != 2) {
            let command = `"${getFFprobePath()}" -v error -select_streams v:0 -show_entries stream=codec_name "${savePath}"`;
            let result = await execCommand(command);
            //去掉换行符
            result = result.replaceAll("\r\n", "");
            //找到编码格式所在的字符串如"=hevc[/STREAM]"
            result = result.substring(result.indexOf("=") + 1);
            //从"[开始截取,取之前的字符串
            let codec = result.substring(0, result.indexOf("["));
            /**
             * 上述作用是截取出视频的编码格式 如h264或者hevc：
             * execCommand(command)执行后cmd会返回如
             * [STREAM]
             * codec_name=hevc
             * [/STREAM]
             */

            //如果是hevc编码格式，则转换为h264
            if ("hevc" === codec) {
                command = `"${ffmpegPath}" -y -i "${filePath}" -c:v libx264 -crf 20 "${savePath}"`;
                await execCommand(command);
            }
            coverPath = savePath + cover_image_suffix;
            command = `"${ffmpegPath}" -i "${savePath}" -y -vframes 1 -vf "scale=min(170\\,iw*min(170/iw\\,170/ih)):min(170\\,ih*min(170/iw\\,170/ih))" "${coverPath}"`;
            await execCommand(command);
        }
        upLoadFile(messageId, savePath, coverPath);
        resolve();
    });
}

const upLoadFile = (messageId, savePath, coverPath) => {
    const formData = new FormData();
    formData.append("messageId", messageId);
    formData.append("file", fs.createReadStream(savePath));
    if (coverPath) {
        formData.append("cover", fs.createReadStream(coverPath));
    }
    const url = `${getDomain()}/api/chat/uploadFile`;
    // console.log("url:"+url);
    const token = store.getUserData("token");
    const config = { headers: { 'Content-Type': 'multipart/form-data', 'token': token } };
    axios.post(url, formData, config).then((res) => {

    }).catch((err) => {
        console.error("文件上传失败"+err);
    });
}

//发出文件将其保存到本地
const getLocalFilePath = (partType, showCover, fileId) => {
    return new Promise(async (resolve, reject) => {

        let localFolder = store.getUserData("localFileFolder");
        let localPath = null;
        if (partType == "avatar") {

        } else if (partType == "chat") {
            let messageInfo = await selectByMessageId(fileId);
            const month = moment(Number.parseInt(messageInfo.sendTime)).format('YYYYMM');
            localFolder = localFolder + "/" + month;
            if (!fs.existsSync(localFolder)) {
                mkdirs(localFolder);
            }
            let fileSuffix = messageInfo.fileName;
            fileSuffix = fileSuffix.substring(fileSuffix.lastIndexOf("."));
            localPath = localFolder + "/" + fileId + fileSuffix;
        }
        resolve(localPath);
    });

}

export {
    saveFile2Local
}