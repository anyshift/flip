<template>
  <PostList :posts="posts" :loading="loading" :is-all-post="true" :size-per-page="sizePerPage" :current-page="currentPage"
            :total-page="totalPage" :total-items="posts.length" @changePage="changeCurrentPage"/>
</template>

<script setup>
import {onBeforeMount, onMounted, ref} from 'vue';
import PostList from "../../../components/post/PostList.vue";
import {getAllPostsAPI} from "../../../api/postAPI";
import {useRoute, useRouter} from "vue-router";

const router = useRouter();
const route = useRoute();

const posts = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const sizePerPage = ref(30);
const totalItems = ref();
const totalPage = ref();

function getAllPost(page) {
  loading.value = true
  setTimeout(() => {
    getAllPostsAPI(page).then(response => {
      posts.value = response.data.posts;
      currentPage.value = Number(response.data.currentPage);
      sizePerPage.value = Number(response.data.sizePerPage);
      totalItems.value = Number(response.data.totalItems);
      totalPage.value = Number(response.data.totalPage);
      console.log(response)
    }).catch(error => {
      console.log(error)
    }).finally(() => {
      loading.value = false;
    })
  }, 500)
}

onBeforeMount(() => {
  if (route.query.page) {
    currentPage.value = Number(route.query.page);
  }
  getAllPost(currentPage.value);
})

function changeCurrentPage(page) {
  currentPage.value = page;
  getAllPost(page);

  if (page === 1) {
    router.push({name: 'index'})
  } else {
    router.push({name: 'index', query: {page: page}})
  }
}
</script>

<style scoped>
.trend-body {
  padding: 0 10px;
  background: var(--el-bg-color-overlay);
  border: 1px solid #d5d7de;
  border-top: 0;
  border-radius: 0 0 5px 5px;
}
</style>