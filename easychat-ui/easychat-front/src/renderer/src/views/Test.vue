<template>
  <div>
    <div class="blockchain-demo">
      <button @click="connectWallet" :disabled="isConnecting">
        {{ isConnected ? '已连接钱包' : '连接MetaMask' }}
      </button>

      <div v-if="isConnected">
        <div>钱包地址: {{ walletAddress }}</div>
        <div>余额: {{ walletBalance }} WBT</div>

        <button @click="createClosure" :disabled="isLoading">创建闭环</button>
        <button @click="findNoClosureMsg" :disabled="isLoading">查找未闭环消息</button>

        <div v-if="statusMessage">{{ statusMessage }}</div>
        <!-- 修改表格展示部分 -->
        <div v-if="localChain && localChain.length > 0" class="chain-table-container">
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
                  <!-- <th>签名</th> -->
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
                  <!-- <td class="block-sign">{{ truncateHash(block.sign) }}</td> -->
                  <td class="block-prev-hash">{{ truncateHash(block.previousHash) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
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
  </div>
</template>

<script setup>
import BrowserBlockchainClosure from '@/utils/Blockchain/BlockchainClosureTool.js'
import { ethers } from 'ethers'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
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
const refreshLocalChain = (data) => {
  console.log('刷新本地链数据:', data)
  localChain.value = blockchainClosure.getLocalChain()
  for (let i = 1; i <= data.length; i++) {
    if (i - 1 > 0) localChain.value[i].timestamp = formatTimestamp(data[i - 1].sendTime)
  }
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
.chain-table-container {
  margin-top: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  padding: 15px;
}

.table-responsive {
  overflow-x: auto;
}

.chain-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.chain-table th,
.chain-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eaeaea;
}

.chain-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #333;
  position: sticky;
  top: 0;
}

.chain-table tr:hover {
  background-color: #f9fafc;
}

.chain-table .block-index {
  font-weight: 500;
  color: #4a90e2;
}

.chain-table .block-timestamp,
.chain-table .block-proof {
  color: #555;
}

.chain-table .block-user,
.chain-table .block-prev-hash,
.chain-table .block-sign {
  color: #6c757d;
  font-family: monospace;
  font-size: 0.9em;
}

.chain-table .block-data {
  color: #333;
  word-break: break-all;
}

.divider {
  border-bottom: 1px solid #eee;
  margin: 10px 0;
}
</style>