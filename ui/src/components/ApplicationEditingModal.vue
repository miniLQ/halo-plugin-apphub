<script lang="ts" setup>
import type {Application, ApplicationList} from "@/types";
import { reset, submitForm } from "@formkit/core";
import { axiosInstance } from "@halo-dev/api-client";
import { VButton, VModal, VSpace } from "@halo-dev/components";
import { useMagicKeys } from "@vueuse/core";
import { cloneDeep } from "lodash-es";
import { computed, nextTick, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    visible: boolean;
    group: Application | null;
  }>(),
  {
    visible: false,
    group: null,
  }
);

const emit = defineEmits<{
  (event: "update:visible", visible: boolean): void;
  (event: "close"): void;
}>();

const initialFormState: Application = {
  apiVersion: "core.erzip.com/v1alpha1",
  kind: "Application",
  metadata: {
    name: "",
    generateName: "app-group-",
  },
  spec: {
    type: "PLUGIN", // 默认值设为 PLUGIN
    cover: "",
    displayName: "",
    description: "",
    screenshots: [],
    features: [],
    owner: {
      name: "",
      avatar: "",
      label: "",
      description: "",
      homepage: "",
      repo: "",
      issues: "",
      license: ""
    },
    priceConfig: {
      mode: "FREE",
      oneTimePrice: 0
    },
    publish: false,
    deprecated: false,
    priority: 0
  },
  status: {
    releaseCount: 0,
    viewCount: 0,
    downloadCount: 0,
  },
};

const formState = ref<Application>(initialFormState);
const saving = ref(false);

const isUpdateMode = computed(() => {
  return !!formState.value.metadata.creationTimestamp;
});
const isMac = /macintosh|mac os x/i.test(navigator.userAgent);
const modalTitle = computed(() => {
  return isUpdateMode.value ? "编辑分组" : "新建分组";
});
const annotationsGroupFormRef = ref();

const handlePriceModeChange = (value: string) => {
  if (value === "FREE") {
    formState.value.spec.priceConfig.oneTimePrice = 0;
  }
};

const handleCreateOrUpdateGroup = async () => {
  annotationsGroupFormRef.value?.handleSubmit();
  await nextTick();
  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } = annotationsGroupFormRef.value || {};
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
      
      
      console.log("更新：")
      // 1. 获取最新数据
      const { data } = await axiosInstance.get<Application>(`/apis/console.api.apphub.erzip.com/v1alpha1/application/${formState.value.metadata.name}`);

      // 2. 同步更新状态（立即生效）
      const updatedForm = {
        ...formState.value,
        status: data.status, // 确保正确覆盖状态
        metadata: data.metadata // 确保正确覆盖 metadata
      };
      console.log(updatedForm)
      await axiosInstance.put(
        `/apis/core.erzip.com/v1alpha1/applications/${formState.value.metadata.name}`,
        updatedForm
      );
    } else {
      await axiosInstance.post("/apis/core.erzip.com/v1alpha1/applications", formState.value);
    }
    onVisibleChange(false);
  } catch (e) {
    console.error("Failed to create app group", e);
  } finally {
    saving.value = false;
  }
};

const onVisibleChange = (visible: boolean) => {
  emit("update:visible", visible);
  if (!visible) {
    emit("close");
  }
};

const handleResetForm = () => {
  formState.value = cloneDeep(initialFormState);
  reset("app-group-form");
};





watch(
  () => props.visible,
  (visible) => {
    if (visible && props.group) {
      formState.value = cloneDeep(props.group);
      return;
    }
    handleResetForm();
  }
);

const { ControlLeft_Enter, Meta_Enter } = useMagicKeys();

watch(ControlLeft_Enter, (v) => {
  if (v && !isMac) {
    submitForm("app-group-form");
  }
});

watch(Meta_Enter, (v) => {
  if (v && isMac) {
    submitForm("app-group-form");
  }
});
</script>
<template>
  <VModal :visible="visible" :width="600" :title="modalTitle" @update:visible="onVisibleChange">
    <FormKit
      id="app-group-form"
      v-model="formState.spec"
      name="app-group-form"
      :classes="{ form: 'w-full' }"
      type="form"
      :config="{ validationVisibility: 'submit' }"
      @submit="handleCreateOrUpdateGroup"
    >
      <div class="md:grid md:grid-cols-4 md:gap-6">
        <div class="md:col-span-1">
          <div class="sticky top-0">
            <span class="text-base font-medium text-gray-900"> 常规 </span>
          </div>
        </div>
        <div class="mt-5 divide-y divide-gray-100 md:col-span-3 md:mt-0">
          <div class="pt-5">
            <div class="pb-2 font-medium text-gray-900">作者信息</div>
            <FormKit
              type="group"
              name="owner"
              label="作者信息"
            >
              <div class="space-y-4">
                <FormKit
                  type="text"
                  name="name"
                  label="作者名称"
                  validation="required"
                  help="作者的名字或昵称"
                />
                <FormKit
                  type="text"
                  name="avatar"
                  label="作者头像"
                  help="作者头像图片地址"
                />
                <FormKit
                  type="text"
                  name="label"
                  label="作者标签"
                  help="作者的标签信息，例如：java开发工程师"
                />
                <FormKit
                  type="text"
                  name="description"
                  label="作者描述"
                  help="作者的信息描述"
                />
                <FormKit
                  type="text"
                  name="homepage"
                  label="主页"
                  help="作者的个人主页地址"
                />
                <FormKit
                  type="text"
                  name="repo"
                  label="仓库地址"
                  help="项目仓库地址（如GitHub）"
                />
                <FormKit
                  type="text"
                  name="issues"
                  label="issues地址"
                  help="项目issues地址（如GitHub issues）"
                />
                <FormKit
                  type="text"
                  name="license"
                  label="许可协议"
                  help="项目使用的许可协议（如MIT）"
                />
              </div>
            </FormKit>
          </div>

          <div class="pt-5">
            <div class="pb-2 font-medium text-gray-900">应用信息</div>

            <FormKit
              name="type"
              label="应用类型"
              type="select"
              :options="[
    { label: '插件', value: 'PLUGIN' },
    { label: '主题', value: 'THEME' }
  ]"
              validation="required"
              help="选择应用类型"
            ></FormKit>
            <FormKit
              name="cover"
              label="应用封面"
              type="attachment"
              :accepts="['image/*']"
              help="应用封面图片地址"
            ></FormKit>
            <FormKit
              name="displayName"
              label="应用名称"
              type="text"
              validation="required"
              help="可根据此名称查询应用"
            ></FormKit>
            <FormKit
              name="description"
              label="应用描述"
              type="text"
              help="对该应用的描述"
            ></FormKit>
            <!-- 新增的价格配置部分 -->
            <FormKit
              type="group"
              name="priceConfig"
              label="价格配置"
            >
              <div class="space-y-4">
                <FormKit
                  name="mode"
                  label="价格模式"
                  type="select"
                  :options="[
                    { label: '免费', value: 'FREE' },
                    { label: '一次性付费', value: 'ONE_TIME' }
                  ]"
                  validation="required"
                  help="选择应用的价格模式"
                  @input="handlePriceModeChange"
                />
                <FormKit
                  v-if="formState.spec.priceConfig.mode === 'ONE_TIME'"
                  name="oneTimePrice"
                  label="一次性价格"
                  type="number"
                  number="float"
                  :step="0.01"
                  validation="required|number|min:0.01"
                  validation-visibility="live"
                  help="请输入一次性购买的价格（大于0）"
                  value="0"
                />
                <!-- 隐藏字段确保始终提交 oneTimePrice -->
                <FormKit
                  v-else
                  name="oneTimePrice"
                  type="hidden"
                  :value="0"
                  preserve="true"
                />
              </div>
            </FormKit>
            <FormKit
              name="publish"
              label="发布状态"
              type="checkbox"
              outer-class="mb-4"
              help="勾选表示应用已发布"
            />
            <FormKit
              name="deprecated"
              label="弃用状态"
              type="checkbox"
              outer-class="mb-4"
              help="勾选表示应用已弃用"
            />
<!--            <FormKit-->
<!--              name="viewCount"-->
<!--              label="浏览量"-->
<!--              type="number"-->
<!--              number="integer"-->
<!--              :step="1"-->
<!--              validation="required|number|integer|min:0"-->
<!--              validation-visibility="live"-->
<!--              help="应用浏览量(次)"-->
<!--              value=0-->
<!--            ></FormKit>-->
<!--            <FormKit-->
<!--              name="downloadCount"-->
<!--              label="下载量"-->
<!--              type="number"-->
<!--              number="integer"-->
<!--              :step="1"-->
<!--              validation="required|number|integer|min:0"-->
<!--              validation-visibility="live"-->
<!--              help="应用下载量(次)"-->
<!--              value=0-->
<!--            ></FormKit>-->

            <FormKit
              type="repeater"
              name="screenshots"
              label="应用截图"
              add-label="添加截图"
              :value="[{}]"
              help="最多可上传10张应用截图"
              #default="{ index }"
            >
              <FormKit
                :name="`displayName`"
                label="截图标题"
                type="text"
                validation="required"
                help="截图标题"
              />
              <FormKit
                :name="`description`"
                label="截图描述"
                type="text"
                validation="required"
                help="截图描述"
              />
              <FormKit
                type="attachment"
                :name="`url`"
                label="截图"
                :accepts="['image/*']"
                help="上传PNG/JPG格式的截图"
              />
            </FormKit>

            <FormKit
              type="repeater"
              name="features"
              label="应用特点"
              add-label="添加特点"
              :value="[{}]"
              help="列出应用的核心特点和优势"
              #default="{ index }"
            >
              <FormKit
                type="textarea"
                :name="`feature`"
                label="特点描述"
                placeholder="例如：极速加载、无广告体验、AI智能推荐等"
                help="简短描述核心优势"
              />
            </FormKit>
          </div>
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
          ref="annotationsGroupFormRef"
          :value="formState.metadata.annotations"
          kind="Application"
          group="core.erzip.com"
        />
      </div>
    </div>
    <template #footer>
      <VSpace>
        <VButton type="secondary" @click="submitForm('app-group-form')">
          提交 {{ `${isMac ? "⌘" : "Ctrl"} + ↵` }}
        </VButton>
        <VButton @click="onVisibleChange(false)">取消 Esc</VButton>
      </VSpace>
    </template>
  </VModal>
</template>
