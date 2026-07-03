<template>
  <div class="auth-container">
    <!-- 动画背景装饰 -->
    <div class="auth-bg">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>

    <transition name="auth-card" appear>
      <el-card class="auth-card" shadow="never">
        <!-- Logo -->
        <div class="auth-logo">
          <el-icon :size="48" color="#409eff" class="auth-logo-icon">
            <Coin />
          </el-icon>
          <h2 class="auth-title">悠然记账</h2>
          <p class="auth-subtitle">创建你的个人账本</p>
        </div>

        <!-- 注册表单 -->
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input
              v-model="form.nickname"
              placeholder="昵称"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="确认密码"
              :prefix-icon="Lock"
              show-password
              size="large"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="auth-btn" @click="handleRegister" :loading="loading" size="large">
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </el-card>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { User, Lock, Coin } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (value !== form.password) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await authStore.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
    })
    ElMessage.success('注册成功')
    router.push('/dashboard')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

/* ─── 动画背景几何形状 ────────────────────────────── */
.auth-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;
}

.shape-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, #fff 0%, transparent 70%);
  top: -150px;
  right: -100px;
  animation: float 12s ease-in-out infinite;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #fff 0%, transparent 70%);
  bottom: -100px;
  left: -80px;
  animation: float-reverse 15s ease-in-out infinite;
}

.shape-3 {
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.2);
  top: 50%;
  left: 20%;
  transform: rotate(45deg);
  border-radius: 20%;
  animation: float 10s ease-in-out infinite;
}

/* ─── 玻璃拟态卡片 ────────────────────────────────── */
.auth-card {
  width: 420px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  padding: var(--spacing-xl) var(--spacing-xl) var(--spacing-lg);
  position: relative;
  z-index: 1;
}

/* Auth 卡片入场动画 */
.auth-card-enter-active {
  animation: scale-in 0.5s ease;
}

/* ─── Logo 区域 ────────────────────────────────────── */
.auth-logo {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.auth-logo-icon {
  margin-bottom: var(--spacing-sm);
}

.auth-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-xs);
}

.auth-subtitle {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0;
}

/* ─── 表单增强 ──────────────────────────────────────── */
.auth-card :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  transition: box-shadow var(--transition-fast);
  padding: 0 12px;
}

.auth-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--color-primary-light);
}

.auth-card :deep(.el-input__inner) {
  height: 44px;
}

.auth-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: var(--radius-md);
  margin-top: var(--spacing-sm);
}

/* ─── 底部链接 ──────────────────────────────────────── */
.auth-footer {
  text-align: center;
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-top: var(--spacing-lg);
}

.auth-footer a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.auth-footer a:hover {
  text-decoration: underline;
}

/* ─── 暗色模式 ────────────────────────────────────────── */
html.dark .auth-container {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
}

html.dark .auth-card {
  background: rgba(30, 30, 60, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}
</style>
