/// <reference types="vite/client" />

// 👈 加入這段，TypeScript 才能理解 .vue 檔案的「類型」
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}
