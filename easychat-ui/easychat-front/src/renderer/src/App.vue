<template>
  <el-config-provider :locale="locale" :message="config">
    <router-view> </router-view>
  </el-config-provider>
</template>
<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
const locale = ref(zhCn)
const config = reactive({
  max: 1
})
</script>

<style lang="less"></style>
