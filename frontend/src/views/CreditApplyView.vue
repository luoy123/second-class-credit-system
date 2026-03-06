<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { applyCredit, getStudentRecordsPage, listActivities, listStudents } from '../api'

const loading = ref(false)
const records = ref([])
const error = ref('')
const success = ref('')

const students = ref([])
const activities = ref([])

const form = reactive({
  studentId: '',
  activityId: '',
  category: '',
  remark: '',
})

watch(
  () => form.activityId,
  () => {
    const hit = activities.value.find((item) => item.id === Number(form.activityId))
    if (hit) {
      form.category = hit.category
    }
  },
)

async function loadBase() {
  try {
    const [studentList, activityList] = await Promise.all([listStudents(), listActivities()])
    students.value = studentList
    activities.value = activityList
  } catch (err) {
    error.value = err.message || '初始化数据失败'
  }
}

async function loadRecords() {
  if (!form.studentId) {
    records.value = []
    return
  }
  try {
    const page = await getStudentRecordsPage(form.studentId, { page: 0, size: 10 })
    records.value = page.content || []
  } catch (err) {
    error.value = err.message || '查询记录失败'
  }
}

async function submitApply() {
  loading.value = true
  error.value = ''
  success.value = ''
  try {
    const result = await applyCredit({
      studentId: Number(form.studentId),
      activityId: Number(form.activityId),
      category: form.category,
      remark: form.remark,
    })
    success.value = `申请成功，记录ID：${result.id}`
    form.remark = ''
    await loadRecords()
  } catch (err) {
    error.value = err.message || '申请失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadBase)
</script>

<template>
  <div class="page">
    <div class="card">
      <div class="card-title">学分申请</div>
      <form class="form-grid" @submit.prevent="submitApply">
        <div class="field">
          <label>学生</label>
          <select v-model="form.studentId" required @change="loadRecords">
            <option value="">请选择学生</option>
            <option v-for="item in students" :key="item.id" :value="item.id">
              {{ item.studentNo }} - {{ item.name }}
            </option>
          </select>
        </div>
        <div class="field">
          <label>活动</label>
          <select v-model="form.activityId" required>
            <option value="">请选择活动</option>
            <option v-for="item in activities" :key="item.id" :value="item.id">
              {{ item.title }}（{{ item.category }}）
            </option>
          </select>
        </div>
        <div class="field">
          <label>分类</label>
          <input v-model.trim="form.category" required />
        </div>
        <div class="field">
          <label>备注</label>
          <textarea v-model.trim="form.remark" />
        </div>
        <div class="actions">
          <button class="btn-primary" type="submit" :disabled="loading">
            {{ loading ? '提交中...' : '提交申请' }}
          </button>
        </div>
      </form>
      <p v-if="success" class="notice notice-success">{{ success }}</p>
      <p v-if="error" class="notice notice-error">{{ error }}</p>
    </div>

    <div class="card">
      <div class="card-title">该学生最近 10 条申请</div>
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
            <tr v-for="item in records" :key="item.id">
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
            <tr v-if="records.length === 0">
              <td colspan="7">请先选择学生，或暂无申请记录</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
