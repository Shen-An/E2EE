const { exec } = require('child_process');
const fs = require('fs');
const path = require('path');
import store from '../store'
// 替换为你的 Python 解释器的完整路径
const pythonPath = 'D:\\Anaconda\\python.exe';
const userDir = 'D:'
const saveDir = userDir + "\\.easyChat\\fileStorage\\keys\\"//保存密钥的目录
// 调用 Python 脚本的函数
const execute = () => {
    exec(`${pythonPath} -X utf8 "D:/java code/Chat/easychat-ui/easychat-front/src/main/SPCGSwTT_py/UserKey.py"`, {
        encoding: 'utf8',
    }, (error, stdout, stderr) => {
        if (error) {
            console.error(`执行错误: ${error.message}`);
            return;
        }
        // 过滤 jieba 的初始化日志（非错误）
        const isJiebaLog = /Building prefix dict|Loading model|Prefix dict/.test(stderr);
        if (stderr && !isJiebaLog) {
            console.error(`Python 脚本错误: ${stderr}`);
            return;
        }
        try {
            const data = JSON.parse(stdout);
            console.log("User:", data.user);
            console.log("Tag:", data.tag);
            // 保存数据到文件
            saveDataToFile(data);
        } catch (parseError) {
            console.error("解析 JSON 数据时出错:", parseError);
        }
    });
};

// 保存数据到文件的函数
const saveDataToFile = (data) => {
    const userDataPath = path.join(saveDir, store.getUserId()+'_SPCE.json');
    const jsonData = JSON.stringify(data, null, 2);
    fs.writeFile(userDataPath, jsonData, (err) => {
        if (err) {
            console.error('保存数据到文件时出错:', err);
        } else {
            console.log('数据已成功保存到文件:', userDataPath);
        }
    });
};

// 从文件读取数据的函数
const readDataFromFile = () => {
    const userDataPath = path.join(saveDir, store.getUserId()+'_SPCE.json');
    try {
        const fileContent = fs.readFileSync(userDataPath, 'utf8');
        return JSON.parse(fileContent);
    } catch (error) {
        console.error('从文件读取数据时出错:', error);
        return null;
    }
};

// 导出函数
export {
    execute,
    readDataFromFile
};