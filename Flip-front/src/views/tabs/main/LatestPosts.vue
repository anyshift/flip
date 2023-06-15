<template>
  <div>
    <el-skeleton :loading="loading" animated style="padding: 10px 0;">
      <template #template>
        <div style="display: flex" v-for="n of sizePerPage">
          <el-skeleton-item variant="image" class="post-avatar-skeleton"></el-skeleton-item>
          <el-skeleton-item variant="text" class="post-title-skeleton"></el-skeleton-item>
        </div>
      </template>
      <template #default>
        <el-empty description="加载内容出错" v-if="loadError || totalItems === undefined"/>
        <table class="post-table" v-if="!loadError">
          <tbody class="post-tbody">
          <tr v-for="post of posts" :key="post.id.toString()" class="posts">
            <td>
              <router-link :to="{path: '/u/' + post.author}">
                <el-avatar class="post-author-avatar" :src="post.avatar" fit="fill"/>
              </router-link>
            </td>
            <td class="post-title">
              <router-link :to="{name: 'viewPost', params: {pid: post.id}}" v-text="post.title" class="post-title-text"/>
            </td>
            <td v-if="post.nickname !== null" class="post-author hidden-xs-only">
              <router-link :to="{path: '/u/' + post.author}" v-text="post.nickname"/>
            </td>
            <td v-else class="post-author hidden-xs-only">
              <router-link :to="{path: '/u/' + post.author}" v-text="post.author"/>
            </td>
            <td class="view-and-reply">
              <span class="reply" v-text="post.replyNumber"/>
              <span class="view hidden-xs-only" v-text="post.viewNumber"/>
            </td>
            <el-tooltip :content="post.createTime" placement="right" :show-after="600">
              <td class="post-time hidden-xs-only" v-text="moment(post.createTime).fromNow()"/>
            </el-tooltip>
          </tr>
          </tbody>
        </table>

        <el-pagination class="pagination"
                       v-if="!loading && !loadError && totalItems !== undefined"
                       layout="prev, pager, jumper, next, ->, total"
                       small background
                       :total="posts.length"
                       :page-count="totalPage"
                       :pager-count="5"
                       :current-page="currentPage"
                       @update:current-page="changeCurrentPage"/>

        <el-empty description="没有更多了" v-if="!loadError && posts.length < 20"/>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import {onBeforeMount, onMounted, ref} from "vue";
import {getLatestPostsAPI} from '../../../api/postAPI';
import {useRoute, useRouter} from "vue-router";
import moment from "moment";

const route = useRoute();
const router = useRouter();

const posts = ref([]);
const currentPage = ref(1);
const sizePerPage = ref(30);
const totalItems = ref();
const totalPage = ref();

const loading = ref(false);
const loadError = ref(false);

/*获取最新主题*/
const getNewestPosts = (page) => {
  loading.value = true;
  setTimeout(() => {
    getLatestPostsAPI(page).then(response => {
      loading.value = false;
      loadError.value = false;
      posts.value = response.data.posts;
      currentPage.value = Number(response.data.currentPage);
      sizePerPage.value = Number(response.data.sizePerPage);
      totalItems.value = Number(response.data.totalItems);
      totalPage.value = Number(response.data.totalPage);
      console.log(response)
    }).catch(error => {
      loading.value = false;
      loadError.value = true;
      console.log(error)
    })
  }, 500)
}

onBeforeMount(() => {
  if (route.query.page) {
    currentPage.value = Number(route.query.page);
  }
  getNewestPosts(currentPage.value);
})

/*切换页码*/
const changeCurrentPage = (page) => {
  currentPage.value = page;
  getNewestPosts(page);

  if (page === 1) {
    router.push({name: 'latestPosts'})
  } else {
    router.push({name: 'latestPosts', query: {page: page}})
  }
}
</script>

<style scoped>
.post-table {
  border-spacing: 0;
}

.posts {
  height: 55px;
}

.post-author-avatar {
  width: 33px;
  height: 33px;
  border-radius: var(--custom-border-radius);
  vertical-align: middle;
  margin-right: 8px;
}
.post-avatar-skeleton {
  width: 33px;
  height: 33px;
  margin: 5px 10px 5px 0;
}
.post-title-skeleton {
  margin-top: 13px;
  height: 18px;
}

.post-title-text {
  font-size: 0.9rem;
  color: var(--custom-trend-tr-color);;
  text-decoration: none;
}

.post-title-text:hover {
  text-decoration: underline;
}

.post-title {
  width: 74%;
  padding-right: 8px;
}

.post-author {
  width: 10%;
  font-size: 0.75rem;
  text-align: center;
}
.post-author > a {
  color: var(--custom-trend-tr-color);
  text-decoration: unset;
}

.view-and-reply {
  width: 8%;
  text-align: center;
}

.reply {
  display: inline-block;
  padding: 0 3px 0 6px;
  border-radius: 10px 0 0 6px;
  font-size: 11px;
  background: var(--custom-reply-num-bg-color);
  color: var(--custom-reply-num-color);
  margin-bottom: 5px;
}

.view {
  display: inline-block;
  padding: 0 6px 0 3px;
  border-radius: 0 6px 10px 0;
  font-size: 11px;
  background: var(--custom-view-num-bg-color);
  color: var(--custom-view-num-color);
}

@media screen and (max-width: 768px) {
  .post-author-avatar {
    width: 24px;
    height: 24px;
  }
  .post-title {
    width: 88%;
  }
  .view-and-reply {
    width: 10%;
  }
  .view-and-reply {
    text-align: right;
  }
  .reply {
    padding: 0 6px;
    border-radius: 3px;
  }
  .post-avatar-skeleton {
    width: 24px;
    height: 24px;
    margin: 5px 10px 5px 0;
  }
  .post-title-skeleton {
    margin-top: 9px;
    height: 16px;
  }
  .posts {
    height: 50px;
  }
}

.post-time {
  width: 8%;
  text-align: right;
  font-size: 11px;
  color: var(--custom-trend-tr-time-color);
  padding-right: 4px;
}

.pagination {
  margin: 10px 0;
}

td {
  border-bottom: 1px solid var(--custom-trend-td-bottom-color);
  padding: 10px 0;
}

tr:hover {
  background-color: var(--el-bg-color);
}
html.dark tr:hover {
  background-color: var(--el-bg-color-overlay) !important;
}

html.dark tr:hover {
  background-color: #232830;
}
</style>

<style>
.pagination .el-pagination__jump {
  margin-left: unset !important;
}
.pagination .el-pagination__total {
  margin-right: unset !important;
}
@media screen and (max-width: 767px) {
  .pagination .el-pagination__jump, .pagination .el-pagination__total {
    display: none !important;
  }
}

.pagination .el-pagination__goto, .pagination .el-pagination__classifier {
  display: none !important;
}
.pagination .el-input__wrapper {
  transition: unset;
}
</style>