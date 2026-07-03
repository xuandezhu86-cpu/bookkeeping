<template>
  <div class="records-page">
    <el-card class="main-card">
      <template #header>
        <div class="header-bar">
          <span class="header-title">
            <el-icon><Document /></el-icon>
            消费记录
          </span>
          <el-button type="primary" @click="showDialog = true">
            <el-icon><Plus /></el-icon>
            新增记录
          </el-button>
        </div>
      </template>

      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-form :inline="true" class="filter-form">
          <el-form-item label="分类">
            <el-cascader
              v-model="filterCategory"
              :options="categoryOptions"
              :props="{ label: 'name', value: 'id', children: 'children', emitPath: false }"
              clearable
              placeholder="全部分类"
              @change="handleFilter"
            />
          </el-form-item>
          <el-form-item label="日期">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="handleFilter"
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- 记录表格 -->
      <el-table
        :data="store.records"
        highlight-current-row
        class="record-table"
      >
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column label="分类" width="140">
          <template #default="{ row }">
            <div class="category-cell">
              <span
                class="category-dot"
                :style="{ background: categoryColors[getCategoryIndex(row.categoryId)] }"
              ></span>
              {{ getCategoryName(row.categoryId) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="150">
          <template #default="{ row }">
            <span class="amount-text">¥{{ row.amount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="130" align="center">
          <template #default="{ row }">
            <el-tooltip content="编辑" placement="top">
              <el-button size="small" circle @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button size="small" type="danger" circle @click="handleDelete(row.id)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="page"
          :page-size="10"
          :total="store.total"
          layout="prev, pager, next, total"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="showDialog"
      :title="isEdit ? '编辑记录' : '新增记录'"
      width="500px"
      class="record-dialog"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
            v-model="form.categoryId"
            :options="categoryOptions"
            :props="{ label: 'name', value: 'id', children: 'children', emitPath: false }"
            style="width: 100%"
            clearable
          />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="form.recordDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.note" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRecordStore } from '../../stores/records'
import { categoryApi } from '../../api/categories'
import { Document, Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { CategoryVo, ExpenseRecord } from '../../types'

const categoryColors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#b37feb', '#5cdbd3', '#ff85c0']

const store = useRecordStore()
const page = ref(1)
const showDialog = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const submitting = ref(false)
const filterCategory = ref<number | undefined>()
const dateRange = ref<[string, string] | null>(null)
const categoryOptions = ref<CategoryVo[]>([])
const categoryMap = ref<Record<number, string>>({})
const categoryIdList = ref<number[]>([])

const form = ref({
  amount: 0,
  categoryId: undefined as number | undefined,
  recordDate: '',
  note: '',
})

onMounted(async () => {
  await loadCategories()
  await loadRecords()
})

async function loadCategories() {
  const res = await categoryApi.getTree()
  categoryOptions.value = res.data || []
  const map: Record<number, string> = {}
  const ids: number[] = []
  function buildMap(list: CategoryVo[]) {
    for (const item of list) {
      map[item.id] = item.name
      ids.push(item.id)
      if (item.children) buildMap(item.children)
    }
  }
  buildMap(res.data || [])
  categoryMap.value = map
  categoryIdList.value = ids
}

function getCategoryName(id: number) {
  return categoryMap.value[id] || '未知'
}

function getCategoryIndex(id: number): number {
  const idx = categoryIdList.value.indexOf(id)
  return idx >= 0 ? idx : 0
}

async function loadRecords() {
  const params: any = { page: page.value, size: 10 }
  if (filterCategory.value) params.categoryId = filterCategory.value
  if (dateRange.value) {
    params.startDate = dateRange.value[0]
    params.endDate = dateRange.value[1]
  }
  await store.fetchList(params)
}

function handleFilter() {
  page.value = 1
  loadRecords()
}

function handlePageChange(newPage: number) {
  page.value = newPage
  loadRecords()
}

function handleEdit(row: ExpenseRecord) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    amount: row.amount,
    categoryId: row.categoryId,
    recordDate: row.recordDate,
    note: row.note,
  }
  showDialog.value = true
}

async function handleSubmit() {
  if (!form.value.amount || !form.value.categoryId) {
    ElMessage.warning('请填写金额和分类')
    return
  }
  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await store.updateRecord(editId.value, {
        categoryId: form.value.categoryId,
        amount: form.value.amount,
        recordDate: form.value.recordDate,
        note: form.value.note,
      })
      ElMessage.success('更新成功')
    } else {
      await store.createRecord({
        categoryId: form.value.categoryId,
        amount: form.value.amount,
        recordDate: form.value.recordDate,
        note: form.value.note,
      })
      ElMessage.success('新增成功')
    }
    showDialog.value = false
    await loadRecords()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该记录？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await store.deleteRecord(id)
    ElMessage.success('删除成功')
    await loadRecords()
  } catch {
    // cancelled
  }
}
</script>

<style scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

/* ─── 筛选栏 ─────────────────────────────────────────── */
.filter-bar {
  background: var(--color-bg-white);
  padding: var(--spacing-md) var(--spacing-lg);
  border-radius: var(--radius-md);
  margin-bottom: var(--spacing-md);
  border: 1px solid var(--color-border);
}

.filter-form {
  display: flex;
  gap: var(--spacing-md);
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

/* ─── 表格 ───────────────────────────────────────────── */
.record-table {
  width: 100%;
}

.record-table :deep(th.el-table__cell) {
  background: var(--color-bg-page) !important;
  font-weight: 600;
  color: var(--color-text-primary);
}

.record-table :deep(.el-table__body tr:hover > td) {
  background: rgba(64, 158, 255, 0.03) !important;
}

.category-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.category-dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-round);
  flex-shrink: 0;
}

.amount-text {
  font-weight: 600;
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  color: var(--color-text-primary);
}

.pagination-bar {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* ─── 对话框 ─────────────────────────────────────────── */
.record-dialog :deep(.el-dialog__header) {
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--color-border);
  margin: 0;
}

.record-dialog :deep(.el-dialog__body) {
  padding: var(--spacing-lg) var(--spacing-xl);
}

.record-dialog :deep(.el-dialog__footer) {
  padding: 0 var(--spacing-xl) var(--spacing-lg);
  border-top: 1px solid var(--color-border);
  padding-top: var(--spacing-md);
}
</style>
