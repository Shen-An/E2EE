const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
const userDir = 'D:'
const saveDir = userDir + "\\.easyChat\\fileStorage\\keys\\"//保存密钥的目录



// 派生 AES 密钥
const deriveAESKey = (email) => {
    const sharedSecretPath = path.join(saveDir, `${email}_sharedSecret.txt`);
    if (!fs.existsSync(sharedSecretPath)) {
        throw new Error(`未找到 ${email} 的共享密钥文件`);
    }
    const sharedSecret = fs.readFileSync(sharedSecretPath, 'utf8');
    const hash = crypto.createHash('sha256'); // 使用 SHA-256 哈希函数
    hash.update(sharedSecret, 'hex'); // 输入共享密钥
    return hash.digest(); // 输出 256 位（32 字节）的 AES 密钥
};

// 加密消息
const encryptMessage = (message, key) => {
    const iv = crypto.randomBytes(16); // 生成 16 字节的随机 IV
    const cipher = crypto.createCipheriv('aes-256-cbc', key, iv); // 使用 AES-256-CBC 模式
    let encrypted = cipher.update(message, 'utf8', 'hex');
    encrypted += cipher.final('hex');
    return { iv: iv.toString('hex'), encrypted }; // 返回 IV 和密文
};

// 解密消息
const decryptMessage = (encrypted, key, iv) => {
    const decipher = crypto.createDecipheriv('aes-256-cbc', key, Buffer.from(iv, 'hex'));
    let decrypted = decipher.update(encrypted, 'hex', 'utf8');
    decrypted += decipher.final('utf8');
    return decrypted;
};

// 示例：生成 AES 密钥并加密/解密消息
const aesKey = deriveAESKey("1@qq.com");
console.log('AES 密钥:', aesKey.toString('hex'));

const message = 'Hello, Bob!';
const { iv, encrypted } = encryptMessage(message, aesKey);
console.log('加密后的消息:', encrypted);
console.log('IV:', iv);

const decryptedMessage = decryptMessage(encrypted, aesKey, iv);
console.log('解密后的消息:', decryptedMessage);