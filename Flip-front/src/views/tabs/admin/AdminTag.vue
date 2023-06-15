<template>
  <div class="sys-tag">
    <div class=sys-tag-header>
      <el-input v-model="tagSearch" class="tag-search" size="large" placeholder="搜索标签" :suffix-icon="Search"/>
      <el-button v-text="'新建标签'" type="primary" plain @click="showAddTagDialog = !showAddTagDialog"/>

      <!-- 添加标签对话框 -->
      <el-dialog v-model="showAddTagDialog" title="添加标签" class="tag-dialog" align-center>
        <el-form ref="tagFormRef" :label-position="'right'" label-width="80px" :model="tagForm" :rules="tagRules">
          <el-form-item label="标签名称" prop="name" :error="tagNameErrorText">
            <el-input v-model="tagForm.name" size="large"/>
          </el-form-item>
          <el-form-item label="英文标识" prop="label" :error="tagLabelErrorText">
            <el-input v-model="tagForm.label" size="large"/>
          </el-form-item>
          <el-form-item label="标签图标" prop="icon">
            <el-input v-model="tagForm.icon" size="large"/>
          </el-form-item>
          <el-form-item label="标签类型" prop="option" :error="tagOptionErrorText">
            <el-radio-group v-model="tagForm.option">
              <template v-for="option of tagOptions" :key="option.id">
                <el-radio :label="option.id">{{ option.name }}</el-radio>
              </template>
              <el-button v-if="tagOptions.length === 0" v-text="'添加类型'" @click="showAddTagOptionDialog = !showAddTagOptionDialog"/>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="tagOptions.length !== 0">
            <el-button v-text="'添加类型'" @click="showAddTagOptionDialog = !showAddTagOptionDialog"/>
            <el-button v-text="'管理类型'" @click="openTagOptionManagementDialog"/>
          </el-form-item>
          <el-form-item label="标签描述" prop="detail">
            <el-input v-model="tagForm.detail" size="large" rows="5" type="textarea"/>
          </el-form-item>
          <el-form-item>
            <div>
              <el-button size="default" @click="showAddTagDialog = false">取消</el-button>
              <el-button size="default" @click="handleCreateTag(tagFormRef)" :loading="creatTagLoading">添加</el-button>
            </div>
          </el-form-item>
        </el-form>

        <!-- 添加标签Option对话框的子对话框 -->
        <el-dialog v-model="showAddTagOptionDialog" title="添加标签类型" class="add-tap-option-dialog" append-to-body align-center @open="tagOptionFormRef.resetFields()">
          <el-form ref="tagOptionFormRef" :label-position="'top'" label-width="80px" :model="tagOptionForm" :rules="tagOptionRules">
            <el-form-item label="显示名称" prop="name" :error="newTagOptionNameErrorText">
              <el-input v-model="tagOptionForm.name" size="large" placeholder="例如：生活"/>
            </el-form-item>
            <el-form-item label="英文标识" prop="label" :error="newTagOptionLabelErrorText">
              <el-input v-model="tagOptionForm.label" size="large" placeholder="eg: life"/>
            </el-form-item>
            <el-form-item>
              <div>
                <el-button size="large" @click="showAddTagOptionDialog = false">取消</el-button>
                <el-button size="large" @click="handleCreateTagOption(tagOptionFormRef)" :loading="addTagOptionLoading">添加</el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-dialog>
      </el-dialog>

      <!-- 管理标签Option对话框 -->
      <el-dialog v-model="showManageTagOptionDialog" title="管理标签类型" class="manage-tag-option-dialog" align-center>
        <el-table :data="tagOptions" stripe style="width: 100%" border>
          <el-table-column prop="name" label="类型名称" :width="nameColumnWidth" align="center"/>
          <el-table-column prop="label" label="英文标识" :width="labelColumnWidth" align="center"/>
          <el-table-column label="操作" align="center">
            <template #default="scope">
              <el-button :disabled="scope.row.label === 'other'" size="small" @click="openEditTagOptionDialog(scope.$index, scope.row)">编辑</el-button>
              <el-popconfirm confirm-button-text="是" cancel-button-text="否" title="确定要删除吗?" @confirm="handleDeleteTagOption(scope.$index, scope.row)" :hide-after="50">
                <template #reference>
                  <el-button :disabled="scope.row.label === 'other'" size="small" type="danger" class="delete-option" :loading="deleteTagOptionLoading[scope.row.id]">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <!-- 管理标签Option对话框的（编辑）子对话框 -->
        <el-dialog v-model="showEditTagOptionDialog" class="update-tag-option-dialog" title="编辑标签类型" align-center>
          <el-form ref="editTagOptionFormRef" label-position="right" label-width="80px" :model="editTagOptionForm" :rules="editTagOptionFormRules">
            <el-form-item label="显示名称" prop="name" :error="tagOptionNameErrorText">
              <el-input v-model="editTagOptionForm.name" size="large"></el-input>
            </el-form-item>
            <el-form-item label="英文标识" prop="label" :error="tagOptionLabelErrorText">
              <el-input v-model="editTagOptionForm.label" size="large"></el-input>
            </el-form-item>
            <el-form-item>
              <div>
                <el-button size="default" @click="showEditTagOptionDialog = false">取消</el-button>
                <el-button size="default" @click="updateTagOption(editTagOptionFormRef)" :loading="updateTagOptionLoading">修改</el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-dialog>
      </el-dialog>
    </div>
    <div class=sys-tag-body>
      <table class="tag-table">
        <template v-for="(tag, index) of tags" :key="tag.id">
          <tr v-if="(index % 2) === 0">
            <td>
              <el-card shadow="hover" class="tag-card">
                <div class="tag">
                  <div>
                    <span class="tag-name" v-text="tag.name"/>
                    <span class="tag-label" v-text="tag.label"/>
                  </div>
                  <el-button v-text="'编辑'" @click="handleShowUpdateTagDialog(tag)"/>
                </div>
              </el-card>
            </td>
            <td v-if="(index + 1) < tags.length">
              <el-card shadow="hover" class="tag-card">
                <div class="tag">
                  <div>
                    <span class="tag-name" v-text="tags[index+1].name"/>
                    <span class="tag-label" v-text="tags[index+1].label"/>
                  </div>
                  <el-button v-text="'编辑'" @click="handleShowUpdateTagDialog(tags[index+1])"/>
                </div>
              </el-card>
            </td>
          </tr>
        </template>
      </table>

      <!-- 编辑标签对话框 -->
      <el-dialog v-model="showUpdateTagDialog" title="编辑标签" class="update-tag-dialog" align-center>
        <el-form ref="updateTagFormRef" :label-position="'right'" label-width="80px" :model="updateTagForm" :rules="updateTagFormRules">
          <el-form-item label="标签名称" prop="name" :error="updateTagNameErrorText">
            <el-input v-model="updateTagForm.name" size="large" :disabled="updateTagForm.name === '未分类'"/>
          </el-form-item>
          <el-form-item label="英文标识" prop="label" :error="updateTagLabelErrorText">
            <el-input v-model="updateTagForm.label" size="large" :disabled="updateTagForm.name === '未分类'"/>
          </el-form-item>
          <el-form-item label="标签图标" prop="icon">
            <el-input v-model="updateTagForm.icon" size="large" placeholder="eg: fa-solid fa-flag"/>
          </el-form-item>
          <el-form-item label="标签类型" prop="option" :error="updateTagOptionErrorText">
            <el-radio-group v-model="updateTagForm.option" :disabled="updateTagForm.name === '未分类'">
              <template v-for="option of tagOptions" :key="option.id">
                <el-radio :label="option.id">{{ option.name }}</el-radio>
              </template>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="标签详情" prop="detail">
            <el-input v-model="updateTagForm.detail" size="large"/>
          </el-form-item>
          <el-form-item>
            <div>
              <el-button size="default" @click="showAddTagOptionDialog = false">取消</el-button>
              <el-button size="default" @click="handleUpdateTag(updateTagFormRef)" :loading="updateTagLoading">更新</el-button>
              <el-popconfirm confirm-button-text="是" cancel-button-text="否" title="确定要删除吗?" @confirm="handleDeleteTag" :hide-after="50">
                <template #reference>
                  <el-button size="default" type="danger" :loading="deleteTagLoading[updateTagForm.id]" :disabled="updateTagForm.name === '未分类'">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
          </el-form-item>
        </el-form>
      </el-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, onBeforeMount, onMounted, nextTick, h} from 'vue';
import { Search } from '@element-plus/icons-vue';
import type { FormInstance, FormRules } from 'element-plus';
import {addTagAPI, addTagOptionAPI, updateTagAPI, updateTagOptionAPI, getTagsAndOptionsAPI, deleteTagAPI,
   forceDeleteTagAPI, deleteTagOptionAPI, forceDeleteTagOptionAPI} from '../../../api/admin/tagAPI';
import {ElMessage, ElMessageBox} from "element-plus";
import {classifyTagOptions} from '../../../utils/tags';

const tags = ref([]);
const tagOptions = ref([]);

onBeforeMount(() => {
  getTagsAndOptionsAPI().then(response => {
    console.log(response)
    tagOptions.value = classifyTagOptions(response.data.tagOptions);
    tags.value = response.data.tags;
  }).catch(error => {
    console.log(error);
  })
})

const tagSearch = ref('');
const showAddTagDialog = ref(false);

const creatTagLoading = ref(false);
const tagNameErrorText = ref();
const tagLabelErrorText = ref();
const tagOptionErrorText = ref();
const tagFormRef = ref<FormInstance>();
const tagForm = reactive({
  name: '',
  icon: '',
  label: '',
  option: '',
  detail: '',
})
function validateTagName(rule: any, value: any, callback: any) {
  const reg = /^[\u4E00-\u9FA5A-Za-z0-9]+$/;
  setTimeout(() => {
    if (!reg.test(value)) {
      callback(new Error('不能包含特殊字符'));
    } else callback();
  }, 200)
}
function validateTagLabel(rule: any, value: any, callback: any) {
  const reg = /^[A-Za-z\d\-]+$/;
  setTimeout(() => {
    if (!reg.test(value)) {
      callback(new Error('只能是英文、数字或短横杠'));
    } else callback();
  }, 200)
}
const tagRules = reactive<FormRules>({
  name: [
    {required: true, message: '请输入标签名'},
    {validator: validateTagName, trigger: 'change'}
  ],
  label: [
    {required: true, message: '请输入标签的英文标识'},
    {validator: validateTagLabel, trigger: 'change'}
  ],
  option: [
    {required: true, message: '请选择标签类型'}
  ]
})

function handleCreateTag(formRef: FormInstance | undefined) {
  if (!formRef) return;
  creatTagLoading.value = true;
  setTimeout(() => {
    formRef.validate((isValid) => {
      if (isValid) {
        addTagAPI(tagForm.label, tagForm.name, tagForm.icon, tagForm.option, tagForm.detail).then(response => {
          console.log(response);
          showAddTagDialog.value = false;
          tags.value.push(response.data.newTag);
          ElMessage({
            message: '添加标签成功',
            type: 'success',
          })
          showAddTagDialog.value = false;
        }).catch(error => {
          if (error.data && error.data.name === '标签名不能为空') {
            tagNameErrorText.value = '请输入标签名'; return;
          } else if (error.data && error.data.name === '标签名不能包含特殊字符') {
            tagNameErrorText.value = "不能包含特殊字符"; return;
          } else if (error.msg === '标签名已存在') {
            tagNameErrorText.value = '已存在，请更换新名称'; return;
          }

          if (error.data && error.data.label === '标签英文标识不能为空') {
            tagLabelErrorText.value = '请输入英文标识'; return;
          } else if (error.data && error.data.label === '标签英文标识不能包含特殊字符') {
            tagLabelErrorText.value = '只能是英文、数字或短横杠'; return;
          } else if (error.msg === '标签英文标识已存在') {
            tagLabelErrorText.value = '已存在，请更换新名称'; return;
          }

          if (error.data && error.data.optionId === '标签类型不能为空') {
            tagOptionErrorText.value = '请选择标签类型'; return;
          }

          ElMessage({
            message: "添加失败",
            type: 'error',
          })
          console.log(error)
        }).finally(() => {
          creatTagLoading.value = false;
        })
      } else {
        creatTagLoading.value = false;
      }
    })
  }, 500)
}


const showAddTagOptionDialog = ref(false);
const addTagOptionLoading = ref(false);
const newTagOptionNameErrorText = ref();
const newTagOptionLabelErrorText = ref();
const tagOptionFormRef = ref<FormInstance>();
const tagOptionForm = reactive({
  label: '',
  name: '',
})
function tagOptionLabelValidator(rule: any, value: any, callback: any) {
  const reg = /^[A-Za-z\d\-]+$/;
  setTimeout(() => {
    if (!reg.test(value)) {
      callback(new Error('只能是英文、数字或短横杠'));
    } else callback();
  }, 200)
}
function tagOptionNameValidator(rule: any, value: any, callback: any) {
  const reg = /^[\u4E00-\u9FA5A-Za-z0-9]+$/;
  setTimeout(() => {
    if (!reg.test(value)) {
      callback(new Error('不能是特殊字符'));
    } else callback();
  }, 200)
}
const tagOptionRules = reactive<FormRules>({
  name: [
    {required: true, message: '请输入类型名'},
    {validator: tagOptionNameValidator, trigger: 'change'}
  ],
  label: [
    {required: true, message: '请输入类型英文标识'},
    {validator: tagOptionLabelValidator, trigger: 'change'}
  ],
})
function handleCreateTagOption(formRef: FormInstance | undefined) {
  if (!formRef) return;
  addTagOptionLoading.value = true;
  setTimeout(() => {
    formRef.validate((isValid) => {
      if (isValid) {
        addTagOptionAPI(tagOptionForm.label, tagOptionForm.name).then(response => {
          console.log(response)
          tagOptions.value.push(response.data.newTagOption);
          tagOptions.value = classifyTagOptions(tagOptions.value);
          ElMessage({
            message: '添加标签类型成功',
            type: 'success',
          })
          showAddTagOptionDialog.value = false;
        }).catch(error => {
          console.log(error)

          if (error.data && error.data.name === '类型的显示名称不能为空') {
            newTagOptionNameErrorText.value = "请输入类型名"; return;
          } else if (error.data && error.data.name === '类型的显示名称不能包含特殊字符') {
            newTagOptionNameErrorText.value = "不能是特殊字符"; return;
          } else if (error.msg === '类型显示名称已存在') {
            newTagOptionNameErrorText.value = "已存在，请更换新名称"; return;
          }

          if (error.data && error.data.label === '类型的英文标识不能为空') {
            newTagOptionLabelErrorText.value = "请输入英文标识"; return;
          } else if (error.data && error.data.label === '类型英文标识不能包含特殊字符') {
            newTagOptionLabelErrorText.value = "只能是英文、数字或短横杠"; return;
          } else if (error.msg === '类型英文标识已存在') {
            newTagOptionLabelErrorText.value = "已存在，请更换新名称"; return;
          }

          ElMessage({
            message: '添加标签类型失败',
            type: 'error',
          })
        }).finally(() => {
          addTagOptionLoading.value = false;
        })
      } else {
        addTagOptionLoading.value = false;
      }
    })
  }, 500)
}

const showUpdateTagDialog = ref(false);
const updateTagNameErrorText = ref();
const updateTagLabelErrorText = ref();
const updateTagOptionErrorText = ref();
const updateTagFormRef = ref<FormInstance>();
const updateTagLoading = ref(false);
const updateTagForm = reactive({
  id: '',
  label: '',
  name: '',
  icon: '',
  detail: '',
  option: '',
})
const updateTagFormRules = reactive<FormRules>({
  name: [
    {required: true, message: '请输入标签名'},
    {validator: validateTagName, trigger: 'change'}
  ],
  label: [
    {required: true, message: '请输入标签英文标识'},
    {validator: validateTagLabel, trigger: 'change'}
  ],
  option: [
    {required: true, message: '请选择标签类型'},
  ],
})
function handleShowUpdateTagDialog(tag) {
  updateTagForm.id = tag.id;
  updateTagForm.label = tag.label;
  updateTagForm.name = tag.name;
  updateTagForm.icon = tag.icon;
  updateTagForm.detail = tag.detail;
  updateTagForm.option = tag.tagOption.id;
  showUpdateTagDialog.value = true;
}
function handleUpdateTag(formRef: FormInstance | undefined) {
  if (!formRef) return;
  updateTagLoading.value = true;
  setTimeout(() => {
    formRef.validate((isValid) => {
      if (isValid) {
        updateTagAPI(updateTagForm).then(response => {
          tags.value = response.data.tags;
          ElMessage({
            message: '更新成功',
            type: 'success',
          })
          showUpdateTagDialog.value = false;
          console.log(response)
        }).catch(error => {
          console.log(error)

          if (error.data && error.data.name === '标签名不能为空') {
            updateTagNameErrorText.value = '请输入标签名'; return;
          } else if (error.data && error.data.name === '标签名不能包含特殊字符') {
            updateTagNameErrorText.value = "不能包含特殊字符"; return;
          } else if (error.msg === '标签名已存在') {
            updateTagNameErrorText.value = '已存在，请重新输入'; return;
          }

          if (error.data && error.data.label === '标签英文标识不能为空') {
            updateTagLabelErrorText.value = '请输入英文标识'; return;
          } else if (error.data && error.data.label === '标签英文标识不能包含特殊字符') {
            updateTagLabelErrorText.value = '只能输入英文字符'; return;
          } else if (error.msg === '标签英文标识已存在') {
            updateTagLabelErrorText.value = '已存在，请重新输入'; return;
          }

          if (error.data && error.data.optionId === '标签类型不能为空') {
            updateTagOptionErrorText.value = '请选择标签类型'; return;
          }

          ElMessage({
            message: "更新失败",
            type: 'error',
          })
        }).finally(() => {
          updateTagLoading.value = false;
        })
      } else {
        updateTagLoading.value = false;
      }
    })
  }, 500)
}

const nameColumnWidth = ref(200);
const labelColumnWidth = ref(200);
const windowWidth = ref()
onMounted(() => {
  windowWidth.value = window.innerWidth;
  if (windowWidth.value < 768) {
    nameColumnWidth.value = 90;
    labelColumnWidth.value = 120;
  }
  window.onresize = () => {
    windowWidth.value = window.innerWidth;
    if (windowWidth.value < 768) {
      nameColumnWidth.value = 90;
      labelColumnWidth.value = 120;
    } else {
      nameColumnWidth.value = 200;
      labelColumnWidth.value = 200;
    }
  }
})


const showManageTagOptionDialog = ref(false);
function openTagOptionManagementDialog() {
  showAddTagDialog.value = false;
  showAddTagOptionDialog.value = false;
  showManageTagOptionDialog.value = true;
}


const showEditTagOptionDialog = ref();
const tagOptionNameErrorText = ref();
const tagOptionLabelErrorText = ref();
const updateTagOptionLoading = ref();
const tempTagOption = ref();
const editTagOptionFormRef = ref<FormInstance>();
const editTagOptionForm = reactive({
  id: '',
  name: '',
  label: '',
})
const editTagOptionFormRules = reactive<FormRules>({
  name: [
    {required: true, message: '请输入类型显示名称'},
    {validator: tagOptionNameValidator, trigger: 'change'}
  ],
  label: [
    {required: true, message: '请输入标签英文标识'},
    {validator: tagOptionLabelValidator, trigger: 'change'}
  ]
})
function openEditTagOptionDialog(index: number, row)  {
  showEditTagOptionDialog.value = true;
  tempTagOption.value = row;
  editTagOptionForm.id = row.id;
  editTagOptionForm.name = row.name;
  editTagOptionForm.label = row.label;
}
function updateTagOption(formRef: FormInstance | undefined) {
  if (!formRef) return;
  updateTagOptionLoading.value = true;
  setTimeout(() => {
    formRef.validate((isValid) => {
      if (isValid) {
        if (editTagOptionForm.name === tempTagOption.value.name && editTagOptionForm.label === tempTagOption.value.label) {
          ElMessage({
            message: '类型无变化',
            type: 'info',
          })
          updateTagOptionLoading.value = false;
          showEditTagOptionDialog.value = false;
        } else {
          updateTagOptionAPI(editTagOptionForm).then(response => {
            ElMessage({
              message: '更新类型成功',
              type: 'success',
            })
            nextTick(() => {
              tempTagOption.value.name = response.data.newOption.name;
              tempTagOption.value.label = response.data.newOption.label;
            })
            showEditTagOptionDialog.value = false;
            console.log(response)
          }).catch(error => {
            tempTagOption.value = null;
            ElMessage({
              message: '更新类型失败',
              type: 'error',
            })
            if (error.data && error.data.name === '类型的显示名称不能为空') {
              tagOptionNameErrorText.value = "请输入显示名称";
            } else if (error.data && error.data.name === '类型的显示名称不能包含特殊字符') {
              tagOptionNameErrorText.value = "不能是特殊字符";
            }
            if (error.data && error.data.label === '类型的英文标识不能为空') {
              tagOptionLabelErrorText.value = "请输入英文标识";
            } else if (error.data && error.data.label === '类型英文标识不能包含特殊字符') {
              tagOptionLabelErrorText.value = "只能是英文、数字或短横杠";
            }
            console.log(error)
          }).finally(() => {
            updateTagOptionLoading.value = false;
          })
        }
      } else {
        updateTagOptionLoading.value = false;
      }
    })
  }, 500)
}

const deleteTagLoading = ref([])
function handleDeleteTag() {
  deleteTagLoading.value[updateTagForm.id] = true;
  setTimeout(() => {
    deleteTagAPI(updateTagForm).then(response => {
      console.log(response)
      tags.value = response.data.tags;
      showUpdateTagDialog.value = false;
      ElMessage({
        type: 'success',
        message: `删除标签成功`,
      })
    }).catch(error => {
      if (error.msg === "该标签已存在主题绑定") {
        console.log(error);
        ElMessageBox.confirm('该标签已存在主题绑定', '删除失败', {
              confirmButtonText: '强制删除',
              cancelButtonText: '取消删除',
              type: 'error',
            }
        ).then(() => {
          ElMessageBox.prompt('强制删除后，已绑定到该标签的主题将分类至「未分类」标签', '强制删除提醒', {
            confirmButtonText: '强制删除',
            cancelButtonText: '取消删除',
            inputPlaceholder: '请输入：确定强制删除该标签',
            inputValidator(value) {
              if (value !== '确定强制删除该标签') {
                return "请输入：确定强制删除该标签";
              }
            },
            beforeClose: (action, instance, done) => {
              if (action === 'confirm') {
                instance.confirmButtonLoading = true
                instance.confirmButtonText = '删除中...'
                setTimeout(() => {
                  forceDeleteTagAPI(updateTagForm).then(resp => {
                    console.log(resp)
                    tags.value = resp.data.tags;
                    showUpdateTagDialog.value = false;
                    done();
                    ElMessage({
                      type: 'success',
                      message: `已删除该标签`,
                    })
                  }).catch(err => {
                    console.log(err)
                    instance.confirmButtonText = '强制删除';
                    ElMessage({
                      type: 'error',
                      message: `删除标签失败`,
                    })
                  }).finally(() => {
                    instance.confirmButtonLoading = false;
                  })
                }, 500)
              } else done();
            },
          }).then(({ value }) => {}).catch(() => {}) //catch的是cancel后的结果
        }).catch(() => {}) //catch的是cancel后的结果
        return;
      }
      ElMessage({
        type: 'error',
        message: error.msg,
      })
    }).finally(() => {
      deleteTagLoading.value[updateTagForm.id] = false;
    })
  }, 500)
}

const deleteTagOptionLoading = ref([])
function handleDeleteTagOption (index: number, row) {
  deleteTagOptionLoading.value[row.id] = true;
  setTimeout(() => {
    deleteTagOptionAPI(row).then(response => {
      console.log(response)
      tagOptions.value = classifyTagOptions(response.data.tagOptions);
      showUpdateTagDialog.value = false;
      ElMessage({
        type: 'success',
        message: `已删除该类型`,
      })
    }).catch(error => {
      if (error.msg === "当前类型已存在标签绑定") {
        console.log(error);
        ElMessageBox.confirm('当前类型已存在标签绑定', '删除失败', {
              confirmButtonText: '强制删除',
              cancelButtonText: '取消删除',
              type: 'error',
        }).then(() => {
          ElMessageBox.prompt('强制删除后，已绑定到该类型的标签将移动至「其它」分类', '强制删除提醒', {
            confirmButtonText: '强制删除',
            cancelButtonText: '取消删除',
            inputPlaceholder: '请输入：确定强制删除该类型',
            inputValidator(value) {
              if (value !== '确定强制删除该类型') {
                return "请输入：确定强制删除该类型";
              }
            },
            beforeClose: (action, instance, done) => {
              if (action === 'confirm') {
                instance.confirmButtonLoading = true
                instance.confirmButtonText = '删除中...'
                setTimeout(() => {
                  forceDeleteTagOptionAPI(row).then(resp => {
                    console.log(resp)
                    tags.value = resp.data.tags;
                    tagOptions.value = classifyTagOptions(resp.data.tagOptions);
                    showUpdateTagDialog.value = false;
                    done();
                    ElMessage({
                      type: 'success',
                      message: `已删除该类型`,
                    })
                  }).catch(err => {
                    console.log(err)
                    instance.confirmButtonText = '强制删除'
                    ElMessage({
                      type: 'error',
                      message: `删除类型失败`,
                    })
                  }).finally(() => {
                    instance.confirmButtonLoading = false
                  })
                }, 500)
              } else done();
            },
          }).then(({ value }) => {}).catch(() => {}) //catch的是cancel后的结果
        }).catch(() => {}) //catch的是cancel后的结果
        return;
      }
      ElMessage({
        type: 'error',
        message: error.msg,
      })
    }).finally(() => {
      deleteTagOptionLoading.value[row.id] = false;
    })
  }, 500)
}
</script>

<style scoped>
.sys-tag {
  padding: 12px;
}

.sys-tag-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 12px 24px;
  border-bottom: 1px solid #f2f2f2;
}
html.dark .sys-tag-header {
  border-bottom-color: #4c4d4f;
}

.sys-tag-body {
  padding: 24px 2px;
}

.tag-table {
  width: 100%;
  border-spacing: 10px 0;
}
.tag-table td {
  width: 50%;
}
@media only screen and (max-width: 768px) {
  .tag-table td {
    width: 100%;
    display: block;
  }
}

.tag-card {
  margin-bottom: 10px;
  transition: unset !important;
}

.tag-card .tag {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tag-card .tag-name {
  font-size: 15px;
  margin-right: 10px;
}

.tag-card .tag-label {
  color: grey;
}

.tag-search {
  width: 200px;
}
</style>

<style>
@media only screen and (min-width: 1650px) {
  .tag-dialog, .manage-tag-option-dialog, .add-tap-option-dialog,
  .update-tag-option-dialog, .update-tag-dialog {
    --el-dialog-width: 38% !important;
  }
}

@media only screen and (min-width: 1550px) and (max-width: 1650px) {
  .tag-dialog, .manage-tag-option-dialog, .add-tap-option-dialog,
  .update-tag-option-dialog, .update-tag-dialog {
    --el-dialog-width: 45% !important;
  }
}

@media only screen and (min-width: 1200px) and (max-width: 1350px) {
  .tag-dialog, .manage-tag-option-dialog, .add-tap-option-dialog,
  .update-tag-option-dialog, .update-tag-dialog {
    --el-dialog-width: 55% !important;
  }
}

@media only screen and (min-width: 992px) and (max-width: 1200px) {
  .tag-dialog, .manage-tag-option-dialog, .add-tap-option-dialog,
  .update-tag-option-dialog, .update-tag-dialog {
    --el-dialog-width: 62% !important;
  }
}

@media only screen and (min-width: 768px) and (max-width: 992px) {
  .tag-dialog, .manage-tag-option-dialog, .add-tap-option-dialog,
  .update-tag-option-dialog, .update-tag-dialog {
    --el-dialog-width: 75% !important;
  }
}

@media only screen and (max-width: 768px) {
  .tag-dialog, .manage-tag-option-dialog, .add-tap-option-dialog,
  .update-tag-option-dialog, .update-tag-dialog {
    --el-dialog-width: 100% !important;
  }
  .delete-option {
    margin-left: 5px !important;
  }
}

.sys-tag-header .el-input__wrapper {
  transition: unset;
}
</style>