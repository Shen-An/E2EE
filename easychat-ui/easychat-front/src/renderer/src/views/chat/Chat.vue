<template>
  <Layout>
    <template #left-content>
      <div class="drag-panel drag"></div>
      <div class="top-search">
        <el-input clearable v-model="searchKey" size="small" placeholder="搜索" @keyup="search">
          <template #suffix>
            <span class="iconfont icon-search"></span>
          </template>
        </el-input>
      </div>
      <div class="chat-session-list">
        <template v-for="item in chatSessionList">
          <ChatSession
            :data="item"
            @click="chatSessionClickHandler(item)"
            @contextmenu.stop="onContextMenu(item, $event)"
            :currentSession="item.contactId == currentChatSession.contactId"
          ></ChatSession>
        </template>
      </div>
    </template>
    <template #right-content>
      <div class="title-panel drag" v-if="Object.keys(currentChatSession).length > 0">
        <div class="title">
          <span>{{ currentChatSession.contactName }}</span>
          <span v-if="currentChatSession.contactType == 1"
            >({{ currentChatSession.memberCount }})</span
          >
        </div>
      </div>
      <div
        v-if="currentChatSession.contactType == 1"
        class="iconfont icon-more no-drag"
        @click="showGroupDetail"
      ></div>
      <div class="chat-panel" v-show="Object.keys(currentChatSession).length > 0">
        <div class="message-panel" id="message-panel">
          <!-- 为消息项添加唯一 key，缺少 key 优化导致全量 DOM 更新，导致滚动闪了一下 -->
          <div
            class="message-item"
            v-for="(data, index) in messageList"
            :id="'message' + data.messageId"
            :key="data.messageId"
          >
            <!--展示时间 300000ms 5分钟-->
            <template
              v-if="
                index > 1 &&
                data.sendTime - messageList[index - 1].sendTime >= 300000 &&
                (data.messageType == 2 || data.messageType == 5)
              "
            >
              <ChatMessageTime :data="data"></ChatMessageTime>
            </template>
            <!--系统消息 具体messageType见后端MessageTypeEnum-->
            <template
              v-if="
                data.messageType == 3 ||
                data.messageType == 1 ||
                data.messageType == 9 ||
                data.messageType == 8 ||
                data.messageType == 11 ||
                data.messageType == 12
              "
            >
              <ChatMessageSys :data="data"></ChatMessageSys>
            </template>

            <template
              v-if="data.messageType == 1 || data.messageType == 2 || data.messageType == 5"
            >
              <ChatMessage
                :data="data"
                :currentChatSession="currentChatSession"
                @showMediaDetail="showMediaDetailHandler"
              ></ChatMessage>
            </template>
          </div>
        </div>
        <MessageSend
          :currentChatSession="currentChatSession"
          @sendMessage4Local="sendMessage4LocalHandler"
        ></MessageSend>
      </div>
      <div class="chat-blank" v-show="Object.keys(currentChatSession).length == 0">
        <Blank></Blank>
      </div>
    </template>
  </Layout>
  <ChatGroupDetail ref="chatGroupDetailRef" @delChatSessionCallback="delChatSession"></ChatGroupDetail>
</template>

<script setup>
import ChatGroupDetail from './ChatGroupDetail.vue'
import ChatMessageSys from './ChatMessageSys.vue'
import ChatMessageTime from './ChatMessageTime.vue'

import MessageSend from './MessageSend.vue'
import ChatSession from './ChatSession.vue'
import ChatMessage from './ChatMessage.vue'
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
import ContextMenu from '@imengyu/vue3-context-menu'

import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css'


const router = useRouter()
const route = useRoute()
const searchKey = ref()
const search = () => {}

const chatSessionList = ref([])

const loadChatSession = () => {
  window.ipcRenderer.send('loadSessionData')
}

// 会话排序
const sortChatSessionList = (dataList) => {
  dataList.sort((a, b) => {
    const topTypeResult = b['topType'] - a['topType']
    if (topTypeResult == 0) {
      return b['lastReceiveTime'] - a['lastReceiveTime']
    }
    return topTypeResult
  })
}

//删除会话
const delChatSessionList = (contactId) => {
  setTimeout(()=>{
    chatSessionList.value = chatSessionList.value.filter((item) => {
    return item.contactId != contactId
  })
  },100)
}

const setTop = (data) => {
  // console.log('setTop:', data)
  data.topType = data.topType == 0 ? 1 : 0
  // 会话排序
  sortChatSessionList(chatSessionList.value)

  window.ipcRenderer.send('topChatSession', { contactId: data.contactId, topType: data.topType })
}

//当前选中的会话
const currentChatSession = ref({})

const messageCountInfo = {
  totalPage: 0,
  pageNo: 0,
  maxMessageId: null, //最大消息ID,只取小于该ID的消息
  noData: false
}
//是否滚动到底部,距离
let distanceBottom = 0

//点击会话
const messageList = ref([])
const chatSessionClickHandler = (item) => {
  distanceBottom = 0
  // debugger
  currentChatSession.value = Object.assign({}, item)
  //TODO 未读消息记录要清空
  messageList.value = []
  messageCountInfo.pageNo = 0
  messageCountInfo.totalPage = 1
  messageCountInfo.maxMessageId = null
  messageCountInfo.noData = false
  loadChatMessage()

  //设置选中session
  setSessionSelect({ contactId: item.contactId, sessionId: item.sessionId })
}
const setSessionSelect = ({ contactId, sessionId }) => {
  window.ipcRenderer.send('setSessionSelect', {
    contactId,
    sessionId
  })
}
const loadChatMessage = () => {delChatSession
  if (messageCountInfo.noData) {
    return
  }
  messageCountInfo.pageNo++
  window.ipcRenderer.send('loadChatMessage', {
    sessionId: currentChatSession.value.sessionId,
    pageNo: messageCountInfo.pageNo,
    maxMessageId: messageCountInfo.maxMessageId
  })
}

const delChatSession = (contactId) => {
  // 从当前列表中删除
  delChatSessionList(contactId)
  console.log('delChatSession:', contactId)
  // 设置选中的会话
  currentChatSession.value = {}
  window.ipcRenderer.send('delChatSession', contactId)
}

const onContextMenu = (data, e) => {
  ContextMenu.showContextMenu({
    x: e.x,
    y: e.y,
    items: [
      {
        label: data.topType == 0 ? '置顶' : '取消置顶',
        onClick: () => {
          setTop(data)
        }
      },
      {
        label: '删除聊天',
        onClick: () => {
          proxy.Confirm({
            message: `确定删除与${data.contactName}的聊天记录吗？`,
            okfun: () => {
              delChatSession(data.contactId)
            }
          })
        }
      }
    ]
  })
}

const onLoadChatMessage = () => {
  window.ipcRenderer.on('loadChatMessageCallback', (e, { dataList, pageTotal, pageNo }) => {
    if (pageNo == pageTotal) {
      messageCountInfo.noData = true
    }
    dataList.sort((a, b) => {
      return a.messageId - b.messageId
    })
    //记录最后一条消息，用于分页加载
    const lastMessage = messageList.value[0]

    messageList.value = dataList.concat(messageList.value)
    messageCountInfo.pageNo = pageNo
    messageCountInfo.pageTotal = pageTotal
    if (pageNo == 1) {
      messageCountInfo.maxMessageId =
        dataList.length > 0 ? dataList[dataList.length - 1].messageId : null
      //滚动条滚动到底部
      gotoBottom()
    } else {
      //分页滚动调整滚动条位置
      nextTick(() => {
        document.querySelector('#message' + lastMessage.messageId).scrollIntoView()
      })
    }
    // console.log('loadChatMessageCallback:', messageList.value)
  })
}

const onLoadSessionData = () => {
  window.ipcRenderer.on('loadSessionDataCallback', (e, dataList) => {
    // 会话排序
    sortChatSessionList(dataList)
    chatSessionList.value = dataList
    // console.log('loadSessionDataCallback:', chatSessionList.value)
  })
}
const onReceiveMessage = () => {
  window.ipcRenderer.on('receiveMessage', (e, message) => {
    // console.log('receiveMessage:', message)

    //更新对方发送来的图片/视频
    if (message.messageType == 6) {
      //6文件上传完成
      const localMessage = messageList.value.find((item) => {
        if (item.messageId == message.messageId) {
          return item
        }
      })
      if (localMessage != null) {
        localMessage.status = 1
      }
      return
    }

    let curSession = chatSessionList.value.find((item) => {
      return item.contactId == message.contactId
    })

    if (curSession == null) {
      chatSessionList.value.push(message.extendData)
    } else {
      Object.assign(curSession, message.extendData)
    }
    sortChatSessionList(chatSessionList.value)
    if (message.sessionId != currentChatSession.value.sessionId) {
      // TODO 未读消息气泡提醒
    } else {
      Object.assign(currentChatSession.value, message.extendData)
      messageList.value.push(message)
      gotoBottom()
    }
  })
}
/*
在代码中，curSession 被修改后，sortChatSessionList(chatSessionList.value)会对chatSessionList.value 进行排序。
由于 curSession 是 chatSessionList.value 中某个对象的引用，Object.assign(curSession, message.extendData)
 的修改会直接影响 chatSessionList.value，从而影响排序的结果。
如果使用 curSession = message.extendData，chatSessionList.value 中的对应对象不会被修改，
排序的结果也不会受到影响。
*/

/*
Object.assign(curSession, message.extendData): 这种方式会将 message.extendData 的属性合并到 curSession 中，
保留 curSession 原有的属性，除非 message.extendData 中有同名的属性，才会覆盖。
curSession = message.extendData: 这种方式会完全替换 curSession，curSession 原有的属性会被丢弃，
curSession 会变成 message.extendData 的一个引用。
 */

const onAddLocalMessage = () => {
  window.ipcRenderer.on('addLocalCallback', (e, { messageId, status }) => {
    // debugger
    const findMessage = messageList.value.find((item) => {
      if (item.messageId == messageId) {
        return item
      }
    })
    if (findMessage != null) {
      findMessage.status = status
    }
  })
}

const sendMessage4LocalHandler = (messageObj) => {
  messageList.value.push(messageObj)
  const chatSession = chatSessionList.value.find((item) => {
    return item.contactId == messageObj.contactId
  })
  if (chatSession) {
    chatSession.lastMessage = messageObj.lastMessage
    chatSession.lastReceiveTime = messageObj.sendTime
  }
  sortChatSessionList(chatSessionList.value)

  gotoBottom()
}

//滚动到底部，解决发送消息后滚动条不自动滚动到底部
const gotoBottom = () => {
  nextTick(() => {
    //距离超过200不自动滚动到底部
    if(distanceBottom > 200){
      return
    }
    const items = document.querySelectorAll('.message-item')
    if (items.length > 0) {
      setTimeout(() => {
        items[items.length - 1].scrollIntoView()
      }, 100)
    }
  })
}

//用于媒体信息查看
const showMediaDetailHandler = (messageId) => {
  console.log('showMediaDetailHandler:', messageId)
  let showFileList = messageList.value.filter((item) => {
    return item.messageType == 5
  })
  showFileList = showFileList.map((item) => {
    return {
      partType: 'chat',
      fileId: item.messageId,
      fileType: item.fileType,
      fileName: item.fileName,
      fileSize: item.fileSize,
      forceGet: false
    }
  })
  // 打印 showFileList
  console.log('showFileList:', showFileList)
  window.ipcRenderer.send('newWindow', {
    windowId: 'media',
    title: '图片查看',
    path: '/showMedia',
    data: {
      currentField: messageId,
      fileList: showFileList
    }
  })
}

//显示群组详情
const chatGroupDetailRef = ref()
const showGroupDetail = () => {
  chatGroupDetailRef.value.show(currentChatSession.value.contactId)
}
onMounted(() => {
  onReceiveMessage()
  onLoadSessionData()
  loadChatSession()
  onLoadChatMessage()
  onAddLocalMessage()
  nextTick(() => {
    // const messagePanel = document.getElementById('message-panel')
    const messagePanel = document.querySelector('#message-panel')
    messagePanel.addEventListener('scroll', (e) => {
      const scrollTop = e.target.scrollTop
      //计算滚动条距离底部的距离
      //e.target.scrollHeight为内容高度,e.target.clientHeight为滚动条头部距离顶部的高度,scrollTop为滚动条高度
      distanceBottom = e.target.scrollHeight - e.target.clientHeight - scrollTop
      if (scrollTop == 0 && messageList.value.length > 0) {
        loadChatMessage()
      }
    })
  })
})
onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('receiveMessage')
  window.ipcRenderer.removeAllListeners('loadSessionDataCallback')
  window.ipcRenderer.removeAllListeners('loadChatMessageCallback')
  window.ipcRenderer.removeAllListeners('addLocalCallback')
})

// const init = () => {
//   window.ipcRenderer.send('getLocalStore', 'devWsDomain')
//   window.ipcRenderer.on('getLocalStoreCallback', (e, data) => {

//     console.log('getLocalStoreCallback:', data)
//   })
// }

// onMounted(() => {
//   init()
// })
</script>

<style lang="scss" scoped>
.drag-panel {
  height: 25px;
  background: #f7f7f7;
}
.top-search {
  padding: 0px 10px 9px 10px;
  background: #f7f7f7;
  display: flex;
  align-items: center;
  .iconfont {
    font-size: 12px;
  }
}
.chat-session-list {
  height: calc(100vh - 62px);
  overflow-y: hidden;
  border-top: 1px solid #ddd;
  &:hover {
    overflow: auto;
  }
}
.search-list {
  height: calc(100vh - 62px);
  background: #f7f7f7;
  overflow: hidden;
  &:hover {
    overflow: auto;
  }
}
.title-panel {
  display: flex;
  align-items: center;
  .title {
    height: 60px;
    line-height: 60px;
    padding-left: 10px;
    font-size: 18px;
    color: #000000;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.icon-more {
  position: absolute;
  z-index: 1;
  top: 30px;
  right: 3px;
  width: 20px;
  font-size: 20px;
  margin-right: 5px;
  cursor: pointer;
}
.chat-panel {
  border-top: 1px solid #ddd;
  background: #f5f5f5;
  .message-panel {
    padding: 10px 30px 0px 30px;
    height: calc(100vh - 200px - 62px);
    overflow-y: auto;
    .message-item {
      margin-bottom: 15px;
      text-align: center;
    }
  }
}
</style>