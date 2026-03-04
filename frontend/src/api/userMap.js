import request from '@/utils/request'

// 创建用户地图
export const createUserMap = (data) => {
  return request({
    url: '/userMap/create',
    method: 'post',
    data
  })
}

// 更新用户地图
export const updateUserMap = (data) => {
  return request({
    url: '/userMap/update',
    method: 'put',
    data
  })
}

// 删除用户地图
export const deleteUserMap = (mapId) => {
  return request({
    url: `/userMap/delete/${mapId}`,
    method: 'delete'
  })
}

// 获取用户的所有地图
export const getUserMaps = () => {
  return request({
    url: '/userMap/list',
    method: 'get'
  })
}

// 获取用户的默认地图
export const getDefaultMap = () => {
  return request({
    url: '/userMap/default',
    method: 'get'
  })
}

// 设置默认地图
export const setDefaultMap = (mapId) => {
  return request({
    url: `/userMap/setDefault/${mapId}`,
    method: 'put'
  })
}

// 获取公开的地图列表（社区地图）
export const getPublicMaps = (params) => {
  return request({
    url: '/userMap/public',
    method: 'get',
    params
  })
}

// 获取地图详情
export const getMapDetail = (mapId) => {
  return request({
    url: `/userMap/detail/${mapId}`,
    method: 'get'
  })
}

// 点赞地图
export const likeMap = (mapId) => {
  return request({
    url: `/userMap/like/${mapId}`,
    method: 'post'
  })
}

// 取消点赞
export const unlikeMap = (mapId) => {
  return request({
    url: `/userMap/like/${mapId}`,
    method: 'delete'
  })
}

// 分享地图
export const shareMap = (data) => {
  return request({
    url: '/userMap/share',
    method: 'post',
    data
  })
}

// 获取社区分享列表
export const getCommunityShares = (params) => {
  return request({
    url: '/userMap/community',
    method: 'get',
    params
  })
}
