<template>
  <div>
    <div class="banner" v-if="false"></div>
    <el-container class="main-container">
      <el-main>
        <div class="trend" id="trend">
          <div class="trend-header">
            <el-scrollbar>
              <div class="main-tab">
                <div class="tabs">
                  <router-link :to="{path: tab.path}" v-for="tab in tabStore.mainTabs" :key="tab.path"
                               :class="isActive(tab) ? 'is-active' : '' ">
                    <div v-if="tab.icon" style="margin-top: 1.2px; margin-right: 2px;"><i :class="tab.icon"/></div>
                    <span v-text="tab.name"></span>
                  </router-link>
                </div>
                <div class="activity hidden-xs-only" v-if="userStore.isLogin">
                  <el-tooltip effect="dark" content="活跃度 100% 时将自动签到" placement="left" :show-after="600">
                    <el-progress
                        :text-inside="true"
                        :stroke-width="22"
                        :percentage="65"
                        status="warning"
                        style="width: 15%; cursor: default;"
                    />
                  </el-tooltip>
                </div>
              </div>
            </el-scrollbar>
          </div>
          <div class="trend-body">
            <router-view v-slot="{ Component, route }" name="trendBody">
              <transition :name="route.meta.bodyTransition || 'fade'" mode="out-in">
                <keep-alive>
                  <component :is="Component" :key="route.path"/>
                </keep-alive>
              </transition>
            </router-view>
          </div>
        </div>
      </el-main>
      <el-aside width="230px" class="hidden-sm-and-down">
        <StatisticsAside/>
        <div style="height: 15px"></div>
        <HotTagAside/>
        <div style="height: 15px"></div>
        <CountdownAside/>
      </el-aside>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import useTabStore from '../../stores/tabStore';
import useUserStore from '../../stores/userStore';
import {useRoute, useRouter} from "vue-router";
import 'element-plus/theme-chalk/display.css';
import StatisticsAside from "../../components/layout/aside/index/StatisticsAside.vue";
import HotTagAside from "../../components/layout/aside/index/HotTagAside.vue";
import CountdownAside from "../../components/layout/aside/index/CountdownAside.vue";

const route = useRoute();
const router = useRouter();
const tabStore = useTabStore();
const userStore = useUserStore();

function isActive(tab) {
  return tab.path === route.path;
}
</script>


<style scoped>
.trend {
  box-shadow: var(--custom-aside-box-shadow);
  border-radius: var(--custom-border-radius);
  margin: 2px;
}

.trend-header {
  padding: 10px;
  border-radius: var(--custom-border-radius) var(--custom-border-radius) 0 0;
  background: var(--el-bg-color-overlay);
  border-bottom: 1px solid var(--custom-trend-td-bottom-color);
}
html.dark .trend-header {
  background-color: var(--custom-trend-header-bg-color);
}

.trend-body {
  padding: 0 10px 1px 10px;
  min-height: 98vh;
  background: var(--el-bg-color-overlay);
  border-radius: 0 0 var(--custom-border-radius) var(--custom-border-radius);
}
html.dark .trend-body {
  background-color: var(--custom-trend-header-bg-color);
}

.is-active {
  color: var(--el-menu-active-color) !important;
}

.tabs {
  display: flex;
}

.tabs > a {
  display: flex;
  text-decoration: none;
  white-space: nowrap;
  color: var(--custom-tabs-color);
  transition: all .2s;
}

.tabs > a:not(:last-child) {
  margin-right: 25px;
}

.tabs > a:hover {
  color: var(--el-menu-active-color);
}

.activity {
  display: flex;
  justify-content: flex-end;
  margin-top: -22px;
}

.aside-block {
  height: 200px;
  border-radius: var(--custom-border-radius);
  background-color: var(--custom-trend-header-bg-color);
}
.aside-block.one {
  background: linear-gradient(45deg,#dca 12%,transparent 0,transparent 88%,#dca 0),linear-gradient(135deg,transparent 37%,#a85 0,#a85 63%,transparent 0),linear-gradient(45deg,transparent 37%,#dca 0,#dca 63%,transparent 0) #753;
  background-size: 25px 25px;
}
.aside-block.two {
  background: url(http://localhost:8080/static/image/brick.webp);
  background-size: 300px;
}
.aside-block.three {
  background: url(http://localhost:8080/static/image/tg.svg),linear-gradient(-20deg,#dcb0ed 0%,#99c99c 100%) repeat center center;
  background-blend-mode: soft-light;
  background-size: 300px,cover;
}
</style>

<style>
html.dark .el-pagination.is-background .btn-next {
  background-color: #333841 !important;
}

html.dark .el-pagination.is-background .btn-prev:disabled,
html.dark .el-pagination.is-background .btn-next:disabled {
  background-color: #303030 !important;
}

html.dark .el-progress.is-warning .el-progress-bar__inner {
  background-color: #9acd31;
}

html.dark .el-progress-bar__outer {
  background-color: var(--el-bg-color);
}

.el-progress-bar__innerText {
  color: #303133;
}

.fade-leave-active, .fade-enter-active {
  transition: all .5s;
}
.fade-enter {
  opacity: 0;
}
.fade-leave-to {
  opacity: 0;
}

.main-container .el-main {
  padding: unset !important;
}

@media only screen and (min-width: 991px) {
  .main-container .el-main {
    margin-right: 15px;
  }
}
</style>