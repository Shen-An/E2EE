<template>
  <Content-panel>
    <div class="search-form">
      <el-input
        clearable
        placeholder="请输入用户Id或群组Id"
        v-model="contactId"
        size="large"
        @keydown.enter="search"
      ></el-input>
      <div class="search-btn iconfont icon-search" @click="search">搜索</div>
    </div>
  </Content-panel>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
const contactId = ref('')
const searchResult = ref(null)
const search = async () => {
  if (!contactId.value) {
    proxy.Message.warning('请输入用户Id或群组Id')
    return
  }
  let resp = await proxy.Request({
    url: proxy.Api.search,
    params: {
      contactId: contactId.value
    }
  })

  if (!resp) {
    return
  }
  console.log(resp)
  searchResult.value = resp.data
  console.log(searchResult.value)
}
</script>

<style lang="scss" scoped>
.search-form {
  padding-top: 50px;
  display: flex;
  align-items: center;
  :deep(.el-input__wrapper) {
    border-radius: 4px 0px 0px 4px;
    border-right: none;
  }
  .search-btn {
    background: #07c160;
    color: #fff;
    line-height: 40px;
    width: 80px;
    text-align: center;
    border-radius: 0px 5px 5px 0px;
    cursor: pointer;
    &:hover {
      background: #0dd36c;
    }
  }
}
.no-data {
  padding: 30px 0px;
}
.search-result-panel {
  .search-result {
    padding: 30px 20px 20px 20px;
    background: #fff;
    border-radius: 5px;
    margin-top: 10px;
    position: relative;
    .contact-type {
      position: absolute;
      top: 0px;
      left: 0px;
      background: #2cb6fe;
      color: #fff;
      padding: 2px 5px;
      border-radius: 5px 0px 0px 0px;
      font-size: 12px;
    }
  }
  .op-btn {
    border-radius: 5px;
    margin-top: 10px;
    padding: 10px;
    background: #fff;
    text-align: center;
  }
}
</style>