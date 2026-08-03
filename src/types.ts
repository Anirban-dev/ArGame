export interface AndroidFile {
  id: string;
  name: string;
  path: string;
  language: "kotlin" | "xml" | "groovy" | "json";
  content: string;
  isMain?: boolean;
}

export interface NavItem {
  id: string;
  label: string;
  icon: string;
  badgeCount?: number;
}

export interface PreviewCard {
  id: string;
  title: string;
  subtitle: string;
  badge?: string;
  value?: string;
  tag?: string;
  category?: string;
  completed?: boolean;
  time?: string;
  avatar?: string;
}

export interface AndroidPreviewState {
  title: string;
  subtitle?: string;
  theme: "light" | "dark";
  primaryColor: string;
  screenType: "task" | "dashboard" | "e-commerce" | "fitness" | "finance" | "social" | "media";
  navigation: NavItem[];
  activeTab: string;
  cards: PreviewCard[];
  actionButtonText: string;
  searchQuery?: string;
}

export interface AndroidProject {
  appName: string;
  appDescription: string;
  packageName: string;
  version: string;
  targetSdk: number;
  minSdk: number;
  files: AndroidFile[];
  previewState: AndroidPreviewState;
}

export interface BuildLog {
  timestamp: string;
  type: "info" | "success" | "warning" | "error";
  message: string;
}
