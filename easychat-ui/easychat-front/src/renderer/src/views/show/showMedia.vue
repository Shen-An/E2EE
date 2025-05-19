<template>
  <div class="media-window">
    <div class="win-title drag"></div>
    <div class="media-op no-drag">
        <div>
            <div
                :class="['iconfont', 'icon-left', currentIndex == 0 ? 'not-allow' : '']"
                @dblclick.stop
                title="上一张"
                @click="next(-1)"
            ></div>
        </div>
    </div>
    <div class="media-panel"></div>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue"
const { proxy } = getCurrentInstance();
import { useRouter, useRoute } from 'vue-router';
const router = useRouter();
const route = useRoute();

const currentIndex = ref(0);
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