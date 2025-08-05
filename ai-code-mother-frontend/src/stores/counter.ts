/**
 * 计数器状态管理 Store
 * 使用 Pinia 管理全局计数器状态
 */
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useCounterStore = defineStore('counter', () => {
  // 计数器值
  const count = ref(0)
  
  // 计算属性：双倍计数值
  const doubleCount = computed(() => count.value * 2)
  
  // 递增计数器
  function increment() {
    count.value++
  }

  return { count, doubleCount, increment }
})
