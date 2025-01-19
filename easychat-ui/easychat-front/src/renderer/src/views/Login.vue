<template>
  <div class="login_panel">
    <div class="title drag">飞信</div>
    <div class="login-form">
      <div class="error-msg">{{ errorMsg }}</div>
      <el-form :model="formData" :rules="rules" label-width="0px" @submit.prevent>
        <el-form-item prop="email">
          <el-input size="large" clearable placeholder="请输入邮箱" v-model.trim="formData.email">
            <template #prefix>
              <span class="iconfont icon-email"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="nickName" v-if="isLogin == false">
          <el-input size="large" clearable placeholder="请输入昵称" v-model.trim="formData.nickName">
            <template #prefix>
              <span class="iconfont icon-user-nick"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input size="large" show-password clearable placeholder="请输入密码" v-model.trim="formData.password" >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>


        <el-form-item prop="rePassword" v-if="isLogin == false">
          <el-input size="large" show-password clearable placeholder="请确认密码" v-model.trim="formData.rePassword" >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="checkCode">
          <el-input show-password clearable placeholder="请输入验证码" v-model.trim="formData.checkCode">
            <template #prefix>
              <span size="large" class="iconfont icon-checkcode"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" class="login-btn">{{ isLogin ? "登录" : "注册" }}</el-button>
        </el-form-item>
        <div class="bottom-link">

          <span class="a-link" @click="changeOpType">{{ isLogin ? "还没有账号？注册" : "已有账号？登录" }}</span>
        </div>
      </el-form>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
const formData = ref({})

const rules = { title: [{ required: true, message: '请输入内容' }] }
const isLogin = ref(true)

const errorMsg = ref('')

const changeOpType = () => {
  window.ipcRenderer.send('loginOrRegister', !isLogin.value)
  isLogin.value = !isLogin.value
}

const submit = () => {

  cleanVerify()
  debugger
  if (!checkValue('checkEmail', formData.value.email, '请输入正确的邮箱')) {
    return
  }
  if (!checkValue('checkPassword', formData.value.password, '密码只能是数字、字母、特殊字符8-18位')) {
    return
  }
  if (!checkValue('null', formData.value.checkCode, '请输入验证码')) {
    return
  }
}
const checkValue = (type, value, msg) => {

  if (proxy.Utils.isEmpty(value)) {
    errorMsg.value = msg
    return false
  }
  /**
   * 验证
   * Verify[type] 是通过 type 动态访问 Verify 对象的属性，从而调用对应的验证函数。
   */
  if(type && !proxy.Verify[type](value)){
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
<style lang="less">
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
