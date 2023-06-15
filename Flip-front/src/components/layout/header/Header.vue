<template>
  <el-affix style="width: 100%">
    <el-row id="header" justify="center">
      <!-- 顶部主体左边距 -->
      <el-col :xs="0" :sm="0" :md="0" :lg="5" :xl="5" id="header-left"/>

      <!-- 顶部主体内容部分 -->
      <el-col :xs="24" :sm="24" :md="23" :lg="14" :xl="14" id="header-body">
        <el-row style="height: 100%;">
          <!-- 左边的菜单栏 -->
          <el-col :xs="7" :sm="10" :md="10" :lg="9" :xl="9" class="header-body-left">
            <router-link :to="{name: 'index'}" class="logo" v-show="!needHiddenLogo">
              <el-image :src="logo" fit="fill"/>
            </router-link>

            <div v-show="needHiddenLogo" class="left-expand-bar" @click="showLeftExpandDrawer = true">
              <el-icon size="27" color="auto"><Expand /></el-icon>
            </div>
            <LeftExpandMenu :show-left-expand="showLeftExpandDrawer" @closeLeftExpandDrawer="closeLeftExpandDrawer"/>

            <div class="more-menu hidden-md-and-up">
              <el-dropdown placement="bottom-start" trigger="hover">
                <el-icon size="25" color="#76b755" class="more-menu-icon"><Menu /></el-icon>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="(tab, index) of tabStore.menuTabs" :key="tab.path">
                      <div @click="router.push({path: tab.path})" class="menu-tabs">
                        <div><i :class="tab.icon"></i></div>
                        <div v-text="tab.name"></div>
                      </div>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>

            <el-menu :default-active="activeMenu()" router class="header-menu hidden-sm-and-down" mode="horizontal"
                     @select="handleSelect" background-color="transparent">
              <el-menu-item index="/">首页</el-menu-item>
              <el-menu-item index="/about">关于</el-menu-item>
              <el-menu-item index="/test" v-if="false">测试</el-menu-item>
            </el-menu>
          </el-col>

          <!-- 右边的功能栏 -->
          <el-col :xs="17" :sm="14" :md="14" :lg="15" :xl="15" class="header-body-right">

            <SearchBar/>

            <ThemeToggle/>

            <div class="search hidden-sm-and-up" @click="router.push({name: 'search'})">
              <i class="czs-search-l"/>
            </div>

            <div v-if="!userStore.isLogin" class="no-login">
              <el-button color="#626aef" plain @click="router.push({name: 'login'})" class="login">登录</el-button>
              <el-button color="#626aef" plain @click="router.push({name: 'register'})" class="register">注册</el-button>
            </div>

            <div v-if="userStore.isLogin" class="logged">

              <el-badge :value="311" class="item" :max="9">
                <div style="margin-top: 1.2px;"><i class="czs-bell" style="font-size: 20px; color: var(--custom-tabs-color);"></i></div>
              </el-badge>

              <el-dropdown placement="bottom-end" trigger="click">
                <div class="write-icon"><i class="czs-pen-write"></i></div>
                <template #dropdown>
                  <el-dropdown-menu class="drop-item">
                    <el-dropdown-item>
                      <div class="write-post" @click="toWrite()">
                        <div><i class="czs-pen-write"></i></div>
                        <div>创建主题</div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item command="copyURL">
                      <div class="write-blog" @click="toWrite()">
                        <div><i class="czs-book-l"></i></div>
                        <div>发表文章</div>
                      </div>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>

              <el-dropdown placement="bottom-end" trigger="click">
                <template #default>
                  <div class="user-avatar">
                    <el-avatar size="" :src="userStore.avatar" shape="square" class="avatar"/>
                  </div>
                </template>
                <template #dropdown>
                  <el-dropdown-menu class="drop-item">
                    <el-dropdown-item>
                      <div class="user-profile" @click="router.push({path: '/u/' + userStore.username})">
                        <div><i class="czs-user-l"/></div>
                        <div>个人中心</div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <div class="user-setting" @click="router.push({name: 'setting'})">
                        <div><i class="czs-setting-l"/></div>
                        <div>账号设置</div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item v-if="hasRole(['ADMIN'])">
                      <div class="admin-setting" @click="router.push({name: 'adminControl'})">
                        <div><i class="czs-home-l"/></div>
                        <div>管理中心</div>
                      </div>
                    </el-dropdown-item>
                    <el-dropdown-item>
                      <div class="user-logout" @click="doLogout">
                        <div><i class="czs-out-l"/></div>
                        <div>退出登录</div>
                      </div>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </el-col>
        </el-row>
      </el-col>

      <!-- 顶部主体右边距 -->
      <el-col :xs="0" :sm="0" :md="0" :lg="5" :xl="5" id="header-right"/>
    </el-row>

    <el-row id="banner" justify="center" v-if="!userStore.emailVerified && route.name !== 'login'">
      <el-col :xs="0" :sm="0" :md="0" :lg="5" :xl="5" id="banner-left"/>
      <el-col :xs="23" :sm="23" :md="23" :lg="14" :xl="14" id="banner-body">
        <div class="no-verify-email-banner">
          <el-alert type="warning" effect="dark" close-text="马上激活" :closable="false">
            <template #title>
              <div style="display: flex; justify-content: space-between; align-items: center; font-size: 14px;">
                <div>
                  <span>账号邮箱还未激活</span><span class="hidden-xs-only">，</span>
                  <router-link :to="{name: 'activate'}" class="to-verify hidden-xs-only">马上激活</router-link>
                </div>
                <div>
                  <i class="czs-nail hidden-xs-only"/>
                  <router-link :to="{name: 'activate'}" class="to-verify hidden-sm-and-up">马上激活</router-link>
                </div>
              </div>

            </template>
          </el-alert>
        </div>
      </el-col>
      <el-col :xs="0" :sm="0" :md="0" :lg="5" :xl="5" id="banner-right"/>
    </el-row>
  </el-affix>

  <NoVerifyEmailDialog :show-no-verify-email-dialog="showNoVerifyEmailDialog" @closeNoVerifyEmailDialog="handleCloseNoVerifyEmailDialog"/>
</template>

<script setup lang="ts">
import {getCurrentInstance, onMounted, ref, watch} from "vue";
import ThemeToggle from "./togger/ThemeToggle.vue";
import useUserStore from '../../../stores/userStore';
import {ElMessage, ElMessageBox} from "element-plus";
import {useRoute, useRouter} from "vue-router";
import {getToken} from '../../../utils/token';
import 'element-plus/theme-chalk/display.css';
import useTabStore from '../../../stores/tabStore';
import {hasRole, hasAuthority} from '../../../utils/permission';
import SearchBar from "./search/SearchBar.vue";
import NoVerifyEmailDialog from "../dialog/NoVerifyEmailDialog.vue";
import {Expand, Menu} from '@element-plus/icons-vue';
import LeftExpandMenu from "./expand/LeftExpandMenu.vue";

const logo = import.meta.env.VITE_LOGO_ADDRESS;
const userStore = useUserStore();

const router = useRouter();
const route = useRoute();
const tabStore = useTabStore();

const needHiddenLogo = ref(false);
const currentRouteName = ref('');
const windowWidth = ref()
onMounted(() => {
  windowWidth.value = window.innerWidth;
  watch(() => route.name, (New:String) => {
    currentRouteName.value = New;
    needHiddenLogo.value = (windowWidth.value < 992) && (currentRouteName.value.indexOf("admin") !== -1);
  })
  if (windowWidth.value > 992) {
    showLeftExpandDrawer.value = false;
  }
  window.onresize = () => {
    windowWidth.value = window.innerWidth;
    needHiddenLogo.value = (windowWidth.value < 992) && (currentRouteName.value.indexOf("admin") !== -1);
    if (windowWidth.value > 992) {
      showLeftExpandDrawer.value = false;
    }
  }
})

const handleSelect = (key: string, keyPath: string[]) => {
  /*console.log(key, keyPath)*/
}

const doLogout = () => {
  ElMessageBox.confirm(
      '确认退出登录?',
      '确认提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        setTimeout(() => {
          if (!getToken()) { //如果用户把localStorage中的Token删掉了，则直接重置userStore即可
            userStore.$reset();
            ElMessage({
              type: 'success',
              message: '已退出登录',
            })
            return;
          }
          userStore.logout().then(response => {
            localStorage.removeItem(userStore.$id)
            ElMessage({
              type: 'success',
              message: '已退出登录',
            })
            router.push('/')
          }).catch(error => {
            console.log(error);
          })
        }, 500)
      }).catch(() => {})
}

function activeMenu() {
  if (route.name === 'index' || route.name === 'latestPosts' || route.name === 'hotPosts') {
    return '/';
  } else return route.path;
}

function toWrite() {
  if (userStore.emailVerified) {
    router.push({name: 'writePost'});
  } else {
    showNoVerifyEmailDialog.value = true;
  }
}

const showNoVerifyEmailDialog = ref(false);
function handleCloseNoVerifyEmailDialog() {
  showNoVerifyEmailDialog.value = false;
}

const showLeftExpandDrawer = ref(false);
function closeLeftExpandDrawer() {
  showLeftExpandDrawer.value = false;
}
</script>

<style scoped>
#header {
  background: var(--custom-header-bg-color);
  border-bottom: solid 1px var(--el-menu-border-color);
  display: flex;
  height: 55px;
}
html.dark #header {
  border-bottom: 0;
}

.header-menu {
  height: 50px;
  border-bottom: 0 !important;
}

.header-body-left, .header-body-right {
  display: flex;
  align-items: center;
}

.header-body-right {
  justify-content: flex-end;
}

.logged > div {
  display: block;
  margin: 0 8px;
}

.header-body-right > div:last-child {
  padding-right: 0;
}

.no-login, .logged {
  display: flex;
  align-items: center;
}

.no-login {
  margin-left: 5px;
}

.write-icon .czs-pen-write::before {
  font-size: 20px !important;
}

.search {
  font-size: 16px;
  margin: 3px 8px 0 8px;
}
.search:hover {
  cursor: pointer;
  color: var(--el-menu-active-color);
}

.write-icon:hover {
  cursor: pointer;
  color: var(--el-menu-active-color);
}

el-dropdown > .write-icon {
  margin-right: 3px;
}

.user-avatar {
  padding: 0.07em;
  width: 38px;
  margin-right: -8px;
}

.user-avatar:hover {
  border-radius: var(--custom-border-radius);
  cursor: pointer;
}

.avatar {
  width: 38px; height: 38px;border-radius: var(--custom-border-radius);
}

a {
  text-decoration: none;
  color: currentColor;
}

.user-profile, .user-setting, .user-logout, .admin-setting, .write-post, .write-blog {
  display: flex;
  padding: 5px 16px;
}

.user-profile > div:first-child,
.user-setting > div:first-child,
.admin-setting > div:first-child {
  margin-top: 2px;
}
.user-logout > div:first-child {
  margin-top: 0.15em;
}

.more-menu {
  display: flex;
  align-items: center;
  margin-left: 10px;
}

.logo {
  display: flex;
  width: 32px;
  height: 32px;
  margin-bottom: 2px;
}

.header-menu > li {
  text-shadow: 1px 1px 1px rgb(0 0 0 / 10%);
  font-size: 15px;
}

.write-post, .write-blog {
  display: flex;
}
.write-post > div:first-child, .write-blog > div:first-child {
  margin-top: 1.5px;
}

.menu-tabs {
  display: flex;
}
.menu-tabs > div {
  margin-top: 1.3px;
}

.to-verify {
  color: #0000cd;
}
.to-verify:hover {
  text-decoration: underline;
}
html.dark .to-verify {
  color: #03ff7f;
}

.left-expand-bar {
  display: flex;
  width: 32px;
  align-items: center;
}
.left-expand-bar:hover, .more-menu-icon:hover {
  cursor: pointer;
  color: #d3691e;
}
</style>

<style>
.el-menu--horizontal .el-menu-item:not(.is-disabled):focus,
.el-menu--horizontal .el-menu-item:not(.is-disabled):hover,
.el-menu--horizontal>.el-sub-menu .el-sub-menu__title:hover {
  color: var(--el-menu-active-color) !important;
}

/*特定像素下扩宽顶部主体*/
@media only screen and (min-width: 1200px) and (max-width: 1300px) {
  #header-left, #banner-left {
    max-width: 7% !important;
  }
  #header-body, #banner-body {
    min-width: 86% !important;
  }
  #header-right, #banner-right {
    max-width: 7% !important;
  }
}
@media only screen and (min-width: 1300px) and (max-width: 1450px) {
  #header-left, #banner-left {
    max-width: 10% !important;
  }
  #header-body, #banner-body {
    min-width: 80% !important;
  }
  #header-right, #banner-right {
    max-width: 10% !important;
  }
}
@media only screen and (min-width: 1450px) and (max-width: 1650px) {
  #header-left, #banner-left {
    max-width: 13% !important;
  }
  #header-body, #banner-body {
    min-width: 74% !important;
  }
  #header-right, #banner-right {
    max-width: 13% !important;
  }
}

@media only screen and (min-width: 1650px) and (max-width: 1750px) {
  #header-left, #banner-left {
    max-width: 19% !important;
  }
  #header-body, #banner-body {
    min-width: 62% !important;
  }
  #header-right, #banner-right {
    max-width: 19% !important;
  }
}

@media screen and (max-width: 991px) {
  .header-body-left, #banner-left {
    padding-left: 10px;
  }
  .header-body-right, #banner-right {
    padding-right: 5px;
  }
  .no-login {
    padding-right: 5px !important;
  }
}

.header-menu.el-menu--horizontal>.el-menu-item,
.header-menu.el-menu--horizontal>.el-menu-item.is-active,
.header-menu.el-menu--horizontal>.el-sub-menu.is-active .el-sub-menu__title{
  border-bottom: 0 !important;
}

.header-menu {
  width: 100%;
  height: 50px;
}

.header-menu.el-menu--horizontal>.el-menu-item.is-active:hover {
  color: #d3691e !important;
}

.header-menu .el-menu-item:hover {
  background-color: transparent !important;
}

.header-menu .el-menu-item .el-image__inner {
  vertical-align: middle !important;
  margin-bottom: 5px;
}

.header-menu.el-menu {
  border-right: 0 !important;
}

.header-menu .el-sub-menu__title {
  margin-top: 0.07em;
}

.header-menu.el-menu--horizontal .el-menu-item:not(.is-disabled):focus,
.header-menu.el-menu--horizontal .el-menu-item:not(.is-disabled):hover {
  background-color: transparent !important;
}

.header-menu .el-menu-item, .el-sub-menu__title {
  transition: border-color var(--el-transition-duration),background-color var(--el-transition-duration) !important;
}

.drop-item .el-dropdown-menu__item {
  padding: 0 !important;
}

.user-logout.el-dropdown-menu__item {
  justify-content: center;
}

.no-verify-email-banner .el-alert {
  border-top-right-radius: 0;
  border-top-left-radius: 0;
  background-color: #eabc7a !important;
}
html.dark .no-verify-email-banner .el-alert {
  background-color: #a18763 !important;
}

.no-verify-email-banner .el-alert__content {
  width: 100%;
}
</style>