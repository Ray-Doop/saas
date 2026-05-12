import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/product'
    },
    {
      path: '/product',
      name: 'Product',
      component: () => import('@/views/product/ProductList.vue'),
      meta: { title: '商品管理' }
    },
    {
      path: '/product/detail/:id?',
      name: 'ProductDetail',
      component: () => import('@/views/product/ProductDetail.vue'),
      meta: { title: '商品详情' }
    },
    {
      path: '/workflow',
      name: 'Workflow',
      component: () => import('@/views/workflow/WorkflowList.vue'),
      meta: { title: '工作流管理' }
    },
    {
      path: '/workflow/editor/:id?',
      name: 'WorkflowEditor',
      component: () => import('@/views/workflow/WorkflowEditor.vue'),
      meta: { title: '工作流编辑器' }
    },
    {
      path: '/task',
      name: 'Task',
      component: () => import('@/views/task/TaskList.vue'),
      meta: { title: '任务记录' }
    },
    {
      path: '/task/:id',
      name: 'TaskDetail',
      component: () => import('@/views/task/TaskDetail.vue'),
      meta: { title: '任务详情' }
    }
  ]
})

export default router
