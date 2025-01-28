import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  mode: 'hash',
  history: createWebHashHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '默认路径',
      redirect: '/login'
    },
    {
      path: '/login',
      name: '登录',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/main',
      name: '主界面',
      redirect: '/chat',
      component: () => import('@/views/Main.vue'),
      children: [{
        path: '/chat',
        name: '聊天',
        component: () => import('@/views/chat/Chat.vue')
      }, {
        path: '/contact',
        name: '联系人',
        component: () => import('@/views/contact/Contact.vue')
      },
      {
        path: '/setting',
        name: '设置',
        component: () => import('@/views/setting/Setting.vue')
      }
      ]
    }
  ]
})
export default router
