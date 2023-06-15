<template>
  <div class="status-box" v-loading="loading" element-loading-text="正在加载">
    <el-empty v-if="!loading && cpu.coreNum === 0" description="加载出错"/>
    <div v-if="!loading && cpu.coreNum !== 0">
      <div class="system-status">
        <el-progress type="dashboard" :percentage="cpu.idle" :color="colors">
          <template #default="{ percentage }">
            <span class="percentage-value">{{ percentage }}%</span>
            <span class="percentage-label">CPU使用率</span>
          </template>
        </el-progress>
        <el-progress type="dashboard" :percentage="cpu.temperature" :color="colors">
          <template #default="{ percentage }">
            <span class="percentage-value">{{ percentage }}℃</span>
            <span class="percentage-label">CPU温度</span>
          </template>
        </el-progress>
        <el-progress type="dashboard" :percentage="memory.availablePercentage" :color="colors">
          <template #default="{ percentage }">
            <span class="percentage-value">{{ percentage }}%</span>
            <span class="percentage-label">内存使用率</span>
          </template>
        </el-progress>
      </div>
      <div class="forum-status">
        <el-card shadow="hover" class="status-card" style="width: 200px;">
          <el-statistic title="用户数" :value="forum.userNumber" />
        </el-card>
        <el-card shadow="hover" class="status-card" style="width: 200px;">
          <el-statistic title="文章数" :value="forum.postNumber" />
        </el-card>
      </div>
      <el-empty description="其它状态正在生产中" style="padding-top: 0;"/>
    </div>
  </div>
</template>

<script setup>
import {onBeforeMount, onBeforeUnmount, reactive, ref, watch} from 'vue';
import {getSystemAPI, getForumAPI} from "../../../api/admin/overviewAPI";
import {useRouter} from "vue-router";

const router = useRouter();

const cpu = reactive({
  coreNum: 0,
  idle: 0,
  temperature: 0,
})
const forum = reactive({
  userNumber: 0,
  postNumber: 0,
})
const memory = reactive({
  total: '',
  available: '',
  availablePercentage: 0,
})

const colors = [
  { color: '#006400', percentage: 10 },
  { color: '#2E8B57', percentage: 20 },
  { color: '#8FBC8F', percentage: 30 },
  { color: '#9ACD32', percentage: 40 },
  { color: '#48D1CC', percentage: 50 },
  { color: '#EEDC82', percentage: 60 },
  { color: '#BDB76B', percentage: 70 },
  { color: '#DAA520', percentage: 80 },
  { color: '#F4A460', percentage: 90 },
  { color: '#A52A2A', percentage: 100 },
]

onBeforeMount(() => {
  getSystemInfo();
})
onBeforeUnmount(() => {
  clearInterval(getSystemInfoInternal);
})
watch(() => cpu.coreNum, (New, Old) => {
  if (New > 0) {
    getSystemInfoInternal;
  }
})

const loading = ref(false);
function getSystemInfo() {
  loading.value = true;
  getSystemAPI().then(response => {
    console.log(response)
    let cpuData = response.data.cpu;
    cpu.idle = cpuData.idle.substring(cpuData.idle, cpuData.idle.length-1);

    cpu.coreNum = cpuData.coreNum;
    cpu.temperature = cpuData.temperature;

    let forumData = response.data.forum;
    forum.userNumber = forumData.userNumber;
    forum.postNumber = forumData.postNumber;

    let memoryData = response.data.memory;
    memory.total = memoryData.total;
    memory.available = memoryData.available;

    handleAvailableMemory(memory.total, memory.available)
  }).finally(() => {
    loading.value = false;
  })
}
const getSystemInfoInternal = setInterval(() => {
  getSystemAPI().then(response => {
    let cpuData = response.data.cpu;
    cpu.idle = cpuData.idle.substring(cpuData.idle, cpuData.idle.length-1);
    cpu.coreNum = cpuData.coreNum;
    cpu.temperature = cpuData.temperature;

    let forumData = response.data.forum;
    forum.userNumber = forumData.userNumber;
    forum.postNumber = forumData.postNumber;

    let memoryData = response.data.memory;
    memory.total = memoryData.total;
    memory.available = memoryData.available;

    handleAvailableMemory(memory.total, memory.available)
  })
}, 1500)

function handleAvailableMemory(total, available) {
  let totalUnit = total.slice(-2);
  let totalNumber = total.substring(0, total.length - 2);

  let availableUnit = available.slice(-2);
  let availableNumber = available.substring(0, available.length - 2);

  if (totalUnit === availableUnit) {
    memory.availablePercentage = ((totalNumber - availableNumber) / totalNumber * 100).toFixed(2);
  } else if (totalUnit === 'GB' && availableUnit === 'MB') {
    memory.availablePercentage = (((totalNumber * 1024 - availableNumber) / (totalNumber * 1024)) * 100).toFixed(2);
  }
}
</script>

<style scoped>
.percentage-value {
  display: block;
  margin-top: 10px;
  font-size: 28px;
}
.percentage-label {
  display: block;
  margin-top: 10px;
  font-size: 12px;
}
.demo-progress .el-progress--line {
  margin-bottom: 15px;
  width: 350px;
}
.demo-progress .el-progress--circle {
  margin-right: 15px;
}

.system-status, .forum-status {
  display: flex;
  align-items: center;
  justify-content: space-evenly;
  padding: 50px 0;
}
.system-status {
  border-bottom: 1px solid #e4e7ec;
}
html.dark .system-status {
  border-bottom-color: #333840;
}
</style>

<style>
.status-card .el-statistic__head,
.status-card .el-statistic__content {
  display: flex;
  justify-content: left;
}

.status-box .el-loading-spinner {
  margin-top: 100px !important;
}

.status-card {
  transition: unset !important;
}
</style>