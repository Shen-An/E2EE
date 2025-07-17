<template>
  <div>
    <Dialog
      :show="dialogConfig.show"
      :title="dialogConfig.title"
      :buttons="dialogConfig.buttons"
      width="400px"
      :showCancel="false"
      @close="dialogConfig.show = false"
    >
      <el-form :model="formData" :rules="rules" ref="formDataRef" @submit.prevent>
        <el-form-item label="" prop="">
          <el-input
            type="textarea"
            :rows="5"
            clearable
            placeholder="请输入验证信息"
            v-model.trim="formData.applyInfo"
            resize="none"
            show-word-limit
            maxLength="100"
          ></el-input>
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
const { proxy } = getCurrentInstance()
import { useUserInfoStore } from '@/stores/UserInfoStore'
const userInfoStore = useUserInfoStore()
const formData = ref({})
import { useContactStateStore } from '@/stores/ContactStateStore'
const contactStateStore = useContactStateStore()
const formDataRef = ref()
const rules = { title: [{ required: true, message: '请输入内容' }] }
const dialogConfig = ref({
  show: false,
  title: '提交申请',
  buttons: [
    {
      text: '确定',
      type: 'primary',
      click: (e) => {
        submitApply()
      }
    }
  ]
})
const emit = defineEmits(['reLoad'])
const submitApply = async () => {
  const { contactId, contactType, applyInfo } = formData.value
  let resp = await proxy.Request({
    url: proxy.Api.applyAdd,
    params: {
      contactId,
      applyInfo,
      contactType
    }
  })
  if (!resp) {
    return
  }
  if (resp.data == 0) {
    proxy.Message.success('添加成功')
  } else {
    proxy.Message.success('申请成功，等待对方同意')
  }
  dialogConfig.value.show = false
  emit('reLoad')

  if (resp.data == 0) {
    contactStateStore.setContactReload(contactType)
  }
  //计算通信共享密钥

  //找对方用户信息
  let resp1 = await proxy.Request({
    url: proxy.Api.loadDataList,
    params: {
      userId: contactId //这里面的contactId是receiverId，即对方的
    }
  })
  //得到对方的pk
  let resp2 = await proxy.Request({
    url: proxy.Api.loadPkDataList,
    params: {
      //对方的email
      email: resp1.data.list[0].email
    }
  })
  // console.log('pk:', resp2.data.list[0].ecdhPublicKey)
  let pk = resp2.data.list[0].ecdhPublicKey
  console.log(userInfoStore.getInfo())
  let shareKey = await sendGetShareKey(pk, userInfoStore.getInfo().email,resp1.data.list[0].email)//自己的email
  // console.log('shareKey:', shareKey)
  //这里共享密钥只是发送请求，让主进程node环境保存共享密钥到本地，并不会其他地方使用，
  //共享密钥用于主进程node生成AES密钥，用于加密通信  
}

//发送获取ShareKeySk的请求
const sendGetShareKey = async (pk, email1,email2) => {
  //1是自己的email，2是对方的email
  window.ipcRenderer.send('loadShareKey', { pk: pk, email1: email1, email2: email2 })
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

const show = (data) => {
  dialogConfig.value.show = true
  nextTick(() => {
    formDataRef.value.resetFields()
    formData.value = Object.assign({}, data)
    formData.value.applyInfo = '我是' + userInfoStore.getInfo().nickName
  })
}
defineExpose({
  show
})
</script>