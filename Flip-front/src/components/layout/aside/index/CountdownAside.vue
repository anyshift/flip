<template>
  <el-card class="box-card">
    <el-countdown format="DD[天]HH[时]mm[分]ss[秒]" :value="value">
      <template #title>
        <div style="display: inline-flex; align-items: center">
          <el-icon style="margin-right: 4px" :size="12">
            <Calendar />
          </el-icon>
          距离下个月还有
        </div>
      </template>
    </el-countdown>
    <div class="countdown-footer">{{ value.format('YYYY-MM-DD') }}</div>
  </el-card>
</template>

<script setup>
import {ref} from 'vue';
import dayjs from 'dayjs';
import { Calendar } from '@element-plus/icons-vue';

const value = ref(dayjs().add(1, 'month').startOf('month'))
</script>

<style scoped>
.countdown-footer {
  margin-top: 8px;
  text-align: center;
}
</style>

<style>
.box-card .el-statistic__head, .box-card .el-statistic__content {
  text-align: center !important;
}

.box-card {
  transition: unset !important;
}

html.dark .box-card {
  background-color: var(--custom-trend-header-bg-color);
}
</style>