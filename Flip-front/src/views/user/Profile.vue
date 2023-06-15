<template>
  <el-empty v-if="user.state.length === 0" description="该用户未注册"/>

  <el-container v-else class="user-container">
    <el-main class="user-container-main">
      <div class="user-box">
        <div class="user-info">
          <div class="user-avatar-info">
            <el-skeleton animated :loading="loadingProfile">
              <template #template>
                <el-skeleton-item variant="image" class="user-avatar-skeleton"/>
              </template>
              <template #default>
                <el-avatar :src="user.state.avatar" :size="80" shape="square" class="user-avatar"/>
              </template>
            </el-skeleton>
          </div>
          <div class="user-detail-info">
            <el-skeleton animated :loading="loadingProfile" :rows="1" style="display: flex; flex-direction: column;">
              <template #default>
                <div class="detail-top">
                  <div v-if="user.state.nickname" v-text="user.state.nickname" class="nickname"/>
                  <div v-else v-text="user.state.username" class="nickname"/>
                  <el-tag class="mod-tag" v-text="'MOD'" size="small" type="danger" effect="dark" v-if="false"/>
                  <div v-text="'@' + user.state.username" class="username"></div>
                  <div>
                    <el-tooltip v-if="user.state.emailVerified" content="已激活" placement="top" :show-after="600">
                      <el-icon class="verified-icon"><SuccessFilled /></el-icon>
                    </el-tooltip>
                  </div>
                </div>
                <div class="detail-bottom">
                  <div class="online-status" v-show="false">
                    <div class="online-dot"></div>
                    <div class="online-text">在线</div>
                  </div>
                  <div class="last-online-time" v-show="false">
                    <div style="margin: 2px 3px 0 0;"><i class="czs-time-l"/></div>
                    <el-tooltip effect="dark" content="上次活跃时间" placement="bottom" :show-after="600">
                      <div class="offline-time" v-text="'5 小时前'"></div>
                    </el-tooltip>
                  </div>
                  <div class="reg-time">
                    <div style="margin: 3px 3px 0 0;"><i class="czs-bar-chart-l"/></div>
                    <el-tooltip effect="dark" :content="user.state.createTime" placement="bottom" :show-after="600">
                      <div class="offline-time" v-text="'加入于 ' + moment(user.state.createTime).fromNow()"></div>
                    </el-tooltip>
                  </div>
                  <div v-text="'No.' + user.state.id" class="serial-number"/>
                </div>
              </template>
            </el-skeleton>
          </div>
        </div>
      </div>
      <el-tabs :tab-position="'top'" class="user-tabs" type="border-card">

        <el-tab-pane label="主题" lazy>
          <UserPosts :username="props.username"/>
        </el-tab-pane>
        <el-tab-pane label="评论" lazy>
          <UserComments/>
        </el-tab-pane>
        <el-tab-pane label="关注" lazy>
          <UserFollows/>
        </el-tab-pane>
        <el-tab-pane label="收藏" lazy>
          <UserBookmarks/>
        </el-tab-pane>
      </el-tabs>
    </el-main>

    <el-aside width="230px" class="hidden-sm-and-down" style="margin-left: 20px">
      <StatisticsAside/>
      <div style="height: 15px"></div>
      <CountdownAside/>
      <div style="height: 15px"></div>
    </el-aside>
  </el-container>

</template>

<script setup lang="ts">
import {onBeforeMount, onMounted, reactive, ref} from "vue";
import useUserStore from '../../stores/userStore';
import {getUserProfileAPI} from '../../api/userApi';
import moment from "moment";
import useTabStore from '../../stores/tabStore';
import UserPosts from "../../components/user/profile/UserPosts.vue";
import UserComments from "../../components/user/profile/UserComments.vue";
import UserFollows from "../../components/user/profile/UserFollows.vue";
import UserBookmarks from "../../components/user/profile/UserBookmarks.vue";
import {SuccessFilled} from '@element-plus/icons-vue';
import StatisticsAside from "../../components/layout/aside/index/StatisticsAside.vue";
import CountdownAside from "../../components/layout/aside/index/CountdownAside.vue";

const userStore = useUserStore();
const tabStore = useTabStore();

const global_title = import.meta.env.VITE_GLOBAL_TITLE;

const props = defineProps({
  username: String,
})

const user = reactive({
  state: {}
});

const loadingProfile = ref(false);

onBeforeMount(() => {
  if (userStore.username && userStore.username === props.username) {
    user.state = userStore;
    document.title = '个人主页 - ' + global_title;
  } else {
    loadingProfile.value = true;
    setTimeout(() => {
      getUserProfileAPI(props.username).then(response => {
        user.state = response.data;
        document.title = '@' + user.state.username + ' - ' + global_title;
        console.log(response)
      }).catch(error => {
        console.log(error)
      }).finally(() => {
        loadingProfile.value = false;
      })
    }, 500)
  }
})
</script>

<style scoped>
.user-container-main {
  padding: 0 !important;
  display: flex;
  flex-direction: column;
}

.user-box {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 8px 8px 0 0;
  border: 1px solid #dcdfe6;
  border-bottom: 0;
}

html.dark .user-box {
  background: var(--custom-trend-header-bg-color);
  border-color: #414243;
}

.user-info {
  display: flex;
  justify-content: space-between;
  margin: 5px 0;
}

.user-detail-info {
  width: 100%;
  margin: auto 0 auto 20px;
}

.detail-top {
  display: flex;
  margin-top: -5px;
  align-items: baseline;
}

.detail-bottom {
  margin-top: 5px;
  display: inline-flex;
}

.nickname {
  font-size: 35px;
  font-weight: 600;
  margin-right: 8px;
  background: linear-gradient(to right,#E4A93C,#AF7323);
  -webkit-background-clip: text;
  color: transparent;
}

.username {
  font-size: 18px;
  margin-right: 8px;
  background: linear-gradient(to right,#409EFF,#529b2e);
  -webkit-background-clip: text;
}

.username, .serial-number {
  color: transparent;
}

.serial-number {
  background: linear-gradient(to right,#409EFF,#529b2e);
  -webkit-background-clip: text;
}

.detail-bottom > div:not(:last-child) {
  margin-right: 12px;
}

.online-status {
  display: inline-flex;
  align-items: center;
  margin-top: -2px;
}

.online-dot {
  height: 12px;
  width: 12px;
  border-radius: 100%;
  background: #7fba02;
}

.online-text {
  margin-left: 3px;
}

.last-online-time, .reg-time {
  display: inline-flex;
  cursor: pointer;
}

.last-online-time .czs-time-l {
  font-size: 15px;
}

.reg-time .czs-bar-chart-l {
  font-size: 17px;
}

.user-tabs {
  min-height: 90vh;
  border-radius: 0 0 var(--custom-border-radius) var(--custom-border-radius);
}

html.dark .user-tabs {
  border-color: #3a3d3f !important;
}

.user-avatar-skeleton {
  width: 80px;
  height: 80px;
}

.user-avatar-info {
  display: flex;
  align-items: center;
}

@media screen and (max-width: 768px) {
  .user-avatar, .user-avatar-skeleton {
    width: 66px;
    height: 66px;
  }

  .nickname {
    font-size: 23px;
  }

  .username {
    font-size: 18px;
  }

  .user-detail-info {
    margin: auto 0 auto 10px;
  }

  .user-info {
    margin-bottom: -5px;
  }

  .user-box {
    padding: 12px 12px 15px;
  }
}

.verified-icon {
  color: #8ebc8f;
}
</style>

<style>
.user-tabs .el-tabs__item.is-active {
  border-top-color: var(--custom-tabs-border-color) !important;
  color: var(--el-menu-active-color) !important;
}

.user-tabs .el-tabs__item.is-active {
  transition: unset !important;
}

.user-tabs .user-tabs .el-tabs__header {
  border-right-color: var(--custom-tabs-border-color) !important;
}

html.dark .user-tabs .el-tabs__header {
  background-color: var(--custom-trend-header-bg-color) !important;
  border-bottom: 1px solid #3a3d3f;
}

.user-tabs .el-tabs__content {
  min-height: 85vh;
}
</style>