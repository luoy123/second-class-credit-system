<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createStudent, getStudentById, listStudents } from '../api'

const loading = ref(false)
const error = ref('')
const success = ref('')
const students = ref([])
const queryId = ref('')
const queryResult = ref(null)

const form = reactive({
  studentNo: '',
  name: '',
  major: '',
  grade: '',
})

async function loadStudents() {
  loading.value = true
  error.value = ''
  try {
    students.value = await listStudents()
  } catch (err) {
    error.value = err.message || '加载学生列表失败'
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  success.value = ''
  error.value = ''
  try {
    await createStudent(form)
    success.value = '学生创建成功'
    form.studentNo = ''
    form.name = ''
    form.major = ''
    form.grade = ''
    await loadStudents()
  } catch (err) {
    error.value = err.message || '创建失败'
  }
}

async function queryStudent() {
  queryResult.value = null
  error.value = ''
  if (!queryId.value) return
  try {
    queryResult.value = await getStudentById(queryId.value)
  } catch (err) {
    error.value = err.message || '查询失败'
  }
}

onMounted(loadStudents)
</script>

<template>
  <div class="page">
    <div class="card">
      <div class="card-title">新增学生</div>
      <form class="form-grid" @submit.prevent="submitCreate">
        <div class="field">
          <label>学号</label>
          <input v-model.trim="form.studentNo" required />
        </div>
        <div class="field">
          <label>姓名</label>
          <input v-model.trim="form.name" required />
        </div>
        <div class="field">
          <label>专业</label>
          <input v-model.trim="form.major" />
        </div>
        <div class="field">
          <label>年级</label>
          <input v-model.trim="form.grade" />
        </div>
        <div class="actions">
          <button type="submit" class="btn-primary">保存学生</button>
        </div>
      </form>
      <p v-if="success" class="notice notice-success">{{ success }}</p>
      <p v-if="error" class="notice notice-error">{{ error }}</p>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-title">按 ID 查询学生</div>
        <div class="actions">
          <input v-model.trim="queryId" type="number" placeholder="输入学生ID" />
          <button class="btn-secondary" type="button" @click="queryStudent">查询</button>
        </div>
        <div v-if="queryResult" class="card" style="margin-top: 12px">
          <div>ID：{{ queryResult.id }}</div>
          <div>学号：{{ queryResult.studentNo }}</div>
          <div>姓名：{{ queryResult.name }}</div>
          <div>专业：{{ queryResult.major || '未设置' }}</div>
          <div>年级：{{ queryResult.grade || '未设置' }}</div>
        </div>
      </div>

      <div class="card">
        <div class="card-title">学生列表</div>
        <div class="actions">
          <button class="btn-ghost" type="button" @click="loadStudents" :disabled="loading">
            {{ loading ? '加载中...' : '刷新' }}
          </button>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>学号</th>
                <th>姓名</th>
                <th>专业</th>
                <th>年级</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in students" :key="item.id">
                <td>{{ item.id }}</td>
                <td>{{ item.studentNo }}</td>
                <td>{{ item.name }}</td>
                <td>{{ item.major || '未设置' }}</td>
                <td>{{ item.grade || '未设置' }}</td>
              </tr>
              <tr v-if="!loading && students.length === 0">
                <td colspan="5">暂无学生数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>
