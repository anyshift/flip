<template>
  <div class="tag-container" v-loading="loading" element-loading-text="正在获取内容" element-loading-background="transparent">
    <el-empty v-if="!loading && loadingError" :description="loadingErrorText"/>
    <el-container v-if="!loading && !loadingError">
      <el-header class="tag-header">
        <div class="tag-breadcrumb">
          <el-breadcrumb :separator-icon="DArrowRight">
            <el-breadcrumb-item>
              <font-awesome-icon v-if="tag.icon" :icon="tag.icon"/>
              <font-awesome-icon v-else icon="fa-solid fa-mug-hot" class="tag-icon"/>
            </el-breadcrumb-item>
            <el-breadcrumb-item :to="{path: '/tags'}">标签</el-breadcrumb-item>
            <el-breadcrumb-item :to="{path: '/tags', hash: '#' + tag.tagOption.name}">{{ tag.tagOption.name }}</el-breadcrumb-item>
            <el-breadcrumb-item>{{ tag.name }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
      </el-header>

      <el-container>
        <el-main class="tag-main">
          <div class="tag-box">
            <div class="tag-box-nav">
              <div class="tag-box-nav-left">
                <div>全部</div>
                <div>最新</div>
                <div>热门</div>
              </div>
              <div  class="tag-box-nav-right">
                <font-awesome-icon v-if="tag.icon" :icon="tag.icon"/>
                <font-awesome-icon v-else icon="fa-solid fa-mug-hot" class="tag-icon"/>
                <span class="tag-name" v-text="tag.name"></span>
              </div>
            </div>
            <div class="tag-box-body">
              <PostList :posts="posts" v-if="!loading" :total-items="totalItems" :total-page="totalPages" :current-page="currentPage"
                        :size-per-page="sizePerPage" :is-all-post="false" @changePage="changeCurrentPage" :loading="loading"/>
            </div>
          </div>
        </el-main>

        <el-aside width="230px" class="hidden-sm-and-down">
          <div style="height: 200px; background-color: var(--custom-trend-header-bg-color); border-radius: var(--custom-border-radius);"></div>
          <div style="height: 15px"></div>
          <div style="height: 200px; background-color: var(--custom-trend-header-bg-color); border-radius: var(--custom-border-radius);"></div>
          <div style="height: 15px"></div>
          <div style="height: 200px; background-color: var(--custom-trend-header-bg-color); border-radius: var(--custom-border-radius);"></div>
        </el-aside>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import {onBeforeMount, ref} from 'vue';
import {DArrowRight, Star} from '@element-plus/icons-vue';
import {getTagAPI} from "../../api/admin/tagAPI";
import {useRoute, useRouter} from "vue-router";
import PostList from '../../components/post/PostList.vue';

const route = useRoute();
const router = useRouter();

const props = defineProps({
  tagLabel: String,
})

const global_title = import.meta.env.VITE_GLOBAL_TITLE;
const tag = ref({});
const loading = ref(false);
const loadingErrorText = ref('');
const loadingError = ref(false);

const posts = ref([]);
const currentPage = ref(1);
const sizePerPage = ref(30);
const totalItems = ref();
const totalPages = ref();

function getTagAndPosts(page) {
  loading.value = true;
  setTimeout(() => {
    getTagAPI(props.tagLabel, page).then(response => {
      posts.value = response.data.posts;
      tag.value = response.data.tag;
      document.title = tag.value.name + ' - ' + global_title;
      totalItems.value = posts.value.length;
      totalPages.value = response.data.totalPages;
      loadingError.value = false;
      console.log(response)
    }).catch(error => {
      loadingErrorText.value = error.msg;
      loadingError.value = true;
      console.log(error)
    }).finally(() => {
      loading.value = false;
    })
  }, 500)
}

onBeforeMount(() => {
  if (route.query.page) {
    currentPage.value = Number(route.query.page);
  } else currentPage.value = 1;

  getTagAndPosts(currentPage.value);
})

function changeCurrentPage(newPage) {
  currentPage.value = newPage;
  getTagAndPosts(newPage);

  if (newPage === 1) {
    router.push({path: '/t/' + props.tagLabel})
  } else {
    router.push({path: '/t/' + props.tagLabel, query: {page: newPage}})
  }
}
</script>

<style scoped>
.tag-box {
  border-radius: var(--custom-border-radius);
  margin: 1px;
  border: 1px solid #eaeaea;
}

html.dark .tag-box {
  border-color: #4c4d4f;
}

.tag-box-body {
  padding: 0 10px 1px 10px;
  min-height: 98vh;
  background: var(--el-bg-color-overlay);
  border-radius: 0 0 var(--custom-border-radius) var(--custom-border-radius);
}

.tag-box-nav {
  display: flex;
  padding: 10px;
  align-items: center;
  justify-content: space-between;
  background: var(--el-bg-color-overlay);
  border-radius: var(--custom-border-radius) var(--custom-border-radius) 0 0;
  border-bottom: 1px solid var(--custom-trend-td-bottom-color);
}

.tag-box-nav-left {
  display: flex;
}

.tag-box-nav-left > div:not(:last-child) {
  margin-right: 25px;
}
.tag-box-nav-left > div {
  cursor: pointer;
}
.tag-box-nav-left > div:hover {
  color: var(--el-menu-active-color);
}

.tag-box-nav-right {
  color: #909399;
  display: flex;
  align-items: center;
}
.tag-box-nav-right .tag-name {
  font-size: 16px;
  margin-left: 5px;
}
</style>

<style>
.tag-header {
  height: fit-content;
  margin: 1px 1px 10px;
  border: 1px solid #DCDFE6;
  border-radius: var(--custom-border-radius);
  background-color: var(--el-bg-color-overlay);
}

html.dark .tag-header {
  border-color: #4c4d4f;
}

.el-header.tag-header {
  --el-header-padding: 0;
}

.tag-main.el-main {
  --el-main-padding: 0;
}

@media only screen and (min-width: 991px) {
  .tag-main.el-main {
    margin-right: 15px;
  }

  .tag-header {
    margin: 1px 1px 15px;
  }
}

.tag-breadcrumb .el-breadcrumb {
  padding: 15px 10px;
  background: #FAFAFA;
  border-radius: var(--custom-border-radius) var(--custom-border-radius);
}
.tag-breadcrumb .el-breadcrumb__inner.is-link {
  transition: unset;
  font-weight: 500;
}

html.dark .tag-breadcrumb .el-breadcrumb {
  background: var(--custom-trend-header-bg-color);
  border-bottom-color: #4c4d4f;
}

.tag-container .el-loading-mask {
  transition: unset;
  margin-top: 100px;
}
</style>