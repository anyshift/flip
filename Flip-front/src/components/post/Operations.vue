<template>
  <div class="post-operations">
    <div class="operations">
      <el-button-group class="up-or-down">
        <el-button type="" plain @click="openIncompleteDialog">
          <el-icon>
            <ArrowUpBold/>
          </el-icon>
        </el-button>
        <el-button type="" plain @click="openIncompleteDialog">
          <el-icon>
            <ArrowDownBold/>
          </el-icon>
        </el-button>
      </el-button-group>

      <el-tooltip effect="dark" content="感谢" placement="top-start" :show-after="600" trigger="hover">
        <el-button type="" size="default" circle @click="openIncompleteDialog">
          <i class="czs-heart-l"></i>
        </el-button>
      </el-tooltip>
      <el-tooltip effect="dark" content="关注" placement="top-start" :show-after="600" trigger="hover">
        <el-button type="" size="default" circle @click="openIncompleteDialog">
          <i class="czs-star-l"></i>
        </el-button>
      </el-tooltip>
      <el-tooltip effect="dark" content="收藏" placement="top-start" :show-after="600" trigger="hover">
        <el-button type="" size="default" circle @click="openIncompleteDialog">
          <i class="czs-bookmark-l"></i>
        </el-button>
      </el-tooltip>
    </div>

    <div class="more">
      <el-dropdown placement="bottom-end" trigger="click" @command="handleCommand">
        <el-button type="" size="default" circle>
          <i class="czs-more"></i>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="changeContent" v-if="hasRole(['ADMIN', 'MODERATOR']) || hasAuthority(['activity:post:edit']) || props.post.authorUid === userStore.uid"
                              @click="showDrawerEditor = true">
              修改内容
            </el-dropdown-item>
            <el-dropdown-item command="changeTitle" v-if="hasRole(['ADMIN', 'MODERATOR']) || hasAuthority(['activity:title:edit']) || props.post.authorUid === userStore.uid">
              修改标题
            </el-dropdown-item>
            <el-dropdown-item command="changeTag" v-if="hasRole(['ADMIN', 'MODERATOR'])">
              修改标签
            </el-dropdown-item>
            <el-dropdown-item command="copyURL">
              复制链接
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>

  <!-- 抽屉型编辑器 -->
  <el-drawer v-model="showDrawerEditor" title="编辑主题内容" class="drawer-editor" destroy-on-close size="fit-content"
             :close-on-click-modal="false" :direction="'btt'" :before-close="handleCloseDrawerEditor">
    <Editor ref="drawerEditor" cache-id="editPost" :editing-content="post.content" class="drawer-vditor"
            @content-length-exceed="contentLengthExceed" @content-length-no-exceed="contentLengthNoExceed"/>
    <template #footer>
      <div>
        <el-button color="#626aef" plain @click="handleCloseDrawerEditor">取消修改</el-button>
        <el-button color="#626aef" plain @click="doPostEditedContent" :loading="postEditedLoading"
                   :disabled="disabledPostEditedContent">确认修改</el-button>
      </div>
    </template>
  </el-drawer>

  <!-- 修改标签对话框 -->
  <el-dialog v-model="showTagChangeDialog" title="修改标签" class="tag-cascade-dialog" @open="handleOpenTagChangeDialog">
    <template #default>
      <div class="post-tags">
        <span>当前标签：</span>
        <el-tag v-for="(tag, index) of props.post.tags" v-text="tag.name" size="small" class="post-tag" :type="randomTagType(index)"></el-tag>
      </div>
      <div class="post-tags">
        <span>已选标签：</span>
        <el-tag v-for="(tag, index) of selectedTagsArr" v-text="tag.name" size="small" class="post-tag" :type="randomTagType(index)"></el-tag>
      </div>
      <el-cascader-panel ref="dialogTagCascadeRef" v-model="selectedTags" :show-all-levels="false" placeholder="选择标签" size="large"
                         :props="tagCascadeProp" :options="cascadeOptions" collapse-tags collapse-tags-tooltip popper-class="tag-cascade-panel"
                         tag-type="success" @change="handleTagSelect">
        <template #default="{ node, data }">
          <span v-text="data.label"/>
          <span v-if="!node.isLeaf" v-text="' (' + data.children.length + ')'"/>
        </template>
      </el-cascader-panel>
    </template>
    <template #footer>
      <span class="tag-dialog-footer">
        <el-button @click="showTagChangeDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateTagOfPost()" :loading="updateTagOfPostLoading">确认</el-button>
      </span>
    </template>
  </el-dialog>

  <NoLoginDialog :show-no-login-dialog="showNoLoginDialog" @closeNoLoginDialog="handleCloseNoLoginDialog"/>
  <DevelopingDialog :show-developing-dialog="showDevelopingDialog" @closeDevelopingDialog="handleCloseDevelopingDialog"/>
  <NoVerifyEmailDialog :show-no-verify-email-dialog="showNoVerifyEmailDialog" @closeNoVerifyEmailDialog="handleCloseNoVerifyEmailDialog"/>
</template>

<script setup lang="ts">
import {defineProps, getCurrentInstance, nextTick, reactive, ref} from "vue";
import useUserStore from '../../stores/userStore'
import {ElMessage, ElMessageBox} from "element-plus";
import {doPostEditedContentAPI, doPostEditedTitleAPI, doChangeTagOfPostAPI} from '../../api/postAPI';
import useClipboard from "vue-clipboard3";
import Editor from '../editor/Editor.vue';
import {hasRole, hasAuthority} from '../../utils/permission';
import {getTagsAndOptionsAPI} from '../../api/admin/tagAPI';
import {cascadeTags, classifyTagOptions} from '../../utils/tags'
import NoLoginDialog from "../layout/dialog/NoLoginDialog.vue";
import DevelopingDialog from "../layout/dialog/DevelopingDialog.vue";
import NoVerifyEmailDialog from "../layout/dialog/NoVerifyEmailDialog.vue";
import {isEmailVerified} from '../../utils/permission';

const userStore = useUserStore();

const emits = defineEmits(['changeContent', 'changeTitle', 'changeTags'])
const props = defineProps({
  post: Object,
})

function handleCommand(command: string | number | object) {
  if (command === 'changeContent') {
    showDrawerEditor.value = true;
  } else if (command === 'changeTitle') {
    openChangeTitleMessageBox(); // 打开修改标题消息弹框
  } else if (command === 'copyURL') {
    copyURL(window.location.href); //复制URL到剪贴版
  } else if (command === 'changeTag') {
    showTagChangeDialog.value = true;
  }
}

const drawerEditor = ref(null);
const showDrawerEditor = ref(false) /*是否展开抽屉编辑器*/
const disabledPostEditedContent = ref(false) /*抽屉编辑器内是否禁止发布已编辑的内容*/
const postEditedLoading = ref(false) /*抽屉编辑器内正在发布已编辑的内容时的等待状态*/

function contentLengthExceed() {
  disabledPostEditedContent.value = true
}

function contentLengthNoExceed() {
  disabledPostEditedContent.value = false
}

/*执行修改帖子内容的操作*/
const doPostEditedContent = () => {
  let {editor} = drawerEditor.value;
  postEditedLoading.value = true;
  if (editor.getValue().trim().length <= 0) {
    postEditedLoading.value = false;
    ElMessage({
      duration: 3500,
      message: '请输入帖子内容',
      type: 'error',
    })
  } else if (editor.getValue().trim().length >= 65536) {
    postEditedLoading.value = false;
    ElMessage({
      duration: 3500,
      message: '请缩减内容长度',
      type: 'error',
    })
  } else {
    if (props.post.content === editor.getValue()) {
      postEditedLoading.value = false;
      showDrawerEditor.value = false;
      ElMessage({
        duration: 3500,
        message: '内容无变化',
        type: 'warning',
      })
      return;
    }
    setTimeout(() => {
      if (disabledPostEditedContent.value) return;
      doPostEditedContentAPI(props.post.id, props.post.title, editor.getHTML()).then(response => {
        postEditedLoading.value = false;
        emits("changeContent", response.data.content)
        showDrawerEditor.value = false;
        ElMessage({
          showClose: true,
          duration: 3500,
          message: '修改内容成功',
          type: 'success',
        })
        console.log(response)
      }).catch(error => {
        postEditedLoading.value = false;
        disabledPostEditedContent.value = true;
        ElMessage({
          showClose: true,
          duration: 3500,
          message: error.msg,
          type: 'error',
        })
        console.log(error)
      })
    }, 1500)
  }
}

function handleCloseDrawerEditor() {
  ElMessageBox.confirm('确定退出编辑?', '关闭确认')
      .then(() => {
        showDrawerEditor.value = false;
      })
      .catch(() => {
        // catch error
      })
}

/*打开修改标题对话框*/
const openChangeTitleMessageBox = () => {
  ElMessageBox.prompt('请输入新的文章标题', '修改标题', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    draggable: true,
    lockScroll: false,
    closeOnClickModal: false,
    beforeClose: (action, instance, done) => {
      if (action === 'confirm') {
        if (instance.inputValue === null) {
          ElMessage({
            message: '标题无变化',
            type: 'warning',
          });
          done();
        }
        if (instance.inputValue !== props.post.title) {
          instance.confirmButtonLoading = true;
          instance.confirmButtonText = '请等待...';
          setTimeout(() => {
            done()
            setTimeout(() => {
              instance.confirmButtonLoading = false
            }, 300)
          }, 1500)
        } else done();
      } else done();
    },
  })
      .then(({value}) => {
        if (value !== null) {
          if (value === props.post.title) {
            ElMessage({
              duration: 3500,
              message: '标题无变化',
              type: 'warning',
            })
            return;
          }
          if (value.length > 100) {
            ElMessage({
              message: '请缩减标题长度',
              type: 'warning',
            })
            return;
          }
          doPostEditedTitleAPI(props.post.id, value).then(response => {
            emits('changeTitle', response.data.title)
            ElMessage({
              message: '修改标题成功',
              type: 'success',
            })
            console.log(response)
          }).catch(error => {
            console.log(error);
            ElMessage({
              message: '修改标题失败',
              type: 'error',
            })
          })
        }
      }).catch(() => {})
}

/*复制帖子链接*/
const copyURL = async (url) => {
  const {toClipboard} = useClipboard();
  try {
    await toClipboard(url);  //实现复制
    ElMessage({
      showClose: false,
      message: '已复制链接',
      type: 'success',
    })
  } catch (error) {
    console.log(error)
  }
}

/*修改标签*/
const tags = ref([]);
const tagOptions = ref([]);
const cascadeOptions = ref([]);
const showTagChangeDialog = ref(false);
const selectedTags = ref([]);
const selectedTagsArr = ref([]);
const currentInstance = getCurrentInstance();
const updateTagOfPostLoading = ref(false);
const tagCascadeProp = reactive({
  multiple: true,
  emitPath: false,
})

function handleOpenTagChangeDialog() {
  getTagsAndOptionsAPI().then(response => {
    tagOptions.value = classifyTagOptions(response.data.tagOptions);
    tags.value = response.data.tags;
    cascadeOptions.value = cascadeTags(tags.value, tagOptions.value);
  }).catch(error => {
    console.log(error)
  })
}

function handleTagSelect(value) {
  if (value.length <= 3) {
    selectedTagsArr.value.length = 0;
    let checkedNodes = currentInstance.ctx.$refs['dialogTagCascadeRef'].getCheckedNodes(true);
    checkedNodes.forEach(checked => {
      selectedTagsArr.value.push({
        value: checked.value,
        name: checked.label,
      })
    })
  } else {
    selectedTags.value = [];
    selectedTagsArr.value = [];
    ElMessage({
      message: '不能超过三个标签',
      type: 'warning',
    })
  }
}

function handleUpdateTagOfPost() {
  let selected = [];
  selectedTags.value.forEach(tagLabel => {
    selected.push({label: tagLabel})
  })
  updateTagOfPostLoading.value = true;
  setTimeout(() => {
    doChangeTagOfPostAPI(props.post.id, selected).then(response => {
      ElMessage({
        message: '修改标签成功',
        type: 'success',
      })
      emits("changeTags", response.data.newTags);
      console.log(response)
    }).catch(error => {
      ElMessage({
        message: "修改标签失败",
        type: 'error',
      })
      console.log(error)
    }).finally(() => {
      updateTagOfPostLoading.value = false;
      showTagChangeDialog.value = false;
    })
  }, 500)
}

function randomTagType(index) {
  switch (index) {
    case 0: return 'warning';
    case 1: return 'success';
    case 2: return '';
    case 3: return 'info';
    default: return 'danger';
  }
}

const showNoLoginDialog = ref(false);
function handleCloseNoLoginDialog() {
  showNoLoginDialog.value = false;
}

const showDevelopingDialog = ref(false);
function handleCloseDevelopingDialog() {
  showDevelopingDialog.value = false;
}

function openIncompleteDialog() {
  if (userStore.isLogin) {
    if (!userStore.emailVerified) {
      showNoVerifyEmailDialog.value = true;
    } else {
      showDevelopingDialog.value = true;
    }
  } else showNoLoginDialog.value = true;
}

const showNoVerifyEmailDialog = ref(false);
function handleCloseNoVerifyEmailDialog() {
  showNoVerifyEmailDialog.value = false;
}
</script>

<style scoped>
.post-operations {
  display: flex;
  justify-content: space-between;
}

.operations > div:first-child {
  margin-right: 10px;
}

.up-or-down > button {
  padding: 8px;
}

.post-tags > .post-tag:not(:last-child) {
  margin-right: 5px;
}

.post-tags {
  margin-bottom: 20px;
}
</style>

<style>
.post-container + .el-overlay {
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

  .drawer-vditor {
    height: 100% !important;
  }
}

@media screen and (min-width: 1300px) {
  .el-drawer {
    width: 54% !important;
    margin-left: 23%;
    margin-right: 23%;
    border-radius: var(--custom-border-radius) var(--custom-border-radius) 0 0;;
  }
}

.up-or-down .el-button--primary:first-child {
  border-right-color: var(--el-color-primary-light-5) !important;
}
.up-or-down .el-button--primary:last-child {
  border-left-color: var(--el-color-primary-light-5) !important;
}

.operations .el-button+.el-button:not(.up-or-down .el-button+.el-button) {
  margin-left: 5px;
}

.tag-cascade-dialog .el-cascader-menu {
  min-width: 150px !important;
}

.tag-cascade-dialog .el-cascader-menu__wrap.el-scrollbar__wrap {
  height: 100%;
}

.tag-cascade-dialog .el-dialog__footer {
  padding: 10px 20px 20px;
}

.tag-cascade-dialog .el-dialog__body {
  padding: 20px 20px 10px !important;
}

@media only screen and (min-width: 1650px) {
  .tag-cascade-dialog {
    --el-dialog-width: 35% !important;
  }
}

@media only screen and (min-width: 1550px) and (max-width: 1650px) {
  .tag-cascade-dialog {
    --el-dialog-width: 45% !important;
  }
}

@media only screen and (min-width: 1200px) and (max-width: 1350px) {
  .tag-cascade-dialog {
    --el-dialog-width: 55% !important;
  }
}

@media only screen and (min-width: 992px) and (max-width: 1200px) {
  .tag-cascade-dialog {
    --el-dialog-width: 62% !important;
  }
}

@media only screen and (min-width: 768px) and (max-width: 992px) {
  .tag-cascade-dialog {
    --el-dialog-width: 75% !important;
  }
}

@media only screen and (max-width: 768px) {
  .tag-cascade-dialog {
    --el-dialog-width: 100%;
    border-radius: 0 !important;
  }
}
</style>