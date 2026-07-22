import { createApp } from 'vue'
import App from './App.vue'
import { setUnauthorizedHandler } from './api/client'
import { router } from './router'
import { session } from './session'
import { createSessionExpiredHandler } from './session-expiration'
import './styles/main.css'

setUnauthorizedHandler(createSessionExpiredHandler({
  isInitialized: () => session.initialized.value,
  isLoginPage: () => router.currentRoute.value.name === 'login',
  currentRoute: () => router.currentRoute.value.fullPath,
  invalidate: session.invalidate,
  redirectToLogin: (redirect) => router.replace({
    name: 'login',
    query: { redirect },
  }),
}))

createApp(App).use(router).mount('#app')
