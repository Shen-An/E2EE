console.log('test')
const toCamelCase = (str) => {
    return str.replace(/_([a-z])/g, function (match, p1) {
        return String.fromCharCode(p1.charCodeAt(0) - 32)
    });
}

console.log(toCamelCase('test_id')) 


const convertDbObj2BizObj = (data) => {
    if (!data) {
        return null;
    }
    const bizData = {};
    for (let item in data) {
        bizData[toCamelCase(item)] = data[item]
    }
    return bizData
}

console.log(convertDbObj2BizObj({user_id:1001, user_name:'test'}))