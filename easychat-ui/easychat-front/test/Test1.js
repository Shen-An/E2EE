const { load, cut } = require('@node-rs/jieba');
const CryptoJS = require('crypto-js');

function H(x) {
    // 使用CryptoJS的SHA256算法计算哈希值
    let hash = CryptoJS.SHA256(x).toString();
    // 将哈希值转换为数字并取绝对值后对1000取模
    let num = parseInt(hash, 16);
    return Math.abs(num) % 1000;
}


// 布谷鸟哈希函数，根据不同的哈希函数编号计算哈希值
const cuckooHash = (x, size, num) => {
    if (num === 0) {
        return H(x) % size;
    } else {
        return (H(x) % size + 2) % size;
    }
};

// 随机自规约函数，计算 Q 和 S
const randomSelfReduction = (g, A, H_y, B) => {
    // 固定 beta 和 gamma 进行测试
    const beta = 3;
    const gamma = 5;
    const alpha = 5;
    console.log("H(y)", H_y);
    // 计算 B
    console.log("B", B);
    // 计算 Q
    const Q = ((Math.pow(g, beta) % 1000) * (Math.pow(H_y, gamma) % 1000)) % 1000;
    // 计算 S
    const S = (Math.pow(A, beta) % 1000) * (Math.pow(B, gamma) % 1000) % 1000;

    if (Math.pow(Q, alpha) % 1000 == S) {
        console.log("Q和S相等");
    } else {
        console.log(Math.pow(Q, alpha) % 1000);
    }

    return [Q, S];
};

// SPC 加密函数
const SPCEnc = (msg, B, g, A, alpha) => {
    const words = cut(msg, false);
    const map0Array = [];
    const map1Array = [];

    for (let i = 0; i < words.length; i++) {
        const index0 = cuckooHash(words[i], B.length, 0);
        const index1 = cuckooHash(words[i], B.length, 1);

        const B_new0 = B[index0][0] 
        const B_new1 = B[index1][0] || Math.pow(g, Math.ceil(Math.random() * 10));

        console.log("B_new0", B_new0);
        console.log("B_new1", B_new1);
        const map0 = new Map();
        const map1 = new Map();

        map0.set("g", g);
        map0.set("A", A);
        map0.set("H(y)", H(words[i]));
        map0.set("B", B_new0);

        map1.set("g", g);
        map1.set("A", A);
        map1.set("H(y)", H(words[i]));
        map1.set("B", B_new1);

        map0Array.push(map0);
        map1Array.push(map1);
    }
    let QS0 = []
    let QS1 = []
    for (let i = 0; i < map0Array.length; i++) {
        ret1 = randomSelfReduction(
            map0Array[i].get("g"),
            map0Array[i].get("A"),
            map0Array[i].get("H(y)"),
            map0Array[i].get("B"),
        )
        QS0.push(ret1)
        ret2 = randomSelfReduction(
            map1Array[i].get("g"),
            map1Array[i].get("A"),
            map1Array[i].get("H(y)"),
            map1Array[i].get("B"),
            alpha
        )
        QS1.push(ret2)
    }
    return { QS0, QS1 };
};

// 初始化参数
const alpha = 5;
const g = 3;
const A = 243;

// 调用 SPCEnc 函数进行加密
console.log(SPCEnc("钻石毒品粉", [[], [], [], [], [], [], [], [], [], [901], [], [901], [], [], [], [], [], [], [], [], [], [24], [976], [24], [976], [], [], [], [], []], g, A, alpha));
