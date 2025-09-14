<template>
  <div>
    <div class="blockchain-demo">
      <button @click="connectWallet" :disabled="isConnecting">
        {{ isConnected ? '已连接钱包' : '连接MetaMask' }}
      </button>

      <div v-if="isConnected">
        <div>钱包地址: {{ walletAddress }}</div>
        <div>余额: {{ walletBalance }} WBT</div>

        <input v-model="messageContent" placeholder="输入消息" style="width: 70%" />
        <button @click="sendMessage">发送消息</button>

        <button @click="createClosure" :disabled="isLoading">创建闭环</button>

        <div v-if="statusMessage">{{ statusMessage }}</div>
        <pre v-if="localChain" class="chain-container">{{ localChain }}</pre>
      </div>
    </div>

    <div v-if="isConnected">
      <!-- 新增：消息查看部分 -->
      <div class="message-viewer">
        <h3>我的闭环消息</h3>

        <button @click="fetchMyMessages" :disabled="isLoadingMessages">
          {{ isLoadingMessages ? '加载中...' : '查看我的消息' }}
        </button>

        <div v-for="(msg, index) in messages" :key="index" class="message-item">
          <div><strong>ID:</strong> {{ msg.id }}</div>
          <div><strong>时间:</strong> {{ formatDate(msg.timestamp) }}</div>
          <div>
            <strong>消息:</strong>
            <ul>
              <li v-for="(line, i) in msg.messages" :key="i">{{ line }}</li>
            </ul>
          </div>
          <div class="divider"></div>
        </div>

        <div v-if="messages.length === 0">
          <p>没有找到消息</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import BrowserBlockchainClosure from '@/utils/Blockchain/BlockchainClosureTool.js'
import { ethers } from 'ethers'

const blockchainClosure = new BrowserBlockchainClosure()
const isConnected = ref(false)
const isConnecting = ref(false)
const isLoading = ref(false)
const walletAddress = ref('')
const walletBalance = ref('0')
const messageContent = ref('')
const localChain = ref(null)
const statusMessage = ref('请连接钱包')

const msgArr = []
// 连接钱包
const connectWallet = async () => {
  if (isConnecting.value) return

  try {
    isConnecting.value = true
    statusMessage.value = '正在连接钱包...'

    const address = await blockchainClosure.connectWallet()
    walletAddress.value = address
    isConnected.value = true

    await refreshBalance()
    statusMessage.value = '钱包连接成功'
  } catch (error) {
    statusMessage.value = `连接失败: ${error.message}`
  } finally {
    isConnecting.value = false
  }
}

// 刷新余额
const refreshBalance = async () => {
  try {
    const balance = await blockchainClosure.provider.getBalance(walletAddress.value)
    walletBalance.value = ethers.utils.formatEther(balance)
  } catch (error) {
    console.error('获取余额失败:', error)
    walletBalance.value = '获取失败'
  }
}

// 发送消息
const sendMessage = async () => {
  if (!isConnected.value || !messageContent.value) return

  try {
    statusMessage.value = '发送消息中...'
    const content = messageContent.value
    blockchainClosure.createMessage(walletAddress.value, messageContent.value)
    msgArr.push(content) // 👈 只存字符串
    console.log('当前消息内容:', msgArr)
    statusMessage.value = '消息发送成功'
    messageContent.value = ''

    refreshLocalChain()
  } catch (error) {
    statusMessage.value = `发送失败: ${error.message}`
  }
}

// 创建闭环
// 创建闭环
const createClosure = async () => {
  if (!isConnected.value || isLoading.value) return

  try {
    isLoading.value = true
    statusMessage.value = '创建闭环中...'
    console.log('当前消息内容:', msgArr)
    // 检查余额
    await refreshBalance()
    if (parseFloat(walletBalance.value) < 0.1) {
      throw new Error('余额不足，请获取至少0.1 WBT测试代币')
    }

    // 将当前消息内容传递给createRandomClosure方法
    const closureData = await blockchainClosure.createRandomClosure(3, msgArr)
    statusMessage.value = '上传闭环到区块链...'

    const receipt = await blockchainClosure.uploadClosure(closureData)
    statusMessage.value = `闭环已上传，交易哈希: ${receipt.transactionHash.slice(0, 10)}...`
    messageContent.value = '' // 清空消息输入框
    refreshLocalChain()
  } catch (error) {
    statusMessage.value = `创建失败: ${error.message}`
  } finally {
    isLoading.value = false
  }
}

// 刷新本地链
const refreshLocalChain = () => {
  localChain.value = JSON.stringify(blockchainClosure.getLocalChain(), null, 2)
}
const messages = ref([])
const isLoadingMessages = ref(false)

// 获取当前用户的所有消息
const fetchMyMessages = async () => {
  if (!isConnected.value) return

  try {
    isLoadingMessages.value = true
    statusMessage.value = '正在加载消息...'

    messages.value = await blockchainClosure.getUserMessages(walletAddress.value)
    console.log('获取到的消息:', messages.value)
    statusMessage.value = `成功加载 ${messages.value.length} 条消息`
  } catch (error) {
    statusMessage.value = `加载消息失败: ${error.message}`
    console.error('获取消息失败:', error)
  } finally {
    isLoadingMessages.value = false
  }
}

// 格式化日期函数
const formatDate = (timestamp) => {
  try {
    return new Date(timestamp).toLocaleString()
  } catch (e) {
    return timestamp
  }
}
</script>

<style scoped>
/* 样式与之前保持一致 */
.blockchain-demo {
  padding: 20px;
  font-family: Arial, sans-serif;
}

button {
  padding: 8px 16px;
  margin: 5px 10px 5px 0;
  border: none;
  border-radius: 4px;
  background-color: #4a90e2;
  color: white;
  cursor: pointer;
}
</style>