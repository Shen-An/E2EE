<template>
  <ContentPanel>
    <div class="user-info">
      <UserBaseInfo :userInfo="userInfo"></UserBaseInfo>
      <div class="more-op">
        <el-dropdown placement="bottom-end" trigger="click">
          <span class="el-dropdown-link">
            <div class="iconfont icon-more"></div>
          </span>

          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="addContact2BlackList">加入黑名单</el-dropdown-item>
              <el-dropdown-item @click="delContact">删除联系人</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <div class="part_item">
        <div class="part_title">个性签名</div>
        <div class="part-content">{{ userInfo.personalSignature || '-' }}</div>
      </div>
      <div class="send-message" @click="sendMessage">
        <div class="iconfont icon-chat2"></div>
        <div class="text">发送消息</div>
      </div>
    </div>
  </ContentPanel>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, watch } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
import { useContactStateStore } from '@/stores/ContactStateStore'
const contactStateStore = useContactStateStore()
const router = useRouter()
const route = useRoute()

const userInfo = ref({})
const loadUserDetail = async (contactId) => {
  let resp = await proxy.Request({
    url: proxy.Api.getContactUserInfo,
    params: {
      contactId: contactId
    }
  })
  if (!resp) {
    return
  }
  userInfo.value = resp.data
}

//加入黑名单
const addContact2BlackList = async () => {
  proxy.Confirm({
    message: '是否加入黑名单?',
    okfun: async () => {
      let resp = await proxy.Request({
        url: proxy.Api.addContact2Blacklist,
        params: {
          contactId: userInfo.value.userId
        }
      })
      if (!resp) {
        return
      }
      delContactData()
    }
  })
}

//删除联系人
const delContact = async () => {
  proxy.Confirm({
    message: '确定要删除联系人吗?',
    okfun: async () => {
      let resp = await proxy.Request({
        url: proxy.Api.delContact,
        params: {
          contactId: userInfo.value.userId
        }
      })
      if (!resp) {
        return
      }
      delContactData()
    }
  })
}


const delContactData = () => {

  contactStateStore.setContactReload('REMOVE_USER')

}
watch(
  () => route.query.contactId,
  (newVal, oldVal) => {
    if (newVal) {
      loadUserDetail(newVal)
    }
  },
  {
    immediate: true,
    deep: true
  }
)
</script>

<style lang="scss" scoped>
.user-info {
  position: relative;

  .more-op {
    position: absolute;
    right: 0px;
    top: 20px;

    .icon-more {
      color: #9e9e9e;

      &:hover {
        color: #dddddd;
      }
    }
  }
}

.part_item {
  display: flex;
  border-bottom: 1px solid #eaeaea;
  padding: 20px 0px;

  .part_title {
    width: 60px;
    color: #9e9e9e;
  }

  .part-content {
    flex: 1;
    margin-left: 15px;
    color: #161616;
  }
}

.send-message {
  width: 80px;
  margin: 0px auto;
  text-align: center;
  margin-top: 20px;
  color: #7d8cac;
  padding: 5px;

  .icon-chat2 {
    font-size: 23px;
  }

  .text {
    font-size: 12px;
    margin-top: 5px;
  }

  &:hover {
    background: #e9e9e9;
    cursor: pointer;
  }
}
</style>