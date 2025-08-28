<template>
  <div class="send-panel">
    <div class="toolbar">
      <el-popover
        :visible="showEmojiPopover"
        trigger="click"
        placement="top"
        :teleported="false"
        @show="openPopover"
        @hide="closePopover"
        :popper-style="{
          padding: '0px 10px 10px 10px',
          width: '490px'
        }"
      >
        <template #default>
          <el-tabs v-model="activeEmoji" @click.stop>
            <el-tab-pane :label="emoji.name" :name="emoji.name" v-for="emoji in emojiList">
              <div class="emoji-list">
                <div class="emoji-item" v-for="item in emoji.emojiList" @click="sendEmoji(item)">
                  {{ item }}
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </template>
        <template #reference>
          <div class="iconfont icon-emoji" @click="showEmojiPopoverHandler"></div>
        </template>
      </el-popover>
      <el-upload
        ref="uploadRef"
        name="file"
        :show-file-list="false"
        :multiple="true"
        :limit="fileLimit"
        :http-request="uploadFile"
        :on-exceed="uploadExceed"
      >
        <div class="iconfont icon-folder"></div>
      </el-upload>
    </div>
    <div class="input-area" @drop="dropHandler" @dragover="dragOverHandler">
      <el-input
        rows="5"
        v-model="msgContent"
        type="textarea"
        resize="none"
        maxLength="500"
        show-word-limit
        spellcheck="false"
        input-style="background:#f5f5f5;border:none;"
        @keydown.enter="sendMessage"
        @paste="pasteFile"
      ></el-input>
    </div>
    <div class="send-btn-panel">
      <el-popover
        trigger="click"
        :visible="showSendMsgPopover"
        :hide-after="1500"
        placement="top-end"
        :teleported="false"
        @show="openPopover"
        @hide="closePopover"
        :popper-style="{
          padding: '5px',
          'min-width': '0px',
          width: '120px'
        }"
      >
        <template #default> <span class="empty-msg">不能发送空白信息</span> </template>
        <template #reference>
          <span class="send-btn" @click="sendMessage">发送(S)</span>
        </template>
      </el-popover>
    </div>
    <!-- 添加好友 -->
    <SearchAdd ref="searchAddRef"></SearchAdd>
  </div>
</template>

<script setup>
import SearchAdd from '@/views/contact/SearchAdd.vue'
import emojiList from '@/utils/Emoji'
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()
import { useUserInfoStore } from '@/stores/UserInfoStore'
const userInfoStore = useUserInfoStore()
import { getFileType } from '@/utils/Constants.js'
import { useSysSettingStore } from '@/stores/SysSettingStore'
import { useAESKeyStore } from '@/stores/AESKeyStore'
const AESKeyStore = useAESKeyStore()
import { ArrayToWordArray2Hex, encryptMessage, decryptMessage, isCiphertext } from '@/utils/AES'
import {
    H,
    cuckooHash,
    randomSelfReduction,
    symmetricEncrypt,
    symmetricDecrypt,
    SPCEnc
} from '@/utils/SPCE.js';
import { dataType } from 'element-plus/es/components/table-v2/src/common'

const sysSettingStore = useSysSettingStore()

const props = defineProps({
  currentChatSession: {
    type: Object,
    default: {}
  }
})
const activeEmoji = ref('人物')

const openPopover = () => {
  document.addEventListener('click', hidePopover, false)
}
//触发点击事件收起emoji,无论点击哪里
const closePopover = () => {
  document.removeEventListener('click', hidePopover, false)
}
const showSendMsgPopover = ref(false)

const showEmojiPopoverHandler = () => {
  showEmojiPopover.value = true
}

const sendEmoji = (emoji) => {
  msgContent.value = msgContent.value + emoji
  showEmojiPopover.value = false
}

//隐藏显示pop
const showEmojiPopover = ref(false)

const hidePopover = () => {
  showEmojiPopover.value = false
  showSendMsgPopover.value = false
}

const AESKey = ref('')
const msgContent = ref('')
const sendMessage = async (e) => {
  if (e.shiftKey && e.keyCode === 13) {
    return
  }
  e.preventDefault()
  const messageContent = msgContent.value ? msgContent.value.replace(/\s*$/g, '') : ''
  console.log('发送消息:', messageContent)
  SPCEncMsg(messageContent)
  //消息加密
  // console.log(!props.currentChatSession.contactId.includes('G'))
  if (
    props.currentChatSession.contactId != 'Urobot' &&
    !props.currentChatSession.contactId.includes('G')
  ) {
    //找对方用户信息
    let resp1 = await proxy.Request({
      url: proxy.Api.loadDataList,
      params: {
        userId: props.currentChatSession.contactId //这里面的contactId是receiverId，即对方的
      }
    })

    AESKey.value = await loadAESKey(resp1.data.list[0].email)

    const keyArray = AESKey.value
    const keyHex = ArrayToWordArray2Hex(keyArray)

    AESKeyStore.setAESKey(userInfoStore.getInfo().email, keyHex)
    // console.log('AESKeyStore hex:', AESKeyStore.getAESKey(userInfoStore.getInfo().email))
    let result = encryptMessage(messageContent, keyHex)
    let { iv } = result

    let { encrypted } = result
    if (messageContent == '') {
      showSendMsgPopover.value = true
      return
    }
    sendMessageDo(
      {
        messageContent: `${iv}:${encrypted}`,
        messageType: 2
      },
      true
    )
  } else {
    // debugger
    if (messageContent == '') {
      showSendMsgPopover.value = true
      return
    }
    sendMessageDo(
      {
        messageContent: messageContent,
        messageType: 2
      },
      true
    )
  }
}

const emit = defineEmits(['sendMessage4Local'])

//真正发送消息
const sendMessageDo = async (
  messageObj = {
    messageContent,
    messageType,
    localFilePath,
    fileSize,
    fileName,
    filePath,
    fileType
  },
  cleanMsgContent
) => {
 
  //判断文件大小
  if (!checkFileSize(messageObj.fileType, messageObj.fileSize, messageObj.fileName)) {
    return
  }
  if (messageObj.fileSize == 0) {
    proxy.Confirm({
      message: `${messageObj.fileName}文件为空，无法发送`,
      showCancelBtn: false
    })
    return
  }

  messageObj.sessionId = props.currentChatSession.sessionId
  messageObj.sendUserId = userInfoStore.getInfo().sendUserId
  // console.log("currentChatSession33:", props.currentChatSession);
  // console.log("99"+props.currentChatSession.contactId)
  let resp = await proxy.Request({
    url: proxy.Api.sendMessage,
    showLoading: props.currentChatSession.contactId != 'Urobot' ? false : true,
    params: {
      messageContent: messageObj.messageContent,
      contactId: props.currentChatSession.contactId,
      messageType: messageObj.messageType,
      fileSize: messageObj.fileSize,
      fileName: messageObj.fileName,
      fileType: messageObj.fileType
    },
    showError: false,
    errorCallback: (responseData) => {
      proxy.Confirm({
        message: responseData.info,
        okfun: () => {
          addContact(props.currentChatSession.contactId, responseData.code)
        },
        okText: '重新申请'
      })
    }
  })
  // console.log(resp.info+resp.code)
  if (!resp) {
    return
  }
  if (cleanMsgContent) {
    msgContent.value = ''
  }
  Object.assign(messageObj, resp.data)
  //解密,保存本地为明文,如果是密文

  if (isCiphertext(messageObj.messageContent)) {
    const [iv, encrypted] = messageObj.messageContent.split(':')
    // console.log(AESKeyStore.getAESKey(userInfoStore.getInfo().email))
    let m = decryptMessage(encrypted, AESKeyStore.getAESKey(userInfoStore.getInfo().email), iv)
    messageObj.messageContent = m
    messageObj.lastMessage = m
  }

  //更新列表
  emit('sendMessage4Local', messageObj)
  //保存消息到本地
  window.ipcRenderer.send('addLocalMessage', messageObj)
  //添加消息过滤
  // addMessageFilter(messageObj.messageContent)
  //获取SPCE公钥
  if (
    props.currentChatSession.contactId != 'Urobot' &&
    !props.currentChatSession.contactId.includes('G')
  ) {
    fetchFileContentasync(messageObj.messageContent)
  }
}
const segmentation = (word) => {
  window.ipcRenderer.send('segmentation', word)
  return new Promise((resolve, reject) => {
    window.ipcRenderer.on('segmentationCallback', (e, data) => {
      console.log('分词结果:', data)
      window.ipcRenderer.removeAllListeners('segmentationCallback')
      resolve(data)
    })
  })
}
const SPCEncMsg = async (msg) => {
    // const msg = "你买毒品了吗";
    console.log("原始消息:", msg);
    let words = await segmentation(msg)

    let resp1= await proxy.Request({
      url: proxy.Api.params,

    })
    // console.log("参数:", resp1.data)

    const result = await SPCEnc(msg,words, resp1.data.B, resp1.data.g, resp1.data.A);
    let resp = await proxy.Request({
      url: proxy.Api.receiveSpceCt,
      params: {
        QS0: result.QS0,
        QS1: result.QS1,
        encryptedMessages0: result.encryptedMessages0,
        encryptedMessages1: result.encryptedMessages1,
        iv0Array: result.iv0Array,
        iv1Array: result.iv1Array,
      },
      dataType: 'json',
    })
    

    console.log("加密结果:", result);
};

//定义一个数组，与密文一起发送给服务器，判断是否在布谷鸟过滤器中，顺序一一对应
const boolArr = []


//定义一个全局的msg
const msg = ref('')
//添加消息过滤
const addMessageFilter = async (messageContent) => {
  msg.value = messageContent
  const words = await segmentation(messageContent)

  const hashCodeStr = []
  for (let str of words) {
    hashCodeStr.push(await computeHash(str))
  }
  let resp = await proxy.Request({
    url: proxy.Api.filter,
    params: {
      hashCodeStr: hashCodeStr
    }
  })
  // console.log(resp)
  for (let i = 0; i < words.length; i++) {
    //判断是否包含hash值，包含添加True，不包含添加False
    if (resp.data[i] == 'True') {
      boolArr.push('True')
    } else {
      boolArr.push('False')
    }
  }

  // console.log(boolArr)
}

const fetchFileContentasync = async (messageContent) => {
  let content = null

  try {
    let response = await proxy.Request({
      url: proxy.Api.SPCEGetpk
    })
    content = response
    console.log('文件内容已存储到变量：', content)
    // 处理文件内容
    processFileContent(content, messageContent)
  } catch (error) {
    console.error('获取文件内容失败:', error)
  }
}
const processFileContent = async (content, messageContent) => {
  // 确保 content.data.body 存在
  if (content.data && content.data.body) {
    const body = content.data.body
    // 提取 Serialized A 的内容
    let aMatch = body.match(/Serialized A: (.*)/)
    let serializedA = null
    if (aMatch) {
      try {
        serializedA = JSON.parse(aMatch[1])
      } catch (error) {
        console.error('解析 Serialized A 失败:', error)
      }
    }

    // 提取 Serialized T 的内容
    let tMatch = body.match(/Serialized T: (.*)/)
    let serializedT = null
    if (tMatch) {
      try {
        serializedT = JSON.parse(tMatch[1])
      } catch (error) {
        console.error('解析 Serialized T 失败:', error)
      }
    }

    // 提取 Serialized GroupManager 的内容
    let groupManagerMatch = body.match(/Serialized GroupManager: (.*)/)
    let serializedGroupManager = null
    if (groupManagerMatch) {
      try {
        serializedGroupManager = JSON.parse(groupManagerMatch[1])
      } catch (error) {
        console.error('解析 Serialized GroupManager 失败:', error)
      }
    }

    // 打印解析结果，你可以根据需求进一步处理这些数据
    console.log('Serialized A:', serializedA)
    console.log('Serialized T:', serializedT)
    console.log('Serialized GroupManager:', serializedGroupManager)
    const words = await segmentation(messageContent)
    await addMessageFilter(messageContent)
    // console.log("dataForPython",boolArr)
    // 假设你要将这些数据传递给 Python 进行处理
    let dataForPython = {
      A: serializedA,
      T: serializedT,
      // GroupManager: serializedGroupManager//如果不需要GroupManager，可以不传递
      words: words,
      boolArr: boolArr
    }
    console.log('准备传递给主进程的数据:', dataForPython)
    window.ipcRenderer.send('dataForPython', JSON.stringify(dataForPython))
  } else {
    console.error('响应数据中缺少 body 字段')
  }
}
// 另一种分词技术
// const splitText = (locales, text) => {
//   const segments = Array.from(new Intl.Segmenter(locales, { granularity: 'word' }).segment(text))

//   // 提取词语并过滤非词语内容
//   const words = segments.filter((seg) => seg.isWordLike).map((seg) => seg.segment)

//   return words
//   // console.log(words);
// }

const computeHash = (word) => {
  window.ipcRenderer.send('computeHash', word)
  return new Promise((resolve, reject) => {
    window.ipcRenderer.on('computeHashCallback', (e, hash) => {
      console.log(hash)
      window.ipcRenderer.removeAllListeners('computeHashCallback')
      resolve(hash)
    })
  })
}
const uploadRef = ref()

const uploadFile = (file) => {
  uploadFileDo(file.file)
  uploadRef.value.clearFiles()
}

const getFileTypeByName = (fileName) => {
  const fileSuffix = fileName.substr(fileName.lastIndexOf('.') + 1)
  return getFileType(fileSuffix)
}

//文件上传
const uploadFileDo = (file) => {
  // console.log(  getFileTypeByName(file.name))
  const fileType = getFileTypeByName(file.name)
  sendMessageDo(
    {
      messageContent: '[' + getFileType(fileType) + ']',
      messageType: 5,
      filePath: file.path,
      fileSize: file.size,
      fileName: file.name,
      fileType: fileType
    },
    false
  )
}

// 添加好友
const searchAddRef = ref()
const addContact = (contactId, code) => {
  searchAddRef.value.show({
    contactId,
    contactType: code == 902 ? 'USER' : 'GROUP'
  })
}

//校验文件大小
const checkFileSize = (fileType, fileSize, fileName) => {
  const SIZE_MB = 1024 * 1024
  const settingArray = Object.values(sysSettingStore.getSetting())
  // console.log(settingArray)
  // console.log(fileType)
  const fileSizeNumber = settingArray[fileType]
  if (fileSize > fileSizeNumber * SIZE_MB) {
    proxy.Confirm({
      message: `文件${fileName}超过大小${fileSizeNumber}M限制，无法发送`,
      showCancelBtn: false
    })
    return false
  }
  return true
}

//发送文件数量
const fileLimit = 10
const checkFileLimit = (files) => {
  if (files.length > fileLimit) {
    proxy.Confirm({
      message: `一次最多发送${fileLimit}个文件`,
      showCancelBtn: false
    })
    return
  }
  return true
}

const uploadExceed = (files) => {
  checkFileLimit(files)
}

//拖入文件
const dragOverHandler = (e) => {
  // console.log('拖入文件未松开')
  e.preventDefault()
}
const dropHandler = (event) => {
  // console.log('已经拖入文件')
  event.preventDefault()
  const files = event.dataTransfer.files
  if (!checkFileLimit(files)) {
    return
  }
  for (let i = 0; i < files.length; i++) {
    uploadFileDo(files[i])
  }
}

//复制、截图粘贴
const pasteFile = async (event) => {
  let items = event.clipboardData && event.clipboardData.items
  // console.log(items)
  const fileData = {}

  for (const item of items) {
    if (item.kind != 'file') {
      break
    }
    const file = await item.getAsFile()
    if (file.path != '') {
      //直接复制文件 上传
      uploadFileDo(file)
    } else {
      //剪切板截图
      const imageFile = new File([file], 'temp.jpg')
      let fileReader = new FileReader()
      fileReader.onloadend = function () {
        const byteArray = new Uint8Array(this.result)
        fileData.byteArray = byteArray
        fileData.name = imageFile.name
        //渲染进程无法做此操作，交给主进程
        window.ipcRenderer.send('saveClipBoardFile', fileData)
      }
      fileReader.readAsArrayBuffer(imageFile)
    }
  }
}
const tag = ref('')
const ct = ref('')
const userPk = ref('')
onMounted(async () => {
  window.ipcRenderer.send('getUserPk')
  window.ipcRenderer.on('dataForPythonCallback', async (e, data) => {
    console.log('返回tag和密文：', data)
    // 找到第一个 { 的索引
    const index = data.indexOf('{')
    if (index === -1) {
      console.error('未找到有效的JSON数据起始位置')
      return
    }
    // 提取tag
    tag.value = data.slice(0, index)
    console.log('boolArr:', boolArr)
    // 提取ct的JSON字符串
    ct.value = data.slice(index)
    let resp = await proxy.Request({
      url: proxy.Api.SPCESendCt,
      params: {
        tag: tag.value,
        ct: ct.value,
        boolArr: boolArr
      }, // 直接传递对象，让 Request 方法根据 dataType 处理
      dataType: 'json', // 明确设置数据类型为 json
      showLoading: false
    })
    if (!resp) {
      console.log('发送密文失败')
    }

    console.log('返回结果：', resp.data)
    window.ipcRenderer.send('computeCommit', resp.data)
  })

  window.ipcRenderer.on('computeCommitCallback', async(e, data) => {
    console.log('返回结果：', data)
    // console.log('tag:', tag.value)
    let resp = await proxy.Request({
      url: proxy.Api.SPCESendCommit,
      params: {
        tag: tag.value,
        ct: ct.value,
        data: data,
        boolArr: boolArr,
        userPk: userPk.value,
      },
      dataType: 'json',
      showLoading: false
    })
    boolArr.length = 0
    // console.log('resp:', resp)
    // if(resp.data == 0){
    //   // console.log('发送违规信息，此人已被标记')
    //   // console.log('发送违规信息，此人已被标记',msg.value)
    //   let resp1 = await proxy.Request({
    //     url: proxy.Api.addIllegalMessage,
    //     params: {
    //       messageContent: msg.value,
    //       contactId: props.currentChatSession.contactId,
    //     },
    //     showLoading:false
    //   })
    //   if(!resp1){
    //     console.log('添加违规信息失败')
    //   }
    // }
    if (!resp) {
      console.log('传送承诺失败')
    }
  })
  window.ipcRenderer.on('getUserPkCallback', (e, data) => {
    // console.log('返回结果：', data)
    userPk.value = data
  })
  window.ipcRenderer.on('saveClipBoardFileCallback', (e, file) => {
    const fileType = 0
    sendMessageDo(
      {
        messageContent: '[' + getFileType(fileType) + ']',
        messageType: 5,
        filePath: file.path,
        fileSize: file.size,
        fileName: file.name,
        fileType: fileType
      },
      false
    )
  })
})
//参数为email，他人的email
const loadAESKey = (email) => {
  return new Promise((resolve, reject) => {
    window.ipcRenderer.on('loadAESKeyCallback', (e, data) => {
      window.ipcRenderer.removeAllListeners('loadAESKeyCallback')
      resolve(data.AESKey)
    })
    window.ipcRenderer.send('loadAESKey', userInfoStore.getInfo().email, email) //发送是自己他人的email，收到就要反过来
  })
}

onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('saveClipBoardFileCallback')
})
</script>

<style lang="scss" scoped>
.emoji-list {
  max-width: 490px; // 与 popover 宽度相同
  .emoji-item {
    float: left;
    font-size: 23px;
    padding: 2px;
    text-align: center;
    border-radius: 3px;
    margin-left: 10px;
    margin-top: 5px;
    cursor: pointer;
    &:hover {
      background: #ddd;
    }
  }
}
.send-panel {
  height: 200px;
  border-top: 1px solid #ddd;
  .toolbar {
    height: 40px;
    display: flex;
    align-items: center;
    padding-left: 10px;
    .iconfont {
      color: #494949;
      font-size: 20px;
      margin-left: 10px;
      cursor: pointer;
    }
    :deep(.el-tabs__header) {
      margin-bottom: 0px;
    }
  }
  .input-area {
    padding: 0px 10px;
    outline: none;
    width: 100%;
    height: 115px;
    overflow: auto;
    word-wrap: break-word;
    word-break: break-all;
    :deep(.el-textarea__inner) {
      box-shadow: none;
    }
    :deep(.el-input__count) {
      background: none;
      right: 12px;
    }
  }
  .send-btn-panel {
    text-align: right;
    padding-top: 10px;
    padding-right: 22px;
    .send-btn {
      cursor: pointer;
      color: #07c160;
      background: #e9e9e9;
      border-radius: 5px;
      padding: 8px 25px;
      &:hover {
        background: #d2d2d2;
      }
    }
    .empty-msg {
      font-size: 13px;
    }
  }
}
</style>