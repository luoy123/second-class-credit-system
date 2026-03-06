<script setup>
import { reactive, ref } from 'vue'
import { exportReviewLogsCsv, getReviewLogsPage, getReviewLogStats } from '../api'

const loading = ref(false)
const exporting = ref(false)
const error = ref('')

const pageData = ref({
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  content: [],
})

const stats = ref({
  totalCount: 0,
  successCount: 0,
  failedCount: 0,
  approveCount: 0,
  rejectCount: 0,
  successRate: 0,
})

const filters = reactive({
  recordId: '',
  action: '',
  success: '',
  startDate: '',
  endDate: '',
  page: 0,
  size: 10,
  limit: 1000,
})

function buildParams(withPage = true) {
  const params = {}
  if (filters.recordId !== '' && filters.recordId !== null) {
    params.recordId = Number(filters.recordId)
  }
  if (filters.action) {
    params.action = filters.action
  }
  if (filters.success !== '') {
    params.success = filters.success === 'true'
  }
  if (filters.startDate) {
    params.startDate = filters.startDate
  }
  if (filters.endDate) {
    params.endDate = filters.endDate
  }
  if (withPage) {
    params.page = filters.page
    params.size = filters.size
  }
  return params
}

async function loadData() {
  loading.value = true
  error.value = ''
  try {
    const [pageResult, statResult] = await Promise.all([
      getReviewLogsPage(buildParams(true)),
      getReviewLogStats(buildParams(false)),
    ])
    pageData.value = pageResult
    stats.value = statResult
  } catch (err) {
    error.value = err.message || '加载审批日志失败'
  } finally {
    loading.value = false
  }
}

function search() {
  filters.page = 0
  loadData()
}

function prevPage() {
  if (filters.page <= 0) {
    return
  }
  filters.page -= 1
  loadData()
}

function nextPage() {
  if ((filters.page || 0) + 1 >= (pageData.value.totalPages || 1)) {
    return
  }
  filters.page += 1
  loadData()
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

async function exportCsv() {
  exporting.value = true
  error.value = ''
  try {
    const limit = Math.min(Math.max(Number(filters.limit) || 1000, 1), 5000)
    filters.limit = limit
    const blob = await exportReviewLogsCsv({
      ...buildParams(false),
      limit,
    })
    downloadBlob(blob, 'credit_review_logs.csv')
  } catch (err) {
    error.value = err.message || '导出审批日志失败'
  } finally {
    exporting.value = false
  }
}

loadData()
</script>

<template>
  <div class="page">
    <div class="card">
      <div class="card-title">筛选条件</div>
      <div class="form-grid">
        <div class="field">
          <label>记录 ID</label>
          <input v-model="filters.recordId" type="number" min="1" placeholder="可选" />
        </div>
        <div class="field">
          <label>审批动作</label>
          <select v-model="filters.action">
            <option value="">全部</option>
            <option value="APPROVE">APPROVE</option>
            <option value="REJECT">REJECT</option>
          </select>
        </div>
        <div class="field">
          <label>是否成功</label>
          <select v-model="filters.success">
            <option value="">全部</option>
            <option value="true">成功</option>
            <option value="false">失败</option>
          </select>
        </div>
        <div class="field">
          <label>开始日期</label>
          <input v-model="filters.startDate" type="date" />
        </div>
        <div class="field">
          <label>结束日期</label>
          <input v-model="filters.endDate" type="date" />
        </div>
        <div class="field">
          <label>每页条数</label>
          <select v-model.number="filters.size">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
        </div>
      </div>
      <div class="actions" style="margin-top: 12px">
        <button class="btn-primary" type="button" @click="search" :disabled="loading">
          {{ loading ? '查询中...' : '查询日志' }}
        </button>
        <button class="btn-ghost" type="button" @click="loadData" :disabled="loading">
          刷新
        </button>
        <input
          v-model.number="filters.limit"
          type="number"
          min="1"
          max="5000"
          placeholder="导出条数上限"
          style="max-width: 160px"
        />
        <button class="btn-secondary" type="button" @click="exportCsv" :disabled="exporting">
          {{ exporting ? '导出中...' : '导出 CSV' }}
        </button>
      </div>
      <p v-if="error" class="notice notice-error">{{ error }}</p>
    </div>

    <div class="grid-3">
      <div class="kpi">
        <div class="kpi-title">日志总数</div>
        <div class="kpi-value">{{ stats.totalCount }}</div>
      </div>
      <div class="kpi">
        <div class="kpi-title">成功 / 失败</div>
        <div class="kpi-value">{{ stats.successCount }} / {{ stats.failedCount }}</div>
      </div>
      <div class="kpi">
        <div class="kpi-title">成功率</div>
        <div class="kpi-value">{{ stats.successRate }}%</div>
      </div>
    </div>

    <div class="card">
      <div class="card-title">审批日志分页列表</div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>记录ID</th>
              <th>动作</th>
              <th>操作角色</th>
              <th>结果</th>
              <th>备注</th>
              <th>明细</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in pageData.content" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.recordId }}</td>
              <td>
                <span
                  class="status-pill"
                  :class="item.action === 'APPROVE' ? 'status-approved' : 'status-rejected'"
                >
                  {{ item.action }}
                </span>
              </td>
              <td>{{ item.operatorRole }}</td>
              <td>
                <span class="status-pill" :class="item.success ? 'status-approved' : 'status-rejected'">
                  {{ item.success ? 'SUCCESS' : 'FAILED' }}
                </span>
              </td>
              <td>{{ item.remark || '-' }}</td>
              <td>{{ item.detail || '-' }}</td>
              <td>{{ item.createdAt }}</td>
            </tr>
            <tr v-if="!loading && pageData.content.length === 0">
              <td colspan="8">暂无审批日志</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination" style="margin-top: 12px">
        <div>共 {{ pageData.totalElements }} 条</div>
        <div class="actions">
          <button class="btn-ghost" :disabled="filters.page <= 0" @click="prevPage">上一页</button>
          <span>第 {{ (filters.page || 0) + 1 }} / {{ pageData.totalPages || 1 }} 页</span>
          <button
            class="btn-ghost"
            :disabled="(filters.page || 0) + 1 >= (pageData.totalPages || 1)"
            @click="nextPage"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
