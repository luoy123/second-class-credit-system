<script setup>
import { computed, onMounted, ref } from 'vue'
import { getCategoryStats, getRanking, getReviewLogStats } from '../api'

const loading = ref(false)
const error = ref('')
const categoryStats = ref([])
const ranking = ref([])
const reviewStats = ref({
  totalCount: 0,
  successCount: 0,
  failedCount: 0,
  approveCount: 0,
  rejectCount: 0,
  successRate: 0,
})

const totalCredit = computed(() =>
  categoryStats.value.reduce((sum, item) => sum + Number(item.totalCredit || 0), 0).toFixed(2),
)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [categories, top, stats] = await Promise.all([
      getCategoryStats(),
      getRanking(5),
      getReviewLogStats({}),
    ])
    categoryStats.value = categories
    ranking.value = top
    reviewStats.value = stats
  } catch (err) {
    error.value = err.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="grid-3">
      <div class="kpi">
        <div class="kpi-title">分类总学分</div>
        <div class="kpi-value">{{ totalCredit }}</div>
      </div>
      <div class="kpi">
        <div class="kpi-title">审批总数</div>
        <div class="kpi-value">{{ reviewStats.totalCount }}</div>
      </div>
      <div class="kpi">
        <div class="kpi-title">审批成功率</div>
        <div class="kpi-value">{{ reviewStats.successRate }}%</div>
      </div>
    </div>

    <p v-if="error" class="notice notice-error">{{ error }}</p>

    <div class="grid-2">
      <div class="card">
        <div class="card-title">分类学分</div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>分类</th>
                <th>总学分</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in categoryStats" :key="item.category">
                <td>{{ item.category }}</td>
                <td>{{ item.totalCredit }}</td>
              </tr>
              <tr v-if="!loading && categoryStats.length === 0">
                <td colspan="2">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-title">学生排名 Top 5</div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>排名</th>
                <th>学号</th>
                <th>姓名</th>
                <th>学分</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in ranking" :key="item.studentId">
                <td>{{ item.rank }}</td>
                <td>{{ item.studentNo }}</td>
                <td>{{ item.studentName }}</td>
                <td>{{ item.totalCredit }}</td>
              </tr>
              <tr v-if="!loading && ranking.length === 0">
                <td colspan="4">暂无数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="actions">
      <button type="button" class="btn-secondary" @click="load" :disabled="loading">
        {{ loading ? '刷新中...' : '刷新数据' }}
      </button>
    </div>
  </div>
</template>
