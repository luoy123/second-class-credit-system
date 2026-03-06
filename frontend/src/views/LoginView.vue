<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api'
import { setAuth } from '../utils/auth'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const form = reactive({
  username: 'admin',
  password: 'admin123',
})

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const result = await login(form)
    setAuth(result.token, result.role)
    router.replace('/dashboard')
  } catch (err) {
    error.value = err.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-wrap">
    <div class="login-panel">
      <div class="login-header">
        <h1>第二课堂学分系统</h1>
        <p>前后端分离管理端（Vue + Spring Boot）</p>
      </div>

      <div class="card">
        <div class="card-title">管理员登录</div>
        <form class="form-grid" @submit.prevent="submit">
          <div class="field">
            <label>用户名</label>
            <input v-model.trim="form.username" placeholder="请输入用户名" />
          </div>
          <div class="field">
            <label>密码</label>
            <input v-model.trim="form.password" type="password" placeholder="请输入密码" />
          </div>
          <div class="actions">
            <button type="submit" class="btn-primary" :disabled="loading">
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </div>
        </form>
        <p v-if="error" class="notice notice-error">{{ error }}</p>
        <p class="hint">默认账号：admin / admin123（可用后端环境变量覆盖）</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-wrap {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 20px;
}

.login-panel {
  width: min(520px, 100%);
  display: grid;
  gap: 16px;
}

.login-header {
  text-align: left;
}

.login-header h1 {
  font-size: 34px;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.login-header p {
  margin: 0;
  color: var(--muted);
}

.hint {
  color: var(--muted);
  font-size: 13px;
  margin-top: 8px;
}
</style>
