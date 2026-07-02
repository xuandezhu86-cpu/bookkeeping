<template>
  <div class="records-page">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span>消费记录</span>
          <el-button type="primary" @click="showDialog = true">新增记录</el-button>
        </div>
      </template>

      <el-form :inline="true" class="filter-bar">
        <el-form-item label="分类">
          <el-cascader
            v-model="filterCategory"
            :options="categoryOptions"
            :props="{ label: 'name', value: 'id', children: 'children', emitPath: false }"
            clearable
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

      <el-table :data="store.records" stripe style="width: 100%">
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column prop="categoryId" label="分类" width="120">
          <template #default="{ row }">
            {{ getCategoryName(row.categoryId) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            ¥{{ row.amount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="note" label="备注" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="page"
          :page-size="10"
          :total="store.total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑记录' : '新增记录'" width="500px">
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
          <el-input v-model="form.note" type="textarea" />
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
import type { CategoryVo, ExpenseRecord } from '../../types'

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
  function buildMap(list: CategoryVo[]) {
    for (const item of list) {
      map[item.id] = item.name
      if (item.children) buildMap(item.children)
    }
  }
  buildMap(res.data || [])
  categoryMap.value = map
}

function getCategoryName(id: number) {
  return categoryMap.value[id] || '未知'
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
    await ElMessageBox.confirm('确定删除该记录？', '提示')
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
.filter-bar {
  margin-bottom: 15px;
}
.pagination-bar {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
