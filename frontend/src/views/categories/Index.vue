<template>
  <div class="categories-page">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span>分类管理</span>
          <el-button type="primary" @click="showAddDialog = true">新增分类</el-button>
        </div>
      </template>

      <el-table :data="categoryList" row-key="id" default-expand-all stripe>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="icon" label="图标" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)" v-if="!row.children?.length">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAddDialog" title="新增分类" width="400px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="父级分类">
          <el-select v-model="addForm.parentId" placeholder="留空为一级分类" clearable style="width: 100%">
            <el-option
              v-for="item in parentCategories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="addForm.name" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="addForm.icon" placeholder="图标名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEditDialog" title="编辑分类" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="editForm.icon" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { categoryApi } from '../../api/categories'
import type { Category, CategoryVo } from '../../types'

const categoryList = ref<CategoryVo[]>([])
const parentCategories = ref<Category[]>([])
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const editTarget = ref<CategoryVo | null>(null)

const addForm = ref({
  name: '',
  icon: '',
  parentId: undefined as number | undefined,
})

const editForm = ref({
  name: '',
  icon: '',
})

onMounted(async () => {
  await Promise.all([loadCategories(), loadParents()])
})

async function loadCategories() {
  const res = await categoryApi.getTree()
  categoryList.value = res.data || []
}

async function loadParents() {
  const res = await categoryApi.getParents()
  parentCategories.value = res.data || []
}

async function handleAdd() {
  if (!addForm.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }
  await categoryApi.create(addForm.value)
  ElMessage.success('新增成功')
  showAddDialog.value = false
  addForm.value = { name: '', icon: '', parentId: undefined }
  await loadCategories()
}

function handleEdit(row: CategoryVo) {
  editTarget.value = row
  editForm.value = { name: row.name, icon: row.icon || '' }
  showEditDialog.value = true
}

async function handleEditSubmit() {
  if (!editTarget.value) return
  await categoryApi.update(editTarget.value.id, editForm.value)
  ElMessage.success('更新成功')
  showEditDialog.value = false
  await loadCategories()
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该分类？', '提示')
    await categoryApi.delete(id)
    ElMessage.success('删除成功')
    await loadCategories()
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
</style>
