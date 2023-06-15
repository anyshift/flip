<template>
  <div class="login-module">
    <div class="login-box">
      <el-form
          ref="ruleFormRef"
          class="form-body"
          label-position="top"
          :model="user"
          :rules="userRules"
          status-icon
          size="large"
      >
        <div class="login-banner-body">
          <el-divider class="logo">
            <el-image style="width: 32px; height: 32px" :src="logo" fit="fill"/>
          </el-divider>
          <div class="login-banner">登录到您的账户</div>
        </div>
        <el-form-item v-if="errorLoginMsg">
          <div class="error-login">
            <div v-text="errorLoginMsg"/>
            <div v-if="bannedDeadline.length > 0" v-text="'将于 ' + moment(bannedDeadline).fromNow() + '解除限制'"/>
          </div>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input
              type="text"
              autocomplete="off"
              v-model.trim="user.username"
              @keyup.enter="submitLogin(ruleFormRef, user)"
              :keyup.native="user.username=user.username.replace(/\s+/g, '')"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
              show-password
              type="password"
              autocomplete="off"
              v-model.trim="user.password"
              @keyup.enter="submitLogin(ruleFormRef, user)"
              :keyup.native="user.password=user.password.replace(/\s+/g, '')"
          />
        </el-form-item>
        <el-form-item label="验证码" prop="captcha" class="no-wrap">
          <el-input
              type="text"
              autocomplete="off"
              v-model.trim="user.captcha"
              @keyup.enter="submitLogin(ruleFormRef, user)"
              :keyup.native="user.captcha=user.captcha.replace(/\s+/g, '')"
          />
          <el-image class="captcha-image" :src="captchaImage" @click="getCaptcha()" v-if="!loadCaptchaError"/>
          <div class="captcha-image" v-if="loadCaptchaError">
            <span class="error-load-captcha" v-text="loadCaptchaErrorMsg"/>
          </div>
        </el-form-item>
        <el-form-item style="display: table; margin: 30px auto;">
          <el-button type="primary" @click="submitLogin(ruleFormRef, user)" class="buttonModule" v-loading="loginButtonLoading">登录</el-button>
          <el-button @click="resetForm(ruleFormRef)" class="buttonModule">重置</el-button>
          <el-button @click="router.back()" class="buttonModule">返回</el-button>
          <el-button @click="router.push({path: '/'})" class="buttonModule">取消</el-button>
        </el-form-item>
      </el-form>

      <div class="no-account-banner">
        还没账号?
        <router-link :to="{path: '/register'}" class="register-now">立即注册</router-link>
      </div>
    </div>

    <el-dialog v-model="showLoginSuccessDialog" class="login-success-dialog" title="登录成功" center width="395px"
               destroy-on-close :show-close="false" :close-on-click-modal="false">
      <span v-text="loginSuccessDialogText"></span>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {reactive, ref} from "vue";
import _ from "lodash";
import {FormInstance, FormRules} from "element-plus";
import {useRoute, useRouter} from "vue-router";
import useUserStore from '../../stores/userStore';
import {getCaptchaImage, checkCaptcha} from '../../api/loginApi';
import moment from "moment";

const logo = import.meta.env.VITE_LOGO_ADDRESS;
const userStore = useUserStore()

//使用路由器、路由
const router = useRouter()
const route = useRoute()

//后端校验用户名和密码错误时返回给表单的错误内容
const errorLoginMsg = ref('')
const bannedDeadline = ref('')

const loginButtonLoading = ref(false);

//表单引用，指向整个表单
const ruleFormRef = ref<FormInstance>()

const user = reactive({
  username: '',
  password: '',
  captcha: '',
  captchaOwner: ''
})

/**
 * 校验表单中的用户名
 * @param rule 指向校验规则的对象
 * @param value 需要校验的值
 * @param callback 校验结束的回调函数
 */
const ValidateUsername = (rule: any, value: any, callback: any) => {
  const reg = /^[A-Za-z\d]+$/;
  setTimeout(() => {
    if (!reg.test(value)) {
      callback(new Error('用户名格式不对'));
    } else callback();
  }, 100)
}

/**
 * 校验表单中的密码
 * @param rule 指向校验规则的对象
 * @param value 需要校验的值
 * @param callback 校验结束的回调函数
 */
const ValidatePassword = (rule: any, value: any, callback: any) => {
  const reg = /(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[\W_]).{8,}/;
  setTimeout(() => {
    if (!reg.test(value)) {
      callback(new Error('请检查密码'));
    } else {
      callback(); //校验通过的调用方式
    }
  }, 200)
}

/**
 * 验证表单中输入的图片验证码是否正确
 * @param rule
 * @param value
 * @param callback
 * @constructor
 */
const ValidateCaptcha = (rule: any, value: any, callback: any) => {
  if (value.length === 4) {
    checkCaptcha(value, user.captchaOwner).then(response => {
      callback();
    }).catch(error => {
      callback(new Error(error.msg))
    })
  } else callback(new Error("请继续输入验证码"))
}

const userRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'change' },
    { min: 4, max: 12, message: '请继续输入', trigger: 'change' },
    { validator: ValidateUsername, trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'change' },
    { min: 8, message: '请继续输入', trigger: 'change' },
    { validator: ValidatePassword, trigger: 'change' }
  ],
  captcha: [
    { required: true, message: "请输入验证码", trigger: 'change' },
    { validator: ValidateCaptcha, trigger: 'change' }
  ],
})

const showLoginSuccessDialog = ref();
const loginSuccessDialogText = ref();

/**
 * 处理注册按钮点击后的时事件
 * @param ruleFormReference
 * @param user
 */
const submitLogin = (ruleFormReference, user) => {
  if (!ruleFormReference) return;
  loginButtonLoading.value = true;
  ruleFormReference.validate((valid, fields) => {
    if (valid) {
      userStore.login(user).then(response => {
        userStore.getProfile(); //获取登录用户的个人资料
        errorLoginMsg.value = '';
        ruleFormReference.resetFields();
        showLoginSuccessDialog.value = true;
        let time = 2;
        loginSuccessDialogText.value = '2 秒后将自动跳转到首页...';
        let timer = setInterval(() => {
          time--;
          loginSuccessDialogText.value = time + ' 秒后将自动跳转到首页...';
          if (time === 0) {
            clearInterval(timer);
            showLoginSuccessDialog.value = false;
            loginSuccessDialogText.value = '';
            setTimeout(() => {
              router.push({name: 'index'});
            }, 300)
          }
        }, 1000)
      }).catch(error => {
        console.log(error)
        errorLoginMsg.value = '';
        if (error.code === 403) {
          ruleFormReference.clearValidate('username');
          ruleFormReference.clearValidate('password');
          errorLoginMsg.value = '请检查用户名和密码是否输入正确'
          return;
        }
        if (error.data && error.data.deadline) {
          errorLoginMsg.value = '登录失败，账号已被临时封禁';
          bannedDeadline.value = error.data.deadline;
          return;
        }
        errorLoginMsg.value = error.msg;
      }).finally(() => {
        loginButtonLoading.value = false;
      })
    } else {
      loginButtonLoading.value = false
      return false;
    }
  })
}

/**
 * 重置表单
 * @param userFrom
 */
const resetForm = (userFrom: FormInstance | undefined) => {
  if (!userFrom) return
  userFrom.resetFields()
}

//图形验证码Base64值
const captchaImage = ref('')
//获取图形验证码是否失败，默认为true
const loadCaptchaError = ref(true);
//图形验证码加载错误信息，初始显示验证码加载中
const loadCaptchaErrorMsg = ref("验证码加载中")
//获取图形验证码，防抖处理，避免频繁点击获取新图形验证码
const getCaptcha = _.debounce(() => {
  getCaptchaImage().then(response => {
    loadCaptchaError.value = false; /* 验证码请求成功后需关闭掉loadCaptchaError */
    ruleFormRef.value.resetFields('captcha'); /* 重置图形验证码输入框的内容 */
    captchaImage.value = response.data.codeImage; /* 从后端获取的验证码图片base64 */
    user.captchaOwner = response.data.codeOwner; /* 从后端获取的随验证码一同生产出来的验证码所属者唯一UUID */
  }).catch(error => {
    console.log(error)
    loadCaptchaError.value = true; /* 验证码请求失败后需开启loadCaptchaError */
    ruleFormRef.value.resetFields('captcha'); /* 重置图形验证码输入框的内容 */
  })
}, 500)
getCaptcha()
</script>

<style scoped>
.login-module {
  max-width: 25rem;
  margin: 0 auto;
}

.logo.el-divider--horizontal {
  border-top: unset !important;
  background: linear-gradient(to right, transparent, #888 55%, #999 30%, transparent);
}

.login-box {
  display: block;
  width: 100%;
  height: 100%;
  margin: 0 auto;
}

.login-banner-body {
  margin-bottom: 30px;
  margin-top: -20px;
}

.login-banner {
  display: block;
  text-align: center;
  padding-top: 10px;
  color: #333;
  font-weight: bolder;
  font-size: 20px;
  letter-spacing: 1px;
}
html.dark .login-banner {
  color: darkgrey;
}

.form-body {
  border: 1px solid #d5d7de;;
  border-radius: 8px;
  padding: 30px 20px 10px 20px;
  background: white;
  box-shadow: 0 0 3px rgb(0 0 0 / 12%);
}
html.dark .form-body {
  border-color: var(--custom-header-bg-color);
  background-color: var(--custom-header-bg-color);
}

.buttonModule {
  padding: 11px !important;
}

.captcha-image {
  width: 50%;
  height: 38px;
  border: 1px solid lavender;
  border-radius: 3px;
}
html.dark .captcha-image {
  border-color: #4c4d4f;
}

.error-load-captcha {
  display: grid;
  text-align: center;
  margin: 0 -10px 0 0;
  width: 100%;
  height: 100%;
  color: #d3691e;
}

.error-login {
  color: #f56c6c;
  line-height: 1.5;
  margin: -10px auto 0;
  text-align: center;
}

.no-account-banner {
  text-align: center;
  margin-top: 10px;
  color: #333;
  font-size: 15px;
}
html.dark .no-account-banner {
  color: darkgrey;
}

.register-now {
  text-decoration: unset;
  color: #347ecc;
  font-weight: 500;
}

.register-now:hover {
  text-decoration: underline;
}
</style>

<style>
.el-form-item {
  margin-bottom: 20px !important;
}

.el-checkbox.el-checkbox--large {
  height: 0 !important;
  margin-top: 10px;
}

.el-loading-spinner .circular {
  width: 25px !important;
  height: 39px !important;
}

.el-checkbox.el-checkbox--large .el-checkbox__label {
  font-weight: normal;
  color: #666;
}

.no-wrap .el-form-item__content {
  flex-wrap: nowrap !important;
  justify-content: center;
}

.logo .el-divider__text {
  background-color: white !important;
}
html.dark .logo .el-divider__text {
  background-color: var(--custom-header-bg-color) !important;
}

.form-body .el-input__wrapper {
  transition: unset;
}

@media screen and (max-width: 768px) {
  .el-dialog.login-success-dialog {
    --el-dialog-width: 95% !important;
  }
}
</style>