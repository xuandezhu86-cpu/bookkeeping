<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapsed ? '64px' : '220px'" class="sidebar">
      <div class="sidebar-inner">
        <!-- Logo -->
        <div class="logo" :class="{ collapsed: isCollapsed }">
          <el-icon :size="isCollapsed ? 28 : 24" color="#409eff">
            <Coin />
          </el-icon>
          <span v-show="!isCollapsed" class="logo-text">悠然记账</span>
        </div>

        <!-- 用户信息 -->
        <div class="sidebar-user" v-show="!isCollapsed">
          <el-avatar :size="36" :icon="UserFilled" />
          <div class="user-info-text">
            <div class="user-name">{{ authStore.user?.nickname || authStore.user?.username }}</div>
            <div class="user-role">个人用户</div>
          </div>
        </div>

        <!-- 导航菜单 -->
        <el-menu
          :router="true"
          :default-active="route.path"
          :collapse="isCollapsed"
          :collapse-transition="false"
          class="sidebar-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>首页概览</span>
          </el-menu-item>
          <el-menu-item index="/records">
            <el-icon><Document /></el-icon>
            <span>消费记录</span>
          </el-menu-item>
          <el-menu-item index="/categories">
            <el-icon><Collection /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/reports">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据报表</span>
          </el-menu-item>
          <el-menu-item index="/settings">
            <el-icon><Setting /></el-icon>
            <span>设置</span>
          </el-menu-item>
        </el-menu>

        <!-- 折叠按钮 -->
        <div class="collapse-btn" @click="toggleCollapse">
          <el-icon :size="18" color="#bfcbd9">
            <Fold v-if="!isCollapsed" />
            <Expand v-else />
          </el-icon>
        </div>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumbTitle">{{ breadcrumbTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <!-- 通知 -->
          <el-badge :hidden="true" class="notice-badge">
            <el-icon :size="20" class="header-icon">
              <Bell />
            </el-icon>
          </el-badge>

          <!-- 用户 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="user-name-text">{{ authStore.user?.nickname || authStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import {
  Odometer, Document, Collection, DataAnalysis, Setting,
  ArrowDown, UserFilled, Bell, Fold, Expand, SwitchButton, Coin
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const breadcrumbMap: Record<string, string> = {
  '/dashboard': '首页概览',
  '/records': '消费记录',
  '/categories': '分类管理',
  '/reports': '数据报表',
  '/settings': '设置',
}

const breadcrumbTitle = computed(() => breadcrumbMap[route.path] || '')

// 侧边栏折叠
const isCollapsed = ref(false)

onMounted(() => {
  try {
    const saved = localStorage.getItem('sidebar-collapsed')
    if (saved !== null) isCollapsed.value = saved === 'true'
  } catch { /* ignore */ }
})

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
  try { localStorage.setItem('sidebar-collapsed', String(isCollapsed.value)) } catch { /* ignore */ }
}

function handleCommand(command: string) {
  if (command === 'logout') {
    authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
/* ─── 布局容器 ─────────────────────────────────────── */
.main-layout {
  height: 100vh;
}

/* ─── 侧边栏 ─────────────────────────────────────────── */
.sidebar {
  background: var(--sidebar-bg);
  overflow: hidden;
  transition: width var(--transition-normal);
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;
}

.sidebar-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 220px;
  overflow: hidden;
}

/* Logo */
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  color: #fff;
  background: var(--sidebar-logo-bg);
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.logo.collapsed {
  gap: 0;
}

/* 用户信息 */
.sidebar-user {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md);
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.user-info-text {
  overflow: hidden;
}

.user-name {
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  color: var(--sidebar-text);
  font-size: 12px;
  margin-top: 2px;
}

/* 菜单 */
.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  border-right: none !important;
  padding-top: var(--spacing-xs);
  background-color: transparent !important;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

/* Element Plus 菜单暗色主题样式覆盖 */
.sidebar-menu :deep(.el-menu-item) {
  color: var(--sidebar-text) !important;
  background-color: transparent !important;
  border-radius: 0;
  margin: 0;
  height: 50px;
  line-height: 50px;
  transition: background var(--transition-fast), color var(--transition-fast);
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.08) !important;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  color: var(--sidebar-active-text) !important;
  background-color: rgba(64, 158, 255, 0.1) !important;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  color: inherit !important;
}

.sidebar-menu :deep(.el-menu--collapse) .sidebar-menu :deep(.el-menu-item) {
  justify-content: center;
  padding: 0 !important;
}

/* 折叠按钮 */
.collapse-btn {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--sidebar-text);
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
  transition: background var(--transition-fast);
}

.collapse-btn:hover {
  background: rgba(255, 255, 255, 0.05);
}

/* ─── 头部 ───────────────────────────────────────────── */
.header {
  background: var(--header-bg);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: var(--header-height);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
}

.header-icon {
  color: var(--color-text-regular);
  cursor: pointer;
  transition: color var(--transition-fast);
}

.header-icon:hover {
  color: var(--color-primary);
}

.notice-badge {
  line-height: 1;
}

.user-info {
  cursor: pointer;
  color: var(--color-text-primary);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-name-text {
  font-size: 14px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ─── 内容区 ─────────────────────────────────────────── */
.main-content {
  background: var(--color-bg);
  padding: var(--spacing-lg);
  overflow-y: auto;
}
</style>
