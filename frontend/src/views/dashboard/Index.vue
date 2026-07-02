<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">今日支出</div>
            <div class="stat-value">¥{{ todayTotal }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">本月支出</div>
            <div class="stat-value">¥{{ monthTotal }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">本月笔数</div>
            <div class="stat-value">{{ monthCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">年度支出</div>
            <div class="stat-value">¥{{ yearTotal }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>月度支出趋势</span>
          </template>
          <div style="height: 350px">
            <v-chart :option="trendOption" autoresize />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>分类占比</span>
          </template>
          <div style="height: 350px">
            <v-chart :option="pieOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showAddDialog" title="记一笔" width="500px">
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
          <el-input v-model="recordForm.note" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRecord" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <el-button class="fab" type="primary" circle size="large" @click="openAddDialog">
      <el-icon><Plus /></el-icon>
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import VChart from 'vue-echarts'
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

async function loadTrend() {
  const res = await statisticsApi.getDailyTrend(currentYear, currentMonth)
  const dates = res.data.map((d: any) => d.date.slice(5))
  const amounts = res.data.map((d: any) => d.amount)
  trendOption.value = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [{ name: '支出', type: 'line', data: amounts, smooth: true, areaStyle: {} }],
  }
}

async function loadPie() {
  const startDate = `${currentYear}-${currentMonth}-01`
  const endDate = `${currentYear}-${currentMonth}-31`
  const res = await statisticsApi.getCategorySummary(startDate, endDate)
  const data = res.data.map((d: any) => ({ name: d.name, value: d.amount }))
  pieOption.value = {
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    series: [{ type: 'pie', radius: ['40%', '70%'], data, label: { show: false } }],
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
.stat-item {
  text-align: center;
  padding: 10px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.fab {
  position: fixed;
  right: 30px;
  bottom: 30px;
  z-index: 100;
  width: 56px;
  height: 56px;
  font-size: 24px;
}
</style>
