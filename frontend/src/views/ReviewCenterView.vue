<script setup>
import { reactive, ref } from 'vue'
import { approveCredit, batchApprove, batchReject, getPendingRecordsPage, rejectCredit } from '../api'

const loading = ref(false)
const error = ref('')
const resultHint = ref('')
const selectedIds = ref([])
const pageData = ref({
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  content: [],
})

const filters = reactive({
  page: 0,
  size: 10,
  batchRemark: '',
})

function toggleSelect(id) {
  if (selectedIds.value.includes(id)) {
    selectedIds.value = selectedIds.value.filter((item) => item !== id)
  } else {
    selectedIds.value = [...selectedIds.value, id]
  }
}

async function loadPending() {
  loading.value = true
  error.value = ''
  try {
    pageData.value = await getPendingRecordsPage({ page: filters.page, size: filters.size })
    selectedIds.value = []
  } catch (err) {
    error.value = err.message || '加载待审批记录失败'
  } finally {
    loading.value = false
  }
}

async function approveOne(id) {
  const remark = window.prompt('请输入审批备注（可空）', '审核通过') || ''
  try {
    await approveCredit(id, remark)
    resultHint.value = `记录 ${id} 已通过`
    await loadPending()
  } catch (err) {
    error.value = err.message || '审批失败'
  }
}

async function rejectOne(id) {
  const remark = window.prompt('请输入驳回原因', '材料不完整')
  if (remark === null) return
  try {
    await rejectCredit(id, remark)
    resultHint.value = `记录 ${id} 已驳回`
    await loadPending()
  } catch (err) {
    error.value = err.message || '驳回失败'
  }
}

async function batchApproveAction() {
  if (selectedIds.value.length === 0) return
  try {
    const result = await batchApprove({
      recordIds: selectedIds.value,
      remark: filters.batchRemark,
    })
    resultHint.value = `批量通过完成：成功 ${result.success} / 总计 ${result.total}`
    await loadPending()
  } catch (err) {
    error.value = err.message || '批量通过失败'
  }
}

async function batchRejectAction() {
  if (selectedIds.value.length === 0) return
  try {
    const result = await batchReject({
      recordIds: selectedIds.value,
      remark: filters.batchRemark,
    })
    resultHint.value = `批量驳回完成：成功 ${result.success} / 总计 ${result.total}`
    await loadPending()
  } catch (err) {
    error.value = err.message || '批量驳回失败'
  }
}

loadPending()
</script>

<template>
  <div class="page">
    <div class="card">
      <div class="card-title">审批操作</div>
      <div class="actions">
        <input v-model.trim="filters.batchRemark" placeholder="批量操作备注（可空）" />
        <button class="btn-secondary" type="button" @click="batchApproveAction">批量通过</button>
        <button class="btn-danger" type="button" @click="batchRejectAction">批量驳回</button>
        <button class="btn-ghost" type="button" @click="loadPending" :disabled="loading">
          {{ loading ? '刷新中...' : '刷新待审批' }}
        </button>
      </div>
      <p v-if="error" class="notice notice-error">{{ error }}</p>
      <p v-if="resultHint" class="notice notice-success">{{ resultHint }}</p>
    </div>

    <div class="card">
      <div class="card-title">待审批记录</div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>
                选择
              </th>
              <th>ID</th>
              <th>学生ID</th>
              <th>活动ID</th>
              <th>分类</th>
              <th>学分</th>
              <th>备注</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in pageData.content" :key="item.id">
              <td>
                <input
                  type="checkbox"
                  :checked="selectedIds.includes(item.id)"
                  @change="toggleSelect(item.id)"
                />
              </td>
              <td>{{ item.id }}</td>
              <td>{{ item.studentId }}</td>
              <td>{{ item.activityId }}</td>
              <td>{{ item.category }}</td>
              <td>{{ item.credit }}</td>
              <td>{{ item.remark || '-' }}</td>
              <td>{{ item.createdAt }}</td>
              <td class="row-actions">
                <button class="btn-secondary" type="button" @click="approveOne(item.id)">通过</button>
                <button class="btn-danger" type="button" @click="rejectOne(item.id)">驳回</button>
              </td>
            </tr>
            <tr v-if="!loading && pageData.content.length === 0">
              <td colspan="9">暂无待审批记录</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination" style="margin-top: 12px">
        <div>共 {{ pageData.totalElements }} 条</div>
        <div class="actions">
          <button class="btn-ghost" :disabled="filters.page <= 0" @click="filters.page -= 1; loadPending()">上一页</button>
          <span>第 {{ (filters.page || 0) + 1 }} / {{ pageData.totalPages || 1 }} 页</span>
          <button
            class="btn-ghost"
            :disabled="(filters.page || 0) + 1 >= (pageData.totalPages || 1)"
            @click="filters.page += 1; loadPending()"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
