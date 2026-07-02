<template>
  <div class="settings-page">
    <el-card>
      <template #header><span>应用设置</span></template>

      <el-form label-width="100px">
        <el-form-item label="主题模式">
          <el-switch v-model="isDark" active-text="暗色模式" inactive-text="亮色模式" @change="toggleTheme" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header><span>预算管理</span></template>

      <el-form :inline="true">
        <el-form-item label="月份">
          <el-date-picker v-model="budgetMonth" type="month" @change="loadBudgets" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="showSetBudget = true">设置预算</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="budgetList" stripe>
        <el-table-column prop="month" label="月份" width="120" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ row.categoryId ? getCategoryName(row.categoryId) : '总预算' }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="预算金额">
          <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="handleDeleteBudget(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showSetBudget" title="设置预算" width="400px">
      <el-form :model="budgetForm" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="budgetForm.categoryId" placeholder="留空为总预算" clearable style="width: 100%">
            <el-option label="总预算" :value="null" />
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="budgetForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSetBudget = false">取消</el-button>
        <el-button type="primary" @click="handleSetBudget">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { toggleDark } from '../../utils/theme'
import { budgetApi } from '../../api/budgets'
import { categoryApi } from '../../api/categories'
import type { Budget, Category } from '../../types'

const isDark = ref(false)
const showSetBudget = ref(false)
const budgetMonth = ref(new Date())
const budgetList = ref<Budget[]>([])
const categoryList = ref<Category[]>([])
const categoryMap = ref<Record<number, string>>({})

const budgetForm = ref({
  categoryId: null as number | null,
  amount: 0,
})

onMounted(async () => {
  await Promise.all([loadCategories(), loadBudgets()])
})

function toggleTheme(val: boolean) {
  toggleDark(val)
}

async function loadCategories() {
  const res = await categoryApi.getParents()
  categoryList.value = res.data || []
  const map: Record<number, string> = {}
  ;(res.data || []).forEach((c: Category) => { map[c.id] = c.name })
  categoryMap.value = map
}

function getCategoryName(id: number) {
  return categoryMap.value[id] || '未知'
}

async function loadBudgets() {
  const year = budgetMonth.value.getFullYear()
  const month = String(budgetMonth.value.getMonth() + 1).padStart(2, '0')
  const res = await budgetApi.getList(`${year}-${month}`)
  budgetList.value = (res.data || []).filter((b: Budget) => b.amount > 0)
}

async function handleSetBudget() {
  const year = budgetMonth.value.getFullYear()
  const month = String(budgetMonth.value.getMonth() + 1).padStart(2, '0')
  await budgetApi.set({
    categoryId: budgetForm.value.categoryId || undefined,
    month: `${year}-${month}`,
    amount: budgetForm.value.amount,
  })
  ElMessage.success('预算设置成功')
  showSetBudget.value = false
  await loadBudgets()
}

async function handleDeleteBudget(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该预算？', '提示')
    await budgetApi.delete(id)
    ElMessage.success('删除成功')
    await loadBudgets()
  } catch {
    // cancelled
  }
}
</script>
