<template>
  <div class="main">
    <div class="left-sider">
      <div>
        <Avatar :userId="userInfoStore.getInfo().userId" :width="35" :showDetail="false"></Avatar>
      </div>
      <div class="menu-list">
        <template v-for="item in menuList">
          <div
            :class="[
              'tab-item iconfont',
              item.icon,
              item.icon,
              item.path == currentMenu.path ? 'active' : ''
            ]"
            :key="item.index"
            v-if="item.position == 'top'"
            @click="changeMenu(item)"
          >
            <template v-if="item.name == 'chat'"> </template>
          </div>
        </template>
      </div>

      <!-- 控制底部设置菜单 -->
      <div class="menu-list menu-bottom">
        <template v-for="item in menuList">
          <div
            :class="['tab-item iconfont', item.icon, item.path == currentMenu.path ? 'active' : '']"
            :key="item.index"
            v-if="item.position == 'bottom'"
            @click="changeMenu(item)"
          ></div>
        </template>
      </div>
    </div>

    <div class="right-content">
      <router-view v-slot="{ Component }">
        <keep-alive include="chat">
          <component :is="Component" ref="componentRef"> </component>
        </keep-alive>
      </router-view>
    </div>
  </div>
  <win-op></win-op>
</template>

<script setup>
import { useGlobalInfoStore } from '../stores/GlobalInfoStore'
const globalInfoStore = useGlobalInfoStore()
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
const { proxy } = getCurrentInstance()
import { useUserInfoStore } from '@/stores/UserInfoStore'
const userInfoStore = useUserInfoStore()
import { useRouter } from 'vue-router'
const router = useRouter()
import {useSysSettingStore} from '@/stores/SysSettingStore'
const sysSettingStore = useSysSettingStore()    


const getLoginInfo = async () => {
  let resp = await proxy.Request({
    url: proxy.Api.getUserInfo
  })
  if (!resp) {
    return
  }
  userInfoStore.setInfo(resp.data)
  window.ipcRenderer.send('getLocalStore', resp.data.userId + 'localServerPort')
}

const menuList = ref([
  {
    name: 'chat',
    icon: 'icon-chat',
    path: '/chat',
    countKey: 'chatCount',
    position: 'top'
  },
  {
    name: 'contact',
    icon: 'icon-user',
    path: '/contact',
    countKey: 'contactApplyCount',
    position: 'top'
  },
  {
    name: 'mysetting',
    icon: 'icon-more2',
    path: '/setting',
    position: 'bottom'
  }
])
const currentMenu = ref(menuList.value[0])

const changeMenu = (item) => {
  currentMenu.value = item
  router.push(item.path)
}

const getSysSetting = async () => {
  let resp = await proxy.Request({
    url: proxy.Api.getSysSetting1
  })
  if (!resp) {
    return
  }
//   console.log(resp.data)
  sysSettingStore.setSetting(resp.data)
}

onMounted(() => {
  getSysSetting()
  getLoginInfo()
  window.ipcRenderer.on('getLocalStoreCallback', (event, serverPort) => {
    globalInfoStore.setInfo('localServerPort', serverPort)
  })
})
</script>

<style lang="less" scoped>
.main {
  background: #ddd;
  display: flex;
  border-radius: 0px 3px 3px 0px;
  overflow: hidden;

  .left-sider {
    width: 55px;
    background: #2e2e2e;
    text-align: center;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 35px;
    border: 1px solid #2e2e2e;
    border-right: none;
    padding-bottom: 10px;

    .menu-list {
      width: 100%;
      flex: 1;

      .tab-item {
        color: #d3d3d3;
        font-size: 20px;
        height: 40px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-top: 10px;
        cursor: pointer;
        font-size: 22px;
        position: relative;
      }

      .active {
        color: #07c160;
      }
    }

    .menu-bottom {
      display: flex;
      flex-direction: column;
      justify-content: flex-end;
    }
  }

  .right-content {
    flex: 1;
    overflow: hidden;
    border: 1px solid #ddd;
    border-left: none;
  }
}

.popover-user-panel {
  padding: 10px;

  .popover-user {
    display: flex;
    border-bottom: 1px solid #ddd;
    padding-bottom: 20px;
  }

  .send-message {
    margin-top: 10px;
    text-align: center;
    padding: 20px 0px 0px 0px;
  }
}
</style>