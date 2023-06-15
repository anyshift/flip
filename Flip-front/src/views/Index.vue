<template>

  <Header/> <!-- 引入顶部主体 -->

  <el-row id="core" justify="center"> <!-- 核心区 -->
    <el-col :xs="0" :sm="0" :md="0" :lg="5" :xl="5" id="core-left"/> <!-- 内容主体左边距 -->

    <el-col :xs="23" :sm="23" :md="23" :lg="14" :xl="14" id="core-body"> <!-- 主体内容 -->
      <router-view v-slot="{ Component, route }">
        <transition :name="route.meta.mainTransition || 'fade-transform'" mode="out-in">
          <keep-alive :exclude="['ViewPost', 'Comments', 'MakeComment', 'Profile', 'Tags', 'Tag']">
            <component :is="Component"/>
          </keep-alive>
        </transition>
      </router-view>
    </el-col>

    <el-col :xs="0" :sm="0" :md="0" :lg="5" :xl="5" id="core-right"/> <!-- 内容主体右边距 -->
  </el-row>

  <Footer/> <!-- 引入底部主体 -->

  <el-backtop :right="250" :bottom="100" class="back-top"/>
</template>

<script setup lang="ts">
import Header from '../components/layout/header/Header.vue';
import Footer from "../components/layout/footer/Footer.vue";
import {useRoute} from "vue-router";

const route = useRoute();
</script>

<style>
/*内容主体*/
#core {
  padding: 20px 0;
  min-height: 100vh;
}

/*缩小内容主体与头部之间的距离*/
@media screen and (max-width: 991px) {
  #core {
    padding: 10px 0 !important;
  }
}

/*特定像素下扩宽内容主体*/
@media only screen and (min-width: 1200px) and (max-width: 1300px) {
  #core-left {
    max-width: 7% !important;
  }

  #core-body {
    min-width: 86% !important;
  }

  #core-right {
    max-width: 7% !important;
  }
}

@media only screen and (min-width: 1300px) and (max-width: 1450px) {
  #core-left {
    max-width: 10% !important;
  }

  #core-body {
    min-width: 80% !important;
  }

  #core-right {
    max-width: 10% !important;
  }
}

@media only screen and (min-width: 1450px) and (max-width: 1650px) {
  #core-left {
    max-width: 13% !important;
  }

  #core-body {
    min-width: 74% !important;
  }

  #core-right {
    max-width: 13% !important;
  }
}

@media only screen and (min-width: 1650px) and (max-width: 1750px) {
  #core-left {
    max-width: 19% !important;
  }

  #core-body {
    min-width: 62% !important;
  }

  #core-right {
    max-width: 19% !important;
  }
}


/*主体过渡特效*/
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

/*调整骨架屏加载颜色*/
.el-skeleton {
  --el-skeleton-color: rgba(164, 166, 173, 0.3) !important;
  --el-skeleton-to-color: rgba(245, 245, 245, 0.3) !important;
}

html.dark .el-skeleton {
  --el-skeleton-color: rgba(164, 166, 173, 0.1) !important;
  --el-skeleton-to-color: rgba(53, 54, 58, 0.1) !important;
}

.el-avatar {
  border: 1px solid var(--custom-border-color);
}

html.dark .el-pagination.is-background .btn-prev,
html.dark .el-pagination.is-background .el-pager li {
  background-color: #333841;
}

html.dark .el-pagination.is-background .el-pager li.is-active {
  color: var(--el-menu-active-color);
  font-size: 14px;
  background-color: #2d4c6f;
}

html.dark .el-pagination__total {
  color: var(--custom-trend-tr-color);
}

.el-button {
  transition: unset !important;
}

@media screen and (max-width: 768px) {
  .back-top {
    right: 20px !important;
    bottom: 50px !important;
  }
}
@media screen and (min-width: 768px) and (max-width: 1200px) {
  .back-top {
    right: 60px !important;
    bottom: 100px !important;
  }
}
@media screen and (min-width: 1200px) and (max-width: 1650px) {
  .back-top {
    right: 100px !important;
    bottom: 100px !important;
  }
}

html.dark .back-top {
  box-shadow: 0 0 6px rgb(35 40 48 / 80%);
}
</style>