<template>
  <el-dialog
    :model-value="modelValue"
    title="禁言用户"
    width="480px"
    :close-on-click-modal="false"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form :model="form" label-width="100px">
      <el-form-item label="目标用户">
        <el-tag type="warning">{{ userNickname }}</el-tag>
      </el-form-item>
      <el-form-item label="禁言等级" required>
        <el-radio-group v-model="form.banType">
          <el-radio :value="1">轻度（3天）</el-radio>
          <el-radio :value="2">中度（7天）</el-radio>
          <el-radio :value="3">重度（永久）</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="禁言原因" required>
        <el-input
          v-model="form.banReason"
          type="textarea"
          :rows="3"
          placeholder="请输入禁言原因"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="danger" :loading="submitting" @click="handleSubmit">确认禁言</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'

const props = defineProps<{
  modelValue: boolean
  userId: number
  userNickname: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'done': []
}>()

const submitting = ref(false)

const form = reactive({
  banType: 1 as number,
  banReason: '',
})

watch(() => props.modelValue, (v) => {
  if (v) {
    form.banType = 1
    form.banReason = ''
  }
})

async function handleSubmit() {
  if (!form.banReason.trim()) {
    ElMessage.warning('请输入禁言原因')
    return
  }
  submitting.value = true
  try {
    await adminApi.banUser({
      userId: props.userId,
      banType: form.banType,
      banReason: form.banReason,
    })
    ElMessage.success('禁言操作成功')
    emit('update:modelValue', false)
    emit('done')
  } catch { /* handled */ }
  finally { submitting.value = false }
}
</script>