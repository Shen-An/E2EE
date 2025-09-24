<template>
  <div class="top-panel">
    <el-card>
      <el-form :model="searchForm" label-width="70px" label-position="right">
        <el-row>
          <el-col :span="5">
            <el-form-item label="UID" label-width="40px">
              <el-input
                class="password-input"
                v-model="searchForm.userId"
                clearable
                @keyup.native="loadDataList"
              >
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="昵称">
              <el-input
                class="password-input"
                v-model="searchForm.nickNameFuzzy"
                clearable
                placeholder="支持模糊查询"
                @keyup.native="loadDataList"
              >
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="4" :style="{ paddingLeft: '10px' }">
            <el-button type="success" @click="loadDataList()">查询</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
  <el-card class="table-data-card">
    <Table :columns="columns" :fetch="loadDataList" :dataSource="tableData" :options="tableOptions">
      <template #slotOperation="{ index, row }">
        <el-dropdown placement="bottom-end" trigger="click">
          <span class="iconfont icon-more"></span>
          <template #dropdown>
            <el-dropdown-item @click="changeAccountStatus(row)">
              {{ row.isLegal == '否' ? '-' : '禁用' }}</el-dropdown-item
            >
          </template>
        </el-dropdown>
      </template>
    </Table>
  </el-card>
</template>
  
  <script setup>
import { ref, reactive, getCurrentInstance, nextTick } from 'vue'
const { proxy } = getCurrentInstance()
import { useRouter, useRoute } from 'vue-router'
const router = useRouter()
const route = useRoute()

const tableData = ref({})
const tableOptions = {}

const changeAccountStatus = (data) => {
  let status = data.status == 0 ? 1 : 0
  let info = status == 0 ? '禁用' : '启用'
  proxy.Confirm({
    message: `确认要【${info}】【${data.nickName}】吗?`,
    okfun: async () => {
      // let result = await proxy.Request({
      //   url: proxy.Api.updateUserstatus,
      //   params: {
      //     userId: data.userId,
      //     status: status
      //   }
      // })
    }
  })
}

const loadDataList = async () => {
  let resp = await proxy.Request({
    url: proxy.Api.selectIllegalInformation
  })
  if (!resp) {
    return
  }
  for(let data of resp.data) {
    // 格式化时间戳
    data.sendTime = formatTimestamp(data.sendTime)
    console.log(data.e2eeCt)
    data.e2eeCt = data.e2eeCt.replace(':', '');
  }
  console.log(resp)
  tableData.value.list = resp.data

  tableData.value.totalCount = resp.data.length
  tableData.value.pageSize = 10
  tableData.value.pageNo = 1
}

const columns = [
  {
    label: '发送者Id',
    prop: 'sendUserId',
    width: 150
  },
  {
    label: '发送时间',
    prop: 'sendTime',
    width: 170
  },

  {
    label: '违规内容',
    prop: 'messageContent',
    width: 180
  },
  {
    label: '违规E2EE密文',
    prop: 'e2eeCt',
    width: 535
  },
  {
    label: '操作',
    prop: 'operation',
    width: 100,
    scopedSlots: 'slotOperation'
  }
]
// 格式化时间戳函数（支持毫秒级时间戳和ISO格式字符串）
const formatTimestamp = (timestamp) => {
  try {
    let date

    // 处理毫秒级时间戳（数字类型或字符串类型的数字）
    if (
      typeof timestamp === 'number' ||
      (typeof timestamp === 'string' && /^\d+$/.test(timestamp))
    ) {
      date = new Date(Number(timestamp))
    }
    // 处理ISO格式日期字符串（如 "2025-06-25T05:54:19.309Z"）
    else if (typeof timestamp === 'string') {
      // 尝试解析ISO格式
      date = new Date(timestamp)
      // 检查解析是否有效
      if (isNaN(date.getTime())) {
        // 尝试作为普通字符串解析
        date = new Date(Date.parse(timestamp))
      }
    } else {
      throw new Error('不支持的时间戳类型')
    }

    // 优化日期显示格式（年-月-日 时:分:秒 时区）
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')

    // 获取时区偏移并格式化为 ±HH:MM
    const offset = date.getTimezoneOffset()
    const offsetHours = Math.abs(Math.floor(offset / 60))
    const offsetMinutes = Math.abs(offset % 60)
    const timezone =
      offset <= 0
        ? `+${String(offsetHours).padStart(2, '0')}:${String(offsetMinutes).padStart(2, '0')}`
        : `-${String(offsetHours).padStart(2, '0')}:${String(offsetMinutes).padStart(2, '0')}`

    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds} `
  } catch (e) {
    console.error('时间戳解析失败:', timestamp, e)
    return timestamp || '未知时间'
  }
}

const searchForm = ref({})
</script>
  
<style lang="scss" scoped>
.icon-man {
  color: #2cb6fe;
}
.icon-woman {
  color: #fb7373;
}
</style>
  
  