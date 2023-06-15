<template>
  <CascadePage :radius="true">
    <template #main>
      <el-row :gutter="0">
        <el-col :xs="0" :sm="1" :md="4" :lg="8" :xl="8"/>
        <el-col :xs="24" :sm="22" :md="16" :lg="8" :xl="8">
          <el-form
              v-if="!activateSuccess"
              ref="activateFormRef"
              :model="activateForm"
              :rules="rules"
              label-width="100px"
              label-position="top"
              class="activate-form"
              size="large"
              status-icon
          >
            <el-alert title="在激活之前，您只具有浏览主题等社区基本权限，执行其它操作将会被限制。" class="before-activate-info" type="warning" :closable="false" />
            <el-form-item label="当前邮箱">
              <el-input v-model="userStore.email" disabled/>
            </el-form-item>
            <el-form-item label="邮箱验证码" prop="emailCode" :error="emailCodeErrorText">
              <el-input type="text" v-model.number="activateForm.emailCode" class="email-code" placeholder="请输入六位数字验证码">
                <template #append>
                  <el-button :disabled="disableGetEmailCode" :loading="getEmailCodeLoading" @click="getEmailCode()">
                    <span v-text="sendEmailCodeText"/>
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button plain type="success" :loading="activateLoading" @click="activateEmail(activateFormRef)">激活账号</el-button>
            </el-form-item>
          </el-form>
          <el-result v-else icon="success" title="邮箱已认证激活"></el-result>
        </el-col>
        <el-col :xs="0" :sm="1" :md="4" :lg="8" :xl="8"/>
      </el-row>
    </template>
  </CascadePage>
</template>

<script setup lang="ts">
import {onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import CascadePage from "../../components/page/CascadePage.vue";
import useUserStore from '../../stores/userStore';
import {getEmailCodeAPI, activateEmailAPI} from '../../api/loginAPI';
import {ElMessage} from "element-plus";

const userStore = useUserStore();
const activateSuccess = ref(false);
activateSuccess.value = userStore.emailVerified;

const activateFormRef = ref<FormInstance>()
const activateForm = reactive({
  emailCode: '',
})
const ValidateEmail = (rule: any, value: any, callback: any) => {
  if (value.toString().length > 0 && value.toString().length < 6) {
    callback(new Error('请继续输入'))
  } else if (value.toString().length > 6) {
    callback(new Error('请检查验证码长度'))
  } else callback();
}
const rules = reactive<FormRules>({
  emailCode: [
    {required: true, message: '请输入邮箱验证码', trigger: 'change'},
    {type: 'number', message: '验证码只能是数字', trigger: 'change'},
    {validator: ValidateEmail, trigger: 'change'},
  ]
})

const disableGetEmailCode = ref(false);
const getEmailCodeLoading = ref(false);
const sendEmailCodeText = ref('发送验证码');
const requestInterval = ref(60);
function getEmailCode() {
  getEmailCodeLoading.value = true;
  sendEmailCodeText.value = '发送中';
  setTimeout(() => {
    getEmailCodeAPI(userStore.uid).then(response => {
      console.log(response)
      ElMessage({
        message: '已发送，十五分钟内有效',
        type: 'success',
      })
      activateFormRef.value.resetFields();
      disableGetEmailCode.value = true;
      sendEmailCodeText.value = requestInterval.value + ' s';
      let timer = setInterval(() => {
        requestInterval.value--;
        sendEmailCodeText.value = requestInterval.value + ' s';
        if (requestInterval.value === 0) {
          requestInterval.value = 60;
          disableGetEmailCode.value = false;
          sendEmailCodeText.value = '发送验证码';
          clearInterval(timer);
        }
      }, 1000)
    }).catch(error => {
      console.log(error)
      ElMessage({
        message: error.msg,
        type: 'error',
      })
      if (error.msg.indexOf("请求过于频繁") !== -1) {
        disableGetEmailCode.value = true;
        sendEmailCodeText.value = '请求频繁';
        return;
      }
      sendEmailCodeText.value = '发送验证码';
    }).finally(() => {
      getEmailCodeLoading.value = false;
    })
  }, 500)
}

onbeforeunload = () => {
  if (disableGetEmailCode.value && requestInterval.value > 0 && sendEmailCodeText.value !== '请求频繁') {
    localStorage.setItem("emailCodeInterval", JSON.stringify(requestInterval.value))
  }
}
onBeforeUnmount(() => {
  if (disableGetEmailCode.value && requestInterval.value > 0 && sendEmailCodeText.value !== '请求频繁') {
    localStorage.setItem("emailCodeInterval", JSON.stringify(requestInterval.value))
  }
})
onMounted(() => {
  let time = localStorage.getItem("emailCodeInterval");
  if (time) {
    requestInterval.value = Number(time);
    disableGetEmailCode.value = true;
    sendEmailCodeText.value = requestInterval.value + ' s';
    let timer = setInterval(() => {
      requestInterval.value--;
      sendEmailCodeText.value = requestInterval.value + ' s';
      if (requestInterval.value === 0) {
        disableGetEmailCode.value = false;
        requestInterval.value = 60;
        sendEmailCodeText.value = '发送验证码';
        localStorage.removeItem("emailCodeInterval");
        clearInterval(timer);
      }
    }, 1000)
  }
})

const activateLoading = ref(false);
const emailCodeErrorText = ref('');
const activateEmail = async (formEl: FormInstance | undefined) => {
  if (!formEl) return;
  activateLoading.value = true;
  emailCodeErrorText.value = '';
  await formEl.validate((valid, fields) => {
    if (valid) {
      setTimeout(() => {
        activateEmailAPI(userStore.uid, activateForm.emailCode).then(response => {
          console.log(response)
          activateSuccess.value = true;
          userStore.emailVerified = true;
        }).catch(error => {
          console.log(error)
          if (error.msg === '验证码已过期' || error.msg === '验证码错误') {
            emailCodeErrorText.value = error.msg;
            return;
          }
          ElMessage({
            message: error.msg,
            type: 'error',
          })
        }).finally(() => {
          activateLoading.value = false;
        })
      }, 500)
    } else {
      activateLoading.value = false;
      return false;
    }
  })
}
</script>

<style scoped>
.activate-form {
  margin-top: 15px;
  padding: 20px;
}

.before-activate-info {
  margin-bottom: 15px;
}
</style>

<style>
.email-code .el-input-group__append {
  background-color: #f0f9eb !important;
  color: var(--el-color-success) !important;
}
.email-code .el-input-group__append:hover {
  background-color: #67c23a !important;
  color: white !important;
}
.email-code .el-input-group__append:active {
  background-color: #529b2e !important;
  color: white !important;
}

.email-code .el-input-group__append button {
  min-width: 115px;
}
html.dark .email-code .el-input-group__append button {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

.email-code button.is-disabled, .email-code button.is-disabled:hover {
  border-color: #dcdfe6;
  border-left: 0;
  background-color: #f5f7fa;
  color: #909399;
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

html.dark .email-code .el-input-group__append {
  background-color: #1c2518 !important;
  border-left: 0;
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}
html.dark .email-code .el-input-group__append:hover {
  background-color: #67c23a !important;
  color: white !important;
}
html.dark .email-code .el-input-group__append:active {
  background-color: #529b2e !important;
  color: white !important;
}

html.dark .email-code button.is-disabled, html.dark .email-code button.is-disabled:hover {
  border: unset;
  border: 1px solid #4c4d4f;
  background-color: #262727;
  color: var(--el-disabled-text-color);
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

html.dark .email-code button.is-loading {
  border-color: revert;
}

.email-code button.is-loading {
  display: flex;
  align-items: center;
}

.before-activate-info .el-alert__content {
  width: 100%;
  text-align: center;
}
.before-activate-info .el-alert__title {
  font-size: 14px !important;
}
</style>