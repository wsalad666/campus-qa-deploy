<template>
  <div class="pagination-wrapper">
    <el-pagination
      :current-page="pageNum"
      :page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, prev, pager, next, sizes, jumper"
      background
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  total: number
  pageNum: number
  pageSize: number
}>()

const emit = defineEmits<{
  (e: 'update:pageNum', value: number): void
  (e: 'update:pageSize', value: number): void
  (e: 'change'): void
}>()

function handleCurrentChange(page: number) {
  emit('update:pageNum', page)
  emit('change')
}

function handleSizeChange(size: number) {
  emit('update:pageSize', size)
  emit('change')
}
</script>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}
</style>