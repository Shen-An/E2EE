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
      path: '/test',
      name: '测试',
      component: () => import('@/views/Test.vue')
    },
    {
      path: '/login',
      name: '登录',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/showMedia',
      name: '展示媒体信息',
      component: () => import('@/views/show/showMedia.vue')
    },
    {
      path: '/admin',
      name: '管理后台',
      redirect: '/admin/userList',
      component: () => import('@/views/admin/Admin.vue'),
      children: [
        {
          path: '/admin/userList',
          name: '用户管理',
          component: () => import('@/views/admin/UserList.vue')
        }, 
        {
          path:'/admin/blockChain',
          name: '区块链',
          component: () => import('@/views/admin/blockChain.vue')
        },
        {
          path: '/admin/groupList',
          name: '群组管理',
          component: () => import('@/views/admin/GroupList.vue')
        },
        {
          path: '/admin/sysSetting',
          name: '系统设置',
          component: () => import('@/views/admin/SysSetting.vue')
        },
        {
          path:'/admin/update',
          name: '版本管理',
          component: () => import('@/views/admin/Update.vue')
        }
      ]
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
        redirect: '/contact/blank',
        component: () => import('@/views/contact/Contact.vue'),
        children: [{
          path: '/contact/blank',
          name: '空白',
          component: () => import('@/views/contact/BlankPage.vue')
        },
        {
          path: '/contact/search',
          name: '搜索',
          component: () => import('@/views/contact/Search.vue')
        },
        {
          path: '/contact/createGroup',
          name: '新建群聊',
          component: () => import('@/views/contact/GroupEdit.vue')
        }, {
          path: '/contact/userDetail',
          name: '用户详情',
          component: () => import('@/views/contact/UserDetail.vue')
        }, {
          path: '/contact/groupDetail',
          name: '群组详情',
          component: () => import('@/views/contact/GroupDetail.vue')
        },
        {
          path: '/contact/contactNotice',
          name: '新的朋友',
          component: () => import('@/views/contact/ContactApply.vue')
        }
        ]
      },
      {
        path: '/setting',
        name: '设置',
        component: () => import('@/views/setting/Setting.vue'),
        redirect: '/setting/userInfo',
        children: [{
          path: '/setting/userInfo',
          name: '个人信息',
          component: () => import('@/views/setting/UserInfo.vue')
        },
        {
          path: '/setting/fileManage',
          name: '文件管理',
          component: () => import('@/views/setting/fileManage.vue')
        },
        {
          path: '/setting/about',
          name: '关于',
          component: () => import('@/views/setting/About.vue')
        },
        ]
      }]
    }
  ]
})
export default router
