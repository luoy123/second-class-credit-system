const TOKEN_KEY = 'second_class_token'
const ROLE_KEY = 'second_class_role'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function getRole() {
  return localStorage.getItem(ROLE_KEY) || ''
}

export function setAuth(token, role) {
  localStorage.setItem(TOKEN_KEY, token || '')
  localStorage.setItem(ROLE_KEY, role || 'ADMIN')
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ROLE_KEY)
}
