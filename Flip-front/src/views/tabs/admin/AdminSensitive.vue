<template>
  <div>
    <div class="box-header">
      <div>
        <el-input v-model="inputVal" placeholder="请输入敏感词" size="large" class="sensitiveWord-input">
          <template #prepend>
            <el-select v-model="selectVal" placeholder="选择" style="width: 85px" size="large">
              <el-option label="添加" value="add" />
              <el-option label="搜索" value="search" />
            </el-select>
          </template>
          <template #append>
            <el-button :icon="(selectVal === 'add') ? Pointer : Search" type="primary" plain
                       :loading="unitButtonLoading" @click="handleClickButton"/>
          </template>
        </el-input>
        <div style="text-align: center;">
          <el-text type="warning">所有涉及敏感词的内容都会被过滤和替换</el-text>
        </div>
      </div>
    </div>
    <div class="box-body">
      <table class="sensitiveWord-table" :style="tableWidth()">
        <template v-for="(item, index) of sensitiveWords" :key="index">
          <tr v-if="(index % 5) === 0">
            <td :style="tdWidth()">
              <el-card shadow="hover" class="sensitiveWord-card">
                <div class="sensitiveWord">
                  <div class="word">
                    <el-text truncated v-text="item.word"/>
                  </div>
                  <el-button v-text="'编辑'" class="edit-button" size="small" @click="handleShowUpdateSensitiveDialog(item)"/>
                </div>
              </el-card>
            </td>
            <td v-if="(index + 1) < sensitiveWords.length" :style="tdWidth()">
              <el-card shadow="hover" class="sensitiveWord-card">
                <div class="sensitiveWord">
                  <div class="word">
                    <el-text truncated v-text="sensitiveWords[index+1].word"/>
                  </div>
                  <el-button v-text="'编辑'" class="edit-button" size="small"
                             @click="handleShowUpdateSensitiveDialog(sensitiveWords[index+1])"/>
                </div>
              </el-card>
            </td>
            <td v-if="(index + 2) < sensitiveWords.length" :style="tdWidth()">
              <el-card shadow="hover" class="sensitiveWord-card">
                <div class="sensitiveWord">
                  <div class="word">
                    <el-text truncated v-text="sensitiveWords[index+2].word"/>
                  </div>
                  <el-button v-text="'编辑'" class="edit-button" size="small"
                             @click="handleShowUpdateSensitiveDialog(sensitiveWords[index+2])"/>
                </div>
              </el-card>
            </td>
            <td v-if="(index + 3) < sensitiveWords.length" :style="tdWidth()">
              <el-card shadow="hover" class="sensitiveWord-card">
                <div class="sensitiveWord">
                  <div class="word">
                    <el-text truncated v-text="sensitiveWords[index+3].word"/>
                  </div>
                  <el-button v-text="'编辑'" class="edit-button" size="small"
                             @click="handleShowUpdateSensitiveDialog(sensitiveWords[index+3])"/>
                </div>
              </el-card>
            </td>
            <td v-if="(index + 4) < sensitiveWords.length" :style="tdWidth()">
              <el-card shadow="hover" class="sensitiveWord-card">
                <div class="sensitiveWord">
                  <div class="word">
                    <el-text truncated v-text="sensitiveWords[index+4].word"/>
                  </div>
                  <el-button v-text="'编辑'" class="edit-button" size="small"
                             @click="handleShowUpdateSensitiveDialog(sensitiveWords[index+4])"/>
                </div>
              </el-card>
            </td>
          </tr>
        </template>
      </table>
    </div>
    <el-empty :description="(sensitiveWords.length === 0) ? '还没有敏感词' : '没有更多了'"/>

    <el-dialog v-model="showUpdateSensitiveDialog" title="编辑敏感词" class="update-sensitiveWord-dialog" align-center @closed="sensitiveDialogObj = {}">
      <el-form label-position="left" label-width="55px" :model="updateSensitiveForm" size="large">
        <el-form-item label="原内容">
          <el-input disabled v-model="sensitiveDialogObj.word"/>
        </el-form-item>
        <el-form-item label="新内容">
          <el-input v-model="updateSensitiveForm.word"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" plain @click="showUpdateSensitiveDialog = false" size="default">取消</el-button>
          <el-button type="primary" plain :loading="updateSensitiveLoading" @click="handleUpdateSensitive" size="default">更新</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import {onBeforeMount, reactive, ref, watch} from 'vue';
import {Pointer, Search} from '@element-plus/icons-vue';
import {addSensitiveWordAPI, getSensitiveWordsAPI, updateSensitiveWordAPI} from "../../../api/admin/sensitiveAPI";
import {ElMessage} from "element-plus";

const inputVal = ref();
const selectVal = ref("add");
const sensitiveWords = ref([]);

onBeforeMount(() => {
  getSensitiveWordsAPI().then(response => {
    console.log(response)
    sensitiveWords.value = response.data.sensitiveWords;
  }).catch(error => {
    console.log(error)
  })
})

const unitButtonLoading = ref(false)
function handleClickButton() {
  unitButtonLoading.value = true;
  setTimeout(() => {
    if (selectVal.value === 'add') {
      if (inputVal.value) {
        addSensitiveWordAPI(inputVal.value).then(response => {
          console.log(response)
          sensitiveWords.value.push(response.data.sensitiveWord);
          inputVal.value = '';
          ElMessage({
            message: "添加敏感词成功",
            type: 'success',
          })
        }).catch(error => {
          console.log(error)
          ElMessage({
            message: "添加敏感词失败",
            type: 'error',
          })
        }).finally(() => {
          unitButtonLoading.value = false;
        })
      } else {
        unitButtonLoading.value = false;
        ElMessage({
          message: "请输入敏感词",
          type: 'warning',
        })
      }
    } else {
      setTimeout(() => {
        unitButtonLoading.value = false;
      }, 500)
    }
  }, 500)
}

const sensitiveDialogObj = ref({})
const showUpdateSensitiveDialog = ref(false);
const updateSensitiveForm = reactive({
  id: -1,
  word: '',
})
function handleShowUpdateSensitiveDialog(sensitiveWord) {
  showUpdateSensitiveDialog.value = true;
  updateSensitiveForm.id = sensitiveWord.id;
  updateSensitiveForm.word = sensitiveWord.word;
  sensitiveDialogObj.value = sensitiveWord;
}

const updateSensitiveLoading = ref(false);
function handleUpdateSensitive() {
  updateSensitiveLoading.value = true;
  setTimeout(() => {
    updateSensitiveWordAPI(updateSensitiveForm).then(response => {
      console.log(response)
      sensitiveDialogObj.value.word = response.data.sensitiveWord.word;
      showUpdateSensitiveDialog.value = false;
      ElMessage({
        message: "更新敏感词成功",
        type: 'success',
      })
    }).catch(error => {
      console.log(error)
    }).finally(() => {
      updateSensitiveLoading.value = false;
    })
  }, 500)
}


function tableWidth() {
  let length = sensitiveWords.value.length;
  if (length === 1) {
    return 'width: 20%';
  } else if (length === 2) {
    return 'width: 40%';
  } else if (length === 3) {
    return 'width: 60%'
  } else if (length === 4) {
    return 'width: 80%'
  } else if (length >= 5) {
    return 'width: 100%'
  }
}

function tdWidth() {
  let length = sensitiveWords.value.length;
  if (length === 1) {
    return 'width: 20%';
  } else if (length === 2) {
    return 'width: 50%';
  } else if (length === 3) {
    return 'width: 33.33%'
  } else if (length === 4) {
    return 'width: 25%'
  } else if (length >= 5) {
    return 'width: 20%'
  }
}
</script>

<style scoped>
.box-header {
  min-height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #E4E7ED;
  border-radius: 8px 8px 0 0;
  /*background: radial-gradient(#e4e7e9 4px,transparent 5px),radial-gradient(#000000 3px,transparent 4px),linear-gradient(#e8ebed 4px,transparent 0),linear-gradient(45deg,transparent 74px,transparent 75px,#a4a4a4 75px,#afb0b2 76px,transparent 77px,transparent 109px),linear-gradient(-45deg,transparent 75px,transparent 76px,#425e6b 76px,#e4e7e9 77px,transparent 78px,transparent 109px),#e8ebed;
  background-size: 109px 109px,109px 109px,100% 6px,109px 109px,109px 109px;*/
}
html.dark .box-header {
  /*background: radial-gradient(#232830 3px,transparent 4px),radial-gradient(#000000 3px,transparent 4px),linear-gradient(#232830 4px,transparent 0),linear-gradient(45deg,transparent 74px,transparent 75px,#425560 75px,#3d5966 76px,transparent 77px,transparent 109px),linear-gradient(-45deg,transparent 75px,transparent 76px,#425560 76px,#425560 77px,transparent 78px,transparent 109px),#232830;
  background-size: 109px 109px,109px 109px,100% 6px,109px 109px,109px 109px;*/
  border: 1px solid #333942;
}

.box-body {
  display: flex;
  padding: 25px 4px 10px 10px;
  align-items: center;
  justify-content: space-between;
}

.sensitiveWord {
  display: flex;
  justify-content: center;
  align-items: center;
}

.sensitiveWord-table {
  width: 100%;
}

.edit-button {
  margin-left: 8px;
}

.sensitiveWord-card {
  transition: unset;
}

.sensitiveWord-card {
  margin-right: 6px;
  margin-bottom: 6px;
}

.sensitiveWord-input {
  margin-bottom: 10px;
}

.word {
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 90px;
  margin-bottom: 2px;
}

@media only screen and (max-width: 992px) {
  .sensitiveWord-table td {
    width: 100% !important;
    display: block;
  }
  .sensitiveWord-card {
    margin-right: unset;
    max-width: unset;
  }
  .sensitiveWord {
    justify-content: space-between;
  }
  .word {
    max-width: 200px;
  }
  tr > td:not(:last-child) > .sensitiveWord-card {
    margin-right: unset;
  }
  .box-body {
    padding: 10px 5px;
  }
  .box-header {
    min-height: 150px;
    border-radius: 0;
  }
}

</style>

<style>
.sensitiveWord-input .el-input__wrapper {
  transition: unset !important;
}

@media only screen and (min-width: 1650px) {
  .update-sensitiveWord-dialog {
    --el-dialog-width: 38% !important;
  }
}

@media only screen and (min-width: 1550px) and (max-width: 1650px) {
  .update-sensitiveWord-dialog {
    --el-dialog-width: 45% !important;
  }
}

@media only screen and (min-width: 1200px) and (max-width: 1350px) {
  .update-sensitiveWord-dialog {
    --el-dialog-width: 55% !important;
  }
}

@media only screen and (min-width: 992px) and (max-width: 1200px) {
  .update-sensitiveWord-dialog {
    --el-dialog-width: 62% !important;
  }
}

@media only screen and (min-width: 768px) and (max-width: 992px) {
  .update-sensitiveWord-dialog {
    --el-dialog-width: 75% !important;
  }
}

@media only screen and (max-width: 768px) {
  .update-sensitiveWord-dialog {
    --el-dialog-width: 100% !important;
  }
}
</style>