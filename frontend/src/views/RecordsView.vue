<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getStudentRecordsPage, getStudentSummary, listStudents } from '../api'

const loading = ref(false)
const error = ref('')
const students = ref([])
const summary = ref(null)
const pageData = ref({
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  content: [],
})

const filters = reactive({
  studentId: '',
  status: '',
  category: '',
  startDate: '',
  endDate: '',
  page: 0,
  size: 10,
})

async function loadStudents() {
  students.value = await listStudents()
  if (!filters.studentId && students.value.length > 0) {
    filters.studentId = String(students.value[0].id)
  }
}

async function query() {
  if (!filters.studentId) return
  loading.value = true
  error.value = ''
  try {
    const [recordPage, studentSummary] = await Promise.all([
      getStudentRecordsPage(filters.studentId, {
        status: filters.status,
        category: filters.category,
        startDate: filters.startDate,
        endDate: filters.endDate,
        page: filters.page,
        size: filters.size,
      }),
      getStudentSummary(filters.studentId),
    ])
    pageData.value = recordPage
    summary.value = studentSummary
  } catch (err) {
    error.value = err.message || '查询失败'
  } finally {
    loading.value = false
  }
}

async function init() {
  try {
    await loadStudents()
    await query()
  } catch (err) {
    error.value = err.message || '初始化失败'
  }
}

onMounted(init)
</script>

<template>
  <div class="page">
    <div class="card">
      <div class="card-title">筛选条件</div>
      <div class="form-grid">
        <div class="field">
          <label>学生</label>
          <select v-model="filters.studentId">
            <option value="">请选择学生</option>
            <option v-for="item in students" :key="item.id" :value="item.id">
              {{ item.studentNo }} - {{ item.name }}
            </option>
          </select>
        </div>
        <div class="field">
          <label>状态</label>
          <select v-model="filters.status">
            <option value="">全部</option>
            <option value="PENDING">PENDING</option>
            <option value="APPROVED">APPROVED</option>
            <option value="REJECTED">REJECTED</option>
          </select>
        </div>
        <div class="field">
          <label>分类</label>
          <input v-model.trim="filters.category" placeholder="如：志愿服务" />
        </div>
        <div class="field">
          <label>开始日期</label>
          <input v-model="filters.startDate" type="date" />
        </div>
        <div class="field">
          <label>结束日期</label>
          <input v-model="filters.endDate" type="date" />
        </div>
      </div>
      <div class="actions" style="margin-top: 12px">
        <button class="btn-primary" type="button" @click="filters.page = 0; query()">查询</button>
      </div>
      <p v-if="error" class="notice notice-error">{{ error }}</p>
    </div>

    <div v-if="summary" class="card">
      <div class="card-title">学生学分汇总</div>
      <div class="grid-3">
        <div class="kpi">
          <div class="kpi-title">姓名</div>
          <div class="kpi-value">{{ summary.studentName }}</div>
        </div>
        <div class="kpi">
          <div class="kpi-title">学号</div>
          <div class="kpi-value">{{ summary.studentNo }}</div>
        </div>
        <div class="kpi">
          <div class="kpi-title">总学分</div>
          <div class="kpi-value">{{ summary.totalCredit }}</div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-title">分页记录</div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>活动ID</th>
              <th>分类</th>
              <th>学分</th>
              <th>状态</th>
              <th>备注</th>
              <th>创建时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in pageData.content" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.activityId }}</td>
              <td>{{ item.category }}</td>
              <td>{{ item.credit }}</td>
              <td>
                <span class="status-pill" :class="`status-${(item.status || '').toLowerCase()}`">
                  {{ item.status }}
                </span>
              </td>
              <td>{{ item.remark || '-' }}</td>
              <td>{{ item.createdAt }}</td>
            </tr>
            <tr v-if="!loading && pageData.content.length === 0">
              <td colspan="7">暂无记录</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination" style="margin-top: 12px">
        <div>共 {{ pageData.totalElements }} 条</div>
        <div class="actions">
          <button class="btn-ghost" :disabled="filters.page <= 0" @click="filters.page -= 1; query()">上一页</button>
          <span>第 {{ (filters.page || 0) + 1 }} / {{ pageData.totalPages || 1 }} 页</span>
          <button
            class="btn-ghost"
            :disabled="(filters.page || 0) + 1 >= (pageData.totalPages || 1)"
            @click="filters.page += 1; query()"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
