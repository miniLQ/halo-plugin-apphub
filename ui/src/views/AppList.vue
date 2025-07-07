<script lang="ts" setup>
import ReleaseEditingModal from "@/components/ReleaseEditingModal.vue";

import type { Release, ReleaseList } from "@/types";
import { axiosInstance } from "@halo-dev/api-client";
import {
  Dialog,
  IconAddCircle,
  IconArrowLeft,
  IconArrowRight,
  IconCheckboxFill,
  Toast,
  VButton,
  VCard,
  VDropdown,
  VDropdownItem,
  VEmpty,
  VLoading,
  VPageHeader,
  VPagination,
  VSpace,
} from "@halo-dev/components";
import type { AttachmentLike } from "@halo-dev/console-shared";
import { useQuery } from "@tanstack/vue-query";
import Fuse from "fuse.js";

import { computed, nextTick, ref, watch } from "vue";
import RiApps2Line from "~icons/ri/apps-2-line"


import ApplicationList from "../components/ApplicationList.vue"

const selectedRelease = ref<Release | undefined>();
const selectedReleases = ref<Set<Release>>(new Set<Release>());
const selectedGroup = ref<string>();
const editingModal = ref(false);
const checkedAll = ref(false);
const groupListRef = ref();

const page = ref(1);
const size = ref(20);
const total = ref(0);
const keyword = ref("");

const {
  data: releases,
  isLoading,
  refetch,
} = useQuery<Release[]>({
  queryKey: [page, size, keyword, selectedGroup],
  queryFn: async () => {
    if (!selectedGroup.value) {
      return [];
    }
    const { data } = await axiosInstance.get<ReleaseList>("/apis/console.api.apphub.erzip.com/v1alpha1/releases", {
      params: {
        page: page.value,
        size: size.value,
        keyword: keyword.value,
        group: selectedGroup.value,
      },
    });
    total.value = data.total;
    return data.items
      .map((group) => {
        if (group.spec) {
          group.spec.priority = group.spec.priority || 0;
        }
        return group;
      })
      .sort((a, b) => {
        return (a.spec?.priority || 0) - (b.spec?.priority || 0);
      });
  },
  refetchInterval(data) {
    const deletingGroups = data?.filter((group) => !!group.metadata.deletionTimestamp);

    return deletingGroups?.length ? 1000 : false;
  },
  refetchOnWindowFocus: false,
});

const handleSelectPrevious = () => {
  if (!releases.value) {
    return;
  }

  const currentIndex = releases.value.findIndex((release) => release.metadata.name === selectedRelease.value?.metadata.name);

  if (currentIndex > 0) {
    selectedRelease.value = releases.value[currentIndex - 1];
    return;
  }

  if (currentIndex <= 0) {
    selectedRelease.value = undefined;
  }
};

const handleSelectNext = () => {
  if (!releases.value) {
    return;
  }

  if (!selectedRelease.value) {
    selectedRelease.value = releases.value[0];
    return;
  }
  const currentIndex = releases.value.findIndex((release) => release.metadata.name === selectedRelease.value?.metadata.name);
  if (currentIndex !== releases.value.length - 1) {
    selectedRelease.value = releases.value[currentIndex + 1];
  }
};

const handleOpenEditingModal = (release?: Release) => {
  selectedRelease.value = release;
  editingModal.value = true;
};

const handleDeleteInBatch = () => {
  Dialog.warning({
    title: "是否确认删除所选的应用版本？",
    description: "删除之后将无法恢复。",
    confirmType: "danger",
    onConfirm: async () => {
      try {
        const promises = Array.from(selectedReleases.value).map((release) => {
          return axiosInstance.delete(`/apis/core.erzip.com/v1alpha1/releases/${release.metadata.name}`);
        });
        await Promise.all(promises);
      } catch (e) {
        console.error(e);
      } finally {
        pageRefetch();
      }
    },
  });
};

const handleCheckAllChange = (e: Event) => {
  const { checked } = e.target as HTMLInputElement;
  handleCheckAll(checked);
};

const handleCheckAll = (checkAll: boolean) => {
  if (checkAll) {
    releases.value?.forEach((release) => {
      selectedReleases.value.add(release);
    });
  } else {
    selectedReleases.value.clear();
  }
};

const isChecked = (release: Release) => {
  return (
    release.metadata.name === selectedRelease.value?.metadata.name ||
    Array.from(selectedReleases.value)
      .map((item) => item.metadata.name)
      .includes(release.metadata.name)
  );
};

watch(
  () => selectedReleases.value.size,
  (newValue) => {
    checkedAll.value = newValue === releases.value?.length;
  }
);

// search
let fuse: Fuse<Release> | undefined = undefined;

watch(
  () => releases.value,
  () => {
    if (!releases.value) {
      return;
    }

    fuse = new Fuse(releases.value, {
      keys: ["spec.displayName", "metadata.name", "spec.description", "spec.url"],
      useExtendedSearch: true,
    });
  }
);

const searchResults = computed({
  get() {
    if (!fuse || !keyword.value) {
      return releases.value || [];
    }

    return fuse?.search(keyword.value).map((item) => item.item);
  },
  set(value) {
    releases.value = value;
  },
});

// create by attachments
const attachmentModal = ref(false);

const onAttachmentsSelect = async (attachments: AttachmentLike[]) => {
  const releases: {
    url: string;
    displayName?: string;
  }[] = attachments
    .map((attachment) => {
      const post = {
        groupName: selectedGroup.value || "",
      };

      if (typeof attachment === "string") {
        return {
          ...post,
          url: attachment,
        };
      }
      if ("url" in attachment) {
        return {
          ...post,
          url: attachment.url,
        };
      }
      if ("spec" in attachment) {
        return {
          ...post,
          url: attachment.status?.permalink,
          displayName: attachment.spec.displayName,
        };
      }
    })
    .filter(Boolean) as {
    url: string;
    displayName?: string;
  }[];

  const createRequests = releases.map((release) => {
    return axiosInstance.post<Release>("/apis/core.erzip.com/v1alpha1/releases", {
      metadata: {
        name: "",
        generateName: "release-",
      },
      spec: release,
      kind: "Release",
      apiVersion: "core.erzip.com/v1alpha1",
    });
  });

  await Promise.all(createRequests);

  Toast.success(`新建成功，一共创建了 ${releases.length} 个应用版本。`);
  pageRefetch();
};

const groupSelectHandle = (group?: string) => {
  selectedGroup.value = group;
};

const pageRefetch = async () => {
  await groupListRef.value.refetch();
  await refetch();
  selectedReleases.value = new Set<Release>();
};
</script>
<template>
  <ReleaseEditingModal
    v-model:visible="editingModal"
    :release="selectedRelease"
    :group="selectedGroup"
    @close="refetch()"
    @saved="pageRefetch"
  >
    <template #append-actions>
      <span @click="handleSelectPrevious">
        <IconArrowLeft />
      </span>
      <span @click="handleSelectNext">
        <IconArrowRight />
      </span>
    </template>
  </ReleaseEditingModal>
  <AttachmentSelectorModal v-model:visible="attachmentModal" :accepts="['image/*']" @select="onAttachmentsSelect" />
  <VPageHeader title="应用舱">
    <template #icon>
      <RiApps2Line class="mr-2 self-center" />
    </template>
  </VPageHeader>
  <div class="p-4">
    <div class="flex flex-col gap-2 lg:flex-row">
      <div class="w-full flex-none lg:w-96">
        <ApplicationList ref="groupListRef" @select="groupSelectHandle" />
      </div>
      <div class="flex-1 shrink min-w-0">
        <VCard>
          <template #header>
            <div class="block w-full bg-gray-50 px-4 py-3">
              <div class="relative flex flex-col items-start sm:flex-row sm:items-center">
                <div class="mr-4 hidden items-center sm:flex">
                  <input v-model="checkedAll" type="checkbox" @change="handleCheckAllChange" />
                </div>
                <div class="flex w-full flex-1 sm:w-auto">
                  <SearchInput v-if="!selectedReleases.size" v-model="keyword" />
                  <VSpace v-else>
                    <VButton type="danger" @click="handleDeleteInBatch"> 删除 </VButton>
                  </VSpace>
                </div>
                <div v-if="selectedGroup" v-permission="['plugin:apphubs:manage']" class="mt-4 flex sm:mt-0">
                  <VDropdown>
                    <VButton size="xs"> 新增 </VButton>
                    <template #popper>
                      <VDropdownItem @click="handleOpenEditingModal()"> 新增 </VDropdownItem>
                    </template>
                  </VDropdown>
                </div>
              </div>
            </div>
          </template>
          <VLoading v-if="isLoading" />
          <Transition v-else-if="!selectedGroup" appear name="fade">
            <VEmpty message="请选择或新建分组" title="未选择分组"></VEmpty>
          </Transition>
          <Transition v-else-if="!searchResults.length" appear name="fade">
            <VEmpty message="你可以尝试刷新或者新建应用版本" title="当前没有应用版本">
              <template #actions>
                <VSpace>
                  <VButton @click="refetch"> 刷新</VButton>
                  <VButton v-permission="['plugin:apphubs:manage']" type="primary" @click="handleOpenEditingModal()">
                    <template #icon>
                      <IconAddCircle class="size-full" />
                    </template>
                    新增版本
                  </VButton>
                </VSpace>
              </template>
            </VEmpty>
          </Transition>
          <Transition v-else appear name="fade">
            <div class="mt-2 grid grid-cols-1 gap-x-2 gap-y-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-5" role="list">
              <VCard
                v-for="release in releases"
                :key="release.metadata.name"
                :body-class="['!p-0']"
                :class="{
                  'ring-primary ring-1': isChecked(release),
                  'ring-1 ring-red-600': release.metadata.deletionTimestamp,
                }"
                class="hover:shadow"
                @click="handleOpenEditingModal(release)"
              >
                <div class="group relative bg-white">
                  <!-- 只显示应用名称 -->
                  <div class="flex h-32 items-center justify-center bg-gray-100 p-4">
                    <p class="text-lg font-semibold text-gray-800">{{ release.spec.displayName }}</p>
                  </div>

                  <div v-if="release.metadata.deletionTimestamp" class="absolute top-1 right-1 text-xs text-red-300">
                    删除中...
                  </div>

                  <div
                    v-if="!release.metadata.deletionTimestamp"
                    v-permission="['plugin:photos:manage']"
                    :class="{ '!flex': selectedReleases.has(release) }"
                    class="absolute top-0 left-0 hidden h-1/3 w-full cursor-pointer justify-end bg-gradient-to-b from-gray-300 to-transparent ease-in-out group-hover:flex"
                    @click.stop="selectedReleases.has(release) ? selectedReleases.delete(release) : selectedReleases.add(release)"
                  >
                    <IconCheckboxFill
                      :class="{
                        '!text-primary': selectedReleases.has(release),
                      }"
                      class="hover:text-primary mt-1 mr-1 h-6 w-6 cursor-pointer text-white transition-all"
                    />
                  </div>
                </div>
              </VCard>
            </div>
          </Transition>

          <template #footer>
            <VPagination v-model:page="page" v-model:size="size" :total="total" :size-options="[20, 30, 50, 100]" />
          </template>
        </VCard>
      </div>
    </div>
  </div>
</template>
