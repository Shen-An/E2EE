<template>
  <div>
    <ShowLocalImage
      :fileId="data.messageId"
      partType="chat"
      :fileType="data.fileType"
      :showPlay="true"
    >
    </ShowLocalImage>
  </div>
</template>

<script setup>
import ShowLocalImage from '@/components/ShowLocalImage.vue'
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

const props = defineProps({
  data: {
    type: Object,
    default: {}
  }
})
</script>
<style lang="scss" scoped>
</style>