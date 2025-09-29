const p = 997;
const generator = 3; // 3是Z_997*的原根

function generateAllElements(generator, modulus) {
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

// 生成所有元素
const allElements = generateAllElements(generator, p);

// 输出前20个和后20个元素（完整列表共996个）
console.log("Z_997*的前20个元素:");
console.log(allElements.slice(0, 20));
console.log("\nZ_997*的后20个元素:");
console.log(allElements.slice(-20));
console.log("\n元素总数:", allElements);