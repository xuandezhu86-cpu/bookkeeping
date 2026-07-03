<template>
  <div class="settings-page">
    <el-card class="main-card">
      <template #header>
        <span class="header-title">
          <el-icon><Setting /></el-icon>
          设置
        </span>
      </template>

      <el-tabs v-model="activeTab" class="settings-tabs">
        <!-- ─── 标签页1：应用设置 ───────────────────── -->
        <el-tab-pane label="应用设置" name="general">
          <el-card shadow="none" class="setting-card">
            <div class="setting-item">
              <div class="setting-info">
                <div class="setting-icon">
                  <el-icon :size="22" :color="isDark ? '#e6a23c' : '#409eff'">
                    <Sunny v-if="!isDark" />
                    <Moon v-else />
                  </el-icon>
                </div>
                <div class="setting-text">
                  <h4>主题模式</h4>
                  <p>切换明暗主题以适应不同环境</p>
                </div>
              </div>
              <div class="setting-control">
                <el-switch
                  v-model="isDark"
                  @change="toggleTheme"
                  active-text="暗色"
                  inactive-text="亮色"
                />
              </div>
            </div>
          </el-card>
        </el-tab-pane>

        <!-- ─── 标签页2：预算管理 ───────────────────── -->
        <el-tab-pane label="预算管理" name="budget">
          <!-- 预算汇总 -->
          <el-row :gutter="20" class="animate-in" style="margin-bottom: 20px">
            <el-col :span="8">
              <el-card shadow="hover" class="budget-summary-card">
                <div class="budget-summary">
                  <div class="bs-label">总预算</div>
                  <div class="bs-value">¥{{ totalBudget }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="budget-summary-card">
                <div class="budget-summary">
                  <div class="bs-label">已用额度</div>
                  <div class="bs-value bs-spent">¥{{ spentBudget }}</div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="hover" class="budget-summary-card">
                <div class="budget-summary">
                  <div class="bs-label">剩余额度</div>
                  <div class="bs-value" :class="remainingBudget >= 0 ? 'bs-remaining' : 'bs-over'">
                    ¥{{ remainingBudget }}
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 预算操作 -->
          <div class="budget-toolbar">
            <div class="budget-toolbar-left">
              <el-date-picker
                v-model="budgetMonth"
                type="month"
                @change="loadBudgets"
              />
            </div>
            <el-button type="primary" @click="showSetBudget = true">
              <el-icon><Plus /></el-icon>
              设置预算
            </el-button>
          </div>

          <!-- 预算列表 -->
          <el-table :data="budgetList" stripe class="budget-table">
            <el-table-column prop="month" label="月份" width="120" />
            <el-table-column label="分类" width="150">
              <template #default="{ row }">
                {{ row.categoryId ? getCategoryName(row.categoryId) : '总预算' }}
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="预算金额">
              <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button size="small" type="danger" plain @click="handleDeleteBudget(row.id)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!budgetList.length" description="暂无预算" :image-size="60" />
        </el-tab-pane>

        <!-- ─── 标签页3：关于 ────────────────────────── -->
        <el-tab-pane label="关于" name="about">
          <div class="about-section">
            <div class="about-logo">
              <el-icon :size="64" color="#409eff"><Coin /></el-icon>
            </div>
            <h2 class="about-name">悠然记账</h2>
            <p class="about-desc">跨平台个人记账桌面应用</p>
            <p class="about-version">版本 1.0.0</p>

            <el-divider />

            <div class="about-tech">
              <h4>技术栈</h4>
              <div class="tech-tags">
                <el-tag>Vue 3</el-tag>
                <el-tag type="success">TypeScript</el-tag>
                <el-tag type="info">Electron</el-tag>
                <el-tag type="warning">Spring Boot</el-tag>
                <el-tag type="danger">MySQL</el-tag>
                <el-tag>Element Plus</el-tag>
                <el-tag type="success">ECharts</el-tag>
              </div>
            </div>

            <el-divider />

            <p class="about-copyright">© 2026 悠然记账 · 轻松管理每一笔收支</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 设置预算弹窗 -->
    <el-dialog v-model="showSetBudget" title="设置预算" width="450px" class="budget-dialog">
      <el-form :model="budgetForm" label-width="80px">
        <el-form-item label="月份">
          <el-date-picker v-model="budgetForm.month" type="month" style="width: 100%" />
        </el-form-item>
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { toggleDark } from '../../utils/theme'
import { budgetApi } from '../../api/budgets'
import { categoryApi } from '../../api/categories'
import { Setting, Plus, Delete, Coin, Sunny, Moon } from '@element-plus/icons-vue'
import type { Budget, Category } from '../../types'

const activeTab = ref('general')
const isDark = ref(false)
const showSetBudget = ref(false)
const budgetMonth = ref(new Date())
const budgetList = ref<Budget[]>([])
const categoryList = ref<Category[]>([])
const categoryMap = ref<Record<number, string>>({})

// 补齐日期到月末
const monthEnd = computed(() => {
  const y = budgetMonth.value.getFullYear()
  const m = budgetMonth.value.getMonth() + 1
  return new Date(y, m, 0).getDate()
})

const totalBudget = computed(() => {
  return budgetList.value.reduce((sum, b) => sum + b.amount, 0).toFixed(2)
})

const spentBudget = computed(() => {
  // 模拟已用额度（实际应从统计数据获取）
  const total = parseFloat(totalBudget.value)
  return (total * 0.6).toFixed(2) // 占位，未来对接实际数据
})

const remainingBudget = computed(() => {
  return (parseFloat(totalBudget.value) - parseFloat(spentBudget.value)).toFixed(2)
})

const budgetForm = ref({
  month: new Date(),
  categoryId: null as number | null,
  amount: 0,
})

onMounted(async () => {
  const savedDark = localStorage.getItem('dark-mode') === 'true'
  isDark.value = savedDark
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
  const year = budgetForm.value.month.getFullYear()
  const month = String(budgetForm.value.month.getMonth() + 1).padStart(2, '0')
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
    await ElMessageBox.confirm('确定删除该预算？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await budgetApi.delete(id)
    ElMessage.success('删除成功')
    await loadBudgets()
  } catch {
    // cancelled
  }
}
</script>

<style scoped>
.header-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

/* ─── Tab 样式 ────────────────────────────────────────── */
.settings-tabs :deep(.el-tabs__header) {
  margin-bottom: var(--spacing-lg);
}

.settings-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  padding: 0 24px;
  height: 44px;
  line-height: 44px;
}

/* ─── 设置项卡片 ─────────────────────────────────────── */
.setting-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) 0;
}

.setting-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.setting-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  border-radius: var(--radius-md);
}

.setting-text h4 {
  margin: 0 0 4px 0;
  font-size: 15px;
  color: var(--color-text-primary);
  font-weight: 600;
}

.setting-text p {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary);
}

/* ─── 预算汇总 ───────────────────────────────────────── */
.budget-summary-card {
  text-align: center;
  padding: var(--spacing-sm) 0;
}

.budget-summary {
  padding: var(--spacing-sm);
}

.bs-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 6px;
}

.bs-value {
  font-size: 26px;
  font-weight: bold;
  color: var(--color-text-primary);
}

.bs-spent {
  color: var(--color-warning);
}

.bs-remaining {
  color: var(--color-success);
}

.bs-over {
  color: var(--color-danger);
}

/* ─── 预算工具栏 ─────────────────────────────────────── */
.budget-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.budget-toolbar-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.budget-table {
  margin-top: var(--spacing-sm);
}

/* ─── 关于页面 ───────────────────────────────────────── */
.about-section {
  text-align: center;
  padding: var(--spacing-xl) 0;
}

.about-logo {
  margin-bottom: var(--spacing-md);
}

.about-name {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
}

.about-desc {
  font-size: 15px;
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-xs);
}

.about-version {
  font-size: 13px;
  color: var(--color-text-placeholder);
}

.about-tech {
  text-align: center;
}

.about-tech h4 {
  font-size: 15px;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-sm);
  font-weight: 600;
}

.tech-tags {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
}

.about-copyright {
  font-size: 13px;
  color: var(--color-text-secondary);
}
</style>
