<template>
  <div class="profile-page">
    <el-card class="main-card">
      <template #header>
        <span class="header-title">
          <el-icon><UserFilled /></el-icon>
          个人中心
        </span>
      </template>

      <div class="profile-content">
        <!-- ─── 头像区域 ──────────────────────────── -->
        <el-card shadow="none" class="profile-card">
          <div class="avatar-section">
            <div class="avatar-wrapper" @click="triggerUpload">
              <el-avatar
                :size="100"
                :src="avatarUrl"
                class="profile-avatar"
              >
                <el-icon :size="48"><UserFilled /></el-icon>
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon :size="24"><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <div class="avatar-info">
              <h3>{{ authStore.user?.nickname || authStore.user?.username }}</h3>
              <p>{{ authStore.user?.username }}</p>
            </div>
          </div>
          <input
            ref="fileInputRef"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleFileChange"
          />
        </el-card>

        <!-- ─── 基本信息 ──────────────────────────── -->
        <el-card shadow="none" class="profile-card">
          <div class="section-title">
            <el-icon><Edit /></el-icon>
            基本信息
          </div>
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            label-width="80px"
            class="profile-form"
          >
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="50" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" maxlength="100" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingProfile" @click="handleSaveProfile">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- ─── 修改密码 ──────────────────────────── -->
        <el-card shadow="none" class="profile-card">
          <div class="section-title">
            <el-icon><Lock /></el-icon>
            修改密码
          </div>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            class="profile-form"
          >
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入旧密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码（至少6位）"
              />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请再次输入新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="savingPassword" @click="handleChangePassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </el-card>
  </div>

  <!-- 头像裁剪弹窗 -->
  <AvatarCropper
    :visible="cropDialogVisible"
    :image-url="cropImageUrl"
    @close="cropDialogVisible = false"
    @confirm="handleCropConfirm"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { UserFilled, Camera, Edit, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import AvatarCropper from '../../components/AvatarCropper.vue'

const authStore = useAuthStore()
const fileInputRef = ref<HTMLInputElement | null>(null)
const profileFormRef = ref<FormInstance | null>(null)
const passwordFormRef = ref<FormInstance | null>(null)
const savingProfile = ref(false)
const savingPassword = ref(false)
const cropDialogVisible = ref(false)
const cropImageUrl = ref('')

// 头像 URL
const avatarUrl = computed(() => {
  if (authStore.user?.avatar) {
    return `http://localhost:8080${authStore.user.avatar}`
  }
  return ''
})

// 基本信息表单
const profileForm = ref({
  username: '',
  nickname: '',
  email: '',
})

// 密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 密码验证规则
const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

onMounted(() => {
  if (authStore.user) {
    profileForm.value.username = authStore.user.username || ''
    profileForm.value.nickname = authStore.user.nickname || ''
    profileForm.value.email = authStore.user.email || ''
  }
  // 如果 store 中已经有用户信息但可能不是最新的，刷新一下
  authStore.fetchProfile().then(() => {
    if (authStore.user) {
      profileForm.value.username = authStore.user.username || ''
      profileForm.value.nickname = authStore.user.nickname || ''
      profileForm.value.email = authStore.user.email || ''
    }
  }).catch(() => {
    // 静默失败，使用已有数据
  })
})

// 触发文件选择
function triggerUpload() {
  fileInputRef.value?.click()
}

// 处理文件选择
function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  // 验证文件大小
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('头像文件不能超过 5MB')
    return
  }

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.error('仅支持图片文件')
    return
  }

  // 生成临时 URL 并打开裁剪弹窗
  cropImageUrl.value = URL.createObjectURL(file)
  cropDialogVisible.value = true

  // 清空 input 以便再次选择同一文件
  target.value = ''
}

// 裁剪确认后上传
async function handleCropConfirm(blob: Blob) {
  const file = new File([blob], 'avatar.png', { type: 'image/png' })
  try {
    await authStore.uploadAvatar(file)
    ElMessage.success('头像上传成功')
  } catch {
    ElMessage.error('头像上传失败')
  }
}

// 保存基本信息
async function handleSaveProfile() {
  savingProfile.value = true
  try {
    await authStore.updateProfile({
      nickname: profileForm.value.nickname,
      email: profileForm.value.email,
    })
    ElMessage.success('个人信息已更新')
  } catch {
    ElMessage.error('更新失败，请重试')
  } finally {
    savingProfile.value = false
  }
}

// 修改密码
async function handleChangePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return

  savingPassword.value = true
  try {
    await authStore.changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    })
    ElMessage.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch {
    ElMessage.error('密码修改失败，请检查旧密码是否正确')
  } finally {
    savingPassword.value = false
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 700px;
  margin: 0 auto;
}

.header-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

/* ─── 头像卡片 ─────────────────────────────────────── */
.profile-card {
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
  padding: var(--spacing-sm) 0;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}

.profile-avatar {
  display: block;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  gap: 4px;
  opacity: 0;
  transition: opacity var(--transition-fast);
  border-radius: 50%;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-info h3 {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.avatar-info p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-secondary);
}

/* ─── 分区标题 ─────────────────────────────────────── */
.section-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  border-bottom: 1px solid var(--color-border);
}

/* ─── 表单 ─────────────────────────────────────────── */
.profile-form {
  max-width: 420px;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.profile-form :deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}
</style>
