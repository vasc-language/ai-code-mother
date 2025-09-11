import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '@/pages/Home.vue'
import Portfolio from '@/pages/Portfolio.vue'
import About from '@/pages/About.vue'
import Contact from '@/pages/Contact.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/portfolio',
    name: 'Portfolio',
    component: Portfolio
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