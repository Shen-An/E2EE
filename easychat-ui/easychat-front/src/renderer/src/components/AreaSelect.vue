<template>
  <el-cascader
    :options="AreaData"
    v-model="modelValue.areaCode"
    @change="change"
    ref="areaSelectRef"
    clearable
  >
  </el-cascader>
</template>

<script setup>
import AreaData from './AreaData'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => {
      return {
        areaCode: []
      }
    }
  }
})

const emit = defineEmits(['update:modelValue'])
const areaSelectRef = ref(null)
const change = (e) => {
  const areaData = {
    areaName: [],
    areaCode: []
  }
  const checkedNodes = areaSelectRef.value.getCheckedNodes()[0]
  if (!checkedNodes) {
    emit('update:modelValue', areaData)
    return
  }
  const pathValues = checkedNodes.pathValues
  const pathLabels = checkedNodes.pathLabels
  areaData.areaCode = pathValues
  areaData.areaName = pathLabels
  emit('update:modelValue', areaData)
}
</script>


<style lang= "scss" scoped>
</style>