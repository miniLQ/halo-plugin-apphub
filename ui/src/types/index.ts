export interface Metadata {
  name: string;
  generateName?: string;
  labels?: {
    [key: string]: string;
  } | null;
  annotations?: {
    [key: string]: string;
  } | null;
  version?: number | null;
  creationTimestamp?: string | null;
  deletionTimestamp?: string | null;
}

export interface ApplicationSpec {
  type: 'PLUGIN' | 'THEME'; // 添加应用类型字段
  cover: string;
  displayName: string;
  description: string;
  screenshots?: AppScreenshot[];
  features?: AppFeature[];
  owner?: Owner;
  priceConfig: PriceConfig;
  publish: boolean;
  deprecated: boolean;
  priority?: number;
}

export interface PriceConfig{
  mode: 'FREE' | 'ONE_TIME';
  oneTimePrice?: number;
}

export interface Owner{
  name: string;
  avatar: string;
  label: string;
  description: string;
  homepage: string;
  repo: string;
  issues: string;
  license: string;
}

export interface AppScreenshot {
  displayName: string;
  description: string;
  url: string;
}
export interface AppFeature {
  feature: string;
}



export interface PostGroupStatus2 {
  releaseCount: number;
  viewCount: number;
  downloadCount: number;
}

export interface ReleaseSpec {
  displayName: string;
  version: string;
  requires: string;
  packageSize: string;
  url: string;
  updateSpecs: UpdateSpec[];
  priority?: number;
  groupName: string;
}

export interface UpdateSpec{
  title: string;
  notes: Note[];
}
export interface Note{
  description: string;
}


export interface Release {
  spec: ReleaseSpec;
  status: ReleaseStatus;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
}

export interface ReleaseStatus{
  publishTimestamp: Date;
}

export interface Application {
  spec: ApplicationSpec;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
  status: PostGroupStatus2;
}
export interface ReleaseList {
  page: number;
  size: number;
  total: number;
  totalPages: number;
  items: Array<Release>;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}
export interface ApplicationList {
  page: number;
  size: number;
  total: number;
  items: Array<Application>;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}
