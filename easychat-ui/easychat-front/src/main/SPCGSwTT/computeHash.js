const crypto = require('crypto');
// 计算 SHA-256 哈希值的函数
const computeHash = async (word) => {
    const encoder = new TextEncoder();
    const data = encoder.encode(word);
    const hashBuffer = await crypto.subtle.digest('SHA-256', data);
    
    // 使用 DataView 读取前8字节的 64 位有符号整数（BigInt）
    const view = new DataView(hashBuffer.slice(0, 8));
    const bigIntValue = view.getBigInt64(0, false); // 大端序
    
    return bigIntValue.toString(); // 直接返回字符串形式的 BigInt
};
export{
    computeHash
}