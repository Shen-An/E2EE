<template>
  <ContentPanel>
    <el-form ref="formDataRef" label-width="100px" @submit.prevent>
      <el-form-item label="版本信息">
        <div class="version-info">
          <div>
            <span>飞信 {{ config.version }}</span>
            <el-button style="margin-left: 70px;" type="primary" @click="checkUpdate">检查更新</el-button>
          </div>
          <div>
           
          </div>
        </div>
      </el-form-item>
    </el-form>
  </ContentPanel>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRouter, useRoute } from 'vue-router';
const router = useRouter();
const route = useRoute();
import config from '../../../../../package.json'
const checkUpdate = ()=>{
  //TODO 检查更新
}

</script>

<style lang="scss" scoped>
</style>