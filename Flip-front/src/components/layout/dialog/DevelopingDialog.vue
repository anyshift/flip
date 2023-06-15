<template>
  <el-dialog v-model="dialogModel" class="developing-dialog" title="正在开发中，敬请期待" center width="395px"
             destroy-on-close :show-close="false" :close-on-click-modal="false">
    <div v-text="developingText"></div>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from "vue";

const props = defineProps({
  showDevelopingDialog: Boolean,
});
const emits = defineEmits(['closeDevelopingDialog']);

const dialogModel = computed({
  get() {
    return props.showDevelopingDialog
  },
  set() {
    emits('closeDevelopingDialog'); //关闭dialog时的set动作设置为emit到父组件，而不是默认设置props.showNoLoginDialog的值为false
  }
})
const developingText = ref();

watch(() => dialogModel.value, (New, Old) => {
  if (New) {
    let time = 2;
    developingText.value = '2 秒后将自动关闭...';
    let timer = setInterval(() => {
      time--;
      developingText.value = time + ' 秒后将自动关闭...';
      if (time === 0) {
        clearInterval(timer);
        dialogModel.value = false;
      }
    }, 1000)
  }
})
</script>

<style scoped>

</style>

<style>
@media screen and (max-width: 768px) {
  .el-dialog.developing-dialog {
    --el-dialog-width: 95% !important;
  }
}
</style>