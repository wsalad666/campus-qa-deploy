import { ref } from 'vue'

export function usePagination(initialPageSize = 10) {
  const pageNum = ref(1)
  const pageSize = ref(initialPageSize)
  const total = ref(0)

  function reset() {
    pageNum.value = 1
  }

  function handlePageChange(page: number) {
    pageNum.value = page
  }

  function handleSizeChange(size: number) {
    pageSize.value = size
    pageNum.value = 1
  }

  return { pageNum, pageSize, total, reset, handlePageChange, handleSizeChange }
}