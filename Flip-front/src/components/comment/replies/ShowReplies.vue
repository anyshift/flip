<template>
  <div class="replies-icon" @click="showRepliesDialog = true">
    <div style="margin-right: 3px;"><i class="czs-talk"/></div>
    <div v-text="props.repliesNum" v-if="props.repliesNum > 0"></div>
  </div>

  <el-dialog v-model="showRepliesDialog" :title="'查看 ' + props.repliesNum + ' 条回复'" align-center @closed="handleDialogClosed"
             draggable class="replies-dialog" :close-on-click-modal="false" :fullscreen="fullscreen" @open="openDialogBefore"
             :style="dialogBorderRadius ? 'border-radius: var(--custom-border-radius)' : 'border-radius: 0'">
    <el-scrollbar :max-height="maxHeight" :always="replies.length > 0">
      <div class="original-comment">
        <router-link :to="{path: '/u/' + props.comment.fromUsername}" style="height: 40px;">
          <el-avatar shape="square" :size="'default'" :src="props.comment.fromAvatar"></el-avatar>
        </router-link>
        <div class="original-comment-content-box">
          <div class="original-comment-content-header">
            <ul class="original-comment-content-header-left">
              <li v-if="props.comment.fromNickname">
                <router-link :to="{path: '/u/' + props.comment.fromUsername}" v-text="props.comment.fromNickname" class="author-name"/>
              </li>
              <li v-else>
                <router-link :to="{path: '/u/' + props.comment.fromUsername}" v-text="props.comment.fromUsername" class="author-name"/>
              </li>
              <el-tag class="op-tag" v-text="'原评论'" size="small" type="danger" effect="dark" round/>
            </ul>
            <div class="original-comment-content-header-right">
              <div class="replies-icon" @click="showReplyOriginalBox = !showReplyOriginalBox">
                <div v-if="userStore.isLogin && userStore.emailVerified" :style="showReplyOriginalBox ? 'color:var(--el-menu-active-color)' : ''">
                  <i class="czs-talk" style="padding-right: 3px;"/>
                </div>
              </div>
            </div>
          </div>
          <div class="comment-content-sub-header hidden-sm-and-up">
            <el-tooltip :content="comment.createTime" placement="top" :show-after="600">
              <div v-text="moment(comment.createTime).fromNow()" class="create-time"></div>
            </el-tooltip>
          </div>
          <div class="original-comment-content-body">
            <u-fold unfold line="2" class="comment-fold">
              <span class="vditor-reset original-comment-content" id="original-comment-content"
                    v-dompurify-html="props.comment.content"/>
            </u-fold>
          </div>
          <div class="original-comment-content-footer">
            <div class="reply-now" v-if="userStore.isLogin && userStore.emailVerified && showReplyOriginalBox">
              <div class="input-box">
                <el-input v-model="replyOriginalText" size="large" maxlength="512" show-word-limit
                          :placeholder="'回复 @' + (comment.fromNickname ? comment.fromNickname : comment.fromUsername)"/>
              </div>
              <div class="input-action-box">
                <el-button color="#626aef" plain size="small" :loading="replyLoading[0]"
                           @click="doReply(0, comment.fromUid, comment.fromNickname, comment.fromUsername, replyOriginalText, comment.id, comment.id)">提交</el-button>
                <div style="margin-right: 2px;" v-text="'⌘ 不支持 Markdown ⌘'"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="loading-replies" v-loading="loading" v-if="loading"></div>

      <div class="replies-container-box">
        <el-empty v-if="props.repliesNum === 0" description="暂无回复"/>
        <div v-else class="replies">
          <div class="reply" v-for="(reply, index) of replies" :key="index + 1">
            <router-link :to="{path: '/u/' + reply.fromUsername}" style="height: 40px;">
              <el-avatar shape="square" :size="'default'" :src="reply.fromAvatar"></el-avatar>
            </router-link>
            <div class="reply-content-box">
              <div class="reply-content-header">
                <ul class="header-left">
                  <li v-if="reply.fromNickname">
                    <router-link :to="{path: '/u/' + reply.fromUsername}" v-text="reply.fromNickname" class="author-name"/>
                  </li>
                  <li v-else>
                    <router-link  :to="{path: '/u/' + reply.fromUsername}" v-text="reply.fromUsername" class="author-name"/>
                  </li>
                  <el-tag class="op-tag" v-if="props.authorId === reply.fromUid" v-text="'OP'" size="small"
                          type="warning" round effect="plain"/>
                  <el-tooltip :content="reply.createTime" placement="top" :show-after="600">
                    <li v-text="moment(reply.createTime).fromNow()" class="create-time hidden-xs-only"></li>
                  </el-tooltip>
                </ul>
                <div class="header-right">
                  <div style="cursor: pointer" class="hidden-xs-only" v-if="userStore.isLogin && userStore.emailVerified" @click="openIncompleteDialog"><i class="czs-warning"/></div>
                  <div style="cursor: pointer;margin-bottom: -2px;" v-if="userStore.isLogin && userStore.emailVerified" @click="openIncompleteDialog"><i class="czs-heart"/></div>
                  <div v-if="userStore.isLogin && userStore.emailVerified" class="replies-icon" @click="showReplyBox[index + 1] = !showReplyBox[index + 1]"
                       :style="showReplyBox[index + 1] ? 'color:var(--el-menu-active-color)' : ''">
                    <i class="czs-talk"/>
                  </div>
                  <div class="floor" v-text="'#' + (index + 1)"></div>
                </div>
              </div>
              <div class="comment-content-sub-header hidden-sm-and-up">
                <el-tooltip :content="comment.createTime" placement="top" :show-after="600">
                  <div v-text="moment(comment.createTime).fromNow()" class="create-time"></div>
                </el-tooltip>
                <div class="report-comment"><i class="czs-warning"/></div>
              </div>
              <div class="reply-content-body">
                <div class="vditor-reset reply-content" id="reply-content">
                  <span v-if="reply.parentId !== reply.replyId">
                    <span>回复</span>
                    <span v-if="reply.toNickname" v-text="'@' + reply.toNickname" class="to-user"></span>
                    <span v-else v-text="'@' + reply.toUsername" class="to-user"></span>
                    <span>：</span>
                  </span>
                  <span v-text="reply.content"></span>
                </div>
              </div>
              <div class="reply-content-footer">
                <div class="reply-now" v-if="showReplyBox[index + 1]">
                  <div class="input-box">
                    <el-input v-model="replyText[index + 1]" size="large" maxlength="512" show-word-limit
                              :placeholder="'回复 @' + (reply.fromNickname ? reply.fromNickname : reply.fromUsername)"/>
                  </div>
                  <div class="input-action-box">
                    <el-button color="#626aef" plain size="small" :loading="replyLoading[index + 1]"
                               @click="doReply(index + 1, reply.fromUid, reply.fromNickname, reply.fromUsername, replyText[index + 1], reply.parentId, reply.id)">提交</el-button>
                    <div style="margin-right: 2px;" v-text="'⌘ 不支持 Markdown ⌘'"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-if="!loading" description="没有更多了" :class="{'no-more-reply-empty': props.repliesNum === 1}"/>
        </div>
      </div>
    </el-scrollbar>
  </el-dialog>

  <NoLoginDialog :show-no-login-dialog="showNoLoginDialog" @closeNoLoginDialog="handleCloseNoLoginDialog"/>
  <DevelopingDialog :show-developing-dialog="showDevelopingDialog" @closeDevelopingDialog="handleCloseDevelopingDialog"/>
</template>

<script setup lang="ts">
import {getRepliesAPI, doReplyAPI} from "../../../api/commentAPI";
import {onMounted, reactive, ref, watch} from "vue";
import moment from "moment";
import useUserStore from "../../../stores/userStore";
import dayjs from "dayjs";
import {ElMessage} from "element-plus";
import NoLoginDialog from "../../layout/dialog/NoLoginDialog.vue";
import DevelopingDialog from "../../layout/dialog/DevelopingDialog.vue";

const props = defineProps({
  pid: String,
  cid: Number, /*评论的ID，评论的ID是回复的parentId*/
  repliesNum: Number,
  authorId: String,
  comment: Object,
  cIndex: Number, /*comment在comments数组中的index，接收这个用于emit该index的回复数*/
})
const emits = defineEmits(['updateRepliesNum'])

const userStore = useUserStore();

const replies = reactive([]);

const showRepliesDialog = ref(false);

const showReplyBox = ref([])
const replyText = ref([]);

const showReplyOriginalBox = ref(false);
const replyOriginalText = ref('');

const loading = ref(false);

function getReplies() {
  watch(() => showRepliesDialog.value, (New, Old) => {
    if (New && props.repliesNum > 0) {
      loading.value = true;
      setTimeout(() => {
        getRepliesAPI(props.pid, props.cid).then(response => {
          if (response.data.replies.length !== props.repliesNum) {
            emits('updateRepliesNum', props.cIndex, response.data.replies.length);
          }
          for (let i = 0; i < response.data.replies.length; i++) {
            replies[i] = response.data.replies[i];
          }
          loading.value = false;
          console.log(response)
        }).catch(error => {
          loading.value = false;
          console.log(error)
        })
      }, 500)
    }
  })
}

getReplies();

function handleDialogClosed() {
  replies.length = 0; /*reactive定义的数组的清空方式*/
  showReplyBox.value = [];
  showReplyOriginalBox.value = false;
  replyText.value = [];
  replyOriginalText.value = '';
}

const replyLoading = reactive([]);
function doReply(index, toUid, toNickname, toUsername, content, parentId, replyId) {
  if (content.length > 0) {
    replyLoading[index] = true;
    setTimeout(() => {
      doReplyAPI(props.pid, userStore.uid, toUid, content, parentId, replyId).then(response => {
        let newReply = reactive({
          id: response.data.reply.id,
          fromUid: userStore.uid,
          fromAvatar: userStore.avatar,
          fromNickname: userStore.nickname,
          fromUsername: userStore.username,
          content: response.data.reply.content,
          toUid: toUid,
          toNickname: toNickname,
          toUsername: toUsername,
          parentId: parentId,
          replyId: replyId,
          pid: props.pid,
          createTime: dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss'),
        })
        replies.push(newReply);
        emits('updateRepliesNum', props.cIndex, props.repliesNum + 1);
        replyLoading[index] = false;
        showReplyBox.value = [];
        showReplyOriginalBox.value = false;
        replyText.value = [];
        replyOriginalText.value = '';

        ElMessage({
          showClose: true,
          duration: 3000,
          message: '回复成功',
          type: 'success',
        })
        console.log(response)
      }).catch(error => {
        replyLoading[index] = false;
        console.log(error)
      })
    }, 1500)
  }
}

const fullscreen = ref(false);
const maxHeight = ref('');
const dialogBorderRadius = ref(false);

const windowWidth = ref()
onMounted(() => {
  windowWidth.value = window.innerWidth;
  window.onresize = () => {
    windowWidth.value = window.innerWidth;
    if (windowWidth.value > 768) {
      fullscreen.value = false;
      maxHeight.value = '550px';
      dialogBorderRadius.value = true;
    } else {
      fullscreen.value = true;
      maxHeight.value = '100%';
      dialogBorderRadius.value = false;
    }
  }
})
function openDialogBefore() {
  if (windowWidth.value > 768) {
    fullscreen.value = false;
    maxHeight.value = '550px';
    dialogBorderRadius.value = true;
  } else {
    fullscreen.value = true;
    maxHeight.value = '100%';
    dialogBorderRadius.value = false;
  }
}

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
.replies-icon {
  display: flex;
  cursor: pointer;
}

.reply {
  display: flex;
}

.header-left {
  display: flex;
  list-style: none;
  padding: 0 0 0 12px;
  margin: -2px 0 0;
}

.header-left > li:not(:last-child) {
  margin-right: 8px;
}

.reply-content-header {
  display: flex;
  justify-content: space-between;
  padding-right: 3px;
  font-size: 15px;
}

.reply-content-box {
  width: 100%;
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

.reply-content-body {
  padding: 10px 0 0 12px;
}

.reply-content {
  font-size: 15px;
  text-align: justify;
  line-height: 1.6;
  font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica Neue, Arial, Noto Sans, sans-serif, Apple Color Emoji, Segoe UI Emoji, Segoe UI Symbol, Noto Color Emoji;
}

.replies .reply {
  padding: 20px 20px;
}

.replies .reply:not(:last-child) {
  border-bottom: 1px solid #eaeaea;
}
html.dark .replies .reply:not(:last-child) {
  border-color: var(--custom-trend-td-bottom-color);
}

.reply-now {
  padding: 10px 0 0 10px;
}

.original-comment .reply-now {
  padding: 10px 0 10px 10px;
}

.input-box {
  display: flex;
}

.input-action-box {
  display: flex;
  justify-content: space-between;
  padding: 10px 0 0;
  margin-bottom: -5px;
  align-items: center;
}

.op-tag {
  margin-right: 8px;
  margin-top: 1px;
}

.original-comment {
  display: flex;
  padding: 20px 20px 15px;
  border-bottom: 1px solid #eaeaea;
}
html.dark .original-comment {
  border-color: var(--custom-trend-td-bottom-color);
}

.original-comment-content-box {
  width: 100%;
}

.original-comment-content-header {
  display: flex;
  justify-content: space-between;
}

.original-comment-content-body {
  padding: 10px 0 0 12px;
}

.original-comment-content {
  font-size: 15px;
  line-height: 1.6;
  font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica Neue, Arial, Noto Sans, sans-serif, Apple Color Emoji, Segoe UI Emoji, Segoe UI Symbol, Noto Color Emoji;
}

.original-comment-content-header-left {
  display: flex;
  list-style: none;
  padding: 0 0 0 12px;
  margin: -2px 0 0;
}

.original-comment-content-header-right {
  display: flex;
  align-items: center;
}

.original-comment-content-header-left > li:not(:last-child) {
  margin-right: 8px;
}

.to-user {
  color: var(--el-menu-active-color);
}
html.dark .to-user {
  color: #6495ec;
}

.comment-content-sub-header {
  display: flex;
  padding-left: 12px;
  justify-content: space-between;
  align-items: center;
}

@media screen and (max-width: 768px) {
  .header-right > div:not(:last-child) {
    margin-right: 10px;
  }
  .original-comment-content-body, .reply-content-body, .reply-now {
    padding: 10px 0 0;
    margin-left: -35px;
  }
  .original-comment .reply-now {
    padding: 10px 0 10px;
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
.replies-dialog .el-dialog__body {
  padding: 0 !important;
}

@media only screen and (min-width: 1650px) {
  .replies-dialog {
    --el-dialog-width: 38% !important;
  }
}

@media only screen and (min-width: 1550px) and (max-width: 1650px) {
  .replies-dialog {
    --el-dialog-width: 45% !important;
  }
}

@media only screen and (min-width: 1200px) and (max-width: 1350px) {
  .replies-dialog {
    --el-dialog-width: 55% !important;
  }
}

@media only screen and (min-width: 992px) and (max-width: 1200px) {
  .replies-dialog {
    --el-dialog-width: 62% !important;
  }
}

@media only screen and (min-width: 768px) and (max-width: 992px) {
  .replies-dialog {
    --el-dialog-width: 75% !important;
  }
}

@media only screen and (max-width: 768px) {
  .replies-dialog {
    --el-dialog-width: 100%;
    border-radius: 0 !important;
    height: 100%;
    transform: unset !important;
  }
  .replies-dialog .el-scrollbar .el-scrollbar__wrap {
    max-height: 100% !important;
  }
  html.dark .replies-dialog .el-dialog__body {
    background: var(--custom-trend-header-bg-color);
  }
  .replies-dialog .el-dialog__body {
    background: var(--el-bg-color);
  }
}

.replies-dialog .el-dialog__header {
  padding-bottom: 15px;
  margin-right: 0;
  border-bottom: 1px solid #eaeaea;
}
html.dark .replies-dialog .el-dialog__header {
  border-color: var(--custom-trend-td-bottom-color);
}

.comment-fold .txt-box {
  margin-top: 0 !important;
}

.comment-fold .action-box {
  margin-bottom: 0 !important;
}

html.dark .el-dialog.replies-dialog {
  --el-dialog-bg-color: var(--custom-trend-header-bg-color);
}

.original-comment-content-body .u-fold {
  line-height: 1.6 !important;
}
.original-comment-content-body .u-fold .action-box .expand-btn {
  padding-top: 5px;
}

.loading-replies {
  height: 460px;
}
.loading-replies .el-loading-mask {
  background: var(--el-bg-color);
}
html.dark .loading-replies .el-loading-mask{
  background: var(--custom-trend-header-bg-color);
}

.no-more-reply-empty.el-empty {
  --el-empty-padding: 75px 0;
}
</style>