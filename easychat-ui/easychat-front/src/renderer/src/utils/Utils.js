
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

export default {
    isEmpty,
    getAreaInfo
}