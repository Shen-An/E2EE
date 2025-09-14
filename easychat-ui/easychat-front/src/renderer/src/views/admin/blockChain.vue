<template>
  <div class="renderer-blockchain-demo">
    <div class="form-container">
      <div>
        <label>私钥: </label>
        <input v-model="privateKey" type="password" style="width: 80%;">
      </div>
      <button @click="initWithPrivateKey">初始化钱包</button>
      <div v-if="walletAddress">钱包地址: {{ walletAddress }}</div>
      
      <div>
        <input v-model="messageContent" placeholder="输入消息" style="width: 70%;">
        <button @click="sendMessage">发送消息</button>
      </div>
      <button @click="createClosure">创建闭环</button>
      
      <div v-if="statusMessage" class="status-container">{{ statusMessage }}</div>
      <pre v-if="localChain" class="chain-container" style="white-space: pre-wrap;">{{ localChain }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import RendererBlockchainClosure from '@/utils/Blockchain/BlockchainClosureTool.js';

const privateKey = ref('');
const walletAddress = ref('');
const messageContent = ref('');
const localChain = ref(null);
const statusMessage = ref('请输入私钥并初始化');
const blockchainClosure = new RendererBlockchainClosure();

// 初始化钱包
const initWithPrivateKey = async () => {
  if (!privateKey.value) {
    statusMessage.value = '私钥不能为空';
    return;
  }
  
  try {
    statusMessage.value = '初始化中...';
    blockchainClosure.value = new RendererBlockchainClosure();
    walletAddress.value = await blockchainClosure.value.setPrivateKey(privateKey.value);
    statusMessage.value = '钱包初始化成功';
    
    // 初始化本地区块链
    blockchainClosure.value.localBlockchain.createGenesisBlock();
    refreshLocalChain();
  } catch (error) {
    statusMessage.value = `初始化失败: ${error.message}`;
  }
};

// 发送消息
const sendMessage = async () => {
  if (!messageContent.value || !blockchainClosure.value) return;
  
  try {
    statusMessage.value = '发送消息中...';
    const sender = walletAddress.value;
    const message = blockchainClosure.value.createMessage(sender, messageContent.value);
    statusMessage.value = '消息发送成功';
    messageContent.value = '';
    refreshLocalChain();
  } catch (error) {
    statusMessage.value = `发送失败: ${error.message}`;
  }
};

// 创建闭环
const createClosure = async () => {
  if (!blockchainClosure.value) return;
  
  try {
    statusMessage.value = '创建闭环中...';
    const closureData = await blockchainClosure.value.createRandomClosure(3);
    const receipt = await blockchainClosure.value.uploadClosure(closureData);
    statusMessage.value = `闭环已上传，交易哈希: ${receipt.transactionHash.slice(0, 10)}...`;
    refreshLocalChain();
  } catch (error) {
    statusMessage.value = `创建失败: ${error.message}`;
  }
};

// 刷新本地链数据
const refreshLocalChain = () => {
  if (blockchainClosure.value) {
    localChain.value = JSON.stringify(blockchainClosure.value.getLocalChain(), null, 2);
  }
};
</script>

<style scoped>
.renderer-blockchain-demo {
  display: flex;
  height: 100vh; /* 设置容器高度为视口高度 */
}

.form-container {
  flex: 1;
  padding: 20px;
  font-family: Arial, sans-serif;
  overflow: auto; /* 自动显示滚动条 */
  max-width: 100%;
  height: 100%;
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

input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-right: 10px;
}

.status-container {
  margin-top: 10px;
  padding: 10px;
  border-radius: 4px;
}

.chain-container {
  margin-top: 20px;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
  overflow-x: auto; /* 水平滚动条 */
  max-height: 300px; /* 限制高度以显示垂直滚动条 */
  overflow-y: auto; /* 垂直滚动条 */
}

/* 自定义滚动条样式 (可选) */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>