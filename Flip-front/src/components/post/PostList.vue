<template>
  <div>
    <el-skeleton :loading="props.loading" animated class="posts-skeleton">
      <template #template>
        <div style="display: flex" v-for="n of props.sizePerPage">
          <el-skeleton-item variant="image" class="post-avatar-skeleton"></el-skeleton-item>
          <el-skeleton-item variant="text" class="post-title-skeleton"></el-skeleton-item>
        </div>
      </template>
      <template #default>
        <table class="post-table">
          <tbody class="post-tbody">
          <tr v-for="post of props.posts" :key="post.id" class="posts">
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
              <el-tooltip content="回复数" placement="top" :show-after="600">
                <span class="reply">
                  <router-link :to="{path: '/p/' + post.id, hash: '#comments'}" v-text="post.replyNumber"/>
                </span>
              </el-tooltip>
              <el-tooltip content="点击量" placement="top" :show-after="600">
                <span class="view hidden-xs-only">
                  <router-link :to="{path: '/p/' + post.id}" v-text="post.viewNumber"/>
                </span>
              </el-tooltip>
            </td>
            <el-tooltip v-if="props.isAllPost" :content="post.lastCommentTime" placement="top" :show-after="600">
              <td class="post-time hidden-xs-only" v-text="moment(post.lastCommentTime).fromNow()"/>
            </el-tooltip>
            <el-tooltip v-else :content="post.createTime" placement="right" :show-after="600">
              <td class="post-time hidden-xs-only" v-text="moment(post.createTime).fromNow()"/>
            </el-tooltip>
          </tr>
          </tbody>
        </table>

        <el-pagination class="pagination"
                       v-if="!props.loading && !props.loadingError && props.totalItems !== undefined && props.totalItems > 0"
                       layout="prev, pager, jumper, next, ->, total"
                       small background
                       :total="props.totalItems"
                       :page-count="props.totalPage"
                       :pager-count="5"
                       :current-page="props.currentPage"
                       @update:current-page="changeCurrentPage"/>

        <el-empty description="还没有主题" v-if="props.posts && props.posts.length === 0"/>
        <el-empty description="没有更多了" v-if="props.posts && props.posts.length < 20 && props.posts.length > 0"/>
      </template>
    </el-skeleton>
  </div>
</template>

<script setup>
import {useRoute, useRouter} from "vue-router/dist/vue-router";
import {ref} from "vue";
import moment from "moment";

const router = useRouter();
const emits = defineEmits(['changePage'])

const props = defineProps({
  posts: Array,
  loading: Boolean,
  loadingError: Boolean,
  isAllPost: Boolean,
  sizePerPage: Number,
  totalItems: Number,
  totalPage: Number,
  currentPage: Number,
})

function changeCurrentPage(page) {
  emits("changePage", page);
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
  margin-bottom: 5px;
}
.reply > a {
  color: var(--custom-reply-num-color);
  text-decoration: unset;
}

.view {
  display: inline-block;
  padding: 0 6px 0 3px;
  border-radius: 0 6px 10px 0;
  font-size: 11px;
  background: var(--custom-view-num-bg-color);
}
.view > a {
  color: var(--custom-view-num-color);
  text-decoration: unset;
}

.posts-skeleton {
  padding: 10px 0;
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
  .posts-skeleton {
    padding: 5px 0;
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