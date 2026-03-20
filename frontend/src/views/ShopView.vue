<template>
  <div class="shop-container">
    <!-- 顶部导航栏 -->
    <div class="nav-bar">
      <button class="back-btn" @click="goBack">
        ←
      </button>
      <h1 class="shop-title">积分商城</h1>
    </div>
    <!-- 轮播图区域 -->
    <div class="carousel">
      <div class="carousel-item" v-for="(item, index) in carouselList" :key="index" :class="{ active: currentIndex === index }">
        <img :src="item.image" :alt="item.title" />
        <div class="carousel-text">
          <h2>{{ item.title }}</h2>
          <p>{{ item.desc }}</p>
        </div>
      </div>
      <div class="carousel-indicators">
        <span v-for="(_, index) in carouselList" :key="index" :class="{ active: currentIndex === index }" @click="goToSlide(index)"></span>
      </div>
    </div>

    <!-- 商品列表区域 -->
    <div class="goods-list">
      <div class="goods-item" v-for="(good, index) in goodsList" :key="index">
        <img :src="good.image" :alt="good.name" class="goods-img" />
        <div class="goods-info">
          <h3 class="goods-name">{{ good.name }}</h3>
          <div class="goods-points">{{ good.points }} 积分</div>
          <button class="buy-btn" @click="handleBuy(good)">立即兑换</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ShopView',
  data() {
    return {
      currentIndex: 0,
      timer: null,
      // 轮播图改为使用你的商品图片（1.jpg-7.jpg）
      carouselList: [
        {
          image: '/非遗商品/1.jpg', // 剪纸团扇
          title: '剪纸团扇',
          desc: '匠心传承，品味东方美学'
        },
        {
          image: '/非遗商品/2.jpg', // 景泰蓝小葫芦吊坠
          title: '景泰蓝小葫芦吊坠',
          desc: '非遗工艺，精美绝伦'
        },
        {
          image: '/非遗商品/3.jpg', // 蜡染小手帕
          title: '蜡染小手帕',
          desc: '手工印染，自然雅致'
        },
        {
          image: '/非遗商品/4.jpg', // 苗族银饰吊坠
          title: '苗族银饰吊坠',
          desc: '民族特色，工艺精湛'
        },
        {
          image: '/非遗商品/5.jpg', // 皮影挂件
          title: '皮影挂件',
          desc: '传统艺术，趣味十足'
        },
        {
          image: '/非遗商品/6.jpg', // 汝瓷小杯
          title: '汝瓷小杯',
          desc: '温润如玉，器型优美'
        },
        {
          image: '/非遗商品/7.jpg', // 蜀绣香囊
          title: '蜀绣香囊',
          desc: '刺绣精品，香气宜人'
        }
      ],
      // 商品列表图片同步改为数字命名
      goodsList: [
        {
          name: '剪纸团扇',
          image: '/非遗商品/1.jpg',
          points: 680
        },
        {
          name: '景泰蓝小葫芦吊坠',
          image: '/非遗商品/2.jpg',
          points: 1280
        },
        {
          name: '蜡染小手帕',
          image: '/非遗商品/3.jpg',
          points: 390
        },
        {
          name: '苗族银饰吊坠',
          image: '/非遗商品/4.jpg',
          points: 1980
        },
        {
          name: '皮影挂件',
          image: '/非遗商品/5.jpg',
          points: 450
        },
        {
          name: '汝瓷小杯',
          image: '/非遗商品/6.jpg',
          points: 2580
        },
        {
          name: '蜀绣香囊',
          image: '/非遗商品/7.jpg',
          points: 580
        }
      ]
    }
  },
  mounted() {
    this.startAutoPlay()
  },
  beforeUnmount() {
    this.stopAutoPlay()
  },
  methods: {
    startAutoPlay() {
      this.timer = setInterval(() => {
        this.nextSlide()
      }, 3000)
    },
    stopAutoPlay() {
      clearInterval(this.timer)
    },
    nextSlide() {
      this.currentIndex = (this.currentIndex + 1) % this.carouselList.length
    },
    goToSlide(index) {
      this.currentIndex = index
    },
    handleBuy(good) {
      // 获取当前用户积分
      let userPoints = parseInt(localStorage.getItem('userPoints')) || 1000;
      
      // 检查积分是否足够
      if (userPoints < good.points) {
        alert('积分不足，无法兑换该商品');
        return;
      }
      
      // 扣除积分
      userPoints -= good.points;
      
      // 保存更新后的积分
      localStorage.setItem('userPoints', userPoints);
      
      // 显示兑换成功提示
      alert(`您已兑换：${good.name}，消耗积分：${good.points}\n剩余积分：${userPoints}`);
    },
    goBack() {
      this.$router.back()
    }
  }
}
</script>

<style scoped>
.shop-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  padding-top: 0;
}

/* 顶部导航栏样式 */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 100;
  width: 100%;
  box-sizing: border-box;
  margin: 0 -20px 20px -20px;
}
.back-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #a68a64;
  cursor: pointer;
  margin-right: 12px;
  padding: 4px 8px;
  flex-shrink: 0;
}
.shop-title {
  font-size: 18px;
  font-weight: 600;
  flex: 1;
  text-align: center;
  margin: 0;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

/* 轮播图样式 */
.carousel {
  position: relative;
  width: 100%;
  height: 400px;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 30px;
}
.carousel-item {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  transition: opacity 0.5s ease;
}
.carousel-item.active {
  opacity: 1;
}
.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 确保图片适配轮播容器，不拉伸变形 */
}
.carousel-text {
  position: absolute;
  bottom: 40px;
  left: 40px;
  color: white;
  background: rgba(0,0,0,0.5); /* 增加背景，提升文字可读性 */
  padding: 10px 20px;
  border-radius: 4px;
}
.carousel-text h2 {
  font-size: 32px;
  margin-bottom: 10px;
}
.carousel-text p {
  font-size: 18px;
}
.carousel-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
}
.carousel-indicators span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  cursor: pointer;
}
.carousel-indicators span.active {
  background: white;
}

/* 商品列表样式 - 改为每行2个商品 */
.goods-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 固定每行2列 */
  gap: 20px; /* 商品之间的间距 */
  padding: 0 10px;
}
.goods-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  background: white;
  transition: box-shadow 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.goods-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
/* 优化商品图片尺寸，避免过大 */
.goods-img {
  width: 100%;
  max-width: 180px; /* 限制图片最大宽度 */
  height: 180px; /* 固定图片高度 */
  object-fit: cover; /* 保持图片比例 */
  border-radius: 4px;
  margin-bottom: 10px;
}
.goods-info {
  width: 100%;
  text-align: center;
}
.goods-name {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.goods-points {
  font-size: 20px;
  color: #A68A64;
  font-weight: bold;
  margin-bottom: 12px;
}
.buy-btn {
  width: 100%;
  padding: 10px;
  background: #A68A64;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
.buy-btn:hover {
  background: #8B7355;
}

/* 响应式适配 - 小屏幕依然保持2列 */
@media (max-width: 768px) {
  .goods-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  .goods-img {
    max-width: 150px;
    height: 150px;
  }
  .carousel {
    height: 250px;
  }
}
</style>