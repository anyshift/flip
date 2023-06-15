<template>
  <div class="nickname">
    <div class="nickname-header">
      <div>▼ 更改昵称</div>
    </div>
    <el-form ref="nicknameFormRef" :model="nicknameModel" :rules='nicknameRules'
             label-position="top" label-width="100px" size="large" class="nickname-form">
      <el-form-item label="当前昵称">
        <el-input :placeholder="userStore.nickname ? userStore.nickname : userStore.username" readonly></el-input>
      </el-form-item>
      <el-form-item label="更改昵称" prop="nickname" :error="nicknameErrorText">
        <el-input v-model.trim="nicknameModel.nickname" maxlength="12" show-word-limit placeholder="设置专属昵称"
                  :keyup.native="nicknameModel.nickname=nicknameModel.nickname.replace(/\s+/g, '')">
        </el-input>
      </el-form-item>
    </el-form>
    <el-button size="large" class="nickname-button" @click="nicknameSubmit(nicknameFormRef)" :loading="nicknameLoading">保存</el-button>
  </div>

  <div class="social">
    <div class="social-header">
      <div>
        <span v-text="socialFormText" :style="showSocialForm ? 'padding-right: 0.5px;' : null"/><span>社交网络</span>
        <span style="margin-left: 8px; color: #aaa;">按需设置</span>
      </div>
      <div v-text="openSocialFormText" @click="openSocialForm" style="cursor: pointer;"/>
    </div>
    <div v-show="showSocialForm">
      <el-form label-position="top" label-width="100px" size="large" class="social-form">
        <el-form-item label="Github">
          <el-input placeholder="your-username">
            <template #prefix>
              <div>https://github.com/</div>
            </template>
            <template #prepend><i class="czs-github-logo"/></template>
          </el-input>
        </el-form-item>
        <el-form-item label="Telegram">
          <el-input placeholder="your-username">
            <template #prefix>
              <div>https://t.me/</div>
            </template>
            <template #prepend><i class="czs-telegram"/></template>
          </el-input>
        </el-form-item>
        <el-form-item label="Twitter">
          <el-input placeholder="your-username">
            <template #prefix>
              <div>https://twitter.com/</div>
            </template>
            <template #prepend><i class="czs-twitter"/></template>
          </el-input>
        </el-form-item>
        <el-form-item label="Steam">
          <el-input placeholder="your-id">
            <template #prefix>
              <div>https://steamcommunity.com/id/</div>
            </template>
            <template #prepend><i class="czs-steam"/></template>
          </el-input>
        </el-form-item>
        <el-form-item label="Wechat">
          <el-input placeholder="your-wechat">
            <template #prepend><i class="czs-weixin"/></template>
          </el-input>
        </el-form-item>
        <el-form-item label="QQ">
          <el-input placeholder="your-qq">
            <template #prepend><i class="czs-qq"/></template>
          </el-input>
        </el-form-item>
        <el-form-item label="Weibo">
          <el-input placeholder="your-username">
            <template #prefix>
              <div>https://weibo.com/</div>
            </template>
            <template #prepend><i class="czs-weibo"/></template>
          </el-input>
        </el-form-item>
      </el-form>
      <el-button size="large" class="social-button">保存</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {reactive, ref} from 'vue';
import {ElMessage, FormInstance, FormRules} from "element-plus";
import {updateNicknameAPI} from '../../../api/userAPI';
import useUserStore from '../../../stores/userStore'
import _ from 'lodash';

const userStore = useUserStore();

const nicknameFormRef = ref<FormInstance>();
const nicknameModel = reactive({
  nickname: ''
})
const validateNickname = (rule: any, value: any, callback: any) => {
  let nicknameReg = /^[\u4E00-\u9FA5A-Za-z\d]+$/;
  if (value.length !== 0 && !nicknameReg.test(value)) {
    callback(new Error('昵称不能包含特殊字符'));
    return;
  } else if (value.length === 1) {
    callback(new Error('请继续输入'));
    return;
  }
  nicknameErrorText.value = '';
  callback();
}
const nicknameRules = reactive<FormRules>({
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'change' },
    { validator: validateNickname, trigger: 'change' },
  ]
})

const nicknameErrorText = ref('')

const nicknameLoading = ref(false);
const nicknameSubmit = _.throttle((formRef: FormInstance | undefined) => {
  if (!formRef) return;
  nicknameLoading.value = true;
  setTimeout(() => {
    formRef.validate((isValid) => {
      if (isValid) {
        if (nicknameModel.nickname === '' & !userStore.nickname
            || nicknameModel.nickname === '' & userStore.nickname === userStore.username) {
          nicknameLoading.value = false;
          ElMessage({
            message: '昵称无变化',
            type: 'success',
          })
          return;
        }
        if (nicknameModel.nickname === userStore.nickname) {
          nicknameLoading.value = false;
          ElMessage({
            message: '昵称无变化',
            type: 'success',
          })
          return;
        }
        updateNicknameAPI(nicknameModel.nickname).then(response => {
          userStore.nickname = nicknameModel.nickname;
          nicknameFormRef.value.resetFields('nickname')
          ElMessage({
            message: response.msg,
            type: 'success',
          })
          console.log(response)
        }).catch(error => {
          nicknameErrorText.value = error.msg;
          console.log(error)
        }).finally(() => {
          nicknameLoading.value = false;
        })
      } else {
        nicknameLoading.value = false;
      }
    })
  }, 500)
}, 1000)

const showSocialForm = ref(false);
const socialFormText = ref('▶ ');
const openSocialFormText = ref('点击展开');
function openSocialForm() {
  showSocialForm.value = !showSocialForm.value;
  if (openSocialFormText.value === '点击展开') {
    socialFormText.value = '▼ ';
    openSocialFormText.value = '点击收起';
  } else {
    socialFormText.value = '▶ ';
    openSocialFormText.value = '点击展开';
  }
}
</script>

<style scoped>
.nickname-header, .social-header {
  height: 55px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--el-text-color-regular);
  font-size: 15px;
  border-bottom: 1px solid #eaeaea;
}

html.dark .nickname-header, html.dark .social-header {
  border-bottom-color: #414243;
}

.social-header {
  margin-top: 20px;
}

.nickname-form, .social-form {
  padding-top: 15px;
}

.nickname-form, .social-form, .nickname-button, .social-button {
  margin-left: 15px;
}
</style>

<style>
.nickname-form .el-input__wrapper,
.social-form .el-input__wrapper {
  transition: unset;
}

.social-form .el-input__prefix-inner > :last-child {
  margin-right: 0;
}

.nickname-form .el-input > div,
.social-form .el-input > div {
  box-shadow: unset;
  border: 1px solid #eaeaea;
}

html.dark .nickname-form .el-input > div,
html.dark .social-form .el-input > div {
  border-color: #414243;
}

.social-form .el-input-group__prepend {
  font-size: 18px;
}
</style>