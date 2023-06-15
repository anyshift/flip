<template>
  <el-config-provider :locale="locale">

    <Core v-if="notLoading()"/>

    <div v-else v-loading.fullscreen.lock="fullscreenLoading"
         element-loading-text="正在加载" class="app-loading"/>

  </el-config-provider>
</template>

<script setup>
import Core from "@/views/Index.vue";
import {ref} from "vue";
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import useUserStore from "./stores/userStore";
import {tokenExists} from "./utils/token";

const locale = ref(zhCn);
const userStore = useUserStore();
zhCn.el.pagination.goto = "";
zhCn.el.pagination.pageClassifier = "";

const fullscreenLoading = ref(true)
function notLoading() {
  if (tokenExists()) {
    return userStore.role.length !== 0;
  } else return true;
}
</script>

<style>
body {
  background-color: var(--el-bg-color);
}

#app {
  font: 14px/1.5 ui-sans-serif,system-ui,-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica Neue,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji,Segoe UI Symbol,Noto Color Emoji;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}


/*避免加载完成后页面抖动一会*/
body.el-loading-parent--relative {
  position: inherit !important;
}
@media screen and (max-width: 1199px) {
  #nprogress .spinner {
    display: none !important;
  }
}

.el-loading-mask.is-fullscreen .el-loading-spinner {
  margin-top: calc((-15vh - var(--el-loading-fullscreen-spinner-size))/ 2) !important;
}
.el-loading-mask.is-fullscreen .el-loading-spinner .el-loading-text {
  font-size: 15px !important;
  letter-spacing: 1px;
  text-shadow: 1px 1px 1px rgb(0 0 0 / 10%);
}
</style>
