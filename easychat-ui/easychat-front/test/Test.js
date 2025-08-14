const { load, cut } = require('@node-rs/jieba');

load();

let text = '你好,世界';
let res = cut(text, false);

// 过滤掉标点符号
const punctuationRegex = /[^\p{L}\p{N}]+/u;
let filteredRes = res.filter(word => !punctuationRegex.test(word));

console.log(filteredRes);