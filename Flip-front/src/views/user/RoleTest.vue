<template>
  <div>
    <el-empty description="正常工作"/>
    <div style="text-align: center;" v-hasAuthority="['activity:reply:delete']">测试</div>
  </div>
</template>

<script setup>
import {testGetRoleAPI, testGetPermission} from "../../api/testAPI";

testGetRoleAPI().then(response => {
  console.log(response)
}).catch(error => {
  console.log(error)
})
</script>

<style scoped>

</style>