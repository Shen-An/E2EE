<template>
  <el-form :model="formData" :rules="rules" ref="formDataRef" label-width="80px" @submit.prevent>
    <el-form-item label="群名称" prop="groupName">
      <el-input
        maxLength="150"
        clearable
        placeholder="请输入群名称"
        v-model.trim="formData.groupName"
      >
      </el-input>
    </el-form-item>

    <el-form-item label="封面" prop="avatarFile">
      <AvatarUpload v-model="formData.avatarFile" ref="avatarUploadRef" @coverFile="saveCover">
      </AvatarUpload>
    </el-form-item>

    <el-form-item label="加入权限" prop="joinType">
      <el-radio-group v-model="formData.joinType">
        <el-radio label="1">需要管理员同意</el-radio>
        <el-radio label="0">允许任何人加入</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item label="群公告" prop="groupNotice">
      <el-input
        clearable
        placeholder="请输入群公告"
        v-model.trim="formData.groupNotice"
        type="textarea"
        rows="5"
        maxlength="300"
        :show-word-limit="true"
        resize="none"
      ></el-input>
    </el-form-item>

    <el-form :model="formData" style="width: 100%; margin-left: 160px">
      <el-form-item style="text-align: center">
        <el-button type="primary" @click="submit">
          {{ formData.groupId ? '修改群组' : '创建群组' }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-form>
</template>
<script setup>
import { ref, reactive, getCurrentInstance, nextTick, computed } from 'vue'
const { proxy } = getCurrentInstance()
import {useAvatarInfoStore}from '@/stores/AvatarUpdateStore'
const avatarInfoStore = useAvatarInfoStore()

//保存封面
const saveCover = ({ avatarFile, coverFile }) => {
  formData.value.avatarFile = avatarFile
  formData.value.avatarCover = coverFile
}
const formData = ref({})
const formDataRef = ref()
const rules = {
  title: [{ required: true, message: '请输入群名称' }],
  joinType: [{ required: true, message: '请选择加入权限' }],
  avatarFile: [{ required: true, message: '请上传群封面' }]
}
import { useContactStateStore } from '@/stores/ContactStateStore'

const contactStateStore = useContactStateStore()

const emit = defineEmits(['editBack'])

const show = (data) => {
  formDataRef.value.resetFields()
  // 转换 joinType 为字符串类型
  formData.value = {
    ...data,
    joinType: String(data.joinType),
    avatarFile: data.groupId,
  }
}

const submit = async () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return
    }
    let params = {}

    Object.assign(params, formData.value)
    //重新加载头像
    if(params.groupId){
      avatarInfoStore.setForceReload(params.groupId,false)
    }
    let resp = await proxy.Request({
      url: proxy.Api.saveGroup,
      params
    })
    if (!resp) {
      return
    }
    if (params.groupId) {
      proxy.Message.success('群聊修改成功')
      emit('editBack')
    } else {
      proxy.Message.success('群聊创建成功')
    }
    formDataRef.value.resetFields()

    contactStateStore.setContactReload('MY')
    //重新加载头像
    if(params.groupId){
      avatarInfoStore.setForceReload(params.groupId,true)
    }
  })
}

defineExpose({
  show
})
</script>
<style lang="scss" scoped>
</style>