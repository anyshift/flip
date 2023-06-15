<template>
  <el-dialog v-model="dialogModel" class="no-verify-email-dialog" title="该操作需要激活邮箱" center width="395px"
             destroy-on-close>
    <div style="text-align: center;">
      <el-button @click="toActivate()">去激活</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, ref} from 'vue';
import {useRouter} from "vue-router/dist/vue-router";

const router = useRouter();

const props = defineProps({
  showNoVerifyEmailDialog: Boolean
})
const emits = defineEmits(['closeNoVerifyEmailDialog'])

//https://cn.vuejs.org/guide/components/v-model.html#component-v-model
const dialogModel = computed({
  get() {
    return props.showNoVerifyEmailDialog
  },
  set() {
    emits('closeNoVerifyEmailDialog'); //关闭dialog时的set动作设置为emit到父组件，而不是默认设置props.showNoLoginDialog的值为false
  }
})

function toActivate() {
  emits('closeNoVerifyEmailDialog');
  setTimeout(() => {
    router.push({name: 'activate'})
  }, 300)
}
</script>

<style scoped>

</style>

<style>
@media screen and (max-width: 768px) {
  .el-dialog.no-verify-email-dialog {
    --el-dialog-width: 95% !important;
  }
}
</style>