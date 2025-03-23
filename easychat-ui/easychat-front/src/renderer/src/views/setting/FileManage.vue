<template>
  <ContentPanel v-loading="copying" element-loading-text="正在复制文件">
    <el-form 
    label-position="top"
    :model="formData" 
    :rule="rules" 
    ref="formDataRef" 
    label-width="80px" 
    @submit.prevent>
      <el-form-item label="文件管理" prop="" class="file-manage">
        <div class="file-input" :title="formData.sysSetting">
          {{ formData.sysSetting }}
        </div>
        <div class="tips">文件的默认保存位置</div>
      </el-form-item>
      <el-form-item label="" prop="">
        <el-button type="primary" @click="changeFolder">更改</el-button>
        <el-button type="primary" @click="openLocalFolder">打开文件夹</el-button>
      </el-form-item>
    </el-form>
  </ContentPanel>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

const formData = ref({})
const formDataRef = ref(null)
const rules = {
  sysSetting: [
    { required: true, message: '请选择文件保存位置' }
  ]
}

//TODO 获取文件保存位置

const changeFolder = ()=>{
  //TODO 修改文件保存位置
}

const openLocalFolder = ()=>{
  //TODO 打开文件夹
}
</script>

<style lang="scss" scoped>
.file-manage {
    :deep(.el-form-item__content) {
      display: block;
    }
  .file-input {
    background: #fff;
    padding: 0px 5px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: 16px;
  
  }
  .tips {
    color: #888888;
    font-size: 13px;
  }
}
</style>