const api = {
    prodDomain: "http://127.0.0.1:5050",
    devDomain: "http://127.0.0.1:5050",
    prodWsDomain: "ws://127.0.0.1:5051/ws",
    devWsDomain: "ws://127.0.0.1:5051/ws",
    checkCode: "/account/checkCode",
    login: "/account/login",
    register: "/account/register",
    getSysSetting: "/account/getSysSetting",
    loadMyGroup: "/group/loadMyGroup",
    saveGroup: "/group/saveGroup",
    getGroupInfo: "/group/getGroupInfo",
    getGroupInfo4Chat: "/group/getGroupInfo4Chat",
    dissolutionGroup: "/group/dissolutionGroup",//解散群
    leaveGroup: "/group/leaveGroup",//退出群
    addOrRemoveGroupUser: "/group/addOrRemoveGroupUser",//添加或删除群成员
    search:"/contact/search",//搜索好友
    applyAdd:"/contact/applyAdd",//申请添加
    loadApply:"/contact/loadApply",//加载申请列表
    dealWithApply:"/contact/dealWithApply",//处理好友申请
    loadContact:"/contact/loadContact",//加载联系人列表
    getContactUserInfo:"/contact/getContactUserInfo",//获取联系人信息
    addContact2Blacklist:"/contact/addContact2Blacklist",//添加联系人到黑名单
    delContact:"/contact/delContact",//删除联系人
    getContactInfo:"/contact/getContactInfo",//获取联系人信息
    saveUserInfo:"/userInfo/saveUserInfo",//保存用户信息
    getUserInfo:"/userInfo/getUserInfo",//获取用户信息
    updatePassword:"/userInfo/updatePassword",//修改密码
    logout:"/userInfo/logout",//退出登录
    sendMessage:"/chat/sendMessage",//发送消息
    uploadFile:"/chat/uploadFile",//上传文件
    loadAminAccount:"/admin/loadUser",//从后台获取用户列表
    updateUserStatus:"/admin/updateUserStatus",//修改用户状态
    forceOffline:"/admin/forceOffline",//强制下线
    loadGroup:"/admin/loadGroup",//从后台获取群列表
    adminDissolutionGroup:"/admin/dissolutionGroup",//解散群
    saveSysSetting:"/admin/saveSysSetting",//保存系统设置
    getSysSetting4Admin:"/admin/getSysSetting",//获取系统设置
    loadUpdateDataList:"/admin/loadUpdateList",//获取更新数据列表
    delUpdate:"/admin/delUpdate",//删除更新数据
    saveUpdate:"/admin/saveUpdate",//保存更新数据
    postUpdate:"/admin/postUpdate",//发布更新数据
    loadBeautyAccount:"/admin/loadBeautyAccount",//获取靓号列表
    saveBeautyAccount:"/admin/saveBeautyAccount",//保存靓号
    delBeautyAccount:"/admin/delBeautyAccount",//删除靓号
    checkVersion:"/update/checkVersion",//检查版本
}
export default api