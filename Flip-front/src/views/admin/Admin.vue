<template>
  <div style="display: flex;justify-content: space-between;">
    <el-menu
        :default-active="route.path" router
        class="sys-menu-vertical"
        :collapse-transition='false'
        @select="handleMenuSelected"
    >
      <el-menu-item :index="systemCtrlURI">
        <el-icon><DataLine /></el-icon>
        <template #title>概览</template>
      </el-menu-item>
      <el-sub-menu index="2">
        <template #title>
          <el-icon><SetUp /></el-icon>
          <span>系统</span>
        </template>
        <el-menu-item index="1-1" v-if="false">系统配置</el-menu-item>
        <el-menu-item index="/sys-ctrl/sensitive">敏感词配置</el-menu-item>
      </el-sub-menu>
      <el-menu-item :index="systemCtrlURI + '/mail'" v-if="false">
        <el-icon><Message /></el-icon>
        <template #title>邮件</template>
      </el-menu-item>
      <el-menu-item :index="systemCtrlURI + '/tag'">
        <el-icon><PriceTag /></el-icon>
        <template #title>标签</template>
      </el-menu-item>
      <el-menu-item :index="systemCtrlURI + '/authority'" v-if="false">
        <el-icon><Key /></el-icon>
        <template #title>权限</template>
      </el-menu-item>
      <el-menu-item :index="systemCtrlURI + '/user'">
        <el-icon><UserFilled /></el-icon>
        <template #title>用户</template>
      </el-menu-item>
    </el-menu>

    <div class="sys-main">
      <router-view v-slot="{ Component, route }" name="sysMain">
        <transition name="fade-transform" mode="out-in">
          <keep-alive :exclude="['AdminOverview']">
            <component :is="Component" :key="route.path"/>
          </keep-alive>
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import {
  DataLine,
  Key,
  SetUp,
  Message,
  PriceTag,
  UserFilled,
} from '@element-plus/icons-vue';
import {useRoute, useRouter} from "vue-router";

const router = useRouter();
const route = useRoute();

const props = defineProps({
  isLeftExpand: Boolean,
})
const emits = defineEmits(['closeLeftExpandDrawer'])

const systemCtrlURI:string = import.meta.env.VITE_SYSTEM_CONTROL_URI;

function handleMenuSelected(index) {
  emits('closeLeftExpandDrawer')
}
</script>

<style scoped>
.sys-menu-vertical {
  margin-right: 20px;
}
.sys-main {
  width: 100%;
  min-height: 100vh;
  overflow: hidden;
  background: var(--el-bg-color-overlay);
  border-radius: var(--custom-border-radius);
}
html.dark .sys-main {
  background-color: var(--custom-trend-header-bg-color);
}
</style>

<style>
.sys-menu-vertical:not(.el-menu--collapse) {
  width: 230px;
  height: 100%;
}
.sys-menu-vertical.el-menu {
  border-right: 0;
  border-radius: var(--custom-border-radius);
}

html.dark .sys-menu-vertical {
  background-color: var(--custom-trend-header-bg-color);
}

.sys-menu-vertical .el-menu-item {
  transition: border-color var(--el-transition-duration),background-color var(--el-transition-duration);
}

.fade-transform-leave-active, .fade-transform-enter-active {
  transition: all .5s;
}
.fade-transform-enter {
  opacity: 0;
  transform: translateX(-30px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>