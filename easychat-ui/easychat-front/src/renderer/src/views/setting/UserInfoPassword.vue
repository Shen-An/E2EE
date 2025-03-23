<template>
  <div>
    <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
      <el-form-item label="密码" prop="password">
        <el-input
          type="password"
          clearable
          placeholeder="请输入新密码"
          v-model.trim="formData.password"
          show-password
        ></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="rePassword">
        <el-input
          type="password"
          clearable
          placeholeder="请再次输入新密码"
          v-model.trim="formData.rePassword"
          show-password
        ></el-input>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="saveUserInfo">修改密码</el-button>
        <el-button @click="cancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>

import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

const formData = ref({})
const formDataRef = ref(null)

const validateRePass = (rule, value, callback) => {
  if (value !== formData.value.password) {
    callback(new Error(rule.message))
  } else {
    callback()
  }
}
const rules = {
  password: [
    { required: true, message: '请输入新密码' },
    { validator: proxy.Verify.password, message: '密码只能是数字、字母、特殊字符8-18位' }
  ],
  rePassword: [
    { required: true, message: '请再次输入新密码' },
    { validator: validateRePass, message: '两次密码输入不一致' }
  ]
}

const emit = defineEmits(['editBack'])
const saveUserInfo = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    proxy.Confirm({
      message: '确认修改密码？',
      okfun: async () => {
        let params = {}
        Object.assign(params, formData.value)

        let resp = await proxy.Request({
          url: proxy.Api.updatePassword,
          params
        })
        if (!resp) {
          return
        }
        proxy.Message.success('修改成功！请重新登录', () => {
          //TODO 重新登录
          window.icpRenderer.send('reLogin')
        })
        userInfoStore.setInfo(resp.data)
        emit("editBack")
      }
    })
  })
}

const cancel = () => {
  emit("editBack")
}
</script>

<style lang="scss" scoped>
</style>