import { createRouter, createWebHistory } from "vue-router";
import type { RouteRecordRaw } from "vue-router";

import LoginView from "@/views/LoginView.vue";
import AdminLayout from "@/views/AdminLayout.vue";
import AdminHomeView from "@/views/AdminHomeView.vue";
import MemberListView from "@/views/MemberListView.vue";
import MemberEditView from "@/views/MemberEditView.vue";
import MemberCreateView from "@/views/MemberCreateView.vue";

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    component: LoginView
  },
  {
    path: "/admin",
    component: AdminLayout,
    children: [
      {
        path: "",
        component: AdminHomeView
      },
      {
        path: "members",
        component: MemberListView
      },
      {
        path: "members/edit/:id",
        component: MemberEditView
      },
      {
      path: "members/create",
      component: MemberCreateView
    }
  ]
  },
  {
    path: "/",
    redirect: "/login"
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;