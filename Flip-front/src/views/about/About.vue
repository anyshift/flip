<template>
  <div class="about">
    <el-timeline>
      <el-timeline-item timestamp="" placement="top">
        <el-card>
          <h4>里程碑计划 2：</h4>
          <p>1、嵌入Markdown编辑器；</p>
          <p>2、发布帖子；</p>
          <p>3、回复帖子。</p>
        </el-card>
      </el-timeline-item>
      <el-timeline-item timestamp="" placement="top">
        <el-card>
          <h4>里程碑计划 1：</h4>
          <p>1、用户个人中心；</p>
          <p>2、管理中心；</p>
          <p>3、找回密码；</p>
          <p>4、更改邮箱。</p>
        </el-card>
      </el-timeline-item>
      <el-timeline-item center timestamp="2022/11/22" placement="top">
        在经历多个日夜的业余开发后，登录注册功能已完成。
      </el-timeline-item>
      <el-timeline-item center timestamp="2022/10/17" placement="top">
        前端主体架构成型。
      </el-timeline-item>
      <el-timeline-item timestamp="2022/10/16" placement="top">
        项目启动，暂取名 <span style="color: brown;">Flip</span>。
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<script setup>

</script>

<style scoped>
.about .el-timeline {
  padding-left: 0 !important;
}
</style>