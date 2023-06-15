<template>
  <el-container class="setting-container">
    <el-main class="setting-main">
      <el-tabs :tab-position="windowWidth > 768 ? 'left' : 'top'" class="setting-tabs" type="border-card" v-model="activeTab" @tab-click="changeTab">
        <el-tab-pane label="头像" name="avatar">
          <UpdateAvatar/>
        </el-tab-pane>

        <el-tab-pane label="资料" name="profile">
          <UpdateProfile/>
        </el-tab-pane>

        <el-tab-pane label="账号" name="account">
          <UpdateAccount/>
        </el-tab-pane>

<!--        <el-tab-pane label="安全" name="security">
          <el-empty description="该模块正在开发中"/>
        </el-tab-pane>-->

<!--        <el-tab-pane label="隐私" name="privacy">
          <el-empty description="该模块正在开发中"/>
        </el-tab-pane>-->

<!--        <el-tab-pane label="功能" name="function">
          <el-empty description="该模块正在开发中"/>
        </el-tab-pane>-->

<!--        <el-tab-pane label="外观" name="ui">
          <el-empty description="该模块正在开发中"/>
        </el-tab-pane>-->

<!--        <el-tab-pane label="积分" name="balance">
          <el-empty description="该模块正在开发中"/>
        </el-tab-pane>-->

<!--        <el-tab-pane label="邀请" name="invite">
          <el-empty description="该模块正在开发中"/>
        </el-tab-pane>-->
      </el-tabs>
    </el-main>

    <el-aside width="230px" class="hidden-sm-and-down" style="margin-left: 20px">
      <StatisticsAside/>
      <div style="height: 15px"></div>
      <CountdownAside/>
      <div style="height: 15px"></div>
    </el-aside>
  </el-container>
</template>

<script setup lang="ts">
import {onBeforeMount, onMounted, reactive, ref, watch} from "vue";
import {TabsPaneContext} from "element-plus";
import {useRoute, useRouter} from "vue-router";
import UpdateProfile from "../../components/user/setting/UpdateProfile.vue";
import UpdateAvatar from "../../components/user/setting/UpdateAvatar.vue";
import UpdateAccount from "../../components/user/setting/UpdateAccount.vue";
import StatisticsAside from "../../components/layout/aside/index/StatisticsAside.vue";
import CountdownAside from "../../components/layout/aside/index/CountdownAside.vue";

const router = useRouter();
const route = useRoute();

const activeTab = ref();

const changeTab = (tab: TabsPaneContext, event: Event) => {
  if (tab.paneName !== 'avatar') {
    router.push({name: 'setting', query: {tab: tab.paneName}})
  } else {
    router.push({name: 'setting'})
  }
}

//监听窗口宽度变化
const windowWidth = ref()
onMounted(() => {
  windowWidth.value = window.innerWidth;
  window.onresize = () => {
    windowWidth.value = window.innerWidth;
  }
})

onBeforeMount(() => {
  let queryTab = route.query.tab;
  switch (queryTab) {
    case 'profile': activeTab.value = 'profile'; break;
    case 'account': activeTab.value = 'account'; break;
    case 'security': activeTab.value = 'security'; break;
    case 'privacy': activeTab.value = 'privacy'; break;
    case 'function': activeTab.value = 'function'; break;
    case 'ui': activeTab.value = 'ui'; break;
    case 'balance': activeTab.value = 'balance'; break;
    case 'invite': activeTab.value = 'invite'; break;
    default: activeTab.value = 'avatar'; break;
  }
})
</script>

<style scoped>
.setting-tabs {
  min-height: 100vh;
}
html.dark .setting-tabs {
  border-color: #414243;
}

.setting-main {
  padding: 0;
}
</style>

<style>
@media screen and (min-width: 768px) {
  .setting-tabs {
    width: 100%;
    border-radius: var(--custom-border-radius);
    --el-tabs-header-height: 55px;
  }
  .setting-main {
    display: flex;
  }
}

@media screen and (max-width: 768px) {
  .setting-tabs .el-tabs__item.is-top {
    margin-top: -1px;
  }
  .setting-tabs .el-tabs__item.is-top.is-active {
    border-bottom: 1px solid #e6e9ee !important;
    background: #f5f7fa;
    border-left: 0;
    border-right: 0;
  }
  .setting-tabs .el-tabs__item {
    border: 0 !important;
  }
  .setting-tabs .el-tabs__item.is-top {
    border-top-color: transparent;
  }
}
html.dark .setting-tabs .el-tabs__header.is-left {
  border-right-color: #414243;
}

.setting-tabs .el-tabs__item {
  transition: unset !important;
}

.setting-tabs .el-tabs__item.is-active {
  color: var(--el-menu-active-color) !important;
}

html.dark .setting-tabs .el-tabs__item.is-active {
  border-top-color: transparent !important;
  border-bottom-color: transparent !important;
  border-right-color: #414243 !important;
  background: var(--custom-trend-header-bg-color);
}
@media screen and (max-width: 768px) {
  html.dark .setting-tabs .el-tabs__item.is-active {
    border-top-color: #414243 !important;
    border-bottom-color: #414243 !important;
    border-left-color: transparent !important;
    border-right-color: transparent !important;
    background: var(--custom-trend-header-bg-color);
  }
}
html.dark .setting-tabs .el-tabs__header {
  background: var(--custom-trend-header-bg-color);
}

.setting-tabs .el-tabs__content {
  padding: 0 15px 20px;
}
</style>