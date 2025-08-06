const api = {
    prodDomain: "http://127.0.0.1:5050",
    devDomain: "http://127.0.0.1:5050",
    prodWsDomain: "ws://127.0.0.1:5051/ws",
    devWsDomain: "ws://127.0.0.1:5051/ws",
    checkCode: "/account/checkCode",
    login: "/account/login",
    register: "/account/register",
    getSysSetting: "/account/getSysSetting",
    getSysSetting1: "/account/getSysSetting1",
    loadMyGroup: "/groupInfo/loadMyGroup",
    saveGroup: "/groupInfo/saveGroup",
    getGroupInfo: "/groupInfo/getGroupInfo",
    getGroupInfo4Chat: "/groupInfo/getGroupInfo4Chat",
    dissolutionGroup: "/groupInfo/dissolutionGroup",//解散群
    leaveGroup: "/groupInfo/leaveGroup",//退出群
    addOrRemoveGroupUser: "/groupInfo/addOrRemoveGroupUser",//添加或删除群成员
    search: "/userContact/search",//搜索好友
    applyAdd: "/userContact/applyAdd",//申请添加
    loadApply: "/userContact/loadApply",//加载申请列表
    dealWithApply: "/userContact/dealWithApply",//处理好友申请
    loadContact: "/userContact/loadContact",//加载联系人列表
    getContactUserInfo: "/userContact/getContactUserInfo",//获取联系人信息
    addContact2Blacklist: "/userContact/addContact2Blacklist",//添加联系人到黑名单
    delContact: "/userContact/delContact",//删除联系人
    getContactInfo: "/userContact/getContactInfo",//获取联系人信息
    saveUserInfo: "/userInfo/saveUserInfo",//保存用户信息
    getUserInfo: "/userInfo/getUserInfo",//获取用户信息
    updatePassword: "/userInfo/updatePassword",//修改密码
    logout: "/userInfo/logout",//退出登录
    loadDataList:"/userInfo/loadDataList",//获取用户列表
    sendMessage: "/chat/sendMessage",//发送消息
    uploadFile: "/chat/uploadFile",//上传文件
    loadAminAccount: "/admin/loadUser",//从后台获取用户列表
    updateUserStatus: "/admin/updateUserStatus",//修改用户状态
    forceOffline: "/admin/forceOffline",//强制下线
    loadGroup: "/admin/loadGroup",//从后台获取群列表
    adminDissolutionGroup: "/admin/dissolutionGroup",//解散群
    saveSysSetting: "/admin/saveSysSetting",//保存系统设置
    getSysSetting4Admin: "/admin/getSysSetting",//获取系统设置
    loadUpdateDataList: "/admin/loadUpdateList",//获取更新数据列表
    delUpdate: "/admin/delUpdate",//删除更新数据
    saveUpdate: "/admin/saveUpdate",//保存更新数据
    postUpdate: "/admin/postUpdate",//发布更新数据
    loadBeautyAccount: "/admin/loadBeautyAccount",//获取靓号列表
    saveBeautyAccount: "/admin/saveBeautyAccount",//保存靓号
    delBeautyAccount: "/admin/delBeautyAccount",//删除靓号
    checkVersion: "/update/checkVersion",//检查版本
    loadPkDataList:'/ecdhPks/loadPkDataList',//获取ecdh公钥列表
    addPk:'/ecdhPks/addPk',//添加ecdh公钥
    loadChatSessionUserDataList:'/chatSessionUser/loadDataList',//获取聊天会话用户列表
    filter:'/CuckooFilter/filter',//过滤敏感词
    SPCEGetpk:'/SPCE/getPk',//获取SPCE公钥,
    SPCESendCt:'/SPCE/sendCt',//发送SPCE密文
    SPCESendCommit:'/SPCE/sendCommit',//发送SPCE承诺
    addIllegalTrace:'/SPCE/addIllegalTrace',//添加非法信息
}
export default api