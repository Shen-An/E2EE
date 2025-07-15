

<template>
  <div>
    <AvatarBase
      :userId="userId"
      :width="width"
      :borderRadius="borderRadius"
      :showDetail="false"
      v-if="userId == 'Uroot'"
    >
    </AvatarBase>
    <el-popover
      v-else
      :width="280"
      placement="right-start"
      :show-arrow="false"
      trigger="click"
      transition="none"
      :hide-after="0"
      @show="getContactInfo"
      ref="popoverRef"
    >
      <template #reference>
        <AvatarBase
          :userId="userId"
          :width="width"
          :borderRadius="borderRadius"
          :showDetail="false"
        ></AvatarBase>
      </template>
      <template #default>
        <UserBaseInfo :userInfo="userInfo"></UserBaseInfo>
        <div class="op-btn" v-if="userId != userInfoStore.getInfo.userId">
          <!-- 好友 -->
          <el-button v-if="userInfo.contactStatus == 1" type="primary" @click="sendMessage"
            >发消息</el-button
          >
          <!-- 非好友 -->
          <el-button
            v-else-if="
              userInfo.contactStatus == 0 ||
              userInfo.contactStatus == 2 ||
              userInfo.contactStatus == 3 ||
              userInfo.contactStatus == 4 ||
              userInfo.contactStatus == 5 ||
              userInfo.contactStatus == 6
            "
            type="primary"
            @click="addContact"
            >添加好友</el-button
          >
        </div>
      </template>
    </el-popover>
    <searchAdd ref="searchAddRef"></searchAdd>
  </div>
</template>
<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRouter, useRoute } from 'vue-router';
const router = useRouter();
const route = useRoute();
import { useUserInfoStore } from '@/stores/UserInfoStore'
import searchAdd from '@/views/contact/SearchAdd.vue'
const userInfoStore = useUserInfoStore()
// 发送消息
const popoverRef = ref(null)
const emit = defineEmits(['closeDrawer'])
const sendMessage = () => {
  popoverRef.value.hide()
  emit('closeDrawer')
  console.log('userId', props.userId)
  router.push({
    path: '/chat',
    query:{
      chatId:props.userId,
      timestamp: new Date().getTime()
    }
  })
}
//TODO 添加好友
const searchAddRef = ref(null)
const addContact = () => {
  popoverRef.value.hide()
  searchAddRef.value.show({
    contactId: props.userId,
    contactType:'USER'
  })
}
const userInfo = ref({})
const getContactInfo = async () => {
  userInfo.value.userId = props.userId
  if (props.userId == userInfoStore.getInfo().userId) {
    userInfo.value = userInfoStore.getInfo()
  } else {
    let resp = await proxy.Request({
      url: proxy.Api.getContactInfo,
      params: {
        contactId: props.userId
      },
      showLoading: false
    })
    if (!resp) {
      return
    }
    userInfo.value = Object.assign({}, resp.data)
  }
}

const props = defineProps({
  userId: {
    type: String
  },
  width: {
    type: Number,
    default: 40
  },
  borderRadius: {
    type: Number,
    default: 2
  },
  groupId: {
    type: String
  }
})
</script>
<style lang="scss" scoped>
.op-btn {
  text-align: center;
  border-top: 1px solid #eaeaea;
  padding-top: 10px;
}
</style>