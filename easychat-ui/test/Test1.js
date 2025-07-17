import CryptoJS from 'crypto-js';

// 加密消息
const encryptMessage = (message, keyHex) => {
  // 将16进制的 key 转换为 WordArray
  const key = CryptoJS.enc.Hex.parse(keyHex);
  console.log(key);
  // 生成 16 字节（128 位）的随机 IV
  const iv = CryptoJS.lib.WordArray.random(16);
  // 执行加密，注意这里传入 key 而不是 passphrase
  const encrypted = CryptoJS.AES.encrypt(message, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7,
  });
  // 返回对象中包含 iv 和密文（均转换为 16 进制字符串）
  return { iv: iv.toString(CryptoJS.enc.Hex), encrypted: encrypted.ciphertext.toString(CryptoJS.enc.Hex) };
};

// 解密消息
const decryptMessage = (encryptedHex, keyHex, ivHex) => {
  const key = CryptoJS.enc.Hex.parse(keyHex);
  const iv = CryptoJS.enc.Hex.parse(ivHex);
  // 构造一个 CipherParams 对象，密文需要解析为 WordArray
  const cipherParams = CryptoJS.lib.CipherParams.create({
    ciphertext: CryptoJS.enc.Hex.parse(encryptedHex)
  });
  const decrypted = CryptoJS.AES.decrypt(cipherParams, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.Pkcs7,
  });
  return decrypted.toString(CryptoJS.enc.Utf8);
};
encryptMessage("Hello, Bob!", "55774dc645fe4fd88a8ed74d2e05af29aa45f732c7a0f1f545581bbcfe56d57d");