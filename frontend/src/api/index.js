import http from './http'

function cleanParams(params) {
  return Object.fromEntries(
    Object.entries(params || {}).filter(([, value]) => value !== '' && value !== null && value !== undefined),
  )
}

export function login(payload) {
  return http.post('/auth/login', payload)
}

export function createStudent(payload) {
  return http.post('/students', payload)
}

export function getStudentById(id) {
  return http.get(`/students/${id}`)
}

export function listStudents() {
  return http.get('/students')
}

export function createActivity(payload) {
  return http.post('/activities', payload)
}

export function listActivities() {
  return http.get('/activities')
}

export function applyCredit(payload) {
  return http.post('/credits/apply', payload)
}

export function approveCredit(recordId, remark) {
  return http.post(`/credits/${recordId}/approve`, { remark })
}

export function rejectCredit(recordId, remark) {
  return http.post(`/credits/${recordId}/reject`, { remark })
}

export function batchApprove(payload) {
  return http.post('/credits/batch/approve', payload)
}

export function batchReject(payload) {
  return http.post('/credits/batch/reject', payload)
}

export function getStudentSummary(studentId) {
  return http.get(`/credits/students/${studentId}/summary`)
}

export function getStudentRecordsPage(studentId, params) {
  return http.get(`/credits/students/${studentId}/records/page`, { params: cleanParams(params) })
}

export function getPendingRecordsPage(params) {
  return http.get('/credits/pending/page', { params: cleanParams(params) })
}

export function getReviewLogsPage(params) {
  return http.get('/credits/review-logs/page', { params: cleanParams(params) })
}

export function getReviewLogStats(params) {
  return http.get('/credits/review-logs/stats', { params: cleanParams(params) })
}

export function exportReviewLogsCsv(params) {
  return http.get('/credits/review-logs/export', {
    params: cleanParams(params),
    responseType: 'blob',
  })
}

export function getCategoryStats() {
  return http.get('/credits/analytics/categories')
}

export function getMajorStats() {
  return http.get('/credits/analytics/majors')
}

export function getGradeStats() {
  return http.get('/credits/analytics/grades')
}

export function getMonthlyStats(year) {
  return http.get('/credits/analytics/monthly', { params: { year } })
}

export function getRanking(topN) {
  return http.get('/credits/analytics/ranking', { params: { topN } })
}

export function exportCategoryStatsCsv() {
  return http.get('/credits/analytics/export/categories', {
    responseType: 'blob',
  })
}

export function exportRankingCsv(topN) {
  return http.get('/credits/analytics/export/ranking', {
    params: { topN },
    responseType: 'blob',
  })
}
