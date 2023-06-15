<template>
  <div>
    <div class="hidden-xs-only">
      <el-input v-model="title" placeholder="请输入标题" class="title-tag" clearable size="large">
        <template #prepend>
          <el-cascader ref="dialogTagCascadeRef" v-model="selectedTags" :options="cascadeOptions" :show-all-levels="false" placeholder="请选择标签" size="large"
                       :props="tagCascadeProp" collapse-tags collapse-tags-tooltip popper-class="tag-cascade"
                       tag-type="success" @change="handleTagSelect">
            <template #default="{ node, data }">
              <span v-text="data.label"/>
              <span v-if="!node.isLeaf" v-text="' (' + data.children.length + ')'"/>
            </template>
          </el-cascader>
        </template>
      </el-input>
    </div>

    <div class="hidden-sm-and-up">
      <el-cascader ref="dialogTagCascadeRef" v-model="selectedTags" :options="cascadeOptions" :show-all-levels="false" placeholder="请选择标签" size="large"
                   :props="tagCascadeProp" popper-class="tag-cascade" class="mobile-tag-cascader" tag-type="success" @change="handleTagSelect">
        <template #default="{ node, data }">
          <span v-text="data.label"/>
          <span v-if="!node.isLeaf" v-text="' (' + data.children.length + ')'"/>
        </template>
      </el-cascader>
      <el-input v-model="title" placeholder="请输入标题" clearable size="large" class="title-tag"/>
    </div>

    <Editor ref="writeEditor" height="80vh" @limit-exceed="limitExceed" @no-limit-exceed="noLimitExceed" :cache-id="'newPost'"
            :clear-content="clearEditorContent" @clear-content-done="clearEditorContentDone"/>

    <div class="actionButton">
      <el-button color="#626aef" plain>设置</el-button>
      <el-button color="#626aef" plain @click="postAction" :disabled="disabledPost" :loading="postLoading">发布</el-button>
      <el-link type="primary" style="float: right; margin-top: 5px;">⌘ 支持 Markdown 语法 ⌘</el-link>
    </div>

    <el-dialog v-model="showPostResultDialog" :show-close="showPostResultClose" :title="postResultDialogTitle"
               width="395px" center destroy-on-close>
      <span v-text="postResultDialogText"></span>
    </el-dialog>
  </div>
</template>

<script setup>
import Editor from "../../../components/editor/Editor.vue";
import {getCurrentInstance, nextTick, onBeforeMount, reactive, ref} from "vue";
import {doPost} from "../../../api/postApi";
import {ElMessage, ElNotification} from "element-plus";
import {getTagsAndOptionsAPI} from "../../../api/admin/tagAPI";
import {cascadeTags, classifyTagOptions} from "../../../utils/tags";
import {useRouter} from "vue-router";

const router = useRouter();

const writeEditor = ref(null);

const title = ref('')

const tagOptions = ref([]);
const tags = ref([]);
const cascadeOptions = ref([]);
const tagCascadeProp = reactive({
  multiple: true,
  emitPath: false,
})

onBeforeMount(() => {
  getTagsAndOptionsAPI().then(response => {
    tagOptions.value = classifyTagOptions(response.data.tagOptions);
    tags.value = response.data.tags;
    cascadeOptions.value = cascadeTags(tags.value, tagOptions.value);
  }).catch(error => {
    console.log(error)
  })
})

const disabledPost = ref(false)
const limitExceed = () => {
  disabledPost.value = true;
}
const noLimitExceed = () => {
  disabledPost.value = false;
}

const postLoading = ref(false);
const showPostResultDialog = ref(false);
const showPostResultClose = ref(false);
const postResultDialogTitle = ref('');
const postResultDialogText = ref('');
const clearEditorContent = ref(false);
const postAction = () => {
  const { editor } = writeEditor.value;
  if (!title.value || title.value.length <= 0) {
    ElMessage({
      message: '请输入标题',
      type: 'warning',
    })
  } else if (!editor.getValue() || editor.getValue().trim().length <= 0) {
    ElMessage({
      message: '请输入主题内容',
      type: 'warning',
    })
  } else if (selectedTags.value === undefined || selectedTags.value.length === 0) {
    ElMessage({
      message: '请选择主题标签',
      type: 'warning',
    })
  } else {
    let selected = [];
    selectedTags.value.forEach(tagLabel => {
      selected.push({label: tagLabel})
    })
    postLoading.value = true;
    setTimeout(() => {
      doPost(title.value, editor.getHTML(), selected).then(response => {
        showPostResultDialog.value = true;
        showPostResultClose.value = false;
        postResultDialogTitle.value = '发布成功';

        let time = 3;
        postResultDialogText.value = '3 秒后将自动跳转到新主题页...';
        let timer = setInterval(() => {
          time--;
          postResultDialogText.value = time + ' 秒后将自动跳转到新主题页...';
          if (time === 0) {
            clearInterval(timer);
            router.push({path: '/p/' + response.data.postId});
            showPostResultDialog.value = false;
            title.value = null;
            clearEditorContent.value = true;
            selectedTags.value = undefined;
          }
        }, 1000)
        console.log(response)
      }).catch(error => {
        showPostResultDialog.value = true;
        showPostResultClose.value = true;
        postResultDialogTitle.value = '发布失败';
        postResultDialogText.value = '请重试，如仍不能发布，请及时反馈管理员。';
        console.log(error)
      }).finally(() => {
        postLoading.value = false;
      })
    }, 500)
  }
}

const selectedTags = ref();
const oldSelectedTags = ref();
function handleTagSelect(value) {
  if (value.length <= 3) {
    oldSelectedTags.value = value;
  } else {
    ElMessage({
      message: '不能超过三个标签',
      type: 'warning',
    })
    nextTick(() => {
      selectedTags.value = oldSelectedTags.value;
    })
  }
}

function clearEditorContentDone() {
  clearEditorContent.value = false;
}
</script>

<style scoped>
.title-tag {
  background-color: var(--el-fill-color-blank);
  margin-bottom: 10px;
}
.actionButton {
  margin-top: 10px;
}
</style>

<style>
.title-tag .el-input-group__prepend {
  padding: 0;
}

.title-tag .el-input__wrapper, .mobile-tag-cascader .el-input__wrapper {
  transition: unset;
}

html.dark .el-input__wrapper {
  background: #24292f;
}

html.dark .title-tag .el-input--suffix .el-input__wrapper {
  background: #1d2125;
}

.tag-cascade .el-cascader-menu__wrap.el-scrollbar__wrap {
  height: 100%;
}

.mobile-tag-cascader.el-cascader {
  width: 100%;
  margin-bottom: 10px;
}
</style>