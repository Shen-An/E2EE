import moment from 'moment';
const isEmpty = (str) => {
    if (str === null || str === undefined || str === '') {
        return true;
    }
    return false;
}

const getAreaInfo = (data) => {
    if (isEmpty(data)) {
        return '-';
    }
    return data.replace(",", " ")
}

const formatDate = (timeStamp) => {
    const timeStampTime = moment(timeStamp);
    const days = Number.parseInt(moment().format("YYYYMMDD")) - Number.parseInt(timeStampTime.format("YYYYMMDD"));
    if (days === 0) {
        return timeStampTime.format("HH:mm");
    } else if (days === 1) {
        return "昨天";
    } else if (days >= 2 && days < 7) {
        //显示星期几
        return timeStampTime.format("dddd");
    } else if (days >= 7) {
        //显示年月日
        return timeStampTime.format("YY/MM/DD");
    }
}

const size2Str = (limit) => {
    var size = "";
    if (limit < 0.1 * 1024) {                               //小于0.1KB，转成B
        size.limit.toFixed(2) + "B"
    } else if (limit < 0.1 * 1024 * 1024) {                 //小于0.1MB，转成KB
        size = (limit / 1024).toFixed(2) + "KB"
    } else if (limit < 0.1 * 1024 * 1024 * 1024) {          //小于0.1GB，转成MB
        size = (limit / 1024 / 1024).toFixed(2) + "MB"
    } else {                                          //其他转成GB
        size = (limit / 1024 / 1024 / 1024).toFixed(2) + "GB"
    }
    var sizeStr = size + "";                                //转成字符串
    var index = sizeStr.indexOf('.')                        //获取小数点处的索引
    var dou = sizeStr.substring(index + 1, 2)               //获取小数点后两位的值
    if (dou == "00") {                                      //判断后两位是否为00,如果是则删除00
        return sizeStr.substring(0, index) + sizeStr.substr(index + 3, 2);
    }
    return size;
}
export default {
    isEmpty,
    getAreaInfo,
    formatDate,
    size2Str
}