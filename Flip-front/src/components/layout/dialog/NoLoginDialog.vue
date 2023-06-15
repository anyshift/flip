<template>
  <el-dialog v-model="dialogModel" class="login-require-dialog" title="该操作需要登录" center width="395px"
             destroy-on-close>
    <div style="text-align: center;">
      <el-button @click="registerClicked">去注册</el-button>
      <el-button @click="loginClicked">去登录</el-button>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import {useRouter} from "vue-router";
import {computed, ref} from "vue";

const router = useRouter();

const props = defineProps({
  showNoLoginDialog: Boolean
})
const emits = defineEmits(['closeNoLoginDialog'])

//https://cn.vuejs.org/guide/components/v-model.html#component-v-model
const dialogModel = computed({
  get() {
    return props.showNoLoginDialog
  },
  set() {
    emits('closeNoLoginDialog'); //关闭dialog时的set动作设置为emit到父组件，而不是默认设置props.showNoLoginDialog的值为false
  }
})

const registerClicked = () => {
  emits('closeNoLoginDialog');
  setTimeout(() => {
    router.push({name: 'register'});
  }, 300)
}

const loginClicked = () => {
  emits('closeNoLoginDialog');
  setTimeout(() => {
    router.push({name: 'login'})
  }, 300)
}
</script>

<style scoped>

</style>

<style>
@media screen and (max-width: 768px) {
  .el-dialog.login-require-dialog {
    --el-dialog-width: 95% !important;
  }
}
</style>