<template>
  <el-container>
    <el-footer>
      <el-row id="footer" justify="center">
        <!-- 页脚左边距 -->
        <el-col :xs="0" :sm="0" :md="0" :lg="5" :xl="4"></el-col>

        <!-- 页脚模块 -->
        <el-col :xs="23" :sm="23" :md="23" :lg="14" :xl="16">
          <div>
            <span>© 2023 Flip Project. All Rights Reserved. </span>
            <br class="hidden-sm-and-up"/>
            <span>Powered by Flip.</span>
          </div>
        </el-col>

        <!-- 页脚右边距 -->
        <el-col :xs="0" :sm="0" :md="0" :lg="5" :xl="4"></el-col>
      </el-row>
    </el-footer>
  </el-container>
</template>


<style>
#footer {
  padding: 20px 0;
  text-align: center;
  margin: 8px -20px 0;
  background: var(--custom-footer-bg-color);
}

.el-footer {
  height: 100%;
}

.el-containder {
  width: 100%;
  position: absolute;
}
</style>