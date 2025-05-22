<template>
  <div class="media-window">
    <div class="win-title drag"></div>
    <div class="media-op no-drag">
      <div
        :class="['iconfont icon-left', currentIndex == 0 ? 'not-allow' : '']"
        @dblclick.stop
        title="上一张"
        @click="next(-1)"
      ></div>
      <div
        :class="['iconfont icon-right', currentIndex >= allFileList.length - 1 ? 'not-allow' : '']"
        @dblclick.stop
        title="下一张"
        @click="next(1)"
      ></div>
      <template v-if="fileList[0].fileType == 0">
        <el-driver direction="vertical" />
        <div
          class="iconfont icon-enlarge"
          @click.stop="changeSize(0.1)"
          title="放大"
          @dblclick.stop
        ></div>
        <div
          class="iconfont icon-narrow"
          @click="changeSize(-0.1)"
          @dblclick.stop
          title="缩小"
        ></div>
        <div
          :class="['iconfont', isOne2One ? 'icon-resize' : 'icon-source-size']"
          @dblclick.stop
          @click="resize"
          :title="isOne2One ? '图片适应窗口大小' : '图片原始大小'"
        ></div>
        <div class="iconfont icon-rotate" @dblclick.stop @click="rotate" title="旋转"></div>
        <!-- 分割线 -->
        <el-divider direction="vertical" />
        <div class="iconfont icon-download" @dblclick.stop @click="saveAs" title="另存为..."></div>
      </template>
    </div>

    <div class="media-panel">
      <viewer
        :options="options"
        @inited="inited"
        :images="fileList"
        v-if="fileList[0].fileType == 0 && fileList[0].status == 1"
      >
        <img :src="fileList[0].url" />
      </viewer>
      <div
        ref="player"
        id="player"
        v-show="fileList[0].fileType == 1 && fileList[0].status == 1"
        style="width: 100%; height: 100%"
      ></div>
      <div v-if="fileList[0].fileType == 2" class="file-panel">
        <div class="file-item">文件名：{{ fileList[0].fileName }}</div>
        <div class="file-item">文件大小：{{ Utils.size2Str(fileList[0].fileSize) }}</div>
        <div class="file_item downLoad">
          <el-button type="primary" @click="saveAs">下载文件</el-button>
        </div>
      </div>
      <div class="loading" v-if="fileList[0].status != 1">加载中...</div>
    </div>
    <WinOp @closeCallback="closeWin"></WinOp>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted, onUnmounted } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

import 'viewerjs/dist/viewer.css'
import DPlayer from 'dplayer'
import { component as viewer } from 'v-viewer'

const currentIndex = ref(0)
const allFileList = ref([])
const fileList = ref([{ fileType: 0, status: 0 }])
const localServerPort = ref()
const closeWin = () => {
  //关闭视频
  dPlayer.value.pause()
}

const options = ref({
  inline: true,
  toolbar: false,
  navbar: false,
  button: false,
  title: false,
  zoomRatio: 0.1, //缩放比例
  zoomOnWheel: false //滚轮缩放
})

const viewerMy = ref(null)

const inited = (e) => {
  viewerMy.value = e
}

//改变大小
const changeSize = (zoomRatio) => {
  if (!viewerMy.value) {
    return
  }
  viewerMy.value.zoom(zoomRatio, true)
}

//旋转
const rotate = () => {
  viewerMy.value.rotate(90, true)
}

//是否是1：1大小（100%）
const isOne2One = ref(false)

//重设大小
const resize = () => {
  isOne2One.value = !isOne2One.value
  if (!isOne2One.value) {
    viewerMy.value.zoomTo(viewerMy.value.initialImageData.retio, true)
  } else {
    viewerMy.value.zoomTo(1, true)
  }
}

const onWheel = (e) => {
  if (fileList.value[0].fileType !== 0) {
    return
  }
  console.log(e.deltaY)

  if (e.deltaY > 0) {
    changeSize(-0.1)
  } else {
    changeSize(0.1)
  }
}

const next = (index) => {
  if (currentIndex.value + index < 0 || currentIndex.value + index >= allFileList.value.length) {
    return
  }
  currentIndex.value = currentIndex.value + index
  getCurrentFile()
}

const getCurrentFile = () => {
  // debugger
  if (dPlayer.value) {
    dPlayer.value.pause()
  }
  console.log(currentIndex.value)
  const curFile = allFileList.value[currentIndex.value]
  const url = getUrl(curFile)
  fileList.value.splice(0, 1, {
    fileType: curFile.fileType,
    fileSize: curFile.fileSize,
    fileName: curFile.fileName,
    url: url,
    status: 1
  })

  console.log(url)
  if (curFile.fileType == 1) {
    dPlayer.value.switchVideo({
      url: url
    })
  }
}

const getUrl = (curFile) => {
  return `http://127.0.0.1:${localServerPort.value}/file?fileId=${curFile.fileId}&partType=${
    curFile.partType
  }&fileType=${curFile.fileType}&forceGet=${curFile.forceGet}&${new Date().getTime()}`
}

const player = ref(null)
const dPlayer = ref(null)
const initPlayer = () => {
  dPlayer.value = new DPlayer({
    element: player.value, //id player
    theme: '#b7daff',
    screenshot: true,
    video: {
      url: ''
    }
  })
}

onMounted(() => {
  initPlayer()
  window.addEventListener('wheel', onWheel)

  window.ipcRenderer.on('pageInitData', (e, data) => {
    localServerPort.value = data.localServerPort
    allFileList.value = data.fileList
    // 初始化 currentIndex,findIndex用于返回索引
    currentIndex.value = allFileList.value.findIndex((item) => item.fileId === data.currentField)

    if (currentIndex.value === -1) {
      currentIndex.value = 0
    }
    getCurrentFile()
  })
})

onUnmounted(() => {
  window.removeEventListener('wheel', onWheel)
  window.ipcRenderer.removeAllListeners('pageInitData')
})
</script>

<style lang="scss" scoped>
.media-window {
  padding: 0px;
  height: calc(100vh);
  border: 1px solid #ddd;
  background: #fff;
  position: relative;
  overflow: hidden;
  .win-title {
    height: 37px;
  }
  .media-op {
    position: absolute;
    left: 0px;
    top: 0px;
    height: 35px;
    line-height: 35px;
    display: flex;
    align-items: center;
    .iconfont {
      font-size: 18px;
      padding: 0px 10px;
      &:hover {
        background: #f3f3f3;
        cursor: pointer;
      }
    }
    .not-allow {
      cursor: not-allowed;
      color: #ddd;
      text-decoration: none;
      &:hover {
        color: #ddd;
        cursor: not-allowed;
        background: none;
      }
    }
  }
  .media-panel {
    height: calc(100vh - 37px);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    :deep(.viewer-backdrop) {
      background: #f5f5f5;
    }
    .file-panel {
      .file-item {
        margin-top: 5px;
      }
      .download {
        margin-top: 20px;
        text-align: center;
      }
    }
  }
}
</style>