<template>
  <u-search ref="searchRef" :config="config" class="search-bar hidden-xs-only" @submit="submit"></u-search>
</template>

<script setup lang="ts">
import {getCurrentInstance, ref} from 'vue'
import { SearchConfig, SearchInstance } from 'undraw-ui'
import {useRoute, useRouter} from "vue-router";

const router = useRouter();
const route = useRoute();

const searchRef = ref<SearchInstance>()
const config = ref<SearchConfig>({
  keywords: ['交通规则', '交通常识', '交通工具'], // 搜索框关键字滚动
  hotSearchList: [
      '交通工具分类',
      '过马路常识',
      '交通安全小提示',
      '交通规则大全',
      '交通法',
      '交通事故处理',
      '文明驾驶'
  ] // top10 热门搜索 最多显示10条数据
})

const currentInstance = getCurrentInstance();
const submit = (val: string) => {
  //searchRef.value?.close()
  router.push({name: 'search', query: {keyword: val}})
}
</script>

<style scoped>
.search-bar {
  width: 250px !important;
  margin-right: 15px;
}
</style>

<style>
.search-bar .card-box {
  width: 251px !important;
  margin-top: 2px;
}
html.dark .search-bar .card-box {
  background-color: var(--el-bg-color) !important;
  border: 1px solid #1d1e1f;
}

.search-bar .search {
  transition: unset !important;
  border-radius: 6px !important;
}
html.dark .search-bar .search {
  background-color: var(--el-bg-color) !important;
}
</style>