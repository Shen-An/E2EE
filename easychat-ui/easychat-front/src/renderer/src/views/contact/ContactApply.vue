<template>
  <ContentPanel
    :showTopBorder="true"
    :infinite-scroll-immediate="true"
    v-infinite-scroll="loadApply"
  >
    <div>
      <div class="apply-item" v-for="item in applyList" :key="item.id">
        <div :class="['contact-type', item.contactType == 0 ? 'user-contact' : '']">
          {{ item.contactType == 0 ? '好友' : '群聊' }}
        </div>
        <Avatar :width="50" :userId="item.applyUserId"> </Avatar>
        <div class="contact-info">
          <div class="nick-name">{{ item.contactName }}</div>
          <div class="apply-info">{{ item.applyInfo }}</div>
        </div>
        <div class="op-btn">
          <div v-if="item.status == 0">
            <el-dropdown placement="bottom-end" trigger="click">
              <span class="el-dropdown-link">
                <el-button type="primary" size="small">接受</el-button>
              </span>
              <template #dropdown>
                <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 1)"
                  >同意</el-dropdown-item
                >
                <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 2)"
                  >拒绝</el-dropdown-item
                >
                <el-dropdown-item @click="dealWithApply(item.applyId, item.contactType, 3)"
                  >拉黑</el-dropdown-item
                >
              </template>
            </el-dropdown>
          </div>
          <div v-else class="result-name">{{ item.statusName }}</div>
        </div>
      </div>
    </div>
    <div v-if="applyList.length == 0" class="no-data">暂无申请</div>
  </ContentPanel>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
import { useContactStateStore } from '@/stores/ContactStateStore'
const contactStateStore = useContactStateStore()
const router = useRouter()
const route = useRoute()
import { useUserInfoStore } from '@/stores/UserInfoStore'
const userInfoStore = useUserInfoStore()
let pageNo = 0
let pageTotal = 1
const applyList = ref([])
const loadApply = async () => {
  pageNo++
  if (pageNo > pageTotal) {
    return
  }
  let resp = await proxy.Request({
    url: proxy.Api.loadApply,
    params: {}
  })
  if (!resp) {
    return
  }
  pageTotal = resp.data.pageTotal
  if (resp.data.pageNo == 1) {
    applyList.value = []
  }
  applyList.value = applyList.value.concat(resp.data.list)
  pageNo = resp.data.pageNo
}
loadApply()

const dealWithApply = async (applyId, contactType, status) => {
  contactStateStore.setContactReload(null)
  proxy.Confirm({
    message: '确定执行该操作吗？',
    okfun: async () => {
      let resp = await proxy.Request({
        url: proxy.Api.dealWithApply,
        params: {
          applyId: applyId,

          status: status
        }
      })
      if (!resp) {
        return
      }
      pageNo = 0
      loadApply()
      if (status == 1 && contactType == 0) {
        contactStateStore.setContactReload('USER')
      } else if (status == 1 && contactType == 1) {
        contactStateStore.setContactReload('GROUP')
      }
    }
  })

  //如果同意了，则计算共享密钥，已知对方的pk，自己的sk，计算共享密钥
  if (status == 1) {
    let resp1 = await proxy.Request({
      url: proxy.Api.loadPkDataList,
      params: {
        //找对方的email
        email: applyList.value.find((item) => item.applyId == applyId).email
      }
    })

    // console.log('email:', userInfoStore.getInfo().email)
    // console.log('pk:', resp1.data.list[0].ecdhPublicKey)
    let pk = resp1.data.list[0].ecdhPublicKey
    let shareKey = await sendGetShareKey(
      pk,
      userInfoStore.getInfo().email,
      applyList.value.find((item) => item.applyId == applyId).email
    )
    //这里共享密钥只是发送请求，让主进程node环境保存共享密钥到本地，并不会其他地方使用，
    //共享密钥用于主进程node生成AES密钥，用于加密通信
    //在加好友与同意好友这两个情况，双方在本地生成共享密钥，无需发送
  }
}
//发送获取ShareKeySk的请求
const sendGetShareKey = async (pk, email1, email2) => {
  //1是自己的，2是对方的
  window.ipcRenderer.send('loadShareKey', { pk: pk, email2: email2, email1: email1 })
  const sk = await getShareKey()
  return sk
}

//获取ShareKeySk
const getShareKey = () =>
  new Promise((resolve) => {
    window.ipcRenderer.on('loadShareKeyCallback', (e, data) => {
      resolve(data.sharedSecret)
    })
  })
//监听新朋友数量改变
</script>

<style lang="scss" scoped>
.apply-item {
  display: flex;
  align-items: center;

  border-bottom: 1px solid #ddd;
  padding: 10px 0px;
  .contact-type {
    display: flex;
    justify-content: center;
    writing-mode: vertical-rl;
    vertical-align: middle;
    background: #2cb6fe;
    color: #fff;
    border-radius: 0px 0px 0px 0px;
    height: 50px;
  }
  .user-contact {
    background: #08bf61;
  }
  .contact-info {
    width: 260px;
    margin-left: 10px;
    .nick-name {
      color: #000000;
    }
    .apply-info {
      color: #999999;
      font-size: 12px;
      margin-top: 5px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  .op-btn {
    width: 50px;
    text-align: center;
    .result-name {
      color: #999999;
      font-size: 12px;
    }
  }
}
</style>


