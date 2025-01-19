import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'

import 'element-plus/dist/index.css'
import '@/assets/cust-elementplus.scss'
import '@/assets/base.scss'
import '@/assets/icon/iconfont.css'
import Utils from '@/utils/Utils.js'
import Verify from '@/utils/Verify.js'
import router from '@/router'
const app = createApp(App)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
app.config.globalProperties.Utils = Utils
app.config.globalProperties.Verify = Verify