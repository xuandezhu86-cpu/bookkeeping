<template>
  <div class="categories-page">
    <el-card class="main-card">
      <template #header>
        <div class="header-bar">
          <span class="header-title">
            <el-icon><Collection /></el-icon>
            分类管理
          </span>
          <el-button type="primary" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            新增分类
          </el-button>
        </div>
      </template>

      <!-- 分类卡片网格 -->
      <div class="category-grid" v-if="categoryList.length">
        <div
          v-for="(cat, index) in categoryList"
          :key="cat.id"
          class="category-card"
          :style="{ animationDelay: index * 0.05 + 's' }"
        >
          <!-- 卡片头部 -->
          <div class="category-card-header">
            <div
              class="category-icon-wrapper"
              :style="{ background: categoryGradients[index % categoryGradients.length] }"
            >
              <el-icon :size="24">
                <component :is="resolveIcon(cat.icon)" />
              </el-icon>
            </div>
            <div class="category-card-info">
              <div class="category-name">{{ cat.name }}</div>
              <div class="category-count" v-if="cat.children?.length">
                {{ cat.children.length }} 个子分类
              </div>
            </div>
          </div>

          <!-- 子分类标签 -->
          <template v-if="cat.children && cat.children.length > 0">
            <div class="category-children">
              <el-tag
                v-for="child in cat.children"
                :key="child.id"
                size="small"
                class="child-tag"
                :style="{ '--tag-color': categoryColors[index % categoryColors.length] }"
              >
                {{ child.name }}
              </el-tag>
            </div>
          </template>
          <template v-else>
            <div class="category-children empty-children">
              <span class="no-child-text">叶子分类</span>
            </div>
          </template>

          <!-- 操作按钮 -->
          <div class="category-actions">
            <el-button size="small" @click="handleEdit(cat)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              plain
              @click="handleDelete(cat.id)"
              v-if="!cat.children?.length"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无分类" :image-size="80" />
    </el-card>

    <!-- 新增弹窗 -->
    <el-dialog v-model="showAddDialog" title="新增分类" width="450px" class="cat-dialog">
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
          <el-input v-model="addForm.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="addForm.icon" placeholder="图标名称，如 Coin" clearable>
            <template #prefix>
              <el-icon v-if="addForm.icon" :size="18">
                <component :is="resolveIcon(addForm.icon)" />
              </el-icon>
            </template>
          </el-input>
          <div class="icon-grid" v-if="showIconPicker">
            <el-tooltip
              v-for="icon in commonIcons"
              :key="icon"
              :content="icon"
              placement="top"
            >
              <span
                class="icon-item"
                :class="{ active: addForm.icon === icon }"
                @click="addForm.icon = icon"
              >
                <el-icon :size="18">
                  <component :is="resolveIcon(icon)" />
                </el-icon>
              </span>
            </el-tooltip>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑分类" width="450px" class="cat-dialog">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="editForm.icon" placeholder="图标名称，如 Coin" clearable>
            <template #prefix>
              <el-icon v-if="editForm.icon" :size="18">
                <component :is="resolveIcon(editForm.icon)" />
              </el-icon>
            </template>
          </el-input>
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
import { Collection, Plus, Edit, Delete } from '@element-plus/icons-vue'
import * as Icons from '@element-plus/icons-vue'
import type { Category, CategoryVo } from '../../types'

const categoryColors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#b37feb', '#5cdbd3', '#ff85c0']
const categoryGradients = [
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a18cd1, #fbc2eb)',
  'linear-gradient(135deg, #fccb90, #d57eeb)',
  'linear-gradient(135deg, #e0c3fc, #8ec5fc)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
]

const commonIcons = ['Coin', 'Food', 'ShoppingCart', 'Van', 'HomeFilled', 'WalletFilled', 'Present', 'Reading', 'PhoneFilled', 'Airplane', 'Monitor', 'CoffeeCup', 'Sunny', 'Moon', 'Star', 'Heart']

function resolveIcon(name: string) {
  return (Icons as Record<string, any>)[name] || Icons.Coin
}

const categoryList = ref<CategoryVo[]>([])
const parentCategories = ref<Category[]>([])
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const showIconPicker = ref(true)
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
    await ElMessageBox.confirm('确定删除该分类？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
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

.header-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

/* ─── 卡片网格 ───────────────────────────────────────── */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--spacing-md);
}

.category-card {
  background: var(--color-bg-white);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--color-border);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  opacity: 0;
  animation: fade-in-up 0.4s ease forwards;
}

.category-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

/* 卡片头部 */
.category-card-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
}

.category-icon-wrapper {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.category-card-info {
  flex: 1;
  min-width: 0;
}

.category-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 2px;
}

.category-count {
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* 子分类标签 */
.category-children {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: var(--spacing-sm);
  min-height: 28px;
}

.empty-children {
  display: flex;
  align-items: center;
}

.no-child-text {
  font-size: 12px;
  color: var(--color-text-placeholder);
}

.child-tag {
  margin: 2px;
  background: color-mix(in srgb, var(--tag-color, #409eff) 10%, transparent);
  border-color: var(--tag-color, #409eff);
  color: var(--tag-color, #409eff);
}

html.dark .child-tag {
  background: color-mix(in srgb, var(--tag-color, #409eff) 15%, transparent);
}

/* 操作按钮 */
.category-actions {
  display: flex;
  gap: var(--spacing-xs);
  justify-content: flex-end;
  border-top: 1px solid var(--color-border);
  padding-top: var(--spacing-sm);
  margin-top: var(--spacing-xs);
}

/* ─── 图标选择器 ─────────────────────────────────────── */
.icon-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: var(--spacing-sm);
  padding: var(--spacing-sm);
  background: var(--color-bg);
  border-radius: var(--radius-md);
  max-height: 120px;
  overflow-y: auto;
}

.icon-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  cursor: pointer;
  color: var(--color-text-regular);
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}

.icon-item:hover {
  background: var(--color-primary-light);
  color: #fff;
}

.icon-item.active {
  background: var(--color-primary);
  color: #fff;
  border-color: var(--color-primary);
}

/* ─── 对话框 ─────────────────────────────────────────── */
.cat-dialog :deep(.el-dialog__header) {
  padding: var(--spacing-lg) var(--spacing-xl);
  border-bottom: 1px solid var(--color-border);
  margin: 0;
}

.cat-dialog :deep(.el-dialog__body) {
  padding: var(--spacing-lg) var(--spacing-xl);
}

.cat-dialog :deep(.el-dialog__footer) {
  padding: 0 var(--spacing-xl) var(--spacing-lg);
  border-top: 1px solid var(--color-border);
  padding-top: var(--spacing-md);
}
</style>
