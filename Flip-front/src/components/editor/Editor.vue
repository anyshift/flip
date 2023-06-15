<template>
  <div id="vditor"/>
</template>

<script setup lang="ts">
import {ref, onMounted, onBeforeMount, watch} from 'vue';
import Vditor from 'vditor';
import 'vditor/dist/index.css';
import {ElMessage} from "element-plus";
import useThemeStore from '../../stores/themeStore';

const themeStore = useThemeStore();
const theme = ref();
const localTheme = ref();

onBeforeMount(() => {
  localTheme.value = localStorage.getItem('vueuse-color-scheme');
  if (localTheme.value !== '' && localTheme.value !== undefined) {
    theme.value = localTheme.value === 'dark' ? 'dark' : 'classic';
  }
})
onMounted(() => {
  if (localTheme.value === 'auto') {
    theme.value = themeStore.currentTheme;
  }
})
watch(() => themeStore.currentTheme, (New, Old) => {
  if (New === 'dark') {
    editor.value.setTheme('dark', 'dark', 'native');
  } else editor.value.setTheme('classic', 'light', 'emacs');
})

themeStore.$subscribe((mutation, state) => {
  /*console.log(state.currentTheme)*/
})

const toolbar = ref(); //编辑器的按钮
const resize = ref(); //编辑器是否可以调节高度
const windowWidth = ref();
onMounted(() => {
  windowWidth.value = window.innerWidth;
  if (windowWidth.value <= 768) {
    resize.value = false;
    toolbar.value = [
      "bold",
      "link",
      "fullscreen",
      "preview",
    ];
  } else {
    resize.value = true;
    toolbar.value = [
      "emoji",
      "headings",
      "bold",
      "italic",
      "strike",
      "link",
      "|",
      "list",
      "ordered-list",
      "check",
      "outdent",
      "indent",
      "|",
      "quote",
      "line",
      "code",
      "inline-code",
      "insert-before",
      "insert-after",
      "|",
      "upload",
      "record",
      "table",
      "|",
      "undo",
      "redo",
      "|",
      "fullscreen",
      "edit-mode",
      {
        name: "more",
        toolbar: [
          "both",
          "code-theme",
          "content-theme",
          "export",
          "outline",
          "preview",
          "devtools",
          "info",
          /*"help",*/
        ],
      },
    ]
  }
})

const editor = ref<Vditor | null>(null);

const props = defineProps({
  clearContent: Boolean,
  cacheId: {
    type: String,
    default: 'editorCache',
  },
  counterMax: {
    type: Number,
    default: 65536
  },
  height: {
    type: String,
    default: '75vh'
  },
  hljsStyle: {
    type: String,
    default: 'rrt'
  },
  editorPlaceholder: {
    type: String,
    default: '和平交流，理性发言'
  },
  showOutline: {
    type: Boolean,
    default: false
  },
  editingContent: {
    type: String,
    default: ''
  },
})

watch(() => props.clearContent, (New, Old) => {
  if (New) {
    editor.value.setValue('', true);
    emits('clearContentDone');
  }
})

const emits = defineEmits(['contentLengthExceed', 'contentLengthNoExceed', 'clearContentDone'])

defineExpose({
  editor
});

onMounted(() => {
  editor.value = new Vditor('vditor', {
    height: props.height,
    counter: {
      enable: true, /*启用计数器*/
      type: 'markdown', /*计数器统计字数类型*/
      max: props.counterMax, /*允许输入的最大值*/
      after(length: number, counter: { enable: boolean; max?: number; type?: "markdown" | "text" }) {
        if (length > counter.max) {
          ElMessage({
            showClose: true,
            duration: 3000,
            message: '请缩减内容长度',
            type: 'warning',
          })
          emits('contentLengthExceed')
        } else emits('contentLengthNoExceed');
      }
    },
    preview: {
      markdown: {
        autoSpace: true,
        fixTermTypo: true,
      }
    },
    resize: {
      enable: resize.value, /*启用拖拽自定义高度*/
      position: "top", /*拖拽按钮位置*/
    },
    theme: theme.value,
    toolbar: toolbar.value, /*自定义编辑器功能按钮显示*/
    toolbarConfig: {
      hide: false, /*是否隐藏编辑器按钮*/
      pin: false, /*是否固定编辑器按钮*/
    },
    outline: {
      enable: props.showOutline, /*是否展现大纲*/
      position: 'right', /*大纲位置*/
    },
    hint: {
      emoji: { /*自定义emoji图标*/
        "Good": "👍",
        "Bad": "👎",
        "100": "💯",
        "look": "👀",
        "love": "❤️",
        "congratulate": "🎉",
        "rocket": "🚀",
        "white_check_mark": "✅",
        "angry": "😠",
        "anguished": "😧",
        "astonished": "😲",
        "blush": "😊",
        "slightly_smiling_face": "🙂",
        "smile": "😄",
        "confused": "😕",
        "cry": "😢",
        "dizzy_face": "😵",
        "expressionless": "😑",
        "grimacing": "😬",
        "grin": "😁",
        "heart_eyes": "😍",
        "joy": "😂",
        "rofl": "🤣",
        "kissing_heart": "😘",
        "no_mouth": "😶",
        "sunglasses": "😎",
        "sweat_smile": "😅",
        "upside_down_face": "🙃",
      }
    },
    cache: {
      enable: true, /*使用 localStorage 进行缓存*/
      id: props.cacheId
    },
    placeholder: props.editorPlaceholder, /*输入区域为空时的占位符*/
    after: () => {
      editor.value!.setValue(editor.value.html2md(props.editingContent)); /*编辑器默认占位符*/
    },
  });
});
</script>

<style>
html.dark .vditor-reset {
  color: var(--custom-text-color);
}
html.dark .vditor-resize svg {
  color: white;
}

.vditor-resize > div {
  transition: unset;
}

@media screen and (max-width: 768px) {
  .vditor-toolbar {
    padding-left: 6px !important;
  }
  .vditor-toolbar > .vditor-toolbar__item:first-child {
    padding-left: 0;
  }
}
</style>