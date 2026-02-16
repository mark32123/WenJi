<template>
  <div class="model-viewer">
    <!-- 模型核心画布 -->
    <canvas ref="canvasRef" class="model-canvas"></canvas>

    <!-- 控制面板：右下角悬浮，小尺寸+半透明，不遮挡模型 -->
    <div class="controls">
      <button @click="resetView" class="control-btn">重置视图</button>
      <button @click="toggleAutoRotate" class="control-btn">
        {{ autoRotate ? '停止旋转' : '自动旋转' }}
      </button>
      <div class="slider-container">
        <label>转速:</label>
        <input
          type="range"
          min="0.1"
          max="2"
          step="0.1"
          v-model.number="rotationSpeed"
          class="slider"
        />
        <span>{{ rotationSpeed.toFixed(1) }}x</span>
      </div>
      <div class="slider-container">
        <label>缩放:</label>
        <input
          type="range"
          min="0.5"
          max="3"
          step="0.1"
          v-model.number="zoomLevel"
          class="slider"
        />
        <span>{{ zoomLevel.toFixed(1) }}x</span>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
      <p>正在加载模型...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader'
import { DRACOLoader } from 'three/examples/jsm/loaders/DRACOLoader'

const props = defineProps({
  modelUrl: {
    type: String,
    required: true
  }
})

const canvasRef = ref(null)
const loading = ref(true)
const autoRotate = ref(true)
const rotationSpeed = ref(1.0)
const zoomLevel = ref(1.0)

let scene = null
let camera = null
let renderer = null
let controls = null
let mixer = null
let model = null
let animationId = null
let clock = new THREE.Clock()

// 初始化场景（逻辑不变）
const initScene = () => {
  if (!canvasRef.value) return
  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xf8f5f0)
  
  camera = new THREE.PerspectiveCamera(
    45,
    canvasRef.value.clientWidth / canvasRef.value.clientHeight,
    0.1,
    1000
  )
  camera.position.set(5, 5, 5)
  camera.zoom = zoomLevel.value
  camera.updateProjectionMatrix()

  renderer = new THREE.WebGLRenderer({
    canvas: canvasRef.value,
    antialias: true,
    alpha: true
  })
  renderer.setSize(canvasRef.value.clientWidth, canvasRef.value.clientHeight)
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.rotateSpeed = 1.0
  controls.zoomSpeed = 0.8
  controls.panSpeed = 0.8
  controls.minDistance = 1
  controls.maxDistance = 20
  controls.enablePan = true
  controls.target.set(0, 0, 0)
  controls.update()

  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)
  const directionalLight1 = new THREE.DirectionalLight(0xffffff, 1.0)
  directionalLight1.position.set(5, 10, 5)
  directionalLight1.castShadow = true
  directionalLight1.shadow.mapSize.width = 2048
  directionalLight1.shadow.mapSize.height = 2048
  scene.add(directionalLight1)
  const directionalLight2 = new THREE.DirectionalLight(0xffffff, 0.5)
  directionalLight2.position.set(-5, 5, -5)
  scene.add(directionalLight2)

  const gridHelper = new THREE.GridHelper(10, 10, 0xcccccc, 0xe5e2dd)
  gridHelper.position.y = -1
  scene.add(gridHelper)
}

// 加载模型（逻辑不变）
const loadModel = () => {
  if (!scene) return
  loading.value = true
  const loader = new GLTFLoader()
  const dracoLoader = new DRACOLoader()
  dracoLoader.setDecoderPath('https://www.gstatic.com/draco/versioned/decoders/1.5.6/')
  loader.setDRACOLoader(dracoLoader)

  loader.load(
    props.modelUrl,
    (gltf) => {
      if (model) {
        scene.remove(model)
        model.traverse(obj => {
          if (obj.isMesh) {
            obj.geometry.dispose()
            Array.isArray(obj.material) ? obj.material.forEach(m => m.dispose()) : obj.material.dispose()
          }
        })
      }
      model = gltf.scene
      model.updateWorldMatrix(true, true)
      const box = new THREE.Box3().setFromObject(model)
      const center = box.getCenter(new THREE.Vector3())
      const size = box.getSize(new THREE.Vector3())
      model.position.x = -center.x
      model.position.y = -center.y
      model.position.z = -center.z
      const maxDim = Math.max(size.x, size.y, size.z)
      const scale = 3 / maxDim
      model.scale.setScalar(scale)
      scene.add(model)
      camera.position.set(0, 2, 5)
      controls.target.set(0, 0, 0)
      controls.update()
      if (gltf.animations && gltf.animations.length) {
        mixer = new THREE.AnimationMixer(model)
        gltf.animations.forEach((clip) => {
          mixer.clipAction(clip).play()
        })
      }
      loading.value = false
    },
    (xhr) => {
      const percentComplete = (xhr.loaded / xhr.total * 100).toFixed(2)
      console.log(`模型加载进度: ${percentComplete}%`)
    },
    (error) => {
      console.error('模型加载失败:', error)
      loading.value = false
      const geometry = new THREE.BoxGeometry(2, 2, 2)
      const material = new THREE.MeshStandardMaterial({
        color: 0xdc4c46,
        transparent: true,
        opacity: 0.5
      })
      model = new THREE.Mesh(geometry, material)
      scene.add(model)
    }
  )
}

// 动画循环（逻辑不变）
const animate = () => {
  animationId = requestAnimationFrame(animate)
  const delta = clock.getDelta()
  if (mixer) mixer.update(delta)
  if (autoRotate.value && model) model.rotation.y += 0.01 * rotationSpeed.value
  if (controls) controls.update()
  if (renderer && scene && camera) renderer.render(scene, camera)
}

// 重置视图（逻辑不变）
const resetView = () => {
  if (controls && camera) {
    camera.position.set(5, 5, 5)
    controls.target.set(0, 0, 0)
    zoomLevel.value = 1.0
    rotationSpeed.value = 1.0
    controls.update()
  }
}

// 切换自动旋转（逻辑不变）
const toggleAutoRotate = () => {
  autoRotate.value = !autoRotate.value
}

// 窗口适配（逻辑不变）
const handleResize = () => {
  if (!canvasRef.value || !camera || !renderer) return
  camera.aspect = canvasRef.value.clientWidth / canvasRef.value.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(canvasRef.value.clientWidth, canvasRef.value.clientHeight)
}

// 监听缩放（逻辑不变）
watch(zoomLevel, (newVal) => {
  if (camera) {
    camera.zoom = newVal
    camera.updateProjectionMatrix()
  }
}, { immediate: true })

// 监听模型URL（逻辑不变）
watch(() => props.modelUrl, (newUrl) => {
  if (newUrl) loadModel()
}, { immediate: true })

// 生命周期（逻辑不变）
onMounted(() => {
  initScene()
  animate()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (animationId) cancelAnimationFrame(animationId)
  window.removeEventListener('resize', handleResize)
  if (renderer) renderer.dispose()
  if (scene) {
    scene.traverse((obj) => {
      if (obj.isMesh) {
        obj.geometry.dispose()
        Array.isArray(obj.material) ? obj.material.forEach(m => m.dispose()) : obj.material.dispose()
      }
    })
  }
  mixer = null
  model = null
  controls = null
})
</script>

<style scoped>
.model-viewer {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #F8F5F0;
  border-radius: 8px;
}

.model-canvas {
  width: 100%;
  height: 100%;
  display: block;
}

/* 控制面板：右下角悬浮，半透明+小尺寸，不遮挡模型 */
.controls {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(255,255,255,0.85);
  padding: 8px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 140px;
  box-sizing: border-box;
  z-index: 2;
}

/* 按钮样式：适配项目色调，缩小尺寸 */
.control-btn {
  background: #A68A64;
  color: white;
  border: none;
  padding: 6px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 11px;
  transition: opacity 0.2s;
}

.control-btn:hover {
  opacity: 0.9;
}

/* 滑块容器：紧凑布局 */
.slider-container {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 0;
}

.slider-container label {
  font-size: 11px;
  min-width: 35px;
  color: #333;
}

.slider {
  flex: 1;
  height: 3px;
  -webkit-appearance: none;
  appearance: none;
  background: #e5e2dd;
  border-radius: 2px;
  outline: none;
}

.slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 12px;
  height: 12px;
  background: #A68A64;
  border-radius: 50%;
  cursor: pointer;
}

.slider-container span {
  font-size: 11px;
  min-width: 20px;
  text-align: right;
  color: #333;
}

/* 加载状态（逻辑不变） */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(248, 245, 240, 0.95);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  box-sizing: border-box;
}

.loading-overlay p {
  margin-top: 15px;
  color: #333;
  font-size: 14px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e5e2dd;
  border-top: 4px solid #A68A64;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 手机端适配：进一步缩小控件 */
@media (max-width: 428px) {
  .controls {
    bottom: 5px;
    right: 5px;
    padding: 6px;
    min-width: 120px;
  }
  .control-btn {
    padding: 5px 8px;
    font-size: 10px;
  }
  .slider-container label, .slider-container span {
    font-size: 10px;
  }
  .slider::-webkit-slider-thumb {
    width: 10px;
    height: 10px;
  }
}
</style>