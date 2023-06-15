<template>
  <div class="theme-toggle-content" @click.stop="toggleDark()">
    <div class="switch">
      <div class="switch__action">
        <div class="switch__icon">
          <div v-if="!isDark" style="margin-top: -0.05em;"><i class="czs-sun"/></div>
          <div v-if="isDark" style="margin-top: -0.11em; margin-left: 0.08em;"><i class="czs-moon"/></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useDark, useToggle } from '@vueuse/core';
import useThemeStore from "../../../../stores/themeStore";
import {onMounted, watch} from "vue";

const themeStore = useThemeStore();

const isDark = useDark()
const toggleDark = useToggle(isDark);

onMounted(() => {
  if (isDark.value) {
    themeStore.currentTheme = 'dark';
  } else themeStore.currentTheme = 'classic';
})

watch(isDark, (New, Old) => {
  if (New) themeStore.currentTheme = 'dark';
  else themeStore.currentTheme = 'classic';
})
</script>

<style scoped>
.theme-toggle-content {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #303133;
  transition: border-color .3s,background-color .2s;
  background-color: transparent;
  border-radius: 50%;
  height: 24px;
  padding: 0 8px;
}

.switch {
  margin: 0;
  display: inline-block;
  position: relative;
  width: 40px;
  height: 20px;
  border: 1px solid #dddfe6;
  background: #f2f2f2;
  outline: none;
  border-radius: 10px;
  box-sizing: border-box;
  cursor: pointer;
  transition: border-color .3s,background-color .3s;
}
.switch:hover {
  border-color: #a9a9a9;
}
.dark .switch {
  border: 1px solid #4c4d4f;
  background: #232830;
}
.dark .switch:hover {
  border-color: #6c737e;
}

.switch__action, .switch__icon {
  width: 16px;
  height: 16px;
}
.switch__action {
  position: absolute;
  top: 1px;
  left: 1px;
  border-radius: 50%;
  color: #606266;
  background-color: #ffffff;
  transform: translate(0);
  transition: border-color .3s, background-color .3s, transform .3s;
}
.dark .switch__action {
  color: #cfd3dc;
  background-color: black;
  transform: translate(20px);
}

.switch__icon {
  position: relative;
}

.czs-moon::before {
  color: #cfd3dc;
}
</style>