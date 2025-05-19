const windowManage ={}

const saveWidnow =(id,window)=>{
    windowManage[id] = window
}
const getWindow = (id)=>{
    return windowManage[id]
}

const delWindow = (id) =>{
    delete windowManage[id]
}
export{
    saveWidnow,
    getWindow,
    delWindow,
}