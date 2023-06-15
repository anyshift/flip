<template>
  <PostList :posts="posts" :loading="loading" :is-all-post="true" :size-per-page="sizePerPage" :current-page="currentPage"
            :total-page="totalPage" :total-items="posts.length" @changePage="changeCurrentPage"/>
</template>

<script setup>
import {getHotPostsAPI} from '@/api/postAPI';
import {onBeforeMount, ref} from "vue";
import {useRoute, useRouter} from "vue-router/dist/vue-router";
import PostList from '../../../components/post/PostList.vue';

const router = useRouter();
const route = useRoute();

const posts = ref([]);
const loading = ref(false);
const currentPage = ref(1);
const sizePerPage = ref(30);
const totalItems = ref();
const totalPage = ref();

function getHotPost(page) {
  loading.value = true;
  setTimeout(() => {
    getHotPostsAPI().then(response => {
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
  getHotPost(currentPage.value);
})

function changeCurrentPage(page) {
  currentPage.value = page;
  getHotPost(page);

  if (page === 1) {
    router.push({name: 'index'})
  } else {
    router.push({name: 'index', query: {page: page}})
  }
}
</script>

<style scoped>

</style>