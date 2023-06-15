<template>
  <CascadePage>
    <template #main>
      <div class="search-bar-box">
        <el-col :span="searchInputSpan" :offset="searchInputOffset" class="search-col">
          <el-input v-model="inputVal" placeholder="请输入搜索关键词" size="large" clearable @keyup.enter="handleSearch(inputVal)">
            <template #prepend>
              <el-select v-model="selectVal" placeholder="选择" style="width: 85px" size="large">
                <el-option label="主题" value="post" />
                <el-option label="用户" value="user" :disabled="!userStore.isLogin"/>
              </el-select>
            </template>
            <template #append>
              <el-button type="primary" plain :loading="searchLoading" @click="handleSearch(inputVal)">
                <i class="czs-search-l"/>
              </el-button>
            </template>
          </el-input>
        </el-col>
      </div>
      <div class="search-result" v-loading="searchLoading" element-loading-text="正在搜索中" element-loading-background="transparent">
        <el-empty v-if="!searchLoading && emptySearchResult" :description="emptySearchResultDescription">
          <el-button @click="handleInsertToEs" v-hasRole="['ADMIN']">插入数据</el-button>
        </el-empty>
        <SearchedPosts v-if="selectVal === 'post' && posts.length > 0" :posts="posts" :window-width="windowWidth"/>
        <SearchedUsers v-if="selectVal === 'user' && users.length > 0" :users="users" :window-width="windowWidth"/>
        <el-empty v-if="posts.length > 0 && posts.length < 15 || users.length > 0 && users.length < 15" description="没有更多了"/>
      </div>
    </template>
  </CascadePage>
</template>

<script setup lang="ts">
import {onBeforeMount, onMounted, ref, watch} from 'vue';
import {useRoute, useRouter} from "vue-router";
import {searchPostsAPI, searchUsersAPI, addPostsAPI, addUsersAPI} from '../../api/searchAPI';
import SearchedPosts from "../../components/search/SearchedPosts.vue";
import CascadePage from "../../components/page/CascadePage.vue";
import SearchedUsers from "../../components/search/SearchedUsers.vue";
import useUserStore from '../../stores/userStore';
import {ElMessage, ElMessageBox} from "element-plus";

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const inputVal = ref('');
const selectVal = ref('post');
onBeforeMount((() => {
  inputVal.value = <string>route.query.keyword;
}))

watch(() => route.query.keyword, (New:String, Old) => {
  if (New !== inputVal.value && New !== '' && New !== undefined) {
    inputVal.value = New.trim();
    handleSearch(New.trim());
  }
})

const searchInputSpan = ref();
const searchInputOffset = ref();
const windowWidth = ref()
onMounted(() => {
  if (inputVal.value !== '' && inputVal.value !== undefined) {
    handleSearch(inputVal.value.trim());
  }
  windowWidth.value = window.innerWidth;
  if (windowWidth.value < 992 && windowWidth.value >= 768) {
    searchInputSpan.value = 20;
    searchInputOffset.value = 2;
  } else if (windowWidth.value < 768) {
    searchInputSpan.value = 22;
    searchInputOffset.value = 1;
  } else {
    searchInputSpan.value = 10;
    searchInputOffset.value = 7;
  }
})
window.onresize = () => {
  windowWidth.value = window.innerWidth;
  if (windowWidth.value < 992 && windowWidth.value >= 768) {
    searchInputSpan.value = 20;
    searchInputOffset.value = 2;
  } else if (windowWidth.value < 768) {
    searchInputSpan.value = 22;
    searchInputOffset.value = 1;
  } else {
    searchInputSpan.value = 10;
    searchInputOffset.value = 7;
  }
}

const posts = ref([]);
const users = ref([]);
const searchLoading = ref(false);
const emptySearchResult = ref(true);
const emptySearchResultDescription = ref('请输入关键词进行搜索');
function handleSearch(val: String) {
  router.push({name: 'search', query: {keyword: val}});
  posts.value.length = users.value.length = 0;
  if (selectVal.value === 'post') {
    searchLoading.value = true;
    setTimeout(() => {
      searchPostsAPI(val).then(response => {
        console.log(response)
        if (response.msg === '没有与关键词匹配的结果') {
          emptySearchResult.value = true;
          emptySearchResultDescription.value = response.msg;
          return;
        }

        emptySearchResult.value = false;
        if (response.data.posts) {
          posts.value = response.data.posts;
        }
      }).catch(error => {
        console.log(error)
        emptySearchResult.value = true;
        emptySearchResultDescription.value = error.msg;
      }).finally(() => {
        searchLoading.value = false;
      })
    }, 500)
  } else {
    searchLoading.value = true;
    setTimeout(() => {
      searchUsersAPI(val).then(response => {
        console.log(response)
        if (response.msg === '没有与关键词匹配的结果') {
          emptySearchResult.value = true;
          emptySearchResultDescription.value = response.msg;
          return;
        }

        emptySearchResult.value = false;
        if (response.data.users) {
          users.value = response.data.users;
        }
      }).catch(error => {
        console.log(error)
        emptySearchResult.value = true;
        emptySearchResultDescription.value = error.msg;
      }).finally(() => {
        searchLoading.value = false;
      })
    }, 500)
  }
}

function handleInsertToEs() {
  ElMessageBox.confirm(
      '确认插入全部数据到搜索引擎中?',
      '插入提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            instance.confirmButtonLoading = true;
            instance.confirmButtonText = '插入中...';
            setTimeout(() => {
              if (selectVal.value === 'post') {
                addPostsAPI().then(response => {
                  ElMessage({
                    type: 'success',
                    message: response.msg,
                  })
                  done();
                }).catch(error => {
                  console.log(error)
                  ElMessage({
                    type: 'error',
                    message: error.msg,
                  })
                }).finally(() => {
                  instance.confirmButtonText = '确认';
                  instance.confirmButtonLoading = false;
                })
              } else {
                addUsersAPI().then(response => {
                  console.log(response)
                  ElMessage({
                    type: 'success',
                    message: response.msg,
                  })
                  done();
                }).catch(error => {
                  console.log(error)
                  ElMessage({
                    type: 'error',
                    message: error.msg,
                  })
                }).finally(() => {
                  instance.confirmButtonText = '确认';
                  instance.confirmButtonLoading = false;
                })
              }
            }, 500)
          } else done();
        }
      }
  ).then(() => {}).catch(() => {})
}
</script>

<style scoped>
.search-bar-box {
  border-bottom: 1px solid #eaeaea;
}
html.dark .search-bar-box {
  border-bottom-color: var(--custom-trend-td-bottom-color);
}
.search-col {
  padding-top: 50px;
  padding-bottom: 50px;
}

.search-result {
  min-height: 100vh;
  padding: 0 35px;
}

@media only screen and (max-width: 768px) {
  .search-col {
    padding-top: 30px;
    padding-bottom: 30px;
  }
  .search-result {
    padding: 0;
  }
}
</style>

<style>
.search-result .el-loading-spinner {
  top: 15% !important;
}

.search-col .el-input__wrapper {
  transition: unset !important;
}
</style>