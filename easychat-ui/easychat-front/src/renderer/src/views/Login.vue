<template>
  <div class="login_panel">
    <div class="title drag">飞信</div>
    <div class="loading-panel" v-if="showLoading">
      <img src="../assets/img/loading3.gif" alt="" />
    </div>
    <div class="login-form">
      <div class="error-msg">{{ errorMsg }}</div>
      <el-form :model="formData" :rules="rules" label-width="0px" @submit.prevent>
        <el-form-item prop="email">
          <el-input
            size="large"
            clearable
            placeholder="请输入邮箱"
            maxLength="30"
            v-model.trim="formData.email"
            @focus="cleanVerify"
          >
            <template #prefix>
              <span class="iconfont icon-email"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="nickName" v-if="isLogin == false">
          <el-input
            size="large"
            clearable
            maxLength="15"
            placeholder="请输入昵称"
            v-model.trim="formData.nickName"
            @focus="cleanVerify"
          >
            <template #prefix>
              <span class="iconfont icon-user-nick"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            size="large"
            show-password
            clearable
            placeholder="请输入密码"
            v-model.trim="formData.password"
            @focus="cleanVerify"
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="rePassword" v-if="isLogin == false">
          <el-input
            size="large"
            show-password
            clearable
            placeholder="请确认密码"
            v-model.trim="formData.rePassword"
            @focus="cleanVerify"
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="checkCode">
          <div class="check-code-panel">
            <el-input
              clearable
              placeholder="请输入验证码"
              v-model.trim="formData.checkCode"
              @focus="cleanVerify"
            >
              <template #prefix>
                <span size="large" class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img :src="checkCodeUrl" class="check-code" @click="changeCheckCode" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" class="login-btn">{{
            isLogin ? '登录' : '注册'
          }}</el-button>
        </el-form-item>
        <div class="bottom-link">
          <span class="a-link" @click="changeOpType">{{
            isLogin ? '还没有账号？注册' : '已有账号？登录'
          }}</span>
        </div>
      </el-form>
    </div>
  </div>
  <win-op :showSetTop="false" :showMin="false" :showMax="false" :closeType="0"></win-op>
</template>
<script setup>
import { ref, reactive, getCurrentInstance, nextTick, onMounted } from 'vue'
import { md5 } from 'js-md5'
import { useUserInfoStore } from '@/stores/UserInfoStore'
import { useRouter } from 'vue-router'
import { useSPCEKeyGenStore } from '@/stores/SPCEKeyGenStore'
const SPCEKeyGenStore = useSPCEKeyGenStore()
const router = useRouter()
const userInfoStore = useUserInfoStore()
const { proxy } = getCurrentInstance()
const formData = ref({})

const rules = { title: [{ required: true, message: '请输入内容' }] }
const isLogin = ref(true)

const errorMsg = ref('')
const checkCodeUrl = ref(null)
const showLoading = ref(false)
const changeCheckCode = async () => {
  let resp = await proxy.Request({
    url: proxy.Api.checkCode
  })
  if (!resp) {
    return
  }

  checkCodeUrl.value = resp.data.checkCode
  localStorage.setItem('checkCodeKey', resp.data.checkCodeKey)
}
changeCheckCode()
const changeOpType = () => {
  //触发回调函数
  window.ipcRenderer.send('loginOrRegister', !isLogin.value)
  isLogin.value = !isLogin.value
  nextTick(() => {
    formData.value = {}
    cleanVerify()
    changeCheckCode()
  })
}

const submit = async () => {
  cleanVerify()
  if (!checkValue('checkEmail', formData.value.email, '请输入正确的邮箱')) {
    return
  }
  if (!isLogin.value && !checkValue(null, formData.value.nickName, '请输入昵称')) {
    return
  }
  if (
    !checkValue('checkPassword', formData.value.password, '密码只能是数字、字母、特殊字符8-18位')
  ) {
    return
  }
  if (!isLogin.value && formData.value.password != formData.value.rePassword) {
    errorMsg.value = '两次密码不一致，请重新输入'
    return
  }
  if (!checkValue(null, formData.value.checkCode, '请输入验证码')) {
    return
  }
  // console.log('password:', formData.value.password);
  if (isLogin.value) {
    showLoading.value = true
  }

  let resp = await proxy.Request({
    url: isLogin.value ? proxy.Api.login : proxy.Api.register,
    showLoading: isLogin.value ? false : true,
    showError: false,
    params: {
      email: formData.value.email,
      nickName: formData.value.nickName,
      password: isLogin.value ? md5(formData.value.password) : formData.value.password,
      checkCode: formData.value.checkCode,
      checkCodeKey: localStorage.getItem('checkCodeKey')
    },
    errorCallback: (resp) => {
      showLoading.value = false
      changeCheckCode()
      errorMsg.value = resp.info
    }
  })
  if (!resp) {
    return
  }

  if (isLogin.value) {
    //登录
    userInfoStore.setInfo(resp.data)

    localStorage.setItem('token', resp.data.token)
    router.push({ path: '/main' })

    const screenWidth = window.screen.width
    const screenHeight = window.screen.height

    window.ipcRenderer.send('openChat', {
      email: formData.value.email,
      token: resp.data.token,
      userId: resp.data.userId,
      nickName: resp.data.nickName,
      admin: resp.data.admin,
      width: screenWidth,
      height: screenHeight
    })

    window.ipcRenderer.send('setLocalStore', {
      key: 'devWsDomain',
      value: proxy.Api.devWsDomain
    })
    window.ipcRenderer.send('getLocalStore', 'devWsDomain')

    //创建SPCE_UserKey,如果是空的，就创建
    window.ipcRenderer.send('genUserKey')
  
  } else {
    //注册成功后，创建edch keys，用于E2EE加密
    let pk = await generateKeys()

    let resp = await proxy.Request({
      url: proxy.Api.addPk,
      params: {
        email: formData.value.email,
        ecdhPublicKey: pk
      }
    })
    if (!resp) {
      return
    }

    // 注册
    proxy.Message.success('注册成功')
    changeOpType()
  }
}
//监听生成用户key的回调
// 渲染进程代码（前端）
const MAX_RETRY = 3
let retryTimer = null
let retryCount = 0

const onGenUserKeyCallback = () => {
  window.ipcRenderer.on('genUserKeyCallback', async (e, data) => {
    console.log('[IPC] 收到密钥回调:', data)
    
    // 清除之前的重试定时器
    if (retryTimer) {
      clearTimeout(retryTimer)
      retryTimer = null
    }

    // 错误数据处理器
    const handleInvalidData = async () => {
      if (retryCount >= MAX_RETRY) {
        console.error(`[重试终止] 已达最大重试次数 ${MAX_RETRY}`)
        showErrorDialog('密钥生成失败: 服务器响应超时')
        return
      }
      
      retryCount++
      console.log(`[重试 ${retryCount}/${MAX_RETRY}] 1秒后重新请求...`)
      
      // 使用渐近式延迟：1s → 2s → 4s
      const delay = 1000 * Math.pow(2, retryCount - 1)
      retryTimer = setTimeout(() => {
        window.ipcRenderer.send('genUserKey')
      }, delay)
    }

    try {
      // 情况1: 收到明确错误
      if (data?.error) {
        console.error('[主进程报错]', data.error)
        return await handleInvalidData()
      }

      // 情况2: 数据为空或结构不完整
      if (!data?.user?.pk || !data?.tag) {
        console.warn('[数据异常] 收到无效数据结构')
        return await handleInvalidData()
      }

      // 成功情况处理
      console.log('[成功] 收到有效密钥数据')
      retryCount = 0 // 重置计数器

      // 存储数据
      SPCEKeyGenStore.setSPCEKeyGen('user', data.user)
      SPCEKeyGenStore.setSPCEKeyGen('tag', data.tag)

      // 发送到服务端
      const resp = await proxy.Request({
        url: proxy.Api.addIllegalTrace,
        params: {
          userId: userInfoStore.getInfo().userId,
          userPk: data.user.h,
        }
      })
      console.log('服务端响应:', resp)
      
    } catch (err) {
      console.error('[处理异常]', err)
      await handleInvalidData()
    }
  })
}

// 错误弹窗示例
const showErrorDialog = (msg) => {
  // 这里调用你的UI框架弹窗方法
  console.error('最终错误:', msg)
}
const generateKeys = async () => {
  window.ipcRenderer.send('genKeys', { email: formData.value.email })
  const pk = await getPK() // 等待 Promise 完成
  // console.log('pk:', pk) // 此时 pk 有值
  return pk
}

const getPK = () =>
  new Promise((resolve) => {
    window.ipcRenderer.on('genKeysPkCallback', (e, data) => {
      resolve(data.pk)
    })
  })
const init = () => {
  onGenUserKeyCallback()
  window.ipcRenderer.send('setLocalStore', { key: 'prodDomain', value: proxy.Api.prodDomain })
  window.ipcRenderer.send('setLocalStore', { key: 'devDomain', value: proxy.Api.devDomain })
  window.ipcRenderer.send('setLocalStore', { key: 'prodWsDomain', value: proxy.Api.prodWsDomain })
  window.ipcRenderer.send('setLocalStore', { key: 'devWsDomain', value: proxy.Api.devWsDomain })

  // window.ipcRenderer.on('getLocalStoreCallback', (e, data) => {

  //   console.log('getLocalStoreCallback:', data)
  // })
}

onMounted(() => {
  init()
})

const checkValue = (type, value, msg) => {
  if (proxy.Utils.isEmpty(value)) {
    errorMsg.value = msg
    return false
  }
  /**
   * 验证
   * Verify[type] 是通过 type 动态访问 Verify 对象的属性，从而调用对应的验证函数。
   */
  if (type && !proxy.Verify[type](value)) {
    errorMsg.value = msg
    return false
  }
  return true
}

/**
 * 清空验证信息
 */
const cleanVerify = () => {
  errorMsg.value = null
}
</script>
<style lang="less" scoped>
.email-select {
  width: 250px;
}

.loading-panel {
  height: calc(100vh-32px);
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;

  img {
    width: 300px;
  }
}

.login_panel {
  background: #fff;
  border-radius: 3px;
  border: 1px solid #ddd;

  .title {
    height: 30px;
    padding: 5px 0px 0px 10px;
  }

  .login-form {
    padding: 0px 15px 29px 15px;

    :deep(.el-input__wrapper) {
      box-shadow: none;
      border-radius: none;
    }

    .el-form-item {
      border-bottom: 1px solid #ddd;
    }

    .email-pannel {
      align-items: center;
      width: 100%;
      display: flex;

      .input {
        flex: 1;
      }

      .icon-down {
        margin-left: 3px;
        width: 16px;
        cursor: pointer;
        border: none;
      }
    }

    .error-msg {
      line-height: 30px;
      height: 30px;
      color: #fb7373;
    }

    .check-code-panel {
      display: flex;

      .check-code {
        cursor: pointer;
        width: 120px;
        margin-left: 5px;
      }
    }

    .login-btn {
      width: 100%;
      margin-top: 20px;
      background: #07c160;
      height: 36px;
      font-size: 16px;
    }

    .bottom-link {
      text-align: right;
    }
  }
}
</style>
