<script setup>
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { clearAuth, getRole } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const menus = [
  { name: 'dashboard', label: '概览' },
  { name: 'students', label: '学生管理' },
  { name: 'activities', label: '活动管理' },
  { name: 'apply', label: '学分申请' },
  { name: 'review', label: '审批中心' },
  { name: 'records', label: '学分记录' },
  { name: 'analytics', label: '统计分析' },
  { name: 'review-logs', label: '审批日志' },
]

const pageTitle = computed(() => route.meta.title || '第二课堂学分系统')
const role = computed(() => getRole() || 'ADMIN')

function logout() {
  clearAuth()
  router.replace('/login')
}
</script>

<template>
  <div class="shell">
    <aside class="sidenav">
      <div class="brand">
        <div class="brand-title">SecondClass</div>
        <div class="brand-sub">Credit Console</div>
      </div>
      <nav class="nav">
        <RouterLink
          v-for="item in menus"
          :key="item.name"
          :to="{ name: item.name }"
          class="nav-item"
          active-class="nav-item-active"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
    </aside>

    <main class="main">
      <header class="topbar">
        <h1 class="title">{{ pageTitle }}</h1>
        <div class="toolbar">
          <span class="role">角色：{{ role }}</span>
          <button class="btn-ghost" type="button" @click="logout">退出登录</button>
        </div>
      </header>
      <section class="content">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px 1fr;
}

.sidenav {
  border-right: 1px solid var(--line);
  background: linear-gradient(180deg, #f9fbff 0%, #f2f7ff 100%);
  padding: 18px 14px;
}

.brand {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 12px;
  background: #fff;
  margin-bottom: 14px;
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
}

.brand-sub {
  font-size: 12px;
  color: var(--muted);
}

.nav {
  display: grid;
  gap: 8px;
}

.nav-item {
  padding: 9px 10px;
  border-radius: 10px;
  color: var(--muted);
  font-size: 14px;
}

.nav-item:hover {
  background: #e8efff;
  color: var(--primary);
}

.nav-item-active {
  background: var(--primary);
  color: #fff;
}

.main {
  min-width: 0;
  display: grid;
  grid-template-rows: auto 1fr;
  padding: 18px;
  gap: 16px;
}

.topbar {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 12px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.role {
  font-size: 13px;
  color: var(--muted);
}

.content {
  min-width: 0;
}

@media (max-width: 980px) {
  .shell {
    grid-template-columns: 1fr;
  }

  .sidenav {
    border-right: none;
    border-bottom: 1px solid var(--line);
  }

  .nav {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .main {
    padding: 12px;
  }

  .topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .nav {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
