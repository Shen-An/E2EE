import { createApp } from 'vue'
import App from './App.vue'
import ElementPlus from 'element-plus'
import *as Pinia from 'pinia'
import 'element-plus/dist/index.css'
import '@/assets/cust-elementplus.scss'
import '@/assets/base.scss'
import '@/assets/icon/iconfont.css'
import Utils from '@/utils/Utils.js'
import Verify from '@/utils/Verify.js'
import router from '@/router'
import Request from '@/utils/Request.js'
import Message from '@/utils/Message.js'
import Api from '@/utils/Api.js'
import Layout from '@/components/Layout.vue'
import WinOp from '@/components/WinOp.vue'
import Blank from '@/components/Blank.vue'
import ShowLocalImage from '@/components/ShowLocalImage.vue'
import ContentPanel from '@/components/ContentPanel.vue'
import AvatarBase from '@/components/AvatarBase.vue';
import UserBaseInfo from '@/components/UserBaseInfo.vue';

const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.use(Pinia.createPinia())
app.component('Layout', Layout)
app.component('WinOp', WinOp)
app.component('Blank', Blank)
app.component('ShowLocalImage', ShowLocalImage)
app.component('ContentPanel', ContentPanel)
app.component('AvatarBase', AvatarBase)
app.component('UserBaseInfo', UserBaseInfo)

app.mount('#app')
app.config.globalProperties.Utils = Utils
app.config.globalProperties.Verify = Verify
app.config.globalProperties.Request = Request
app.config.globalProperties.Message = Message
app.config.globalProperties.Api = Api