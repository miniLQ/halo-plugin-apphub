import { definePlugin, type CommentSubjectRefProvider, type CommentSubjectRefResult } from "@halo-dev/console-shared";
import { markRaw } from "vue";
import AppList from "@/views/AppList.vue";
import type { Extension } from "@halo-dev/api-client";
import RiApps2Line from "~icons/ri/apps-2-line"

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/app-hubs",
        name: "App-hubs",
        component: AppList,
        meta: {
          permissions: ["plugin:apphubs:view"],
          menu: {
            name: "应用舱",
            group: "content",
            icon: markRaw(RiApps2Line),
          },
        },
      },
    },
  ],
  extensionPoints: {
  },
});
