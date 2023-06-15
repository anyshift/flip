<template>
  <div class="author-box">
    <div class="author-header"></div>
    <div class="author-body">
      <div class="author-avatar">
        <el-skeleton :loading="postLoading" animated style="display: flex; justify-content: center;">
          <template #template>
            <el-skeleton-item variant="image" class="avatar-skeleton"/>
          </template>
          <template #default>
            <div v-if="!props.postLoading">
              <el-avatar :src="props.author.avatar" :size="100"/>
            </div>
          </template>
        </el-skeleton>

      </div>
    </div>
    <div class="author-footer">
      <div class="author-name">
        <el-skeleton style="width: 170px" :loading="postLoading" animated>
          <template #template>
            <el-skeleton-item variant="text" style="height: 18px"/>
          </template>
          <template #default>
            <div v-if="!props.postLoading">
              <div class="author-nickname" v-text="props.author.nickname"/>
              <div class="author-username" v-text="'@' + props.author.name"/>
            </div>
          </template>
        </el-skeleton>
      </div>
      <div class="author-badge">
        <el-skeleton style="width: 170px" :loading="postLoading" animated>
          <template #template>
            <el-skeleton-item variant="text" style="height: 18px"/>
          </template>
          <template #default>
            <div v-if="!props.postLoading" class="badges">
              <el-tag type="warning" round effect="plain" v-text="'文章作者'"/>
              <el-tag round effect="plain" v-text="'No.' + props.author.id"/>
              <el-tag type="success" round effect="plain" v-text="props.author.role.alias"/>
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  author: Object,
  postLoading: Boolean,
})
</script>

<style scoped>
.author-box {
  min-height: 230px;
  background: white;
  border: 1px solid #e4e7ec;
  border-radius: var(--custom-border-radius);
}
html.dark .author-box {
  background-color: var(--custom-trend-header-bg-color);
  border-color: #414243;
}

.author-header {
  background-color: #c0c4cc;
  min-height: 80px;
  border-radius: var(--custom-border-radius) var(--custom-border-radius) 0 0;
}
html.dark .author-header {
  background-color: #6c6e72;
}

.author-footer {
  margin-top: 15px;
}

.author-name, .author-badge {
  display: flex;
  justify-content: center;
}
.author-name {
  margin-bottom: 15px;
}

.author-avatar {
  margin-top: -50px;
  display: flex;
  justify-content: center;
}

.author-nickname {
  background: linear-gradient(to right,#E4A93C,#AF7323);
  -webkit-background-clip: text;
  font-weight: 600;
  -webkit-text-fill-color: transparent;
  margin-right: 5px;
  font-size: 20px;
}
.author-username {
  color: darkgray;
}

.author-name > div {
  display: flex;
  align-items: end;
  line-height: 1;
}

.avatar-skeleton {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid white;
}
html.dark .avatar-skeleton {
  border-color: var(--custom-trend-header-bg-color);
}
</style>

<style>
.author-avatar .el-avatar {
  border: 3px solid white;
}
html.dark .author-avatar .el-avatar {
  border-color: var(--custom-trend-header-bg-color);
}

.badges > .el-tag:not(:last-child) {
  margin-right: 6px;
}
</style>