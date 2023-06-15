<template>
  <div class="user-posts" v-loading="loading" element-loading-background="transparent"
       element-loading-text="获取中">
    <el-empty description="无数据" v-if="!loading && posts.length === 0"></el-empty>

    <div class="user-post-list" v-if="!loading && posts.length > 0">
      <div class="post-item" v-for="(post, index) of posts" :key="index">
        <div class="item-box">
          <div class="box-header">
            <router-link :to="{path: '/p/' + post.id}" class="post-title">
              <h3 class="post-title-text" v-text="post.title"></h3>
            </router-link>
          </div>
          <div class="box-footer">
            <div class="footer-left">
              <el-tooltip effect="dark" :content="post.createTime" placement="right" :show-after="600">
                <div v-text="moment(post.createTime).fromNow()"></div>
              </el-tooltip>
              <div class="replies-number">
                <div style="margin-right: 3px; margin-top: 1px;"><i class="czs-talk"/></div>
                <div v-text="post.replyNumber"></div>
              </div>
            </div>
            <div class="footer-right">

            </div>
          </div>
          <el-divider class="post-item-divider"/>
        </div>
      </div>
      <el-empty description="没有更多了" v-if="!loading && posts.length < 20"/>
    </div>
  </div>
</template>

<script setup>
import {getUserPostsAPI} from "../../../api/userAPI";
import {reactive, ref} from "vue";
import moment from "moment";

const props = defineProps({
  username: String,
})

const posts = ref({});

const loading = ref(false)

function getUserPosts() {
  loading.value = true;
  setTimeout(() => {
    getUserPostsAPI(props.username).then(response => {
      posts.value = response.data.posts;
      /*console.log(response)*/
    }).catch(error => {
      console.log(error)
    }).finally(() => {
      loading.value = false;
    })
  }, 800)
}

getUserPosts();
</script>

<style scoped>
.post-title {
  text-decoration: unset;
  color: #3b4351;
}
.post-title:hover {
  text-decoration: underline;
}

.post-title-text {
  font-weight: 500;
  margin: 0 0 5px;
}

html.dark .post-title {
  color: #D4D7DE;
}

.post-item-divider {
  margin: 10px 0;
  border-top-color: #f1f3f5;
}

html.dark .post-item-divider {
  border-top-color: var(--custom-trend-td-bottom-color);
}

.box-footer {
  display: flex;
  justify-content: space-between;
  color: #bcc3ce;
}

.footer-left {
  display: flex;
}

.footer-left > div:not(:last-child) {
  margin-right: 15px;
}

.replies-number {
  display: flex;
  align-items: center;
}
</style>

<style>
.user-posts {
  min-height: 35vh;
}

.user-posts .el-loading-mask {
  transition: unset;
  margin-top: 20px;
}
</style>