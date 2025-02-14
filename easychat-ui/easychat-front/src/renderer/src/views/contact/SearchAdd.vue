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
}
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