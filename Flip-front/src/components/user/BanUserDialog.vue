<template>
  <el-dialog v-model="dialogModel" title="封禁用户" class="ban-user-dialog">
    <el-form label-width="80px" ref="banFormRef" :model="banForm" :rules="banRules">
      <el-form-item label="封禁用户">
        <span v-text="props.user.nickname" style="margin-right: 8px; color: #ff8c00;"/>
        <span v-text="'@' + props.user.username" style="color: #9acd31;"/>
      </el-form-item>
      <el-form-item label="封禁时长" prop="datetime">
        <el-date-picker
            v-model="banForm.datetime"
            type="datetime"
            placeholder="截止日期和时间"
            :shortcuts="shortcuts"
            value-format="YYYY-MM-DD HH:mm:ss"
            size="large"
        />
      </el-form-item>
      <el-form-item label="封禁缘由" prop="reason">
        <el-input
            :rows="2"
            v-model="banForm.reason"
            type="textarea"
            placeholder="请输入封禁缘由"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="bannedLoading" size="default" @click="handleBanUser(banFormRef)">封禁</el-button>
        <span v-text="'封禁后，该用户在截止日期前将无法登录'" class="hidden-xs-only" style="margin-left: 10px"/>
      </el-form-item>
    </el-form>
  </el-dialog>
</template>

<script setup lang="ts">
import {computed, reactive, ref} from "vue";
import type {FormInstance, FormRules} from 'element-plus';
import {bannedUserAPI} from '../../api/admin/userSysAPI'
import {ElMessage} from "element-plus";

const props = defineProps({
  showBanUserDialog: Boolean,
  user: Object,
})
const emits = defineEmits(['closeBanUserDialog', 'bannedUser'])

const dialogModel = computed({
  get() {
    return props.showBanUserDialog;
  },
  set() {
    emits('closeBanUserDialog');
    //关闭dialog时的set动作设置为emit到父组件，而不是默认设置props.showNoLoginDialog的值为false
  }
})

const shortcuts = [
  {
    text: '1 天',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24)
      return date
    },
  },
  {
    text: '3 天',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24 * 3)
      return date
    },
  },
  {
    text: '7 天',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24 * 7)
      return date
    },
  },
  {
    text: '半个月',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24 * 15)
      return date
    },
  },
  {
    text: '一个月',
    value: () => {
      const date = new Date()
      date.setTime(date.getTime() + 3600 * 1000 * 24 * 30)
      return date
    },
  },
  {
    text: '永久封禁',
    value: new Date(2099, 11, 31, 23, 59, 59),
  },
]

const banFormRef = ref<FormInstance>();
const banForm = reactive({
  uid: '',
  datetime: '',
  reason: '',
})
const banRules = reactive<FormRules>({
  datetime: [
    { required: true, message: '请选择截止时间' },
  ],
  reason: [
    { required: true, message: '请输入封禁缘由' },
  ],
})

const bannedLoading = ref(false);
function handleBanUser(formEl: FormInstance | undefined) {
  if (!formEl) return
  bannedLoading.value = true;
  formEl.validate((valid, fields) => {
    if (valid) {
      banForm.uid = props.user.uid;
      setTimeout(() => {
        bannedUserAPI(banForm).then(response => {
          console.log(response)
          emits('closeBanUserDialog');
          emits('bannedUser', props.user.id);
          ElMessage({
            message: '封禁成功',
            type: 'success'
          })
        }).catch(error => {
          console.log(error)
          ElMessage({
            message: '封禁失败',
            type: 'error'
          })
        }).finally(() => {
          bannedLoading.value = false;
        })
      }, 500)
    } else {
      bannedLoading.value = false;
    }
  })
}
</script>

<style scoped>

</style>

<style>
@media only screen and (min-width: 1650px) {
  .ban-user-dialog {
    --el-dialog-width: 38% !important;
  }
}

@media only screen and (min-width: 1550px) and (max-width: 1650px) {
  .ban-user-dialog {
    --el-dialog-width: 45% !important;
  }
}

@media only screen and (min-width: 1200px) and (max-width: 1350px) {
  .ban-user-dialog {
    --el-dialog-width: 55% !important;
  }
}

@media only screen and (min-width: 992px) and (max-width: 1200px) {
  .ban-user-dialog {
    --el-dialog-width: 62% !important;
  }
}

@media only screen and (min-width: 768px) and (max-width: 992px) {
  .ban-user-dialog {
    --el-dialog-width: 75% !important;
  }
}

@media only screen and (max-width: 768px) {
  .ban-user-dialog {
    --el-dialog-width: 100% !important;
  }
}
</style>