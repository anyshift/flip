<template>
  <div class="comment-now" :style="props.replyNum === 0 ? 'margin: 0 1px 2px' : '10px 0 3px'">
    <el-avatar :src="userStore.avatar" shape="square" style="margin-right: 10px;"></el-avatar>
    <el-input ref="commentInputRef" size="large" :placeholder="props.replyNum === 0 ? '还没有评论，快发表第一条评论吧' : '发表评论'"
              @focus="openDrawerEditor"/>
  </div>

  <el-drawer v-model="showDrawerEditor" class="drawer-editor" title="评论主题" destroy-on-close size="fit-content"
             :lock-scroll="false" :direction="'btt'" :before-close="handleCloseDrawerEditor">
    <Editor height="25vh" ref="editorRef" cache-id="comment" @content-length-exceed="contentLengthExceed" class="comment-vditor"
            @content-length-no-exceed="contentLengthNoExceed" :counter-max="10240"/>
    <template #footer>
      <div>
        <el-button color="#626aef" plain @click="handleCloseDrawerEditor">取消评论</el-button>
        <el-button color="#626aef" plain :loading="commentLoading" @click="doComment"
                   :disabled="disabledButton">确认评论</el-button>
      </div>
    </template>
  </el-drawer>

  <NoLoginDialog :show-no-login-dialog="showNoLoginDialog" @closeNoLoginDialog="handleCloseNoLoginDialog"/>
  <NoVerifyEmailDialog :show-no-verify-email-dialog="showNoVerifyEmailDialog" @closeNoVerifyEmailDialog="handleCloseNoVerifyEmailDialog"/>
</template>

<script setup lang="ts">
import useUserStore from "../../../stores/userStore";
import {defineProps, getCurrentInstance, ref} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import Editor from '../../editor/Editor.vue';
import {doCommentAPI} from '../../../api/commentAPI'
import dayjs from "dayjs";
import NoLoginDialog from "../../layout/dialog/NoLoginDialog.vue";
import NoVerifyEmailDialog from "../../layout/dialog/NoVerifyEmailDialog.vue";

const userStore = useUserStore();

const props = defineProps({
  pid: String,
  replyNum: Number
})

const emits = defineEmits(['insertNewComment'])

const showNoLoginDialog = ref(false);
const showNoVerifyEmailDialog = ref(false);
const openDrawerEditor = (event: FocusEvent) => {
  currentInstance.ctx.$refs['commentInputRef'].blur(); //将评论输入框失焦，不然该方法体会重复执行，导致抽屉编辑器无法关闭
  if (userStore.isLogin) {
    if (!userStore.emailVerified) {
      showNoVerifyEmailDialog.value = true;
    } else {
      showDrawerEditor.value = true;
    }
  } else {
    showNoLoginDialog.value = true;
  }
}

const editorRef = ref(null);
const showDrawerEditor = ref(false) /*是否展开抽屉编辑器*/
const disabledButton = ref(false) /*抽屉编辑器内是否禁止发布已编辑的内容*/
const commentLoading = ref(false) /*抽屉编辑器内正在发布已编辑的内容时的等待状态*/
const currentInstance = getCurrentInstance();

function handleCloseDrawerEditor() {
  ElMessageBox.confirm('确定取消评论?', '关闭确认')
      .then(() => {
        showDrawerEditor.value = false;
      })
      .catch(() => {})
}
function handleCloseNoLoginDialog() {
  showNoLoginDialog.value = false;
}
function handleCloseNoVerifyEmailDialog() {
  showNoVerifyEmailDialog.value = false;
}

function contentLengthExceed() {
  disabledButton.value = true
}

function contentLengthNoExceed() {
  disabledButton.value = false
}

function doComment() {
  let {editor} = editorRef.value;
  commentLoading.value = true;
  if (editor.getValue().trim().length <= 0) {
    commentLoading.value = false;
    ElMessage({
      showClose: true,
      duration: 3500,
      message: '请输入内容',
      type: 'error',
    })
  } else if (editor.getValue().trim().length >= 10240) {
    commentLoading.value = false;
    ElMessage({
      showClose: true,
      duration: 3500,
      message: '请缩减内容长度',
      type: 'error',
    })
  } else {
    setTimeout(() => {
      if (disabledButton.value) return;
      doCommentAPI(props.pid, userStore.uid, editor.getHTML()).then(response => {
        emits('insertNewComment', response.data.comment.id, userStore.avatar, userStore.nickname,
            userStore.username, response.data.comment.content, dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss'));
        showDrawerEditor.value = false;
        console.log(response)
      }).catch(error => {
        if (error.code === 403) {
          ElMessage({
            showClose: true,
            duration: 3000,
            message: error.msg,
            type: 'error',
          })
        } else {
          ElMessage({
            showClose: true,
            duration: 3000,
            message: '评论失败，稍后重试',
            type: 'error',
          })
        }
        console.log(error)
      }).finally(() => {
        commentLoading.value = false;
      })
    }, 1500)
  }
}
</script>

<style scoped>
.comment-now {
  display: flex;
  align-items: center;
  padding: 20px 16px;
  background: white;
  border-radius: var(--custom-border-radius);
  border: 1px solid #E6E8EB;
}
html.dark .comment-now {
  border-color: transparent;
  background: var(--custom-trend-header-bg-color);
}
@media screen and (max-width: 768px) {
  .comment-now {
    padding: 13px 10px;
  }
}

.comment-now .vditor {
  width: 100% !important;
}
</style>

<style>
.post-container .el-overlay {
  overflow: visible !important; /*默认的hidden大概率会导致抽屉抖动出现*/
}

.drawer-editor .el-drawer__footer {
  text-align: center !important;
}

.drawer-editor .el-drawer__header {
  margin-bottom: -10px !important;
}

.drawer-editor .el-drawer__body {
  padding: 30px 20px 0 20px;
  margin-bottom: 10px;
}

@media screen and (max-width: 768px) {
  .el-drawer {
    height: 100% !important;
  }

  .el-drawer__body {
    padding: 20px 10px;
  }

  .comment-vditor {
    height: 100% !important;
  }
}

@media screen and (min-width: 1200px) and (max-width: 1600px) {
  .el-drawer {
    width: 76% !important;
    margin-left: 12%;
    margin-right: 12%;
    border-radius: var(--custom-border-radius) var(--custom-border-radius) 0 0;;
  }
}

@media screen and (min-width: 1600px) {
  .el-drawer {
    width: 58% !important;
    margin-left: 21%;
    margin-right: 21%;
    border-radius: var(--custom-border-radius) var(--custom-border-radius) 0 0;;
  }
}

.comment-now .el-input__wrapper {
  box-shadow: 0 0 0 1px #eaeaea inset;
  transition: unset;
}
.comment-now .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #d0d7de inset;
}

html.dark .comment-now .el-input__wrapper {
  box-shadow: 0 0 0 1px #333942 inset;
}
html.dark .comment-now .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px var(--el-input-hover-border-color) inset;
}
</style>