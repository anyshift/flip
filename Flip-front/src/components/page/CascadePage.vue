<template>
  <div class="single-container" :style="topHeight()">
    <el-row :gutter="0">
      <el-col :span="18" :offset="3"><div class="grid-content bar-bg-1"/></el-col>
      <el-col :span="20" :offset="2"><div class="grid-content bar-bg-2"/></el-col>
      <el-col :span="22" :offset="1"><div class="grid-content bar-bg-3"/></el-col>
      <el-col :span="24" :offset="0">
        <div class="grid-main bar-bg-4" :style="isRadius()">
          <slot name="main"/>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const props = defineProps({
  top: String,
  radius: Boolean,
})

function topHeight() {
  if (props.top !== '' || props.top !== undefined) {
    return 'margin-top: ' + props.top;
  }
}

function isRadius() {
  if (props.radius) {
    return 'border-radius: 0.75rem';
  }
}
</script>

<style scoped>
.single-container {
  margin-top: 50px;
}

.grid-content {
  border-top-left-radius: 0.75rem;
  border-top-right-radius: 0.75rem;
  min-height: 32px;
}
.grid-main {
  border-top-left-radius: 0.75rem;
  border-top-right-radius: 0.75rem;
}

.bar-bg-1 {
  background-color: #bec7d0;
}
html.dark .bar-bg-1 {
  background-color: #656a71;
}
.bar-bg-2 {
  background-color: #cbd5e1;
}
html.dark .bar-bg-2 {
  background-color: #565b63;
}
.bar-bg-3 {
  background-color: #dee7f2;
}
html.dark .bar-bg-3 {
  background-color: #434851;
}
.bar-bg-4 {
  background-color: var(--el-bg-color-overlay);
  border: 1px solid #eaeaea;
}
html.dark .bar-bg-4 {
  background-color: var(--custom-trend-header-bg-color);
  border-color: var(--custom-trend-td-bottom-color);
}

@media only screen and (max-width: 768px) {
  .single-container {
    margin-top: 0 !important;
  }
  .grid-content {
    min-height: 16px;
  }
}
</style>

<style>
.single-container .el-input__wrapper {
  transition: unset !important;
}
</style>