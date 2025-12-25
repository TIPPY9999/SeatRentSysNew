/// <reference types="vite/client" />

// 1. Vue 檔案的型別定義
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 2. 環境變數的型別定義 (放在大括號外面)
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string;
  // 可以在這裡繼續增加其他的環境變數...
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}