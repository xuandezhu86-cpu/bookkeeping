<template>
  <div class="reports-page">
    <el-card>
      <template #header>
        <div class="header-bar">
          <span>数据报表</span>
          <div>
            <el-date-picker
              v-model="reportMonth"
              type="month"
              placeholder="选择月份"
              @change="handleDateChange"
            />
          </div>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-label">月度总支出</div>
              <div class="stat-value">¥{{ monthlyTotal }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-label">日均支出</div>
              <div class="stat-value">¥{{ dailyAvg }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div class="stat-card">
              <div class="stat-label">最高单日</div>
              <div class="stat-value">¥{{ maxDaily }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="14">
          <el-card>
            <template #header>每日支出趋势</template>
            <div style="height: 400px">
              <v-chart :option="lineOption" autoresize />
            </div>
          </el-card>
        </el-col>
        <el-col :span="10">
          <el-card>
            <template #header>分类支出占比</template>
            <div style="height: 400px">
              <v-chart :option="pieOption" autoresize />
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px">
        <template #header>支出排行榜</template>
        <el-table :data="rankList" stripe v-if="rankList.length">
          <el-table-column label="排名" type="index" width="80" />
          <el-table-column prop="name" label="分类" />
          <el-table-column prop="amount" label="金额">
            <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="percentage" label="占比">
            <template #default="{ row }">{{ row.percentage }}%</template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无数据" />
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
import type { StatisticsVo } from '../../types'

use([CanvasRenderer, LineChart, PieChart, BarChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

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

const lineOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: dailyTrend.value.map((d: any) => d.date.slice(5)) },
  yAxis: { type: 'value' },
  series: [{ name: '支出', type: 'line', data: dailyTrend.value.map((d: any) => d.amount), smooth: true }],
}))

const pieOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
  series: [{
    type: 'pie',
    radius: ['30%', '60%'],
    data: pieData.value.map(d => ({ name: d.name, value: d.amount })),
    label: { formatter: '{b}: {d}%' },
  }],
}))

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
.stat-card {
  text-align: center;
  padding: 15px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 26px;
  font-weight: bold;
  color: #303133;
}
</style>
