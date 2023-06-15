<template>
  <div class="header-detail">
    <div>更换或重置个人头像</div>
  </div>
  <el-form label-position="left" label-width="80px" class="update-avatar-form">
    <el-form-item label="温馨提示">
      <ul class="ul-info">
        <li>一个月只能修改一次头像</li>
        <li>为保证浏览体验，请使用分辨率为 200 * 200 及以上的图片作为头像</li>
      </ul>
    </el-form-item>
    <el-form-item label="当前头像">
      <div class="current-avatar hidden-sm-and-up">
        <el-avatar :src="userStore.avatar" :size="80" shape="square"/>
        <el-avatar :src="userStore.avatar" :size="55" shape="square"/>
        <el-avatar :src="userStore.avatar" :size="36" shape="square"/>
        <el-avatar :src="userStore.avatar" :size="22" shape="square"/>
      </div>
      <div class="current-avatar hidden-xs-only">
        <el-avatar :src="userStore.avatar" :size="190" shape="square"/>
        <el-avatar :src="userStore.avatar" :size="110" shape="square"/>
        <el-avatar :src="userStore.avatar" :size="60" shape="square"/>
        <el-avatar :src="userStore.avatar" :size="36" shape="square"/>
        <el-avatar :src="userStore.avatar" :size="22" shape="square"/>
      </div>
    </el-form-item>
    <el-form-item label="更换头像">
      <el-upload
          ref="uploader"
          class="uploader"
          :action="uploaderAction"
          :auto-upload="false"
          :show-file-list="false"
          accept="image/jpeg, image/png"
          :before-upload="beforeUploadAvatar"
          :on-change="onUploaderChange"
          :http-request="httpRequest"
      >
        <el-button class="upload-button">点击上传</el-button>
      </el-upload>
      <el-button style="margin-left: 12px" class="random-button">随机头像</el-button>
      <el-button class="history-button">历史头像</el-button>
    </el-form-item>
    <el-form-item label="限制条件">
      <ul class="ul-info">
        <li>图片格式：JPG / PNG</li>
        <li>图片大小：2 MB 内</li>
      </ul>
    </el-form-item>
  </el-form>

  <!-- 图片裁剪对话框 -->
  <AvatarCropper
      v-if="showCropperDialog"
      :dialog-title="'上传头像'"
      :dialog-visible="showCropperDialog"
      :cropper-image="cropperAvatar"
      img-type="blob"
      @upload-cropped-avatar="uploadCroppedAvatar"
      @close-avatar-cropper-dialog="closeAvatarCropperDialog"
  />
</template>

<script setup lang="ts">
import {ref} from 'vue';
import useUserStore from "../../../stores/userStore";
import AvatarCropper from '../../../components/cropper/AvatarCropper.vue';
import {ElMessage, UploadInstance} from "element-plus";
import {uploadAvatarAPI} from '../../../api/uploadAPI';

const userStore = useUserStore();

const uploader = ref<UploadInstance>();
const uploaderAction = ref('/updateAvatar');
const showCropperDialog = ref(false);
const cropperAvatar = ref();
const croppedAvatar = ref();

function uploadCroppedAvatar(data) {
  croppedAvatar.value = data;
  uploader.value!.submit();
  uploader.value!.clearFiles(); /*手动上传情形中，submit()完成后，要 clearFiles()，不然会多次请求(与上次的文件一起提交)。*/
}

function closeAvatarCropperDialog() {
  showCropperDialog.value = false;
}

/*在手动或自动上传图片前，会触发此钩子函数*/
function beforeUploadAvatar(rawAvatar) {
  if (rawAvatar.size / 1024 / 1024 > 2) {
    ElMessage.error('只能上传2MB以内的图片')
    return false;
  }
  return true;
}

function onUploaderChange(file, fileList) {
  let {raw} = file;
  openCropperDialog(raw)
}

function openCropperDialog(rawAvatar) {
  showCropperDialog.value = true;
  let avatar = rawAvatar;
  let reader = new FileReader();
  reader.onload = e => {
    let data;
    if (typeof e.target.result === "object") {
      data = window.URL.createObjectURL(new Blob([e.target.result]));
    } else {
      data = e.target.result;
    }
    cropperAvatar.value = data;
  }
  reader.readAsArrayBuffer(avatar);
}

/* 手动submit()裁剪后的图片前会执行此函数 */
function httpRequest(request) {
  const {action, file, filename} = request; // filename: file
  let formData = new FormData();
  let originalFileName = file.name.substring(0, file.name.lastIndexOf('.'));
  formData.append(filename, croppedAvatar.value, originalFileName);

  uploadAvatarAPI(action, formData).then(response => {
    userStore.avatar = response.data.newAvatar;
    showCropperDialog.value = false;
    ElMessage.success(response.msg);
  }).catch(error => {
    console.log(error)
  })
}
</script>

<style scoped>
.header-detail {
  height: 55px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--el-text-color-regular);
  font-size: 15px;
  border-bottom: 1px solid #eaeaea;
}

html.dark .header-detail {
  border-bottom-color: #414243;
}

.update-avatar-form {
  padding-top: 10px;
}

.current-avatar {
  margin-top: 8px;
  border-radius: 6px;
}

.ul-info {
  display: block;
  list-style-type: circle;
  padding-inline-start: 18px;
  margin-block-start: 0;
  margin-block-end: 0;
}

.current-avatar > span:not(:last-child) {
  margin-right: 15px;
}

@media screen and (max-width: 768px) {
  .upload-button, .random-button, .history-button {
    padding: 8px;
  }
  .update-avatar-form .el-form-item__content {
    margin-left: -10px;
  }
  .random-button, .history-button {
    margin-left: 8px !important;
  }
}

@media screen and (min-width: 768px) {

}
</style>

<style>
@media screen and (max-width: 768px) {
  .update-avatar-form .el-form-item__content {
    margin-left: -10px;
  }
}
</style>