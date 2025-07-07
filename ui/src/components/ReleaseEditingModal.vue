<script lang="ts" setup>
import type {Release} from "@/types";
import {reset, submitForm} from "@formkit/core";
import {axiosInstance} from "@halo-dev/api-client";
import {IconSave, VButton, VModal} from "@halo-dev/components";
import {cloneDeep} from "lodash-es";
import {computed, nextTick, ref, watch} from "vue";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    release?: Release;
    group?: string;
  }>(),
  {
    visible: false,
    release: undefined,
    group: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:visible", value: boolean): void;
  (event: "close"): void;
  (event: "saved", release: Release): void;
}>();

const initialFormState: Release = {
  metadata: {
    name: "",
    generateName: "release-",
  },
  spec: {
    displayName: "",
    version: "",
    requires: "",
    packageSize: "0",
    url: "",
    groupName: props.group || "",
    updateSpecs: [], // 初始化为空数组（符合 UpdateSpec[] 类型）
    priority: 0, // 可选属性建议初始化默认值
  },
  status:{
    publishTimestamp: new Date(), // 初始化为当前日期
  },
  kind: "Release",
  apiVersion: "core.erzip.com/v1alpha1",
} as Release;

const formState = ref<Release>(cloneDeep(initialFormState));

const saving = ref<boolean>(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});

const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑应用版本" : "添加应用版本";
});

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("release-form");
};

watch(
  () => props.visible,
  (visible) => {
    if (!visible && !props.release) {
      handleResetForm();
    }
  }
);

watch(
  () => props.release,
  (release) => {
    if (release) {
      formState.value = cloneDeep(release);
    } else {
      handleResetForm();
    }
  }
);
const annotationsFormRef = ref();

const handleSaveRelease = async () => {
  annotationsFormRef.value?.handleSubmit();
  await nextTick();
  const {customAnnotations, annotations, customFormInvalid, specFormInvalid} = annotationsFormRef.value || {};
  if (customFormInvalid || specFormInvalid) {
    return;
  }
  formState.value.metadata.annotations = {
    ...annotations,
    ...customAnnotations,
  };
  try {
    saving.value = true;
    if (isUpdateMode.value) {
      await axiosInstance.put<Release>(
        `/apis/core.erzip.com/v1alpha1/releases/${formState.value.metadata.name}`,
        formState.value
      );
    } else {
      if (props.group) {
        formState.value.spec.groupName = props.group;
      }
      const {data} = await axiosInstance.post<Release>(`/apis/core.erzip.com/v1alpha1/releases`, formState.value);
      emit("saved", data);
    }
    onVisibleChange(false);
  } catch (e) {
    console.error(e);
  } finally {
    saving.value = false;
  }
};
</script>
<template>
  <VModal :title="modalTitle" :visible="visible" :width="650" @update:visible="onVisibleChange">
    <template #actions>
      <slot name="append-actions"/>
    </template>

    <FormKit
      id="release-form"
      v-model="formState.spec"
      name="release-form"
      :actions="false"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleSaveRelease"
    >
      <div class="md:grid md:grid-cols-4 md:gap-6">
        <div class="md:col-span-1">
          <div class="sticky top-0">
            <span class="text-base font-medium text-gray-900"> 常规 </span>
          </div>
        </div>
        <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
          <FormKit
            name="displayName"
            label="版本"
            type="text"
            validation="required"
            help="例如: V1.0.0"
          ></FormKit>
          <FormKit
            name="version"
            label="版本号"
            type="text"
            validation="required"
            help="例如: 1.0.0"
          ></FormKit>
          <FormKit
            name="requires"
            label="halo版本要求"
            type="text"
            validation="required"
            help="例如: >=2.21.0"
          ></FormKit>
          <FormKit
            name="packageSize"
            label="安装包大小"
            type="text"
            validation="required"
            help="例如：10Mb、10Kb">
          </FormKit>
          <FormKit
            name="url"
            label="下载地址"
            type="text"
            validation="required"
            help="应用下载地址">
          </FormKit>
          <FormKit
            name="priority"
            label="优先级"
            type="number"
            number="integer"
            validation="min:0" step="1" min="0"
            :validation-messages="{integer: '必须输入整数',
            min: '不能小于0'}"
            validation-visibility="live"
            help="用于排序，0优先级最大"></FormKit>

          <FormKit
            type="repeater"
            name="updateSpecs"
            label="应用更新说明"
            add-label="添加更新说明"
            :value="[{}]"
            help="添加本次更新的详细说明"
            #default="{ index }"
          >
            <div class="mb-4 p-4 border rounded-md">
              <FormKit
                :name="`title`"
                label="更新标题"
                type="text"
                validation="required"
                help="输入更新标题，例如：新增功能、问题修复等"
              />

              <FormKit
                type="repeater"
                :name="`notes`"
                label="更新描述"
                add-label="添加描述条目"
                :value="[{}]"
                help="添加该更新标题下的具体描述"
                #default="{ index: noteIndex }"
              >
                <FormKit
                  :name="`description`"
                  type="textarea"
                  label="描述内容"
                  help="输入具体描述"
                  rows="3"
                  :editor="true"
                />
              </FormKit>
            </div>
          </FormKit>
          
          
        </div>
      </div>
    </FormKit>
    <div class="py-5">
      <div class="border-t border-gray-200"></div>
    </div>
    <div class="md:grid md:grid-cols-4 md:gap-6">
      <div class="md:col-span-1">
        <div class="sticky top-0">
          <span class="text-base font-medium text-gray-900"> 元数据 </span>
        </div>
      </div>
      <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
        <AnnotationsForm
          v-if="visible"
          :key="formState.metadata.name"
          ref="annotationsFormRef"
          :value="formState.metadata.annotations"
          kind="Release"
          group="core.erzip.com"
        />
      </div>
    </div>
    <template #footer>
      <VButton :loading="saving" type="secondary" @click="submitForm('release-form')">
        <template #icon>
          <IconSave class="size-full"/>
        </template>
        保存
      </VButton>
    </template>
  </VModal>
</template>
