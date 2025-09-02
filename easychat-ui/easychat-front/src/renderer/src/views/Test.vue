<template>
    <p>当前消息：{{ message }}</p>
</template>

<script setup>
import whitechainConnector from "@/utils/Blockchain/whitechainConnector.js";
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted, watch } from 'vue'
const message = ref("");

// 获取合约消息
const fetchMsg = async () => {
  try {
    const msg = await whitechainConnector.getMessage();
    message.value = msg;
    console.log("获取到的消息：", msg);
  } catch (err) {
    console.error("获取消息失败：", err);
  }
};

onMounted(async () => {
  fetchMsg();
});

</script>