import CryptoJS from 'crypto-js';

function H(x) {
    let hash = CryptoJS.SHA256(x).toString();
    let num = BigInt('0x' + hash);
    return Number(num % 1000n);
}

const cuckooHash = (x, size, num) => {
    if (num === 0) {
        return H(x) % size;
    } else {
        return (H(x) % size + 2) % size;
    }
};
// 生成Z_p*的所有元素 ，生成元是3，阶是素数997，这样的素数阶循环群
const p = 997;
const generator = 3; // 3是Z_997*的原根
//生成群
const generateAllElements = (generator, modulus) => {
    const elements = new Set();
    let element = 1;
    let exponent = 0;

    // 通过原根的幂生成所有元素
    while (exponent < modulus - 1) {
        elements.add(element);
        element = (element * generator) % modulus;
        exponent++;
    }

    // 转换为数组并排序
    return Array.from(elements).sort((a, b) => a - b);
}

// 随机选取一个元素的函数
const getRandomElement = (generator, modulus) => {
    const elements = generateAllElements(generator, modulus);
    const randomIndex = Math.floor(Math.random() * elements.length);
    return elements[randomIndex];
}
// 模幂运算：计算 base^exponent % modulus
function modPow(base, exponent, modulus) {
    if (modulus === 1) return 0;

    let result = 1;
    base = base % modulus;

    while (exponent > 0) {
        if (exponent % 2 === 1) {
            result = (result * base) % modulus;
        }
        exponent = exponent >> 1; // 右移一位相当于除以2取整
        base = (base * base) % modulus;
    }

    return result;
}

const randomSelfReduction = (g, A, H_y, B) => {
    const beta = getRandomElement(g, p);
    const gamma = getRandomElement(g, p);

    // 使用 modPow 替代 Math.pow
    const Q = (modPow(g, beta, p) * modPow(H_y, gamma, p)) % p;
    const S = (modPow(A, beta, p) * modPow(B, gamma, p)) % p;


    return [Q, S];
};

async function symmetricEncrypt(S, message) {
    // 将S转换为字符串，作为密钥输入
    const SStr = S.toString();

    // 使用固定盐（16字节0）以兼容后端
    const salt = new Uint8Array(16).buffer; // 盐值全0

    // 生成PBKDF2密钥
    const keyMaterial = await crypto.subtle.importKey(
        'raw',
        new TextEncoder().encode(SStr),
        { name: 'PBKDF2' },
        false,
        ['deriveKey']
    );
    const key = await crypto.subtle.deriveKey(
        {
            name: 'PBKDF2',
            salt: salt,
            iterations: 100000,
            hash: 'SHA-256'
        },
        keyMaterial,
        { name: 'AES-CBC', length: 128 },
        true,
        ['encrypt', 'decrypt']
    );

    // 加密逻辑（保持AES-CBC，IV需传至后端）
    const iv = crypto.getRandomValues(new Uint8Array(16));
    const encrypted = await crypto.subtle.encrypt(
        { name: 'AES-CBC', iv: iv },
        key,
        new TextEncoder().encode(message)
    );

    // 返回十六进制字符串
    return {

        encryptedMessage: Array.from(new Uint8Array(encrypted), b => b.toString(16).padStart(2, '0')).join(''),
        iv: Array.from(iv, b => b.toString(16).padStart(2, '0')).join('')
    };
}

async function symmetricDecrypt(Q, alpha, encryptedMessage, iv) {
    const S = Math.pow(Q, alpha) % 997;
    // 从 S 生成密钥
    const keyMaterial = await window.crypto.subtle.importKey(
        'raw',
        new TextEncoder().encode(S.toString()),
        { name: 'PBKDF2' },
        false,
        ['deriveBits', 'deriveKey']
    );
    const derivedKey = await window.crypto.subtle.deriveKey(
        {
            name: 'PBKDF2',
            salt: new Uint8Array(16),
            iterations: 100000,
            hash: 'SHA-256'
        },
        keyMaterial,
        { name: 'AES-CBC', length: 128 },
        true,
        ['decrypt']
    );

    const decoder = new TextDecoder();
    const ivArray = hexToUint8Array(iv);
    const encryptedBytes = hexToUint8Array(encryptedMessage);

    const decrypted = await window.crypto.subtle.decrypt(
        {
            name: 'AES-CBC',
            iv: ivArray
        },
        derivedKey,
        encryptedBytes
    );

    return decoder.decode(decrypted);
}

function hexToUint8Array(hex) {
    const len = hex.length;
    const array = new Uint8Array(len / 2);
    for (let i = 0; i < len; i += 2) {
        array[i / 2] = parseInt(hex.substr(i, 2), 16);
    }
    return array;
}

async function SPCEnc(msg, words, B, g, A) {
    const map0Array = [];
    const map1Array = [];
    const encryptedMessages0 = [];
    const encryptedMessages1 = [];


    for (let i = 0; i < words.length; i++) {
        const index0 = cuckooHash(words[i], B.length, 0);
        const index1 = cuckooHash(words[i], B.length, 1);

        const B_new0 = B[index0][0] || Math.pow(g, Math.ceil(Math.random() * 10));
        const B_new1 = B[index1][0] || Math.pow(g, Math.ceil(Math.random() * 10));

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

    let QS0 = [];
    let QS1 = [];
    let iv0Array = []; // 修改变量名，避免与对称加密返回的 iv 冲突
    let iv1Array = []; // 修改变量名，避免与对称加密返回的 iv 冲突
    for (let i = 0; i < map0Array.length; i++) {
        const [Q0, S0] = randomSelfReduction(
            map0Array[i].get("g"),
            map0Array[i].get("A"),
            map0Array[i].get("H(y)"),
            map0Array[i].get("B")
        );
        QS0.push(Q0);
        const { encryptedMessage: encrypted0, iv: iv0 } = await symmetricEncrypt(S0, msg);
        encryptedMessages0.push(encrypted0);
        iv0Array.push(iv0);

        const [Q1, S1] = randomSelfReduction(
            map1Array[i].get("g"),
            map1Array[i].get("A"),
            map1Array[i].get("H(y)"),
            map1Array[i].get("B")
        );
        QS1.push(Q1);
        const { encryptedMessage: encrypted1, iv: iv1 } = await symmetricEncrypt(S1, msg);
        encryptedMessages1.push(encrypted1);
        iv1Array.push(iv1);
    }

    return { QS0, QS1, encryptedMessages0, encryptedMessages1, iv0Array, iv1Array };
}

export {
    H,
    cuckooHash,
    randomSelfReduction,
    symmetricEncrypt,
    symmetricDecrypt,
    SPCEnc
};