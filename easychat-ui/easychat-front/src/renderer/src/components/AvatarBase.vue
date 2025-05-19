<template>
    <!-- 头像 -->
  <div
    class="user-avatar"
    @click="showImageHandler"
    :style="{ width: width + 'px', height: width + 'px', borderRadius: borderRadius + 'px' }"
  >
    <ShowLocalImage
      :width="width"
      :fileId="userId"
      partType="avatar"
      :forceGet="avatarInfoStore.getForceReload(userId)"
    ></ShowLocalImage>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
const { proxy } = getCurrentInstance()
import {useAvatarInfoStore} from '@/stores/AvatarUpdateStore'

const avatarInfoStore = useAvatarInfoStore()
const showImageHandler = () => {
  if (!props.showDetail) {
    return
  }
  //TODO 显示图片详情
}
const props = defineProps({
  userId: {
    type: String
  },
  width: {
    type: Number,
    default: 40
  },
  borderRadius: {
    type: Number,
    default: 0
  },
  showDetail: {
    type: Boolean,
    default: false
  }
})
</script>

<style lang="scss" scoped>
.user-avatar {
  background: #d3d3d3;
  display: flex;
  overflow: hidden;
  cursor: pointer;
  align-items: center;
  justify-content: center;
}
</style>