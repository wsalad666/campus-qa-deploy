<template>
  <div class="rich-text-editor">
    <div class="editor-wrapper">
      <Toolbar
        :editor="editorRef"
        :default-config="toolbarConfig"
        mode="default"
        class="editor-toolbar"
      />
      <Editor
        :default-config="editorConfig"
        v-model="valueHtml"
        mode="default"
        class="editor-body"
        @onCreated="handleCreated"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, shallowRef, watch, onBeforeUnmount } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import type { IDomEditor, IToolbarConfig, IEditorConfig } from '@wangeditor/editor'

const props = defineProps<{
  modelValue: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const editorRef = shallowRef<IDomEditor>()
const valueHtml = ref(props.modelValue)

const toolbarConfig: Partial<IToolbarConfig> = {
  toolbarKeys: [
    'bold',
    'italic',
    'underline',
    '|',
    'header1',
    'header2',
    'header3',
    '|',
    'codeBlock',
    '|',
    'undo',
    'redo',
  ],
}

const editorConfig: Partial<IEditorConfig> = {
  placeholder: '请输入内容...',
  hoverbarKeys: {
    text: {
      menuKeys: ['header1', 'header2', 'header3', 'bold', 'italic', 'underline', 'codeBlock', '|', 'color', 'bgColor'],
    },
  },
}

watch(valueHtml, (val) => {
  emit('update:modelValue', val)
})

watch(
  () => props.modelValue,
  (val) => {
    if (editorRef.value && val !== valueHtml.value) {
      valueHtml.value = val
    }
  }
)

function handleCreated(editor: IDomEditor) {
  editorRef.value = editor
}

onBeforeUnmount(() => {
  editorRef.value?.destroy()
})
</script>

<style scoped>
.rich-text-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
/* 确保编辑器内下拉面板不被裁剪 */
.editor-wrapper {
  position: relative;
  z-index: 1;
}

.editor-toolbar {
  border-bottom: 1px solid #dcdfe6;
}

.editor-body {
  height: 400px;
  overflow-y: auto;
}
/* 确保 wangeditor 下拉和颜色面板不被裁剪 */
:deep(.w-e-text-container) {
  min-height: 300px;
}
</style>

<style>
/* 修复 wangeditor 下拉面板和颜色选择器 */
.w-e-dropdown-panel,
.w-e-panel-container,
.w-e-color-panel,
.w-e-modal {
  z-index: 99999 !important;
}
/* 选中文字弹出的悬浮菜单 */
.w-e-bar {
  z-index: 99999 !important;
}
/* 对话框不裁剪内容 */
.el-dialog__body {
  overflow: visible !important;
}
.el-dialog {
  overflow: visible !important;
}
</style>