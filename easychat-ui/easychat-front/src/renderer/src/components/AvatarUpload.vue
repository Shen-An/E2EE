<template>
  <div class="avatar-upload">
    <div class="avatar-show">
      <template v-if="modelValue">
        <el-image v-if="preview" :src="localFile" fit="scale-down"> </el-image>
        <ShowLocalImage :fileId="props.modelValue" partType="avatar" :width="40" v-else>
        </ShowLocalImage>
      </template>
      <template v-else>
        <el-upload
          name="file"
          :show-file-list="false"
          accept=".png,.PNG,.jpg,.JPG,.jpeg,.JPEG,.gif,.GIF,.bmp,.BMP"
          :multiple="false"
          :http-request="uploadImage"
        >
          <span class="iconfont icon-add"></span>
        </el-upload>
      </template>
    </div>
    <div class="select-btn">
      <el-upload
        name="file"
        :show-file-list="false"
        accept=".png,.PNG,.jpg,.JPG,.jpeg,.JPEG,.gif,.GIF,.bmp,.BMP"
        :multiple="false"
        :http-request="uploadImage"
      >
        <el-button size="small" type="primary">选择</el-button>
      </el-upload>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, computed, onUnmounted } from 'vue'
const { proxy } = getCurrentInstance()

const localFile = ref(null)
const preview = computed(() => {
  return props.modelValue instanceof File
})
//TODO文件上传

const props = defineProps({
  modelValue: {
    type: [String, String],
    default: null
  }
})
const emit = defineEmits(['coverFile'])
const uploadImage = async (file) => {
  file = file.file
  //
  window.ipcRenderer.send('createCover', file.path)
}

onMounted(() => {
  window.ipcRenderer.on('createCoverCallback', (e, { avatarStream, coverStream }) => {
    //得到流，转成图片
    const coverBlob = new Blob([coverStream], { type: 'image/png' })
    const coverFile = new File([coverBlob], '666.jpg')
    let img = new FileReader()
    img.readAsDataURL(coverFile)
    img.onload = ({ target }) => {
      // debugger
      localFile.value = target.result
    }

    const avatarBlob = new Blob([avatarStream], { type: 'image/png' })
    const avatarFile = new File([avatarBlob], '6662.jpg')
    emit('coverFile', { avatarFile, coverFile }) //传到UserInfoEdit.vue saveCover方法
  })
})
onUnmounted(() => {
  window.ipcRenderer.removeAllListeners('createCoverCallback')
})
</script>

<style lang="scss" scoped>
.avatar-upload {
  display: flex;
  justify-content: center;
  align-items: center;
  line-height: normal;
  .avatar-show {
    background: #ededed;
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: relative;
    .icon-add {
      font-size: 30px;
      color: #b9b9b9;
      width: 60px;
      height: 60px;
      text-align: center;
      line-height: 60px;
    }
    img {
      width: 100%;
      height: 100%;
    }
    .op {
      position: absolute;
      color: #0e8aef;
      top: 80px;
    }
  }
  .select-btn {
    vertical-align: bottom;
    margin-left: 5px;
  }
}
</style>