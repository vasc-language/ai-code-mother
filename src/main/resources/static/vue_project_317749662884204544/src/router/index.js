import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '@/pages/Home.vue'
import Products from '@/pages/Products.vue'
import News from '@/pages/News.vue'
import About from '@/pages/About.vue'
import Contact from '@/pages/Contact.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/products',
    name: 'Products',
    component: Products
  },
  {
    path: '/news',
    name: 'News',
    component: News
  },
  {
    path: '/about',
    name: 'About',
    component: About
  },
  {
    path: '/contact',
    name: 'Contact',
    component: Contact
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router