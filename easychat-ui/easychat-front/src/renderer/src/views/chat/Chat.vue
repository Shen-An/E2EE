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
          <div
            class="message-item"
            v-for="(data, index) in messageList"
            :id="'message' + data.messageId"
          >
            {{ data.messageContent }}
          </div>
        </div>
        <MessageSend :currentSession="currentChatSession"></MessageSend>
      </div>
    </template>
  </Layout>
</template>

<script setup>
import MessageSend from './MessageSend.vue'
import ChatSession from './ChatSession.vue'
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
  chatSessionList.value = chatSessionList.value.filter((item) => {
    return item.contactId != contactId
  })
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
//点击会话
const messageList = ref([])
const chatSessionClickHandler = (item) => {
  // debugger
  currentChatSession.value = Object.assign({}, item)
  //TODO 未读消息记录要清空
  messageList.value = []
  messageCountInfo.pageNo = 0
  messageCountInfo.totalPage = 1
  messageCountInfo.maxMessageId = null
  messageCountInfo.noData = false
  loadChatMessage()
}
const loadChatMessage = () => {
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
  //TODO 设置选中的会话
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
    messageList.value = dataList.concat(messageList.value)
    messageCountInfo.pageNo = pageNo
    messageCountInfo.pageTotal = pageTotal
    if (pageNo == 1) {
      messageCountInfo.maxMessageId =
        dataList.length > 0 ? dataList[dataList.length - 1].messageId : null
      //TODO 滚动条滚动到底部
    }
    console.log('loadChatMessageCallback:', messageList.value)
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
    console.log('receiveMessage:', message)
    // //ws连接成功
    // if(message.messageType == 0){
    //   loadChatSession()
    // }
  })
}
onMounted(() => {
  onReceiveMessage()
  onLoadSessionData()
  loadChatSession()
  onLoadChatMessage()
})
onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('receiveMessage')
  window.ipcRenderer.removeAllListeners('loadSessionDataCallback')
  window.ipcRenderer.removeAllListeners('loadChatMessageCallback')
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