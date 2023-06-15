<template>
  <el-card class="box-card hot-tag-card">
    <template #header>
      <div class="card-header">
        热门标签
      </div>
    </template>
    <table class="hot-tag-table">
      <template v-for="(tag, index) of tags">
        <tr v-if="(index % 2) === 0">
          <td><el-button @click="router.push({path: '/t/' + tag.label})" v-text="tag.value"/></td>
          <td v-if="(index + 1) < tags.length"><el-button @click="router.push({path: '/t/' + tags[index+1].label})" v-text="tags[index+1].value"/></td>
        </tr>
      </template>
    </table>
  </el-card>
</template>

<script setup>
import {useRouter} from "vue-router";

const router = useRouter();
const tags = [
  {
    label: 'driving-tips',
    value: '驾驶技巧',
  },
  {
    label: 'sign',
    value: '交通标志',
  },
  {
    label: 'manner',
    value: '交通礼仪',
  },
  {
    label: 'safety-awareness',
    value: '安全意识',
  },
  {
    label: 'driving-rules',
    value: '行车规则',
  },
  {
    label: 'law',
    value: '交通法规',
  },
]
</script>

<style scoped>
.hot-tag-table tr>td:first-child>button {
  margin-left: -4px;
  margin-right: 8px;
}
.hot-tag-table > tr:not(:last-child) button {
  margin-bottom: 6px;
}
</style>

<style>
.box-card {
  box-shadow: unset !important;
}

.hot-tag-card .el-card__body {
  padding: 10px var(--el-card-padding);
}
</style>