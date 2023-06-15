<template>
  <el-card class="box-card">
    <template #header>
      <div>站点统计</div>
    </template>
    <div class="card-header">
      <div>
        <el-statistic title="用户总数" :value="9" />
      </div>
      <el-divider class="divide" direction="vertical" border-style="dashed"/>
      <div>
        <el-statistic title="文章总数" :value="28" />
      </div>
    </div>
  </el-card>
</template>

<script setup>

</script>

<style>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.box-card {
  box-shadow: unset !important;
  border-radius: var(--custom-border-radius);
}

.el-statistic__content {
  text-align: center;
}

.divide {
  height: 3.5em;
}
</style>