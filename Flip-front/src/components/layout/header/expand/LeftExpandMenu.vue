<template>
  <el-drawer v-model="dialogModel" direction="ltr" size="260px" class="left-expand-drawer">
    <Admin @closeLeftExpandDrawer="handleCloseLeftExpandDrawer"/>
  </el-drawer>
</template>

<script setup>
import {computed, onMounted, ref} from "vue";
import Admin from "../../../../views/admin/Admin.vue";

const props = defineProps({
  showLeftExpand: Boolean,
})
const emits = defineEmits(['closeLeftExpandDrawer']);

const dialogModel = computed({
  get() {
    return props.showLeftExpand
  },
  set() {
    emits('closeLeftExpandDrawer');
    //关闭dialog时的set动作设置为emit到父组件，而不是默认设置props.showNoLoginDialog的值为false
  }
})

function handleCloseLeftExpandDrawer() {
  emits('closeLeftExpandDrawer');
}
</script>

<style scoped>

</style>

<style>
.left-expand-drawer .sys-main {
  display: none;
}
.left-expand-drawer .sys-menu-vertical {
  margin-right: unset;
}

@media only screen and (max-width: 992px) {
  .sys-menu-vertical:not(.left-expand-drawer .sys-menu-vertical) {
    display: none !important;
  }
}

.left-expand-drawer .el-drawer__body {
  padding-top: 0 !important;
}
</style>