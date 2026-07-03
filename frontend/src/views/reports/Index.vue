<template>
  <div class="reports-page">
    <el-card class="main-card">
      <template #header>
        <div class="header-bar">
          <span class="header-title">
            <el-icon><DataAnalysis /></el-icon>
            数据报表
          </span>
          <el-date-picker
            v-model="reportMonth"
            type="month"
            placeholder="选择月份"
            @change="handleDateChange"
          />
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="animate-in">
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card stat-card-0">
            <div class="stat-item">
              <div class="stat-icon-wrapper">
                <el-icon :size="22"><WalletFilled /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">月度总支出</div>
                <div class="stat-value">¥{{ monthlyTotal }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card stat-card-1">
            <div class="stat-item">
              <div class="stat-icon-wrapper">
                <el-icon :size="22"><TrendCharts /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">日均支出</div>
                <div class="stat-value">¥{{ dailyAvg }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="stat-card stat-card-2">
            <div class="stat-item">
              <div class="stat-icon-wrapper">
                <el-icon :size="22"><Top /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">最高单日</div>
                <div class="stat-value">¥{{ maxDaily }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表 -->
      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="14">
          <el-card class="chart-card">
            <template #header>
              <span class="chart-title">每日支出趋势</span>
            </template>
            <div style="height: 400px">
              <v-chart :option="lineOption" autoresize />
            </div>
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card class="chart-card">
            <template #header>
              <span class="chart-title">分类支出占比</span>
            </template>
            <div style="height: 400px">
              <v-chart :option="pieOption" autoresize />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 排行榜 -->
      <el-card class="rank-card">
        <template #header>
          <span class="chart-title">
            <el-icon><List /></el-icon>
            支出排行榜
          </span>
        </template>
        <el-table :data="rankList" stripe v-if="rankList.length" class="rank-table">
          <el-table-column label="排名" type="index" width="80">
            <template #default="{ $index }">
              <span class="rank-index" :class="'rank-' + ($index + 1)">
                {{ $index + 1 }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="分类">
            <template #default="{ row, $index }">
              <div class="category-cell">
                <span class="category-dot" :style="{ background: categoryColors[$index % categoryColors.length] }"></span>
                {{ row.name }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="150">
            <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="percentage" label="占比" width="200">
            <template #default="{ row }">
              <div class="percentage-bar">
                <div class="percentage-fill" :style="{ width: row.percentage + '%' }"></div>
                <span class="percentage-text">{{ row.percentage }}%</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无数据" :image-size="80" />
      </el-card>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { statisticsApi } from '../../api/statistics'
import { DataAnalysis, WalletFilled, TrendCharts, Top, List } from '@element-plus/icons-vue'
import type { StatisticsVo } from '../../types'

use([CanvasRenderer, LineChart, PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const categoryColors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#b37feb', '#5cdbd3', '#ff85c0']

const reportMonth = ref(new Date())
const monthlyTotal = ref(0)
const dailyTrend = ref<any[]>([])
const pieData = ref<StatisticsVo[]>([])
const rankList = ref<StatisticsVo[]>([])

const dailyAvg = computed(() => {
  const days = dailyTrend.value.filter((d: any) => d.amount > 0).length || 1
  return (monthlyTotal.value / days).toFixed(2)
})

const maxDaily = computed(() => {
  if (!dailyTrend.value.length) return '0.00'
  return Math.max(...dailyTrend.value.map((d: any) => d.amount)).toFixed(2)
})

function getChartColors() {
  const isDark = document.documentElement.classList.contains('dark')
  return {
    text: isDark ? '#b0b0b0' : '#606266',
    border: isDark ? '#2d2d4a' : '#ebeef5',
    tooltip: isDark ? '#1e1e3a' : '#ffffff',
  }
}

const lineOption = computed(() => {
  const colors = getChartColors()
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltip,
      borderWidth: 0,
      textStyle: { color: colors.text },
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dailyTrend.value.map((d: any) => d.date.slice(5)),
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
      data: dailyTrend.value.map((d: any) => d.amount),
      smooth: true,
      areaStyle: { color: 'rgba(64, 158, 255, 0.12)' },
      lineStyle: { color: '#409eff', width: 2 },
      itemStyle: { color: '#409eff' },
      markLine: {
        silent: true,
        data: [{ type: 'average', name: '平均值' }],
        label: { color: colors.text, formatter: '平均: ¥{c}' },
        lineStyle: { color: '#f56c6c', type: 'dashed' },
      },
    }],
  }
})

const pieOption = computed(() => {
  const colors = getChartColors()
  return {
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
      radius: ['30%', '60%'],
      data: pieData.value.map((d, i) => ({
        name: d.name,
        value: d.amount,
        itemStyle: { color: categoryColors[i % categoryColors.length] },
      })),
      label: { formatter: '{b}: {d}%', color: colors.text },
    }],
  }
})

onMounted(() => {
  loadData()
})

function handleDateChange() {
  loadData()
}

async function loadData() {
  const year = reportMonth.value.getFullYear().toString()
  const month = String(reportMonth.value.getMonth() + 1).padStart(2, '0')
  const startDate = `${year}-${month}-01`
  const endDate = `${year}-${month}-31`

  const [totalRes, trendRes, categoryRes] = await Promise.all([
    statisticsApi.getMonthlyTotal(year, month),
    statisticsApi.getDailyTrend(year, month),
    statisticsApi.getCategorySummary(startDate, endDate),
  ])

  monthlyTotal.value = totalRes.data
  dailyTrend.value = trendRes.data || []
  pieData.value = categoryRes.data || []
  rankList.value = [...(categoryRes.data || [])].sort((a, b) => b.amount - a.amount)
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

/* ─── 统计卡片 ─────────────────────────────────────── */
.stat-card {
  border-left: 4px solid transparent;
  border-image: var(--accent-gradient) 1;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md) !important;
}

.stat-card-0 { --accent-gradient: linear-gradient(135deg, #667eea, #764ba2); }
.stat-card-1 { --accent-gradient: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-card-2 { --accent-gradient: linear-gradient(135deg, #f093fb, #f5576c); }

.stat-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm);
}

.stat-icon-wrapper {
  width: 44px;
  height: 44px;
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
  font-size: 26px;
  font-weight: bold;
  color: var(--color-text-primary);
}

/* ─── 图表卡片 ─────────────────────────────────────── */
.chart-card {
  margin-bottom: 0;
}

.chart-card :deep(.el-card__header),
.rank-card :deep(.el-card__header) {
  padding: var(--spacing-md) var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
}

.chart-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  font-weight: 600;
  color: var(--color-text-primary);
}

/* ─── 排行榜 ─────────────────────────────────────────── */
.rank-card {
  margin-top: 20px;
}

.rank-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--radius-round);
  font-size: 13px;
  font-weight: 600;
  background: var(--color-border-light);
  color: var(--color-text-secondary);
}

.rank-1 {
  background: linear-gradient(135deg, #ffd700, #f5a623);
  color: #fff;
}

.rank-2 {
  background: linear-gradient(135deg, #e8e8e8, #b0b0b0);
  color: #fff;
}

.rank-3 {
  background: linear-gradient(135deg, #ffb07c, #d4875e);
  color: #fff;
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

.percentage-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.percentage-fill {
  height: 8px;
  background: linear-gradient(90deg, var(--color-primary), var(--color-primary-light));
  border-radius: 4px;
  transition: width var(--transition-normal);
  min-width: 4px;
  max-width: 100%;
}

.percentage-text {
  font-size: 13px;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.rank-table :deep(.el-table__header th) {
  background: var(--color-bg-page);
  font-weight: 600;
  color: var(--color-text-primary);
}
</style>
