const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const userDir = 'D:'
const saveDir = userDir + "\\.easyChat\\fileStorage\\keys\\"//保存密钥的目录
// 从文件中加载私钥并生成 ECDH 实例
const loadECDHFromPrivateKey = (email) => {
    return new Promise(async (resolve, reject) => {
        // 1. 读取私钥文件
        const privateKeyPath = path.join(saveDir, `${email}_privateKey.pem`);
        if (!fs.existsSync(privateKeyPath)) {
            throw new Error(`未找到 ${email} 的私钥文件`);
        }

        const privateKeyHex = fs.readFileSync(privateKeyPath, 'utf8');

        // 2. 创建 ECDH 实例并导入私钥
        const ecdh = crypto.createECDH('prime256v1');
        ecdh.setPrivateKey(privateKeyHex, 'hex');

        // 3. 返回 ECDH 实例

        resolve(ecdh);
    });
};

// 从文件中加载公钥
const loadPublicKeyFromFile = (email) => {
    return new Promise(async (resolve, reject) => {
        const publicKeyPath = path.join(saveDir, `${email}_publicKey.pem`);
        if (!fs.existsSync(publicKeyPath)) {
            throw new Error(`未找到 ${email} 的公钥文件`);
        }
        resolve(fs.readFileSync(publicKeyPath, 'utf8'));
    });
};

// 从文件中加载共享密钥
const loadSharedSecretFromFile = (email1,email2) => {
    return new Promise(async (resolve, reject) => {
        const filePath = path.join(saveDir, `${email1}_${email2}_sharedSecret.txt`);
        if (!fs.existsSync(filePath)) {
            throw new Error(`未找到 ${email1}_${email2} 的共享密钥文件`);
        }
        resolve(fs.readFileSync(filePath, 'utf8'));
    });

};

export {
    loadECDHFromPrivateKey,
    loadPublicKeyFromFile,
    loadSharedSecretFromFile
}