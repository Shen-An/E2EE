

<template>
  <Dialog
    :show="dialogConfig.show"
    :title="dialogConfig.title"
    :buttons="dialogConfig.buttons"
    width="450px"
    :style="{ minHeight: '600px' }" 

    :showCancel="false"
    @close="dialogConfig.show = false"
  >
    <GroupEditForm ref="groupEditRef" @editBack="editBack"></GroupEditForm>
  </Dialog>
</template>

<script setup>

import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()
import GroupEditForm from './GroupEditForm.vue'
const dialogConfig = reactive({
  show: false,
  title: '修改群组',
  buttons: []
})

const groupEditRef = ref()
const show = (data) => {
  dialogConfig.show = true
  nextTick(() => {
    groupEditRef.value.show(data)
  })
}

const emit = defineEmits(['reloadGroupInfo'])

const editBack = () => {
  dialogConfig.show = false
  emit('reloadGroupInfo')
}

defineExpose({
  show
})
</script>

<style lang="scss" scoped>
</style>