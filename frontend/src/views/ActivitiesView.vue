<script setup>
import { onMounted, reactive, ref } from 'vue'
import { createActivity, listActivities } from '../api'

const loading = ref(false)
const error = ref('')
const success = ref('')
const activities = ref([])

const form = reactive({
  title: '',
  category: '',
  organizer: '',
  activityDate: '',
  maxCredit: '',
})

async function loadActivities() {
  loading.value = true
  error.value = ''
  try {
    activities.value = await listActivities()
  } catch (err) {
    error.value = err.message || '加载活动失败'
  } finally {
    loading.value = false
  }
}

async function submitCreate() {
  success.value = ''
  error.value = ''
  try {
    await createActivity({
      ...form,
      maxCredit: Number(form.maxCredit),
    })
    success.value = '活动创建成功'
    form.title = ''
    form.category = ''
    form.organizer = ''
    form.activityDate = ''
    form.maxCredit = ''
    await loadActivities()
  } catch (err) {
    error.value = err.message || '创建失败'
  }
}

onMounted(loadActivities)
</script>

<template>
  <div class="page">
    <div class="card">
      <div class="card-title">新增活动</div>
      <form class="form-grid" @submit.prevent="submitCreate">
        <div class="field">
          <label>活动名称</label>
          <input v-model.trim="form.title" required />
        </div>
        <div class="field">
          <label>活动分类</label>
          <input v-model.trim="form.category" required />
        </div>
        <div class="field">
          <label>组织单位</label>
          <input v-model.trim="form.organizer" />
        </div>
        <div class="field">
          <label>活动日期</label>
          <input v-model="form.activityDate" type="date" required />
        </div>
        <div class="field">
          <label>最大学分</label>
          <input v-model="form.maxCredit" type="number" min="0.01" step="0.01" required />
        </div>
        <div class="actions">
          <button type="submit" class="btn-primary">保存活动</button>
        </div>
      </form>
      <p v-if="success" class="notice notice-success">{{ success }}</p>
      <p v-if="error" class="notice notice-error">{{ error }}</p>
    </div>

    <div class="card">
      <div class="card-title">活动列表</div>
      <div class="actions">
        <button class="btn-ghost" type="button" @click="loadActivities" :disabled="loading">
          {{ loading ? '加载中...' : '刷新' }}
        </button>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>名称</th>
              <th>分类</th>
              <th>组织单位</th>
              <th>日期</th>
              <th>最大学分</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in activities" :key="item.id">
              <td>{{ item.id }}</td>
              <td>{{ item.title }}</td>
              <td>{{ item.category }}</td>
              <td>{{ item.organizer || '未设置' }}</td>
              <td>{{ item.activityDate }}</td>
              <td>{{ item.maxCredit }}</td>
            </tr>
            <tr v-if="!loading && activities.length === 0">
              <td colspan="6">暂无活动数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
