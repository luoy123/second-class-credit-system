<script setup>
import { computed, reactive, ref } from 'vue'
import {
  exportCategoryStatsCsv,
  exportRankingCsv,
  getCategoryStats,
  getGradeStats,
  getMajorStats,
  getMonthlyStats,
  getRanking,
} from '../api'

const loading = ref(false)
const exporting = ref(false)
const error = ref('')

const filters = reactive({
  year: new Date().getFullYear(),
  topN: 10,
})

const categoryStats = ref([])
const majorStats = ref([])
const gradeStats = ref([])
const monthlyStats = ref([])
const ranking = ref([])

function toNumber(value) {
  const number = Number(value)
  return Number.isFinite(number) ? number : 0
}

function toPercent(value, max) {
  const safeMax = toNumber(max)
  if (safeMax <= 0) {
    return '0%'
  }
  const percent = (toNumber(value) / safeMax) * 100
  return `${Math.max(percent, 4).toFixed(1)}%`
}

const totalCredit = computed(() =>
  categoryStats.value.reduce((sum, item) => sum + toNumber(item.totalCredit), 0).toFixed(2),
)

const monthlyCredit = computed(() =>
  monthlyStats.value.reduce((sum, item) => sum + toNumber(item.totalCredit), 0).toFixed(2),
)

const maxCategoryCredit = computed(() =>
  Math.max(...categoryStats.value.map((item) => toNumber(item.totalCredit)), 0),
)

const maxMajorCredit = computed(() =>
  Math.max(...majorStats.value.map((item) => toNumber(item.totalCredit)), 0),
)

const maxGradeCredit = computed(() =>
  Math.max(...gradeStats.value.map((item) => toNumber(item.totalCredit)), 0),
)

const maxMonthlyCredit = computed(() =>
  Math.max(...monthlyStats.value.map((item) => toNumber(item.totalCredit)), 0),
)

async function loadAnalytics() {
  loading.value = true
  error.value = ''
  try {
    const year = Number(filters.year) || new Date().getFullYear()
    const topN = Math.min(Math.max(Number(filters.topN) || 10, 1), 100)
    filters.year = year
    filters.topN = topN
    const [categories, majors, grades, monthly, rankList] = await Promise.all([
      getCategoryStats(),
      getMajorStats(),
      getGradeStats(),
      getMonthlyStats(year),
      getRanking(topN),
    ])
    categoryStats.value = categories || []
    majorStats.value = majors || []
    gradeStats.value = grades || []
    monthlyStats.value = monthly || []
    ranking.value = rankList || []
  } catch (err) {
    error.value = err.message || '加载统计数据失败'
  } finally {
    loading.value = false
  }
}

function downloadBlob(blob, fileName) {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}

async function exportCategory() {
  exporting.value = true
  error.value = ''
  try {
    const blob = await exportCategoryStatsCsv()
    downloadBlob(blob, 'category_statistics.csv')
  } catch (err) {
    error.value = err.message || '导出分类统计失败'
  } finally {
    exporting.value = false
  }
}

async function exportRanking() {
  exporting.value = true
  error.value = ''
  try {
    const blob = await exportRankingCsv(filters.topN)
    downloadBlob(blob, `student_ranking_top_${filters.topN}.csv`)
  } catch (err) {
    error.value = err.message || '导出排名失败'
  } finally {
    exporting.value = false
  }
}

loadAnalytics()
</script>

<template>
  <div class="page">
    <div class="card">
      <div class="card-title">查询条件</div>
      <div class="form-grid">
        <div class="field">
          <label>统计年份</label>
          <input v-model.number="filters.year" type="number" min="2000" max="2100" />
        </div>
        <div class="field">
          <label>排名 Top N</label>
          <input v-model.number="filters.topN" type="number" min="1" max="100" />
        </div>
      </div>
      <div class="actions" style="margin-top: 12px">
        <button class="btn-primary" type="button" @click="loadAnalytics" :disabled="loading">
          {{ loading ? '加载中...' : '查询统计' }}
        </button>
        <button class="btn-secondary" type="button" @click="exportCategory" :disabled="exporting">
          {{ exporting ? '导出中...' : '导出分类 CSV' }}
        </button>
        <button class="btn-secondary" type="button" @click="exportRanking" :disabled="exporting">
          {{ exporting ? '导出中...' : '导出排名 CSV' }}
        </button>
      </div>
      <p v-if="error" class="notice notice-error">{{ error }}</p>
    </div>

    <div class="grid-3">
      <div class="kpi">
        <div class="kpi-title">分类总学分</div>
        <div class="kpi-value">{{ totalCredit }}</div>
      </div>
      <div class="kpi">
        <div class="kpi-title">{{ filters.year }} 年月度学分总和</div>
        <div class="kpi-value">{{ monthlyCredit }}</div>
      </div>
      <div class="kpi">
        <div class="kpi-title">排名人数</div>
        <div class="kpi-value">{{ ranking.length }}</div>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-title">分类学分分布</div>
        <div v-for="item in categoryStats" :key="item.category" class="bar-row">
          <div>{{ item.category }}</div>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: toPercent(item.totalCredit, maxCategoryCredit) }"></div>
          </div>
          <div>{{ item.totalCredit }}</div>
        </div>
        <p v-if="!loading && categoryStats.length === 0" class="notice">暂无数据</p>
      </div>

      <div class="card">
        <div class="card-title">专业学分分布</div>
        <div v-for="item in majorStats" :key="item.dimension" class="bar-row">
          <div>{{ item.dimension }}</div>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: toPercent(item.totalCredit, maxMajorCredit) }"></div>
          </div>
          <div>{{ item.totalCredit }}</div>
        </div>
        <p v-if="!loading && majorStats.length === 0" class="notice">暂无数据</p>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-title">年级学分分布</div>
        <div v-for="item in gradeStats" :key="item.dimension" class="bar-row">
          <div>{{ item.dimension }}</div>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: toPercent(item.totalCredit, maxGradeCredit) }"></div>
          </div>
          <div>{{ item.totalCredit }}</div>
        </div>
        <p v-if="!loading && gradeStats.length === 0" class="notice">暂无数据</p>
      </div>

      <div class="card">
        <div class="card-title">{{ filters.year }} 年月度统计</div>
        <div v-for="item in monthlyStats" :key="item.month" class="bar-row">
          <div>{{ item.month }} 月</div>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: toPercent(item.totalCredit, maxMonthlyCredit) }"></div>
          </div>
          <div>{{ item.totalCredit }}</div>
        </div>
        <p v-if="!loading && monthlyStats.length === 0" class="notice">暂无数据</p>
      </div>
    </div>

    <div class="card">
      <div class="card-title">学生学分排名 Top {{ filters.topN }}</div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>排名</th>
              <th>学生ID</th>
              <th>学号</th>
              <th>姓名</th>
              <th>总学分</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in ranking" :key="item.studentId">
              <td>{{ item.rank }}</td>
              <td>{{ item.studentId }}</td>
              <td>{{ item.studentNo }}</td>
              <td>{{ item.studentName }}</td>
              <td>{{ item.totalCredit }}</td>
            </tr>
            <tr v-if="!loading && ranking.length === 0">
              <td colspan="5">暂无排名数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
