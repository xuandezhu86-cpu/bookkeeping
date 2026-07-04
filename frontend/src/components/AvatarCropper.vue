<template>
  <el-dialog
    :model-value="visible"
    title="裁剪头像"
    width="480px"
    :close-on-click-modal="false"
    destroy-on-close
    @closed="handleClosed"
    @open="handleOpen"
    class="avatar-cropper-dialog"
  >
    <div class="cropper-container">
      <div
        ref="viewportRef"
        class="cropper-viewport"
        @mousedown="startDrag"
        @mousemove="onDrag"
        @mouseup="endDrag"
        @mouseleave="endDrag"
        @wheel.prevent="handleWheel"
      >
        <img
          ref="imgRef"
          :src="imageUrl"
          alt="待裁剪图片"
          :style="imgStyle"
          draggable="false"
        />
      </div>
      <div class="cropper-hint">拖动图片调整位置，滚轮缩放</div>
    </div>

    <div class="cropper-toolbar">
      <el-icon :size="16"><ZoomOut /></el-icon>
      <el-slider
        v-model="zoomLevel"
        :min="20"
        :max="400"
        :step="1"
        class="zoom-slider"
      />
      <el-icon :size="16"><ZoomIn /></el-icon>
    </div>

    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleConfirm">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, nextTick } from 'vue'
import { ZoomIn, ZoomOut } from '@element-plus/icons-vue'

const props = defineProps<{
  visible: boolean
  imageUrl: string
}>()

const emit = defineEmits<{
  close: []
  confirm: [blob: Blob]
}>()

const viewportRef = ref<HTMLDivElement | null>(null)
const imgRef = ref<HTMLImageElement | null>(null)

const VIEWPORT_SIZE = 400

const state = reactive({
  naturalW: 0,
  naturalH: 0,
  panX: 0,
  panY: 0,
  zoom: 1,
  dragging: false,
  dragStartX: 0,
  dragStartY: 0,
  dragPanX: 0,
  dragPanY: 0,
})

const baseScale = computed(() =>
  state.naturalW > 0 ? VIEWPORT_SIZE / state.naturalW : 1
)

const effectiveZoom = computed(() => baseScale.value * state.zoom)

const zoomLevel = computed({
  get: () => Math.round(state.zoom * 100),
  set: (val: number) => { state.zoom = val / 100 },
})

const imgStyle = computed(() => {
  const s = effectiveZoom.value
  return {
    width: `${state.naturalW}px`,
    height: `${state.naturalH}px`,
    position: 'absolute' as const,
    left: '50%',
    top: '50%',
    marginLeft: `${-state.naturalW / 2}px`,
    marginTop: `${-state.naturalH / 2}px`,
    transform: `translate(${state.panX}px, ${state.panY}px) scale(${s})`,
    transformOrigin: 'center center',
    cursor: state.dragging ? 'grabbing' : 'grab',
    userSelect: 'none',
    maxWidth: 'none',
  }
})

// 弹窗打开 → 重置状态并加载图片
function handleOpen() {
  resetState()
  // 等待 DOM 渲染完成后加载图片
  nextTick(() => {
    const img = imgRef.value
    if (!img) return
    const onLoad = () => {
      state.naturalW = img.naturalWidth
      state.naturalH = img.naturalHeight
    }
    if (img.complete && img.naturalWidth > 0) {
      onLoad()
    } else {
      img.onload = onLoad
    }
  })
}

// 弹窗完全关闭后
function handleClosed() {
  resetState()
}

function resetState() {
  state.naturalW = 0
  state.naturalH = 0
  state.panX = 0
  state.panY = 0
  state.zoom = 1
  state.dragging = false
}

// ── 拖拽 ──
function startDrag(e: MouseEvent) {
  state.dragging = true
  state.dragStartX = e.clientX
  state.dragStartY = e.clientY
  state.dragPanX = state.panX
  state.dragPanY = state.panY
}

function onDrag(e: MouseEvent) {
  if (!state.dragging) return
  state.panX = state.dragPanX + (e.clientX - state.dragStartX)
  state.panY = state.dragPanY + (e.clientY - state.dragStartY)
}

function endDrag() {
  state.dragging = false
}

// ── 滚轮缩放 ──
function handleWheel(e: WheelEvent) {
  const delta = e.deltaY > 0 ? -0.1 : 0.1
  state.zoom = Math.max(0.2, Math.min(4, state.zoom + delta))
}

// ── 确认裁剪 ──
function handleConfirm() {
  const img = imgRef.value
  if (!img || state.naturalW === 0) return

  const s = effectiveZoom.value
  const cx = VIEWPORT_SIZE / 2
  const cy = VIEWPORT_SIZE / 2

  const srcX = state.naturalW / 2 + (0 - cx - state.panX) / s
  const srcY = state.naturalH / 2 + (0 - cy - state.panY) / s
  const srcW = VIEWPORT_SIZE / s
  const srcH = VIEWPORT_SIZE / s

  const canvas = document.createElement('canvas')
  canvas.width = 256
  canvas.height = 256
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  ctx.imageSmoothingEnabled = true
  ctx.imageSmoothingQuality = 'high'
  ctx.drawImage(img, srcX, srcY, srcW, srcH, 0, 0, 256, 256)

  canvas.toBlob((blob) => {
    if (blob) emit('confirm', blob)
    emit('close')
  }, 'image/png')
}

// ── 取消 ──
function handleCancel() {
  emit('close')
}
</script>

<style scoped>
.avatar-cropper-dialog :deep(.el-dialog__body) {
  padding: 12px 20px;
}

.cropper-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cropper-viewport {
  width: 400px;
  height: 400px;
  background: #f5f5f5;
  border-radius: 50%;
  overflow: hidden;
  position: relative;
  box-shadow: 0 0 0 2px #409eff, 0 4px 12px rgba(0, 0, 0, 0.15);
}

.cropper-hint {
  margin-top: 8px;
  font-size: 12px;
  color: var(--color-text-secondary);
}

.cropper-toolbar {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-md);
  padding: 0 8px;
  width: 100%;
  max-width: 400px;
}

.zoom-slider {
  flex: 1;
}
</style>
