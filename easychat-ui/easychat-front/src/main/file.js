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

//引入express服务器，加载文件(图片，视频等)
const express = require('express');
const expressServer = express();

import { selectByMessageId } from './db/ChatMessageModel'
import { promises } from "dns";

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
    // console.log("messageId",messageId)
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
    axios.post(url, formData, config).then((resp) => {
    }).catch((error) => {
        console.error("File download error:", error);
    });
}

//发出文件将其保存到本地
const getLocalFilePath = (partType, showCover, fileId) => {
    return new Promise(async (resolve, reject) => {

        let localFolder = store.getUserData("localFileFolder");
        let localPath = null;
        if (partType == "avatar") {
            //如果是头像，则保存到avatar文件夹下,这时fileId为用户id
            localFolder = localFolder + "/avatar/";
            if (!fs.existsSync(localFolder)) {
                mkdirs(localFolder);
            }
            localPath = localFolder + fileId + image_suffix;
        } else if (partType == "chat") {
            //如果是聊天文件，则保存到chat文件夹下，这时fileId为消息id
            let messageInfo = await selectByMessageId(fileId);
            const month = moment(Number.parseInt(messageInfo.sendTime)).format('YYYYMM');
            localFolder = localFolder + "/" + month;
            if (!fs.existsSync(localFolder)) {
                mkdirs(localFolder);
            }
            let fileSuffix = messageInfo.fileName;
            fileSuffix = fileSuffix.substring(fileSuffix.lastIndexOf("."));
            localPath = localFolder + "/" + fileId + fileSuffix;
            if (showCover) {
                localPath = localPath + cover_image_suffix;
            }
        }

        resolve(localPath);
    });

}

let server = null;
const startLocalServer = (serverPort) => {
    server = expressServer.listen(serverPort, () => {
        console.log(`本地127.0.0.1文件服务器启动成功，端口号为${serverPort}`);
    }
    );
}
const closeLocalServer = () => {
    if (server) {
        server.close();
    }
}

//express本地服务器
const FILE_TYPE_CONTENT_TYPE = {
    "0": "image/",
    "1": "video/",
    "2": "application/octet-stream"
}

/**
 * 本地服务器接口，从本地获取文件
 * 例如访问http://127.0.0.1:10240/file?fileId=1
 * 则let {partType,fileType,fileId,showCover,forceGet} = req.query;中fileId为1
 */
expressServer.get("/file", async (req, resp) => {
    let { partType, fileType, fileId, showCover, forceGet } = req.query;//forceGet是否强制从服务器获取
    if (!partType || !fileId) {
        resp.send("参数错误");
        return;
    }
    showCover = showCover == undefined ? false : Boolean(showCover);
    const localPath = await getLocalFilePath(partType, showCover, fileId);
    if (!fs.existsSync(localPath) || forceGet == "true") {
        if (forceGet == "true" && partType == "avatar") {
            await downLoadFile(fileId, true, localPath + cover_image_suffix, partType);
        }
        await downLoadFile(fileId, showCover, localPath, partType);
    }
    //获取文件后缀
    const fileSuffix = localPath.substring(localPath.lastIndexOf(".") + 1);
    //如fileType返回0，则FILE_TYPE_CONTENT_TYPE[0]为image/ 则contentType为image/png
    let contentType = FILE_TYPE_CONTENT_TYPE[fileType] + fileSuffix;
    //解决跨域
    resp.setHeader("Access-Control-Allow-Origin", "*");
    //设置响应头
    resp.setHeader("Content-Type", contentType);
    //将文件流写入到响应中
    fs.createReadStream(localPath).pipe(resp);
    return;

})
//头像下载
const downLoadFile = (fileId, showCover, savePath, partType) => {

    //使showCover为字符串类型
    showCover = showCover + "";
    let url = `${getDomain()}/api/chat/downloadFile`;
    const token = store.getUserData("token");

    // let resourcesPath = getResourcesPath();
    // console.log(resourcesPath + "/assets/user.png")

    return new Promise(async (resolve, reject) => {
        const config = {
            responseType: 'stream',
            headers: { 'Content-Type': 'multipart/form-data', 'token': token },
        };
        let resp = await axios.post(url, {
            fileId,
            showCover
        }, config);

        //获取文件目录 如D:/chat/202108/xxxx.png 得到D:/chat/202108
        const folder = savePath.substring(0, savePath.lastIndexOf("/"));

        mkdirs(folder);

        //将文件

        const stream = fs.createWriteStream(savePath);
        // console.log(resp.headers["content-type"])
        //错误处理
        if (resp.headers["content-type"] == "application/json;charset=utf-8") {
            // console.log("savepath:" + savePath);
            // console.log(getResourcesPath() + "/assets/user.png");
            let resourcesPath = getResourcesPath();
            if (partType == "avatar") {
                fs.createReadStream(resourcesPath + "/assets/user.png").pipe(stream);
            } else {
                fs.createReadStream(resourcesPath + "/assets/404.png").pipe(stream);
            }
        } else {
            //将文件流写入到响应中
            resp.data.pipe(stream);
        }
        stream.on('finish', () => {
            stream.close();
            resolve();
        });
    })
}

const createCover = (filePath) => {
    return new Promise(async (resolve, reject) => {
        let ffmpegPath = getFFmpegPath();
        let avatarPath = await getLocalFilePath("avatar", false, store.getUserId() + "_temp");
        //压缩
        let command = `"${ffmpegPath}" -i "${filePath}" "${avatarPath}" -y`;
        await execCommand(command)

        let coverPath = await getLocalFilePath("avatar", false, store.getUserId() + "_temp_cover");
        command = `"${ffmpegPath}" -i "${filePath}" -y -vframes 1 -vf "scale=min(170\\,iw*min(170/iw\\,170/ih)):min(170\\,ih*min(170/iw\\,170/ih))" "${coverPath}"`;
        await execCommand(command)
        resolve({
            avatarStream: fs.readFileSync(avatarPath),
            coverStream: fs.readFileSync(coverPath)
        })
    })
}
export {
    saveFile2Local,
    startLocalServer,
    closeLocalServer,
    createCover
}