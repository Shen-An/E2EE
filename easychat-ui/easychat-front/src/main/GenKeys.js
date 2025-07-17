const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const userDir = 'D:'
const saveDir = userDir + "\\.easyChat\\fileStorage\\keys\\"//保存密钥的目录
// 生成 ECDH 密钥对并保存到文件
const generateAndSaveECDHKeyPair = (email) => {
    // 1. 创建 ECDH 实例
    const ecdh = crypto.createECDH('prime256v1'); // 使用 prime256v1 曲线

    // 2. 生成密钥对
    ecdh.generateKeys();

    // 3. 导出私钥和公钥
    const privateKey = ecdh.getPrivateKey('hex'); // 私钥（16 进制格式）
    const publicKey = ecdh.getPublicKey('hex');   // 公钥（16 进制格式）

    // 4. 保存私钥和公钥到文件
    const privateKeyPath = path.join(saveDir, `${email}_privateKey.pem`);
    const publicKeyPath = path.join(saveDir, `${email}_publicKey.pem`);

    fs.writeFileSync(privateKeyPath, privateKey, 'utf8');
    fs.writeFileSync(publicKeyPath, publicKey, 'utf8');

    console.log(`私钥已保存到: ${privateKeyPath}`);
    console.log(`公钥已保存到: ${publicKeyPath}`);

    // 5. 返回 ECDH 实例
    return new Promise(async (resolve, reject) => {
        resolve(ecdh);
    });
};


// 生成共享密钥
const generateSharedSecret = (ecdhInstance, otherPublicKey) => {
    return new Promise(async (resolve, reject) => {
        resolve(ecdhInstance.computeSecret(otherPublicKey, 'hex', 'hex'));
    });

};

// 保存共享密钥到文件
const saveSharedSecretToFile = (sharedSecret, email1, email2) => {
    const filePath = path.join(saveDir, `${email1}_${email2}_sharedSecret.txt`);
    fs.writeFileSync(filePath, sharedSecret, 'utf8');
    console.log(`共享密钥已保存到: ${filePath}`);
};

export {
    generateAndSaveECDHKeyPair,
    saveSharedSecretToFile,
    generateSharedSecret,
}