import moment from 'moment';
const isEmpty = (str) =>{
    if(str === null || str === undefined || str === ''){
        return true;
    }
    return false;
}

const getAreaInfo = (data)=>{
    if(isEmpty(data)){
        return '-';
    }
    return data.replace(","," ")
}

const formatDate = (timeStamp) => {
   const timeStampTime = moment(timeStamp);
   const days = Number.parseInt(moment().format("YYYYMMDD")) - Number.parseInt(timeStampTime.format("YYYYMMDD"));
    if(days === 0){
         return timeStampTime.format("HH:mm");
    }else if(days === 1){
        return "昨天";
    }else if(days >=2 && days < 7){
        //显示星期几
        return timeStampTime.format("dddd");
    }else if(days >=7){
        //显示年月日
        return timeStampTime.format("YY/MM/DD");
    }
}

export default {
    isEmpty,
    getAreaInfo,
    formatDate
}