<template>
  <div>
    <el-dialog class="cropper-dialog" :model-value="dialogVisible" :title="dialogTitle" center @close="closeDialog">
      <el-row>
        <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12" style="height: 300px;">
          <VueCropper ref="cropper"
                      :img="cropperImage"
                      :info="option.info"
                      :full="option.full"
                      :output-size="option.outputSize"
                      :output-type="option.outputType"
                      :can-scale="option.canScale"
                      :auto-crop="option.autoCrop"
                      :can-move="option.canMove"
                      :can-move-box="option.canMoveBox"
                      :mode="option.mode"
                      :max-image-size="option.maxImageSize"
                      :center-box="option.centerBox"
                      :fixed="option.fixed"
                      :auto-crop-width="option.autoCropWidth"
                      :auto-crop-height="option.autoCropHeight"
                      :info-true="option.infoTrue"
                      :high="option.high"
                      :enlarge="option.enlarge"
                      @real-time="realTimePreview"/>
        </el-col>
        <el-col :xs="0" :sm="12" :md="12" :lg="12" :xl="12" class="hidden-xs-only">
          <div class="preview-box">
            <div style="border: 1px solid #e1dadc;">
              <div :style="{'width': previews.w + 'px', 'height': previews.h + 'px', 'overflow': 'hidden', 'zoom': 200 / previews.w}">
                <div :style="previews.div">
                  <el-image :src="previews.url" :style="previews.img"></el-image>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="saveCroppedImage">确定</el-button>
      </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {getCurrentInstance, ref, watch} from "vue";

/*接收父组件传过来的值*/
const props = defineProps({
  dialogVisible: Boolean,
  cropperImage: String,
  dialogTitle: String,
  imgType: {
    type: String,
    default: 'blob'
  }
})

/*裁剪完成后，通知父组件上传裁剪完成的图*/
const emits = defineEmits(['uploadCroppedAvatar', 'closeAvatarCropperDialog'])

const option = ref({
  info: true, /*裁剪框的大小信息*/
  full: false, /*是否输出原图比例的截图*/
  outputSize: 1, /*裁剪生成图片的质量*/
  outputType: 'png', /*裁剪生成图片的格式*/
  canScale: true, /*允许滚轮缩放图片*/
  autoCrop: true, /*默认生成截图框*/
  canMove: false, /*上传图片是否可以移动*/
  canMoveBox: true, /*截图框能否拖动*/
  mode: 'contain', /*图片默认熏染方式*/
  maxImageSize: 200, /*限制图片最大宽度和高度*/
  centerBox: true, /*截图框是否限制在图片里面*/
  fixed: true, /*开启截图框宽高固定比例*/
  fixedNumber: [1, 1], /*截图框 [宽, 高] 比例*/
  autoCropWidth: 300, /*默认生成截图框宽度*/
  autoCropHeight: 300, /*默认生成截图框高度*/
  infoTrue: false, /* true 为展示真实输出图片宽高, false 展示看到的截图框宽高 */
  high: true, /*是否按照设备的dpr 输出等比例图片*/
  enlarge: 1 /*图片根据截图框输出比例倍数*/
})

/*裁剪框圈住的图的预览*/
const previews = ref({});
const realTimePreview = (data) => {
  previews.value = data
}

/*先这样实现。保证图片格式。*/
watch(previews, (New, Old) => {
  if (New.w <= 20) {
    option.value.enlarge = 16
  } else if (New.w <= 40 && New.w > 20) {
    option.value.enlarge = 6;
  } else if (New.w <= 60 && New.w > 40) {
    option.value.enlarge = 4;
  } else if (New.w <= 80 && New.w > 60) {
    option.value.enlarge = 3.1;
  } else if (New.w <= 100 && New.w > 80) {
    option.value.enlarge = 2.7;
  } else if (New.w <= 125 && New.w > 100) {
    option.value.enlarge = 2.3;
  } else if (New.w <= 150 && New.w > 125) {
    option.value.enlarge = 2;
  } else if (New.w <= 200 && New.w > 150) {
    option.value.enlarge = 1.5;
  } else if (New.w <= 250 && New.w > 200) {
    option.value.enlarge = 1.3;
  } else {
    option.value.enlarge = 1;
  }
})

/*因为vue3没有this，所以用getCurrentInstance()代替this。*/
const currentInstance = getCurrentInstance();
function saveCroppedImage () {
  if(props.imgType === 'blob') {
    currentInstance.ctx.$refs.cropper.getCropBlob(data => {
      emits('uploadCroppedAvatar', data)
    })
  } else {
    currentInstance.ctx.$refs.cropper.getCropData(data => {
      emits('uploadCroppedAvatar', data)
    })
  }
}

function closeDialog() {
  emits('closeAvatarCropperDialog')
}
</script>

<style scoped>
.dialog-footer button:first-child {
  margin-right: 10px;
}

.preview-box {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}
</style>

<style>
.cropper-dialog {
  --el-dialog-width: 100% !important;
}

@media screen and (max-width: 768px) {
  .cropper-dialog .cropper-modal {
    background: rgba(0,0,0,.8);
  }
}

@media screen and (min-width: 1000px) and (max-width: 1300px) {
  .cropper-dialog {
    --el-dialog-width: 80% !important;
  }
}

@media screen and (min-width: 1300px) and (max-width: 1650px) {
  .cropper-dialog {
    --el-dialog-width: 65% !important;
  }
}

@media screen and (min-width: 1650px) {
  .cropper-dialog {
    --el-dialog-width: 50% !important;
  }
}

.cropper-dialog .el-dialog__footer {
  padding: 10px 15px 20px;
}
</style>