import axios from 'axios'
import { clearAuth, getToken } from '../utils/auth'

const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload && typeof payload === 'object' && Object.prototype.hasOwnProperty.call(payload, 'code')) {
      if (payload.code === 0) {
        return payload.data
      }
      if ([4010, 4030, 4031, 4032].includes(payload.code)) {
        clearAuth()
      }
      const err = new Error(payload.message || '请求失败')
      err.code = payload.code
      throw err
    }
    return payload
  },
  (error) => {
    throw error
  },
)

export default http
