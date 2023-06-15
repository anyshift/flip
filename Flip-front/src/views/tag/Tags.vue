<template>
  <el-space direction="vertical" :size="20" fill style="width: 100%;">
    <template v-for="(tagOption, index) of tagOptions" :key="tagOption.id">
      <div class="tag-option-box" :id="tagOption.name">
        <div class="tag-option-box-header" v-text="tagOption.name" @click="router.push({hash: '#' + tagOption.name})"/>
        <div class="tag-option-box-body">
          <table class="tag-option-table" :style="tableWidth(tags[tagOption.label])">
            <template v-for="(tag, index) of tags[tagOption.label]">
              <tr v-if="(index % 3) === 0 && tag.optionId === tagOption.id">
                <td>
                  <div class="tag-flex">
                    <router-link :to="{path: '/t/' + tag.label}" class="tag-a">
                      <font-awesome-icon v-if="tag.icon" :icon="tag.icon" class="tag-icon"/>
                      <font-awesome-icon v-else icon="fa-solid fa-mug-hot" class="tag-icon"/>
                    </router-link>
                    <div>
                      <router-link :to="{path: '/t/' + tag.label}" class="tag-label">
                        <div v-text="tag.name" class="tag-name"></div>
                      </router-link>
                      <div style="margin-top: 5px;">
                        <el-tag size="small" type="info">今日 0</el-tag>
                        <el-divider direction="vertical" border-style="dashed"/>
                        <el-tag  size="small" type="info">总计 10</el-tag>
                      </div>
                    </div>
                  </div>
                </td>
                <td v-if="(index + 1) < tags[tagOption.label].length">
                  <div class="tag-flex">
                    <router-link :to="{path: '/t/' + tags[tagOption.label][index+1].label}" class="tag-a">
                      <font-awesome-icon v-if="tags[tagOption.label][index+1].icon" :icon="tags[tagOption.label][index+1].icon" class="tag-icon"/>
                      <font-awesome-icon v-else icon="fa-solid fa-mug-hot" class="tag-icon"/>
                    </router-link>
                    <div>
                      <router-link :to="{path: '/t/' + tags[tagOption.label][index+1].label}" class="tag-label">
                        <div v-text="tags[tagOption.label][index+1].name" class="tag-name"></div>
                      </router-link>
                      <div style="margin-top: 5px;">
                        <el-tag size="small" type="info">今日 0</el-tag>
                        <el-divider direction="vertical" border-style="dashed"/>
                        <el-tag  size="small" type="info">总计 10</el-tag>
                      </div>
                    </div>
                  </div>
                </td>
                <td v-if="(index + 2) < tags[tagOption.label].length">
                  <div class="tag-flex">
                    <router-link :to="{path: '/t/' + tags[tagOption.label][index+2].label}" class="tag-a">
                      <font-awesome-icon v-if="tags[tagOption.label][index+2].icon" :icon="tags[tagOption.label][index+2].icon" class="tag-icon"/>
                      <font-awesome-icon v-else icon="fa-solid fa-mug-hot" class="tag-icon"/>
                    </router-link>
                    <div>
                      <router-link :to="{path: '/t/' + tags[tagOption.label][index+2].label}" class="tag-label">
                        <div v-text="tags[tagOption.label][index+2].name" class="tag-name"></div>
                      </router-link>
                      <div style="margin-top: 5px;">
                        <el-tag size="small" type="info">今日 0</el-tag>
                        <el-divider direction="vertical" border-style="dashed"/>
                        <el-tag  size="small" type="info">总计 10</el-tag>
                      </div>
                    </div>
                  </div>
                </td>
              </tr>
            </template>
          </table>
        </div>
      </div>
    </template>
  </el-space>
</template>

<script setup>
import {ref, onMounted, watch} from "vue";
import {getTagsAndOptionsAPI} from "../../api/admin/tagAPI";
import {classifyTags, classifyTagOptions} from "../../utils/tags";
import {useRouter} from "vue-router";

const router = useRouter();
const tagOptions = ref([]);
const tags = ref({});

onMounted(() => {
  getTagsAndOptionsAPI().then(response => {
    console.log(response)
    tagOptions.value = classifyTagOptions(response.data.tagOptions);
    tags.value = classifyTags(response.data.tags, tagOptions.value);
  }).catch(error => {
    console.log(error);
  })
})

/**
 * 当某个tagOption下只有两个tag时，限制宽度为2/3，不然会1/2。
 * @param tagOpt 迭代出来的tagOption下的tag数组
 * @returns {string}
 */
function tableWidth(tagOpt) {
  if (tagOpt && tagOpt.length === 2) {
    return 'width: 66.66%';
  }
}
</script>

<style scoped>
.tag-option-box {
  background: #fff;
  border-radius: 3px;
  border: 1px solid #a4cf8d;
}
html.dark .tag-option-box {
  border-color: var(--custom-trend-header-bg-color);
}

.tag-option-box-header {
  background: #dfeed7;
  border-bottom: dashed 1px #a4cf8d;
  padding: 8px 12px;
  font-size: 15px;
}
html.dark .tag-option-box-header {
  background: var(--custom-trend-header-bg-color);
  border-bottom-color: var(--custom-trend-header-bg-color);
}

.tag-option-box-body {
  padding: 0 12px;
}
html.dark .tag-option-box-body {
  background-color: var(--el-bg-color-overlay);
}

.tag-option-table {
  width: 100%;
}

.tag-option-table td {
  width: 33.33%;
}
@media only screen and (max-width: 768px) {
  .tag-option-table td {
    width: 100%;
    display: flex;
  }
  .tag-option-table > tr > td {
    border-bottom: 1px solid var(--custom-trend-td-bottom-color);
  }
  .tag-option-table > tr:last-child > td:last-child {
    border-bottom: 0;
  }
}

.tag-flex {
  display: flex;
  padding: 25px 0;
}

@media only screen and (min-width: 768px) {
  .tag-option-table > tr:not(:last-child) .tag-flex {
    border-bottom: 1px solid var(--custom-trend-td-bottom-color);
  }
}

.tag-name {
  color: #333;
  font-size: 16px;
  margin-top: -1px;
}
html.dark .tag-name {
  color: var(--custom-tabs-color);
}
html.dark .tag-name:hover {
  color: var(--el-menu-active-color);
}

.tag-icon {
  width: 50px;
  color: #cdd6df;
  font-size: 50px;
  margin-right: 15px;
}
html.dark .tag-icon {
  color: #696f79;
}

.tag-label {
  color: #333;
  text-decoration: unset;
}
.tag-label:hover {
  text-decoration: underline;
}
</style>