<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  PictureOutlined,
  ApartmentOutlined,
  UnorderedListOutlined,
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

const selectedKeys = computed(() => {
  const path = route.path
  if (path.startsWith('/product')) return ['product']
  if (path.startsWith('/workflow')) return ['workflow']
  if (path.startsWith('/task')) return ['task']
  return ['product']
})

const menuItems = [
  { key: 'product', icon: PictureOutlined, label: '商品管理', path: '/product' },
  { key: 'workflow', icon: ApartmentOutlined, label: '工作流编辑', path: '/workflow' },
  { key: 'task', icon: UnorderedListOutlined, label: '任务记录', path: '/task' },
]

function navigate(path: string) {
  router.push(path)
}
</script>

<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" collapsible theme="dark">
      <div class="logo">
        <span v-if="!collapsed">智绘云 SaaS</span>
        <span v-else>智</span>
      </div>
      <a-menu
        theme="dark"
        mode="inline"
        :selected-keys="selectedKeys"
        @click="({ key }: any) => navigate(menuItems.find(m => m.key === key)?.path || '/')"
      >
        <a-menu-item v-for="item in menuItems" :key="item.key">
          <component :is="item.icon" />
          <span>{{ item.label }}</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout>
      <a-layout-header class="header">
        <div class="header-left">
          <component
            :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined"
            class="trigger"
            @click="collapsed = !collapsed"
          />
        </div>
        <div class="header-right">
          <a-dropdown>
            <a-space>
              <a-avatar size="small"><UserOutlined /></a-avatar>
              <span>管理员</span>
            </a-space>
            <template #overlay>
              <a-menu>
                <a-menu-item key="logout">
                  <LogoutOutlined />
                  <span style="margin-left:8px">退出登录</span>
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </a-layout-header>

      <a-layout-content class="content">
        <slot />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<style scoped lang="less">
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 2px;
}

.header {
  background: #fff;
  padding: 0 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}

.trigger {
  font-size: 18px;
  cursor: pointer;
}

.content {
  margin: 24px;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  min-height: calc(100vh - 112px);
}
</style>
