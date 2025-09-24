<template>
  <div>
    <div class="blockchain-demo">
      <button @click="connectWallet" :disabled="isConnecting" class="btn primary-btn">
        {{ isConnected ? '已连接钱包' : '连接MetaMask' }}
      </button>

      <div v-if="isConnected" class="wallet-info-card">
        <p><strong>钱包地址:</strong> {{ walletAddress }}</p>
        <p><strong>余额:</strong> {{ walletBalance }} WBT</p>

        <div class="action-buttons">
          <button @click="createClosure" :disabled="isLoading" class="btn success-btn">
            创建闭环
          </button>
          <button @click="findNoClosureMsg" :disabled="isLoading" class="btn info-btn">
            查找未闭环消息
          </button>
          <button @click="isCheat" :disabled="isLoading" class="btn info-btn">
            {{ buttonText }}
          </button>
        </div>

        <div v-if="messages.length > 0" class="messages-list">
          <div v-for="(msg, index) in messages" :key="index" class="message-item">
            <p><strong>ID:</strong> {{ msg.id }}</p>
            <p><strong>时间:</strong> {{ formatDate(msg.timestamp) }}</p>
            <p><strong>消息:</strong></p>
            <ul>
              <li v-for="(line, i) in msg.messages" :key="i">{{ line }}</li>
            </ul>
            <div class="divider"></div>
          </div>
        </div>


        <p
          v-if="statusMessage"
          :class="['status-message', { error: statusMessage.includes('失败') }]"
        >
          {{ statusMessage }}
        </p>

        <div v-if="localChain && localChain.length > 0" class="chain-table-container card">
          <h3>区块链数据</h3>
          <div class="table-responsive">
            <table class="chain-table">
              <thead>
                <tr>
                  <th>区块高度</th>
                  <th>时间戳</th>
                  <th>工作量证明</th>
                  <th>用户地址</th>
                  <th>区块数据</th>
                  <th>前块哈希</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(block, index) in localChain" :key="block.index" class="chain-row">
                  <td class="block-index">{{ block.index }}</td>
                  <td class="block-timestamp">{{ formatTimestamp(block.timestamp) }}</td>
                  <td class="block-proof">{{ block.proof }}</td>
                  <td class="block-user">{{ truncateAddress(block.user) }}</td>
                  <td class="block-data">{{ truncateData(block.data) }}</td>
                  <td class="block-prev-hash">{{ truncateHash(block.previousHash) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div v-if="isConnected" class="message-viewer card">
        <h3>我的闭环消息</h3>

        <button @click="fetchMyMessages" :disabled="isLoadingMessages" class="btn secondary-btn">
          {{ isLoadingMessages ? '加载中...' : '查看我的消息' }}
        </button>

        <div v-if="messages.length > 0" class="messages-list">
          <div v-for="(msg, index) in messages" :key="index" class="message-item">
            <p><strong>ID:</strong> {{ msg.id }}</p>
            <p><strong>时间:</strong> {{ formatDate(msg.timestamp) }}</p>
            <p><strong>消息:</strong></p>
            <ul>
              <li v-for="(line, i) in msg.messages" :key="i">{{ line }}</li>
            </ul>
            <div class="divider"></div>
          </div>
        </div>
        <div v-else>
          <p class="no-messages">没有找到消息</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import BrowserBlockchainClosure from '@/utils/Blockchain/BlockchainClosureTool.js'
import { ethers } from 'ethers'
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

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
const status = ref(null) // null: 初始, true: 是, false: 否
const buttonText = computed(() => {
  if (status.value === null) return '管理员是否恶意伪造'
  return status.value ? '是' : '否'
})

const isCheat = async () => {
  let resp = await proxy.Request({
    url: proxy.Api.selectIllegalInformation,
  })
  messages.value = await blockchainClosure.getUserMessages(walletAddress.value)
  const result = checkAnyE2eeCtNotExist(resp, messages.value);
  status.value = result; // 设置状态为检查结果
// result为true表示存在至少一个e2eeCt不在消息中
// result为false表示所有e2eeCt都在消息中
  console.log('检查结果:', result);
}
// 检查是否存在任何一个e2eeCt不在消息中
// 参数：resp - API响应对象, messages - 用户消息数组
// 返回：如果存在至少一个e2eeCt不在消息中，返回true；否则返回false
const checkAnyE2eeCtNotExist = (resp, messages) => {
  for (const item of resp.data) {
    let exists = false;
    for (const message of messages) {
      for (const subMessage of message.messages) {
        if (subMessage.includes(item.e2eeCt)) {
          exists = true;
          break; // 找到匹配后跳出内层循环
        }
      }
      if (exists) break; // 找到匹配后跳出中层循环
    }
    if (!exists) return true; // 如果某个e2eeCt不存在，立即返回true
  }
  return false; // 所有e2eeCt都存在，返回false
};
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

    let resp = proxy.Request({
      url: proxy.Api.upchain,
      params: {
        msgArr: msgArr
      }
    })
    console.log(resp)

    refreshLocalChain()
  } catch (error) {
    statusMessage.value = `创建失败: ${error.message}`
  } finally {
    isLoading.value = false
  }
}

// 刷新本地链
// 刷新本地链
const refreshLocalChain = (data = []) => {
  console.log('刷新本地链数据:', data)
  localChain.value = blockchainClosure.getLocalChain()

  // 确保data是数组类型，避免undefined或非数组类型报错
  if (!Array.isArray(data)) {
    console.warn('数据格式错误，需要数组类型:', data)
    return
  }

  // 遍历数据并更新时间戳
  data.forEach((item, index) => {
    // 区块索引从1开始（假设创世区块是0）
    const blockIndex = index + 1

    // 确保区块存在且有sendTime属性
    if (blockIndex < localChain.value.length && item.sendTime) {
      localChain.value[blockIndex].timestamp = formatTimestamp(item.sendTime)
      console.log(`更新区块 ${blockIndex} 时间戳为:`, localChain.value[blockIndex].timestamp)
    }
  })
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
// 格式化时间戳
// 格式化时间戳函数（支持毫秒级时间戳和ISO格式字符串）
const formatTimestamp = (timestamp) => {
  try {
    let date

    // 处理毫秒级时间戳（数字类型或字符串类型的数字）
    if (
      typeof timestamp === 'number' ||
      (typeof timestamp === 'string' && /^\d+$/.test(timestamp))
    ) {
      date = new Date(Number(timestamp))
    }
    // 处理ISO格式日期字符串（如 "2025-06-25T05:54:19.309Z"）
    else if (typeof timestamp === 'string') {
      // 尝试解析ISO格式
      date = new Date(timestamp)
      // 检查解析是否有效
      if (isNaN(date.getTime())) {
        // 尝试作为普通字符串解析
        date = new Date(Date.parse(timestamp))
      }
    } else {
      throw new Error('不支持的时间戳类型')
    }

    // 优化日期显示格式（年-月-日 时:分:秒 时区）
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')

    // 获取时区偏移并格式化为 ±HH:MM
    const offset = date.getTimezoneOffset()
    const offsetHours = Math.abs(Math.floor(offset / 60))
    const offsetMinutes = Math.abs(offset % 60)
    const timezone =
      offset <= 0
        ? `+${String(offsetHours).padStart(2, '0')}:${String(offsetMinutes).padStart(2, '0')}`
        : `-${String(offsetHours).padStart(2, '0')}:${String(offsetMinutes).padStart(2, '0')}`

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds} ${timezone}`
  } catch (e) {
    console.error('时间戳解析失败:', timestamp, e)
    return timestamp || '未知时间'
  }
}

const findNoClosureMsg = async () => {
  let resp = await proxy.Request({
    url: proxy.Api.getChatMessagesByDate,
    params: {
      date: Date.now()
    }
  })
  console.log(resp)

  for (let i = 0; i < resp.data.length; i++) {
    if (resp.data[i].isUpChain == 0) {
      const content = resp.data[i].messageContent
      content.replace(':', '')
      msgArr.push(content) // 👈 只存字符串
      blockchainClosure.createMessage(walletAddress.value, content)
    }
  }
  refreshLocalChain(resp.data)
}
// 地址截断函数
const truncateAddress = (address) => {
  if (!address) return ''
  return address.length > 10 ? `${address.slice(0, 6)}...${address.slice(-4)}` : address
}

// 数据截断函数
const truncateData = (data) => {
  if (!data) return ''
  return data.length > 20 ? `${data.slice(0, 16)}...` : data
}

// 哈希值截断函数
const truncateHash = (hash) => {
  if (!hash) return ''
  return hash.length > 12 ? `${hash.slice(0, 8)}...${hash.slice(-4)}` : hash
}
</script>

<style scoped>
/* General Body and Container Styles */
body {
  margin: 0;
  background-color: #f4f7f6; /* Light gray background */
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: #333;
}

.blockchain-demo {
  max-width: 1200px;
  margin: 40px auto;
  padding: 30px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 25px;
}

/* Card Styling for Sections */
.card {
  background: #ffffff;
  border-radius: 10px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  padding: 25px;
  border: 1px solid #e0e0e0;
}

/* Button Styles */
.btn {
  padding: 12px 25px;
  margin-right: 15px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  min-width: 150px;
}

.btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  opacity: 0.7;
}

.primary-btn {
  background-color: #4a90e2;
  color: white;
}

.primary-btn:hover:not(:disabled) {
  background-color: #357bd8;
  transform: translateY(-2px);
}

.success-btn {
  background-color: #28a745;
  color: white;
}

.success-btn:hover:not(:disabled) {
  background-color: #218838;
  transform: translateY(-2px);
}

.info-btn {
  background-color: #17a2b8;
  color: white;
}

.info-btn:hover:not(:disabled) {
  background-color: #138496;
  transform: translateY(-2px);
}

.secondary-btn {
  background-color: #6c757d;
  color: white;
}

.secondary-btn:hover:not(:disabled) {
  background-color: #5a6268;
  transform: translateY(-2px);
}

/* Wallet Info Card */
.wallet-info-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.wallet-info-card p {
  margin: 0;
  font-size: 1.1em;
  color: #555;
}

.wallet-info-card p strong {
  color: #333;
}

.action-buttons {
  margin-top: 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

/* Status Message */
.status-message {
  margin-top: 15px;
  padding: 10px 15px;
  border-radius: 6px;
  font-weight: 500;
  background-color: #e6f7ff; /* Light blue for general status */
  border: 1px solid #91d5ff;
  color: #1890ff;
  font-size: 0.95em;
}

.status-message.error {
  background-color: #fff0f6; /* Light red for error status */
  border-color: #ffadd2;
  color: #eb2f96;
}

/* Table Styles */
.chain-table-container h3 {
  color: #333;
  margin-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.table-responsive {
  overflow-x: auto;
  margin-top: 15px;
}

.chain-table {
  width: 100%;
  border-collapse: separate; /* Use separate to allow border-radius on cells */
  border-spacing: 0;
  margin-top: 0; /* Remove top margin as container has padding */
  min-width: 700px; /* Ensure table doesn't get too narrow */
}

.chain-table th,
.chain-table td {
  padding: 14px 18px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.chain-table th {
  background-color: #eaf1f9; /* Lighter blue for headers */
  font-weight: 700;
  color: #333;
  position: sticky;
  top: 0;
  z-index: 1; /* Ensure header stays above scrolling content */
}

.chain-table thead tr:first-child th:first-child {
  border-top-left-radius: 8px;
}
.chain-table thead tr:first-child th:last-child {
  border-top-right-radius: 8px;
}

.chain-table tbody tr:last-child td {
  border-bottom: none;
}

.chain-table tr:hover {
  background-color: #f7f9fd; /* Even lighter hover effect */
}

.chain-table .block-index {
  font-weight: 600;
  color: #2c7be5; /* Stronger blue */
}

.chain-table .block-timestamp {
  color: #666;
  font-size: 0.95em;
}

.chain-table .block-proof {
  color: #5cb85c; /* Greenish for proof */
  font-family: 'Consolas', monospace;
  font-size: 0.9em;
}

.chain-table .block-user,
.chain-table .block-prev-hash,
.chain-table .block-sign {
  color: #777;
  font-family: 'Consolas', monospace;
  font-size: 0.85em;
  word-break: break-all; /* Ensure long hashes/addresses wrap */
}

.chain-table .block-data {
  color: #444;
  word-break: break-word; /* Allow long data strings to wrap */
  max-width: 250px; /* Limit width to prevent overly wide columns */
}

/* Message Viewer Styles */
.message-viewer {
  margin-top: 25px;
}

.message-viewer h3 {
  color: #333;
  margin-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.messages-list {
  margin-top: 20px;
  display: grid;
  gap: 20px;
}

.message-item {
  background: #fdfdfd;
  border: 1px solid #e9e9e9;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.message-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
}

.message-item p {
  margin: 0 0 8px 0;
  font-size: 0.98em;
  color: #555;
}

.message-item p strong {
  color: #333;
}

.message-item ul {
  list-style-type: none;
  padding-left: 0;
  margin-top: 5px;
  border-left: 3px solid #4a90e2; /* Accent border for messages list */
  padding-left: 10px;
}

.message-item li {
  margin-bottom: 5px;
  color: #666;
  font-size: 0.9em;
}

.divider {
  border-bottom: 1px dashed #e0e0e0; /* Dashed divider for messages */
  margin: 15px 0 5px 0;
}

.no-messages {
  text-align: center;
  color: #999;
  font-style: italic;
  padding: 20px;
  background-color: #f8f8f8;
  border-radius: 8px;
  margin-top: 20px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .blockchain-demo {
    margin: 20px auto;
    padding: 20px;
    gap: 20px;
  }

  .btn {
    width: 100%;
    margin-right: 0;
    margin-bottom: 10px;
  }

  .action-buttons {
    flex-direction: column;
    gap: 10px;
  }

  .chain-table th,
  .chain-table td {
    padding: 10px 12px;
  }

  .chain-table {
    min-width: unset; /* Allow table to shrink on smaller screens */
  }
}

@media (max-width: 480px) {
  .blockchain-demo {
    padding: 15px;
  }

  .btn {
    font-size: 14px;
    padding: 10px 15px;
  }

  .chain-table th,
  .chain-table td {
    font-size: 0.85em;
  }
}
</style>
