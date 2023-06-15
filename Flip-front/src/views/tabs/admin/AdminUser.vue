<template>
  <div class="sys-user">
    <div class="sys-user-header">
      <el-input v-model="searchInput" class="search-user" size="large" placeholder="搜索用户" :suffix-icon="Search"/>
      <el-button v-text="'添加用户'" type="primary" plain/>
    </div>
    <div class="sys-user-body">
      <el-table :data="users" style="width: 100%" border table-layout="fixed" class="users-table" @expand-change="rowExpand">
        <template #default>
          <el-table-column fixed  type="expand">
            <template #default="props">
              <div class="expand-user">
                <div class="left-bar"></div>
                <el-form class="user-form"
                         ref="userFormRef"
                         label-position="right"
                         :rules="userRules"
                         :model="userForm[props.row.id]"
                         size="large"
                         status-icon
                         label-width="70px"
                >
                  <el-form-item label="简介">
                    <span v-text="'第 ' + userForm[props.row.id].id + ' 号成员，'"/>
                    <span v-text="'加入于 ' + moment(userForm[props.row.id].createTime).fromNow()"/>
                    <el-tooltip :content="userForm[props.row.id].createTime" placement="top" :show-after="600">
                      <el-icon style="margin-left: 2px"><QuestionFilled /></el-icon>
                    </el-tooltip>
                    <span v-text="'，注册 IP 为：'"/><code class="register-ip" v-text="userForm[props.row.id].registerIp"></code><span v-text="'。'"/>
                  </el-form-item>
                  <el-form-item label="头像" prop="avatar">
                    <el-avatar :src="userForm[props.row.id].avatar" style="margin-right: 10px;"/>
                    <el-button type="danger" :icon="Delete" circle />
                  </el-form-item>
                  <el-form-item label="用户名" prop="username">
                    <el-input
                        type="text"
                        autocomplete="off"
                        v-model.trim="userForm[props.row.id].username"
                        :keyup.native="userForm[props.row.id].username=userForm[props.row.id].username.replace(/\s+/g, '')"
                    />
                  </el-form-item>
                  <el-form-item label="昵称" prop="nickname">
                    <el-input
                        type="text"
                        autocomplete="off"
                        v-model.trim="userForm[props.row.id].nickname"
                        :keyup.native="userForm[props.row.id].nickname=userForm[props.row.id].nickname.replace(/\s+/g, '')"
                    />
                  </el-form-item>
                  <el-form-item label="邮箱" prop="email">
                    <el-input
                        type="email"
                        autocomplete="off"
                        v-model.trim="userForm[props.row.id].email"
                        :keyup.native="userForm[props.row.id].email=userForm[props.row.id].email.replace(/\s+/g, '')"
                    />
                  </el-form-item>
                  <el-form-item label="激活">
                    <el-switch :disabled="userForm[props.row.id].id === 1" v-model="userForm[props.row.id].emailVerified" inline-prompt active-text="是" inactive-text="否"/>
                    <el-tooltip content="是否激活此用户" placement="top" :show-after="600">
                      <el-icon style="margin-left: 6px; font-size: 18px;"><QuestionFilled/></el-icon>
                    </el-tooltip>
                  </el-form-item>
                  <el-form-item>
                    <el-button size="default" type="primary" style="padding: 5px 10px;">保存</el-button>
                    <el-button v-if="userForm[props.row.id].id !== 1" size="default" type="danger" style="padding: 5px 10px;">删除此用户</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="id" width="50px" label="No." align="center"/>
          <el-table-column prop="avatar" width="80px" label="头像" align="center">
            <template #default="scope">
              <el-avatar :src="scope.row.avatar"/>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" align="center" />
          <el-table-column prop="nickname" label="昵称" align="center">
            <template #default="scope">
              <span v-if="scope.row.nickname" v-text="scope.row.nickname"/>
              <span v-else v-text="'-'"/>
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" align="center">
            <template #default="scope">
              <span v-if="scope.row.role.rid === 1" v-text="scope.row.role.alias"/>
              <span v-if="scope.row.role.rid === 2" v-text="scope.row.role.alias"/>
              <span v-if="scope.row.role.rid === 3" v-text="scope.row.role.alias"/>
              <span v-if="scope.row.role.rid === 4" v-text="scope.row.role.alias"/>
            </template>
          </el-table-column>
          <el-table-column prop="emailVerified" width="80px" label="激活" align="center">
            <template #default="scope">
              <el-icon v-if="scope.row.emailVerified" class="approve"><SuccessFilled/></el-icon>
              <el-icon v-else class="disapprove"><CircleCloseFilled/></el-icon>
            </template>
          </el-table-column>
          <el-table-column prop="baned" label="状态" width="80px" align="center">
            <template #default="scope">
              <el-icon v-if="!scope.row.banned" class="approve"><SuccessFilled/></el-icon>
              <el-icon v-else class="disapprove"><WarnTriangleFilled/></el-icon>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80px" align="center">
            <template #default="scope">
              <el-button link type="primary" size="small" :disabled="scope.row.id === 1" @click="handleBanOperation(scope.row)" class="operation-button">
                <span v-if="scope.row.banned" v-text="'解禁'" class="cancel-ban"/>
                <span v-else v-text="'封禁'" class="ban"/>
              </el-button>
            </template>
          </el-table-column>
        </template>
        <template #empty>
          <div style="min-height: 300px" v-loading="loadingUsers" element-loading-text="加载中"/>
          <el-empty v-if="!loadingUsers && users.length === 0" description="还没有用户"/>
        </template>
      </el-table>
      <el-empty v-if="!loadingUsers && users.length > 0 && users.length < 20" description="没有更多用户了"/>
    </div>

    <BanUserDialog :show-ban-user-dialog="showBanUserDialog" :user="currentBanOperationUser"
                   @closeBanUserDialog="handleCloseBanUserDialog" @bannedUser="handleBanUser"/>
  </div>
</template>

<script setup lang="ts">
import { Search, SuccessFilled, CircleCloseFilled, WarnTriangleFilled, Delete, QuestionFilled } from '@element-plus/icons-vue';
import {onBeforeMount, reactive, ref, watch} from 'vue';
import {getAllUserAPI} from "../../../api/admin/userSysAPI";
import type {FormInstance, FormRules} from 'element-plus';
import moment from "moment";
import BanUserDialog from "../../../components/user/BanUserDialog.vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {cancelBanUserAPI} from '../../../api/admin/userSysAPI';

const searchInput = ref();
const users = ref([]);

const loadingUsers = ref(true);
onBeforeMount(() => {
  setTimeout(() => {
    getAllUserAPI().then(response => {
      console.log(response);
      users.value = response.data.users;
      loadingUsers.value = false;
    })
  }, 1000)
})

function rowExpand(row, expandedRows) {
  userForm[row.id] = {...row};
}

const userFormRef = ref<FormInstance>();
const userForm = reactive([])
const userRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名' },
  ],
  nickname: [
    { required: true, message: '请输入昵称' },
  ],
  email: [
    { required: true, message: '请输入邮箱' }
  ]
})

const showBanUserDialog = ref(false);
const currentBanOperationUser = ref({});
function handleBanOperation(user) {
  currentBanOperationUser.value = user;
  if (!user.banned) {
    showBanUserDialog.value = true;
  } else {
    ElMessageBox.confirm(
        '确认解除该用户的登录限制?',
        '解除封禁提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
          beforeClose: (action, instance, done) => {
            if (action === 'confirm') {
              instance.confirmButtonLoading = true;
              instance.confirmButtonText = '解除中...';
              setTimeout(() => {
                cancelBanUserAPI(user.uid).then(response => {
                  console.log(response)
                  currentBanOperationUser.value.banned = false;
                  done();
                  ElMessage({
                    message: '解除封禁成功',
                    type: 'success',
                  })
                }).catch(error => {
                  console.log(error)
                  instance.confirmButtonText = '确认';
                  ElMessage({
                    message: '解除封禁失败',
                    type: 'error',
                  })
                }).finally(() => {
                  instance.confirmButtonLoading = false;
                })
              }, 500)
            } else done();
          }
        }
    ).then(() => {}).catch(() => {})
  }
}
function handleCloseBanUserDialog() {
  showBanUserDialog.value = false;
}
function handleBanUser(id) {
  currentBanOperationUser.value.banned = true;
}
</script>

<style scoped>
.sys-user {
  padding: 12px;
}

.sys-user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 12px 24px;
  border-bottom: 1px solid #f2f2f2;
}
html.dark .sys-user-header {
  border-bottom-color: #4c4d4f;
}

.sys-user-body {
  padding: 24px 2px;
}

.search-user {
  width: 200px;
}

.approve {
  color: #228b22;
}
.disapprove {
  color: #d3691e;
}

.ban {
  color: #d3691e;
}
.ban:hover {
  color: #ff0000;
}

.expand-user {
  height: 100%;
  display: flex;
}
.expand-user .left-bar {
  width: 53px;
  background-color: #f5f7fa;
  border-right: 1px solid #ebeef5;
}

@media only screen and (max-width: 768px) {
  .expand-user .left-bar {
    width: 55px;
    left: 0;
    position: sticky;
    z-index: 2;
    background: var(--el-bg-color);
    border-right: 0;
  }
  .expand-user .left-bar:after {
    box-shadow: var(--el-table-fixed-left-column) !important;
    content: "";
    position: absolute;
    top: 0;
    width: 10px;
    right: -10px;
    bottom: -1px;
    overflow-x: hidden;
    overflow-y: hidden;
    touch-action: none;
    pointer-events: none;
  }
}

html.dark .left-bar {
  background-color: var(--custom-trend-header-bg-color);
  border-right-color: #363637;
}

.user-form {
  padding: 30px 40px 0 0;
  width: 100%;
}

.register-ip {
  margin: 0;
  line-height: 2;
  padding: 0 5px;
  font-size: 85%;
  border-radius: 3px;
  font-family: mononoki, Consolas, "Liberation Mono", Menlo, Courier, monospace, "Apple Color Emoji", "Segoe UI Emoji", "Noto Color Emoji", "Segoe UI Symbol", "Android Emoji", "EmojiSymbols";
  word-break: break-word;
  background-size: 20px 20px;
  white-space: pre-wrap;
  background-color: rgba(27, 31, 35, .05);
}
</style>

<style>
html.dark .users-table td.el-table-fixed-column--left,
html.dark .users-table td.el-table-fixed-column--right {
  background-color: transparent !important;
  transition: unset !important;
}

.users-table .el-loading-mask {
  margin-bottom: 80px;
}
html.dark .users-table .el-loading-mask {
  background-color: transparent;
}

.sys-user-body .el-table__expanded-cell {
  padding: 0;
}

.users-table .el-table__expanded-cell, .users-table .el-input__wrapper {
  transition: unset !important;
}

.operation-button.is-disabled .ban {
  color: silver;
}
</style>