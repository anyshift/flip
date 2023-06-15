<template>
  <el-row :gutter="20">
    <el-col :span="resultSpan" :offset="resultOffset">
      <div class="results">
        <table class="results-table">
          <template v-for="(user, index) of props.users" :key="index">
            <tr v-if="(index % 2) === 0">
              <td>
                <router-link :to="{path: '/u/' + removeHtml(user.username)}" class="result-link">
                  <div class="result">
                    <el-avatar class="result-avatar" :src="user.avatar" shape="square" :size="80"/>
                    <div class="result-info">
                      <div class="result-info-header">
                        <div>
                          <el-text truncated v-if="user.nickname" v-dompurify-html="user.nickname" class="result-nickname"/>
                          <el-text truncated v-else v-dompurify-html="user.username" class="result-nickname"/>
                          <el-text truncated v-dompurify-html="'@' + user.username" class="result-username"/>
                          <el-icon v-if="user.emailVerified" class="verified-icon"><SuccessFilled /></el-icon>
                        </div>

                      </div>
                      <div class="result-info-bottom">
                        <div class="last-online-time" v-show="false">
                          <div class="time-icon"><i class="czs-time-l"/></div>
                          <el-tooltip effect="dark" content="上次活跃时间" placement="bottom" :show-after="600">
                            <div class="offline-time" v-text="'5 小时前'"></div>
                          </el-tooltip>
                        </div>
                        <div class="reg-time">
                          <div class="time-icon"><i class="czs-bar-chart-l"/></div>
                          <el-tooltip effect="dark" :content="user.createTime" placement="bottom" :show-after="600">
                            <div class="offline-time" v-text="'加入于 ' + moment(user.createTime).fromNow()"></div>
                          </el-tooltip>
                        </div>
                        <div v-text="'No.' + user.id" class="serial-number"/>
                      </div>
                    </div>
                  </div>
                </router-link>
              </td>
              <td v-if="(index + 1) < users.length">
                <router-link :to="{path: '/u/' + removeHtml(users[index + 1].username)}" class="result-link">
                  <div class="result">
                    <el-avatar class="result-avatar" :src="users[index + 1].avatar" shape="square" :size="80"/>
                    <div class="result-info">
                      <div class="result-info-header">
                        <div>
                          <el-text truncated v-if="users[index + 1].nickname" v-dompurify-html="users[index + 1].nickname" class="result-nickname"/>
                          <el-text truncated v-else v-dompurify-html="users[index + 1].username" class="result-nickname"/>
                          <el-text truncated v-dompurify-html="'@' + users[index + 1].username" class="result-username"/>
                          <el-icon v-if="users[index + 1].emailVerified" class="verified-icon"><SuccessFilled /></el-icon>
                        </div>

                      </div>
                      <div class="result-info-bottom">
                        <div class="last-online-time" v-show="false">
                          <div class="time-icon"><i class="czs-time-l"/></div>
                          <el-tooltip effect="dark" content="上次活跃时间" placement="bottom" :show-after="600">
                            <div class="offline-time" v-text="'5 小时前'"></div>
                          </el-tooltip>
                        </div>
                        <div class="reg-time">
                          <div class="time-icon"><i class="czs-bar-chart-l"/></div>
                          <el-tooltip effect="dark" :content="users[index + 1].createTime" placement="bottom" :show-after="600">
                            <div class="offline-time" v-text="'加入于 ' + moment(users[index + 1].createTime).fromNow()"></div>
                          </el-tooltip>
                        </div>
                        <div v-text="'No.' + users[index + 1].id" class="serial-number"/>
                      </div>
                    </div>
                  </div>
                </router-link>
              </td>
            </tr>
          </template>
        </table>
      </div>
    </el-col>
  </el-row>
</template>

<script setup>
import {onMounted, ref, watch} from 'vue';
import moment from "moment";
import {SuccessFilled} from '@element-plus/icons-vue';

const props = defineProps({
  users: Array,
  windowWidth: Number,
})

const resultSpan = ref();
const resultOffset = ref();
watch(() => props.windowWidth, (New, Old) => {
  if (New < 992) {
    resultSpan.value = 24;
    resultOffset.value = 0;
  } else {
    resultSpan.value = 20;
    resultOffset.value = 2;
  }
})
onMounted(() => {
  if (props.windowWidth < 992) {
    resultSpan.value = 24;
    resultOffset.value = 0;
  } else {
    resultSpan.value = 20;
    resultOffset.value = 2;
  }
})

function removeHtml(text) {
  return text.replace(/<[^>]+>/g, "");
}

function tableWidth() {
  /*if (props.users && props.users.length === 2) {
    return 'width: 66.66%';
  }*/
}
</script>

<style scoped>
.results {
  margin-top: 25px;
}

.result {
  display: flex;
  align-items: center;
  border: 1px solid #eaeaea;
  padding: 15px;
  margin-bottom: 15px;
  border-radius: var(--custom-border-radius);
}
html.dark .result {
  border-color: #333942;
}

.result-avatar {
  margin-right: 25px;
}

.result-info {
  display: flex;
  flex-direction: column;
}

.result-info-bottom {
  margin-top: 15px;
  display: inline-flex;
}

.last-online-time, .reg-time {
  display: inline-flex;
  cursor: pointer;
}

html.dark .reg-time {
  color: #778799;
}

.last-online-time .czs-time-l {
  font-size: 15px;
}

.reg-time .czs-bar-chart-l {
  font-size: 17px;
}

.serial-number {
  color: transparent;
  background: linear-gradient(to right,#409EFF,#529b2e);
  -webkit-background-clip: text;
}

.result-nickname {
  font-size: 35px;
  font-weight: 600;
  margin-right: 8px;
  color: #778799;
  -webkit-background-clip: text;
}
html.dark .result-nickname span {
  color: #a5292a !important;
}

.time-icon {
  margin: 2px 3px 0 0;
}

.result-username {
  font-size: 18px;
  color: #778799;
  margin-right: 8px;
}

.result-info-bottom > div:not(:last-child) {
  margin-right: 12px;
}

.verified-icon {
  color: #8ebc8f;
}

.result-link {
  text-decoration: none;
  color: inherit;
}

.result:hover {
  transition: transform .2s;
  transform: scale(102%);
}

.results-table {
  width: 100%;
  border-spacing: 10px 0;
}

.results-table td {
  width: 50%;
}

@media only screen and (max-width: 992px) {
  .result {
    width: unset;
    padding: 20px;
  }
}

@media only screen and (max-width: 768px){
  .results {
    margin-top: 18px;
  }
  .result {
    padding: 10px;
  }
  .result-avatar {
    width: 60px !important;
    height: 60px !important;
  }
  .result-nickname {
    font-size: 22px;
    max-width: 110px;
  }
  .result-username {
    max-width: 100px;
  }
  .result-info-bottom {
    line-height: 1;
  }
  .result-avatar {
    margin-right: 10px;
  }
  .time-icon {
    margin: 0 3px 0 0;
  }
  .results-table td {
    width: 100%;
    display: block;
  }
}
</style>

<style>
.result-info-header .el-text {
  line-height: 1;
  display: inline-flex;
}
</style>