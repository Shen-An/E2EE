<template>
  <ChatMessageTime :data="data"></ChatMessageTime>
  <div class="sys-message">
    {{ dataMessage.messageContent }}
  </div>
</template>

<script setup>
import ChatMessageTime from './ChatMessageTime.vue'
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

const props = defineProps({
  data: {
    type: Object,
    default: {}
  }
})

const dataMessage = computed(()=>{
    const data = {...props.data}
    if(data.messageType == 1){
        data.messageContent = '我们已经成功添加为好友，可以开始聊天啦~'
    }
})
</script>

<style lang="scss" scoped>
.sys-message {
  text-align: center;
  color: #999999;
  font-size: 12px;
  margin-bottom: 20px;
}
</style>