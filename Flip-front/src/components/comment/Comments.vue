<template>
  <div v-if="replyNum === 0" class="comment-container-box">
    <el-empty description="还没有人评论"/>
  </div>

  <div v-else class="comment-container-box" id="comments">
      <div class="comments">
        <el-skeleton :loading="loading" animated style="padding: 0 0 12px 0;">
          <template #template></template>
          <template #default>
            <div class="comment" v-for="(comment, index) of comments" :key="index">
              <router-link :to="{path: '/u/' + comment.fromUsername}" style="height: 40px;">
                <el-avatar shape="square" :size="'default'" :src="comment.fromAvatar"></el-avatar>
              </router-link>
              <div class="comment-content-box">
                <div class="comment-content-header">
                  <ul class="header-left">
                    <li v-if="comment.fromNickname">
                      <router-link :to="{path: '/u/' + comment.fromUsername}" v-text="comment.fromNickname" class="author-name"/>
                    </li>
                    <li v-else>
                      <router-link :to="{path: '/u/' + comment.fromUsername}" v-text="comment.fromUsername" class="author-name"/>
                    </li>
                    <el-tag class="op-tag" v-if="props.authorId === comment.fromUid" v-text="'OP'" size="small" type="warning" round effect="plain"/>
                    <el-tooltip :content="comment.createTime" placement="top" :show-after="600">
                      <li v-text="moment(comment.createTime).fromNow()" class="create-time hidden-xs-only"></li>
                    </el-tooltip>
                  </ul>
                  <div class="header-right">
                    <div v-if="userStore.isLogin && userStore.emailVerified" style="cursor: pointer;" class="hidden-xs-only" @click="openIncompleteDialog"><i class="czs-warning"/></div>
                    <div v-if="userStore.isLogin && userStore.emailVerified" style="cursor: pointer;" @click="openIncompleteDialog"><i class="czs-heart"/></div>
                    <ShowReplies :replies-num="comment.repliesNum" :pid="props.pid" :cid="comment.id" :c-index="index"
                                 :author-id="props.authorId" :comment="comment" @update-replies-num="updateRepliesNum"/>
                    <div class="floor" v-text="'#' + (index + 1)"></div>
                  </div>
                </div>
                <div class="comment-content-sub-header hidden-sm-and-up">
                  <el-tooltip :content="comment.createTime" placement="top" :show-after="600">
                    <div v-text="moment(comment.createTime).fromNow()" class="create-time" style="margin-top: -1px;"></div>
                  </el-tooltip>
                  <div class="report-comment"><i class="czs-warning"/></div>
                </div>
                <div class="comment-content-body">
                  <u-fold unfold line="5" class="comment-fold">
                    <span class="vditor-reset comment-content" id="comment-content" v-dompurify-html="comment.content"/>
                  </u-fold>
                </div>
                <div class="comment-content-footer">

                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </div>
  </div>

  <div class="divider">
    <el-divider class="d-1" direction="vertical"/>
    <el-divider class="d-2" direction="vertical"/>
  </div>

  <MakeComment :reply-num="props.replyNum" :pid="props.pid" @insert-new-comment="insertNewComment"/>

  <NoLoginDialog :show-no-login-dialog="showNoLoginDialog" @closeNoLoginDialog="handleCloseNoLoginDialog"/>
  <DevelopingDialog :show-developing-dialog="showDevelopingDialog" @closeDevelopingDialog="handleCloseDevelopingDialog"/>
</template>

<script setup lang="ts">
import {reactive, ref} from "vue";
import useUserStore from "../../stores/userStore";
import {getCommentsAPI} from "../../api/commentAPI";
import MakeComment from '../comment/make/MakeComment.vue';
import 'element-plus/theme-chalk/display.css';
import VditorPreview from 'vditor/dist/method.min';
import moment from "moment";
import ShowReplies from "./replies/ShowReplies.vue";
import {ElMessage} from "element-plus";
import NoLoginDialog from "../layout/dialog/NoLoginDialog.vue";
import DevelopingDialog from "../layout/dialog/DevelopingDialog.vue";

const userStore = useUserStore();

const props = defineProps({
  pid: String,
  replyNum: Number,
  authorId: String,
})
const emits = defineEmits(['updateCommentNum'])

const comments = reactive([])
const loading = ref(false);

function getComments() {
  if (props.replyNum > 0) {
    loading.value = true;
    setTimeout(() => {
      getCommentsAPI(props.pid).then(response => {
        for (let i = 0; i < response.data.comments.length; i++) {
          comments[i] = response.data.comments[i];
        }
        loading.value = false;
        console.log(response.data)
      }).catch(error => {
        loading.value = false;
        console.log(error)
      })
    }, 0)
  }
}
getComments()

/**
 * 评论成功后，不通过后端拉取，直接把新评论 push 到 comments 数组中。
 * @param id 插入到数据库中生成的自增ID
 * @param avatar 评论者头像，即当前登录用户的头像
 * @param nickname 评论者昵称，即当前登录用户的昵称
 * @param username 评论者用户名，即当前登录用户的用户名
 * @param contentHtml md2html内容
 * @param createTime 评论时间
 */
const insertNewComment = (id, avatar, nickname, username, contentHtml, createTime) => {
  let comment = reactive({
    id: id,
    repliesNum: 0,
    fromUid: userStore.uid,
    fromAvatar: avatar,
    fromNickname: nickname,
    fromUsername: username,
    content: contentHtml,
    createTime: createTime,
  });
  comments.push(comment);
  emits('updateCommentNum')
  ElMessage({
    showClose: true,
    duration: 3000,
    message: '评论成功',
    type: 'success',
  })
}

/**
 * 楼中楼回复成功后同步更新评论列表里的回复数
 * @param target 评论列表里的下标
 * @param newRepliesNum 新的回复数
 */
function updateRepliesNum(target, newRepliesNum) {
  comments[target].repliesNum = newRepliesNum;
}

// 渲染内容
const renderMarkdown = (id, content) => {
  VditorPreview.preview(<HTMLDivElement>document.getElementById(id), content);
}

/**
 * 把评论和回复夹杂着的数据从集合类型转为树形（回复成为评论的内部属性）
 * @param list 评论(回复)数据
 * @returns {*[]} 树形结构的评论(回复)
 */
const listToTree = (list) => {
  const newList = JSON.parse(JSON.stringify(list));
  const map = new Map();
  const result = [];

  newList.forEach((comment) => {
    map.set(comment.id, comment);

    if (comment.parentId !== 0) {
      const parentComment = map.get(comment.parentId);

      if (!parentComment.children) {
        parentComment.children = [];
      }
      parentComment.children.push(comment); /*回复数据push到评论的children数组中*/
    } else {
      result.push(comment);
    }
  });
  return result;
};

const showNoLoginDialog = ref();
function handleCloseNoLoginDialog() {
  showNoLoginDialog.value = false;
}

const showDevelopingDialog = ref();
function handleCloseDevelopingDialog() {
  showDevelopingDialog.value = false;
}

function openIncompleteDialog() {
  if (userStore.isLogin) {
    showDevelopingDialog.value = true;
  } else showNoLoginDialog.value = true;
}
</script>

<style scoped>
.comment-container-box {
  background: white;
  padding: 20px 16px 0;
  border-radius: var(--custom-border-radius);
  border: 1px solid #E6E8EB;
}
html.dark .comment-container-box {
  border-color: transparent;
  background: var(--custom-trend-header-bg-color);
}
@media screen and (max-width: 768px) {
  .comment-container-box {
    padding: 15px 10px 0;
  }
  .comments .comment[data-v-cfc11ff5] {
    padding-bottom: 12px;
  }
}

.comment {
  display: flex;
}

.comment-content-box {
  width: 100%;
}

.comment-content-header {
  display: flex;
  justify-content: space-between;
  padding-right: 3px;
  font-size: 15px;
}

.header-left {
  display: flex;
  list-style: none;
  padding: 0 0 0 12px;
  margin: -2px 0 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.header-right > div:not(:last-child) {
  margin-right: 18px;
}

.header-left > li:not(:last-child) {
  margin-right: 8px;
}

.create-time, .header-right {
  color: #bcc3ce;
}
html.dark .create-time, html.dark .header-right{
  color: #8f959f;
}

.comment-content-body {
  padding: 10px 0 0 12px;
}

.comment-content {
  text-align: justify;
  font-size: 15px;
  line-height: 1.6;
  font-family: ui-sans-serif,system-ui,-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Helvetica Neue,Arial,Noto Sans,sans-serif,Apple Color Emoji,Segoe UI Emoji,Segoe UI Symbol,Noto Color Emoji;
}

.comments .comment:not(:first-child) {
  padding-top: 20px;
}

.comments .comment {
  padding-bottom: 20px;
}

.comments .comment:not(:last-child) {
  border-bottom: 1px solid #eaeaea;
}
html.dark .comments .comment:not(:last-child) {
  border-color: var(--custom-trend-td-bottom-color);
}

.floor {
  display: inline-block;
  padding: 0 6px 2px;
  border-radius: 3px;
  font-size: 12px;
  background: var(--custom-reply-num-bg-color);
  color: var(--custom-reply-num-color);
  height: 15px;
  margin-top: -1px;
}

.divider {
  display: flex;
  justify-content: space-between;
}

.d-1, .d-2 {
  height: 24px;
  border-left: 3px;
  border-color: #C0C4CC;
}
.d-1 {
  border-style: inset;
  margin: 0 0 0 47px;
}
.d-2 {
  border-style: outset;
  margin: 0 47px 0 0;
}
html.dark .d-1, html.dark .d-2 {
  border-color: #606266;
}
@media screen and (max-width: 768px) {
  .d-1, .d-2 {
    height: 10px;
  }
}

.op-tag {
  margin-right: 8px;
  margin-top: 2px;
}

.comment-content-sub-header {
  display: flex;
  justify-content: space-between;
  padding-left: 12px;
  align-items: center;
}

.comment-actions > div:not(:last-child) {
  margin-right: 8px;
}

@media screen and (max-width: 768px) {
  .header-right > div:not(:last-child) {
    margin-right: 10px;
  }
  .comment-content-body {
    padding: 10px 0 0;
    margin-left: -35px;
  }
  .report-comment {
    cursor: pointer;
    display: inline-block;
    padding: 0 8px;
    border-radius: 3px;
    font-size: 11px;
    background: var(--custom-reply-num-bg-color);
    color: var(--custom-reply-num-color);
    height: 15px;
    margin-right: 3px;
    opacity: 0;
  }
}

.author-name {
  color: #2f4f4f;
  text-decoration: unset;
}
html.dark .author-name {
  color: #f5f5f5;
}
</style>

<style>


.comment-content-body .u-fold {
  line-height: 1.6 !important;
}

.comment-content-body .u-fold .action-box .expand-btn {
  padding-top: 5px;
}
</style>