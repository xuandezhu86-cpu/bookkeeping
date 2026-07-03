<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="animate-in">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card-0">
          <div class="stat-item">
            <div class="stat-icon-wrapper">
              <el-icon :size="24"><Coin /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">今日支出</div>
              <div class="stat-value">¥{{ todayTotal }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card-1">
          <div class="stat-item">
            <div class="stat-icon-wrapper">
              <el-icon :size="24"><WalletFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">本月支出</div>
              <div class="stat-value">¥{{ monthTotal }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card-2">
          <div class="stat-item">
            <div class="stat-icon-wrapper">
              <el-icon :size="24"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">本月笔数</div>
              <div class="stat-value">{{ monthCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card stat-card-3">
          <div class="stat-item">
            <div class="stat-icon-wrapper">
              <el-icon :size="24"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">年度支出</div>
              <div class="stat-value">¥{{ yearTotal }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>月度支出趋势</span>
              <span class="chart-month">{{ currentYear }}年{{ currentMonth }}月</span>
            </div>
          </template>
          <div style="height: 350px">
            <v-chart :option="trendOption" autoresize />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>分类占比</span>
            </div>
          </template>
          <div style="height: 350px">
            <v-chart :option="pieOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷记账弹窗 -->
    <el-dialog v-model="showAddDialog" title="记一笔" width="500px" class="record-dialog">
      <el-form :model="recordForm" label-width="80px">
        <el-form-item label="金额">
          <el-input-number v-model="recordForm.amount" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
            v-model="selectedCategory"
            :options="categoryOptions"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            style="width: 100%"
            clearable
          />
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="recordForm.recordDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="recordForm.note" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 浮动按钮 -->
    <el-button class="fab" type="primary" circle size="large" @click="openAddDialog">
      <el-icon><Plus /></el-icon>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { ElMessage } from 'element-plus'
import { Plus, Coin, WalletFilled, Document, TrendCharts } from '@element-plus/icons-vue'
import { recordApi } from '../../api/records'
import { categoryApi } from '../../api/categories'
import { statisticsApi } from '../../api/statistics'

use([CanvasRenderer, LineChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const now = new Date()
const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
const currentMonth = String(now.getMonth() + 1).padStart(2, '0')
const currentYear = String(now.getFullYear())

const todayTotal = ref(0)
const monthTotal = ref(0)
const monthCount = ref(0)
const yearTotal = ref(0)
const trendOption = ref({})
const pieOption = ref({})
const showAddDialog = ref(false)
const submitting = ref(false)
const categoryOptions = ref<any[]>([])

const selectedCategory = ref<number[]>([])
const recordForm = ref({
  amount: 0,
  recordDate: today,
  note: '',
})

onMounted(async () => {
  await loadSummary()
  await loadTrend()
  await loadPie()
  await loadCategories()
})

async function loadSummary() {
  const [todayRes, monthRes, yearRes] = await Promise.all([
    recordApi.getDailySummary(today),
    statisticsApi.getMonthlyTotal(currentYear, currentMonth),
    statisticsApi.getYearlyTotal(currentYear),
  ])
  todayTotal.value = todayRes.data.total
  monthTotal.value = monthRes.data
  yearTotal.value = yearRes.data
  monthCount.value = todayRes.data.count
}

function getChartColors() {
  const isDark = document.documentElement.classList.contains('dark')
  return {
    text: isDark ? '#b0b0b0' : '#606266',
    border: isDark ? '#2d2d4a' : '#ebeef5',
    tooltip: isDark ? '#1e1e3a' : '#ffffff',
  }
}

async function loadTrend() {
  const res = await statisticsApi.getDailyTrend(currentYear, currentMonth)
  const dates = res.data.map((d: any) => d.date.slice(5))
  const amounts = res.data.map((d: any) => d.amount)
  const colors = getChartColors()
  trendOption.value = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltip,
      borderWidth: 0,
      textStyle: { color: colors.text },
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: colors.border } },
      axisLabel: { color: colors.text },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: colors.border, type: 'dashed' } },
      axisLabel: { color: colors.text },
    },
    series: [{
      name: '支出',
      type: 'line',
      data: amounts,
      smooth: true,
      areaStyle: { color: 'rgba(64, 158, 255, 0.15)' },
      lineStyle: { color: '#409eff', width: 2 },
      itemStyle: { color: '#409eff' },
    }],
  }
}

const categoryColors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#b37feb', '#5cdbd3', '#ff85c0']

async function loadPie() {
  const startDate = `${currentYear}-${currentMonth}-01`
  const endDate = `${currentYear}-${currentMonth}-31`
  const res = await statisticsApi.getCategorySummary(startDate, endDate)
  const data = res.data.map((d: any, i: number) => ({
    name: d.name,
    value: d.amount,
    itemStyle: { color: categoryColors[i % categoryColors.length] },
  }))
  const colors = getChartColors()
  pieOption.value = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: ¥{c} ({d}%)',
      backgroundColor: colors.tooltip,
      borderWidth: 0,
    },
    legend: {
      bottom: 0,
      textStyle: { color: colors.text },
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data,
      label: { show: false },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)',
        },
      },
    }],
  }
}

async function loadCategories() {
  const res = await categoryApi.getTree()
  categoryOptions.value = res.data || []
}

function openAddDialog() {
  selectedCategory.value = []
  recordForm.value = { amount: 0, recordDate: today, note: '' }
  showAddDialog.value = true
}

async function submitRecord() {
  if (!recordForm.value.amount || !selectedCategory.value.length) {
    ElMessage.warning('请填写金额和分类')
    return
  }
  submitting.value = true
  try {
    await recordApi.create({
      categoryId: selectedCategory.value[selectedCategory.value.length - 1],
      amount: recordForm.value.amount,
      recordDate: recordForm.value.recordDate,
      note: recordForm.value.note,
    })
    ElMessage.success('记录成功')
    showAddDialog.value = false
    await loadSummary()
    await loadTrend()
    await loadPie()
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
/* ─── 统计卡片 ─────────────────────────────────────── */
.stat-card {
  border-left: 4px solid transparent;
  border-image: var(--accent-gradient, linear-gradient(135deg, #409eff, #66b1ff)) 1;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md) !important;
}

/* 各卡片渐变色 */
.stat-card-0 { --accent-gradient: linear-gradient(135deg, #667eea, #764ba2); }
.stat-card-1 { --accent-gradient: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-card-2 { --accent-gradient: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-card-3 { --accent-gradient: linear-gradient(135deg, #43e97b, #38f9d7); }

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm);
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--accent-gradient);
  color: #fff;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--color-text-primary);
}

/* ─── 图表卡片 ─────────────────────────────────────── */
.chart-card :deep(.el-card__header) {
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: var(--color-text-primary);
}

.chart-month {
  font-size: 13px;
  font-weight: 400;
  color: var(--color-text-secondary);
}

/* ─── 浮动按钮 ─────────────────────────────────────── */
.fab {
  position: fixed;
  right: 30px;
  bottom: 30px;
  z-index: 100;
  width: 56px;
  height: 56px;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.fab:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.6);
}

/* ─── 对话框 ───────────────────────────────────────── */
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
