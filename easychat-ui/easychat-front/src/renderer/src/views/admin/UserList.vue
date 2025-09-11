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
    <Table
      :columns="columns"
      :fetch="loadDataList"
      :dataSource="tableData"
      :options="tableOptions"
    >
    <template #slotOperation="{index,row}">
      <el-dropdown placement="bottom-end" trigger="click">
        <span class="iconfont icon-more"></span>
        <template #dropdown>
          <el-dropdown-item @click="changeAccountStatus(row)">
          {{
            row.isLegal =='否'?'-' :'禁用'
             }}</el-dropdown-item>
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

const loadDataList = () => {
  tableData.value = {
    list: [
      {
        pkHash: '10a3128b75caf0e566fc455017fa21ca87f23417578d59200c600d66ecb6e90c',
        isLegal: '否',
        illegalMsg: '-',
        illegalCount: '0',
        email: '-'
      },
      {
        pkHash: 'fsas128b75saf3342530e5645333531ca57f3175e8d590cwqe003d66ec6e80df',
        isLegal: '否',
        illegalMsg: '-',
        illegalCount: '0',
        email: '-'
      },
      {
        pkHash: 't0a31285b76caf0766fc45z081791ca87k3417k347348d59f00c600dk6ef9x0c',
        isLegal: '否',
        illegalMsg: '-',
        illegalCount: '1',
        email: '-'
      },
      {
        pkHash: 'g0a31285b76sff076vfct5z081g1carg7bk34v7k347u4w59f0wc60f6wwwj9x0c',
        isLegal: '否',
        illegalMsg: '-',
        illegalCount: '0',
        email: '-'
      },
      {
        pkHash: '10a3128b75caf0e566fc455017fa21ca87f23417578d59200c600d66ecb6e90c',
        isLegal: '是',
        illegalMsg: '买粉吗',
        illegalCount: '3',
        email: '1@qq.com'
      }
    ],
    // 添加分页相关字段，匹配表格组件的预期
    totalCount: 5, // 总记录数
    pageSize: 10, // 每页大小
    pageNo: 1 // 当前页码
  }
}

const columns = [
  {
    label: '邮箱',
    prop: 'email',
    width: 200
  },
  {
    label: '身份标识',
    prop: 'pkHash',
    width: 200
  },
  {
    label: '是否违规',
    prop: 'isLegal',
    width: 200
  },
  {
    label: '违规次数',
    prop: 'illegalCount',
    width: 200
  },
  {
    label: '违规信息',
    prop: 'illegalMsg',
    width: 200
  },

  {
    label: '操作',
    prop: 'operation',
    width: 100,
    scopedSlots: 'slotOperation'
  }
]

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
  
  