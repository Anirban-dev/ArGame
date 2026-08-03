import React, { useState } from "react";
import {
  Smartphone,
  Code2,
  Terminal,
  FolderTree,
  Play,
  Copy,
  Check,
  Sparkles,
  Download,
  Moon,
  Sun,
  Plus,
  Search,
  CheckSquare,
  Home,
  Compass,
  Settings,
  ShieldAlert,
  Layers,
  FileCode,
  Folder,
  ChevronRight,
  ChevronDown,
  RefreshCw,
  ExternalLink
} from "lucide-react";
import { defaultAndroidProject } from "./data/defaultAndroidProject";
import { GeminiLinkBanner } from "./components/GeminiLinkBanner";
import { AndroidFile, PreviewCard } from "./types";

export default function App() {
  const [project, setProject] = useState(defaultAndroidProject);
  const [selectedFileId, setSelectedFileId] = useState<string>(project.files[0]?.id || "main-activity");
  const [copied, setCopied] = useState(false);
  const [activeView, setActiveView] = useState<"split" | "phone" | "code">("split");
  const [customPrompt, setCustomPrompt] = useState("");
  const [isGenerating, setIsGenerating] = useState(false);
  const [phoneTheme, setPhoneTheme] = useState<"light" | "dark">("light");
  const [activePhoneTab, setActivePhoneTab] = useState("home");
  const [cardsState, setCardsState] = useState<PreviewCard[]>(project.previewState.cards);
  const [buildLogs, setBuildLogs] = useState<string[]>([
    "Executing tasks: [:app:assembleDebug]",
    "Starting Gradle Daemon...",
    "Gradle Daemon started in 1s 204ms",
    "> Task :app:preBuild UP-TO-DATE",
    "> Task :app:compileDebugKotlin SUCCESS",
    "> Task :app:mergeDebugResources SUCCESS",
    "> Task :app:packageDebug SUCCESS",
    "BUILD SUCCESSFUL in 2.4s",
    "APK generated at /app/build/outputs/apk/debug/app-debug.apk"
  ]);

  const selectedFile = project.files.find((f) => f.id === selectedFileId) || project.files[0];

  const handleCopyCode = () => {
    if (selectedFile) {
      navigator.clipboard.writeText(selectedFile.content);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleApplyPrompt = async (promptText: string) => {
    setCustomPrompt(promptText);
    setIsGenerating(true);
    setBuildLogs((prev) => [...prev, `[AI Studio] Generating Kotlin Jetpack Compose app: "${promptText.slice(0, 40)}..."`]);

    try {
      const response = await fetch("/api/generate-kotlin", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ prompt: promptText })
      });
      const data = await response.json();

      if (data.success && data.data) {
        const generated = data.data;
        const newFiles: AndroidFile[] = generated.files.map((f: any, idx: number) => ({
          id: `gen-${idx}-${Date.now()}`,
          name: f.path.split("/").pop() || `File${idx}.kt`,
          path: f.path,
          language: f.language || "kotlin",
          content: f.content
        }));

        setProject((prev) => ({
          ...prev,
          appName: generated.appName || prev.appName,
          appDescription: generated.appDescription || prev.appDescription,
          packageName: generated.packageName || prev.packageName,
          files: newFiles.length > 0 ? newFiles : prev.files,
          previewState: generated.previewState || prev.previewState
        }));

        if (generated.previewState?.cards) {
          setCardsState(generated.previewState.cards);
        }
        if (newFiles.length > 0) {
          setSelectedFileId(newFiles[0].id);
        }

        setBuildLogs((prev) => [
          ...prev,
          "> Task :app:compileDebugKotlin SUCCESS",
          `BUILD SUCCESSFUL: Updated Kotlin App "${generated.appName}"`
        ]);
      } else {
        setBuildLogs((prev) => [...prev, `[Warning] Using fallback preview. Note: ${data.error || "Failed to parse Response"}`]);
      }
    } catch (err: any) {
      console.error(err);
      setBuildLogs((prev) => [...prev, `[Error] ${err?.message || "Generation error"}`]);
    } finally {
      setIsGenerating(false);
    }
  };

  const toggleCardCompletion = (cardId: string) => {
    setCardsState((prev) =>
      prev.map((c) => (c.id === cardId ? { ...c, completed: !c.completed } : c))
    );
  };

  const handleAddNewCard = () => {
    const newCard: PreviewCard = {
      id: Date.now().toString(),
      title: "New Compose State Task",
      subtitle: "Created interactively in Jetpack Compose preview",
      badge: "High",
      value: "Now",
      tag: "Kotlin Flow",
      completed: false
    };
    setCardsState([newCard, ...cardsState]);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans antialiased selection:bg-indigo-500 selection:text-white">
      {/* Top Studio Header */}
      <header className="h-14 border-b border-slate-800 bg-slate-900/90 backdrop-blur px-4 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center font-bold text-white shadow-lg shadow-indigo-600/30">
            <Smartphone className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="font-semibold text-sm text-slate-100">
                {project.appName}
              </h1>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-indigo-950 text-indigo-400 border border-indigo-800/50 font-mono">
                Kotlin Native App
              </span>
            </div>
            <p className="text-[11px] text-slate-400 font-mono">
              {project.packageName} &bull; targetSdk {project.targetSdk}
            </p>
          </div>
        </div>

        {/* View Switchers */}
        <div className="flex items-center gap-1 bg-slate-950 p-1 rounded-lg border border-slate-800 text-xs">
          <button
            onClick={() => setActiveView("split")}
            className={`px-3 py-1.5 rounded-md flex items-center gap-1.5 transition-colors ${
              activeView === "split"
                ? "bg-indigo-600 text-white shadow-sm"
                : "text-slate-400 hover:text-slate-200"
            }`}
          >
            <Layers className="w-3.5 h-3.5" />
            Split View
          </button>
          <button
            onClick={() => setActiveView("phone")}
            className={`px-3 py-1.5 rounded-md flex items-center gap-1.5 transition-colors ${
              activeView === "phone"
                ? "bg-indigo-600 text-white shadow-sm"
                : "text-slate-400 hover:text-slate-200"
            }`}
          >
            <Smartphone className="w-3.5 h-3.5" />
            Android Device
          </button>
          <button
            onClick={() => setActiveView("code")}
            className={`px-3 py-1.5 rounded-md flex items-center gap-1.5 transition-colors ${
              activeView === "code"
                ? "bg-indigo-600 text-white shadow-sm"
                : "text-slate-400 hover:text-slate-200"
            }`}
          >
            <Code2 className="w-3.5 h-3.5" />
            Kotlin Source
          </button>
        </div>

        {/* Quick Action Badges */}
        <div className="flex items-center gap-2">
          <button
            onClick={() => {
              setBuildLogs((prev) => [
                ...prev,
                `[Sync] Gradle sync started for ${project.packageName}`,
                "BUILD SUCCESSFUL: All Kotlin dependencies up to date."
              ]);
            }}
            className="px-2.5 py-1.5 text-xs bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-md border border-slate-700 flex items-center gap-1.5 transition-colors"
          >
            <RefreshCw className="w-3.5 h-3.5 text-emerald-400" />
            Gradle Sync
          </button>
        </div>
      </header>

      {/* Notice Banner explaining Gemini link status & AI prompt options */}
      <GeminiLinkBanner onApplyPrompt={handleApplyPrompt} />

      {/* AI Prompt Input Bar */}
      <div className="bg-slate-900/60 border-b border-slate-800 px-4 py-2.5">
        <div className="max-w-7xl mx-auto flex items-center gap-2">
          <div className="relative flex-1">
            <Sparkles className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-indigo-400" />
            <input
              type="text"
              value={customPrompt}
              onChange={(e) => setCustomPrompt(e.target.value)}
              placeholder="Describe any Kotlin Jetpack Compose screen or app feature to generate (e.g., Crypto Wallet, Fitness Tracker, Social Feed)..."
              className="w-full bg-slate-950 text-xs text-slate-200 pl-9 pr-4 py-2 rounded-lg border border-slate-800 focus:outline-none focus:border-indigo-500 transition-colors placeholder:text-slate-500"
              onKeyDown={(e) => {
                if (e.key === "Enter" && customPrompt.trim()) {
                  handleApplyPrompt(customPrompt);
                }
              }}
            />
          </div>
          <button
            onClick={() => customPrompt.trim() && handleApplyPrompt(customPrompt)}
            disabled={isGenerating || !customPrompt.trim()}
            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white rounded-lg text-xs font-medium flex items-center gap-1.5 transition-colors shrink-0"
          >
            {isGenerating ? (
              <>
                <RefreshCw className="w-3.5 h-3.5 animate-spin" />
                Generating Kotlin...
              </>
            ) : (
              <>
                <Sparkles className="w-3.5 h-3.5" />
                Generate App
              </>
            )}
          </button>
        </div>
      </div>

      {/* Main Studio Body */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left File Tree Panel (Visible in Split or Code mode) */}
        {activeView !== "phone" && (
          <aside className="w-64 border-r border-slate-800 bg-slate-900/40 flex flex-col shrink-0">
            <div className="p-3 border-b border-slate-800/80 flex items-center justify-between text-xs font-semibold text-slate-400">
              <div className="flex items-center gap-1.5">
                <FolderTree className="w-4 h-4 text-indigo-400" />
                <span>ANDROID PROJECT TREE</span>
              </div>
            </div>

            <div className="p-2 overflow-y-auto flex-1 font-mono text-xs space-y-1">
              <div className="text-slate-500 px-2 py-1 flex items-center gap-1 text-[11px] font-sans">
                <Folder className="w-3.5 h-3.5 text-amber-400" />
                app / src / main / java
              </div>
              {project.files.map((file) => {
                const isSelected = file.id === selectedFileId;
                return (
                  <button
                    key={file.id}
                    onClick={() => setSelectedFileId(file.id)}
                    className={`w-full text-left px-3 py-2 rounded-md flex items-center justify-between gap-2 transition-colors ${
                      isSelected
                        ? "bg-indigo-600/20 text-indigo-300 border border-indigo-500/30 font-medium"
                        : "text-slate-400 hover:bg-slate-800/50 hover:text-slate-200"
                    }`}
                  >
                    <div className="flex items-center gap-2 truncate">
                      <FileCode
                        className={`w-3.5 h-3.5 shrink-0 ${
                          file.name.endsWith(".kt")
                            ? "text-purple-400"
                            : file.name.endsWith(".xml")
                            ? "text-amber-400"
                            : "text-blue-400"
                        }`}
                      />
                      <span className="truncate">{file.name}</span>
                    </div>
                    {file.isMain && (
                      <span className="text-[9px] px-1.5 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800/50 font-sans">
                        ENTRY
                      </span>
                    )}
                  </button>
                );
              })}

              <div className="pt-4 text-slate-500 px-2 py-1 flex items-center gap-1 text-[11px] font-sans border-t border-slate-800/60 mt-4">
                <Folder className="w-3.5 h-3.5 text-blue-400" />
                Gradle Scripts
              </div>
              <div className="px-3 py-1.5 text-slate-400 text-xs flex items-center gap-2">
                <FileCode className="w-3.5 h-3.5 text-blue-400" />
                build.gradle.kts (App)
              </div>
              <div className="px-3 py-1.5 text-slate-400 text-xs flex items-center gap-2">
                <FileCode className="w-3.5 h-3.5 text-blue-400" />
                settings.gradle.kts
              </div>
            </div>
          </aside>
        )}

        {/* Center: Kotlin Source Viewer */}
        {activeView !== "phone" && selectedFile && (
          <div className="flex-1 flex flex-col min-w-0 bg-slate-950 border-r border-slate-800">
            {/* Code Editor Tab Bar */}
            <div className="h-10 border-b border-slate-800 bg-slate-900/60 px-4 flex items-center justify-between text-xs">
              <div className="flex items-center gap-2 font-mono text-slate-300">
                <FileCode className="w-4 h-4 text-purple-400" />
                <span>{selectedFile.path}</span>
              </div>
              <button
                onClick={handleCopyCode}
                className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-200 flex items-center gap-1.5 transition-colors text-xs font-sans"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-emerald-400" /> : <Copy className="w-3.5 h-3.5" />}
                {copied ? "Copied!" : "Copy Code"}
              </button>
            </div>

            {/* Code Body */}
            <div className="flex-1 overflow-auto p-4 font-mono text-xs leading-relaxed text-slate-200 selection:bg-indigo-900 selection:text-white">
              <pre className="whitespace-pre">
                <code>{selectedFile.content}</code>
              </pre>
            </div>

            {/* Bottom Gradle Console Log */}
            <div className="h-32 border-t border-slate-800 bg-slate-900/80 p-3 flex flex-col font-mono text-[11px]">
              <div className="flex items-center justify-between text-slate-400 mb-1 pb-1 border-b border-slate-800">
                <div className="flex items-center gap-1.5">
                  <Terminal className="w-3.5 h-3.5 text-emerald-400" />
                  <span>GRADLE BUILD CONSOLE LOGS</span>
                </div>
                <span className="text-emerald-400">BUILD SUCCESSFUL</span>
              </div>
              <div className="flex-1 overflow-y-auto space-y-0.5 text-slate-400">
                {buildLogs.map((log, i) => (
                  <div
                    key={i}
                    className={
                      log.includes("SUCCESSFUL")
                        ? "text-emerald-400 font-semibold"
                        : log.includes("Error")
                        ? "text-rose-400"
                        : ""
                    }
                  >
                    {log}
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Right: Interactive Android Device Emulator Preview */}
        {activeView !== "code" && (
          <div className={`flex-1 bg-slate-900/30 p-6 flex flex-col items-center justify-center overflow-y-auto ${activeView === "phone" ? "w-full" : "max-w-md"}`}>
            <div className="mb-3 flex items-center justify-between w-full max-w-[340px] text-xs text-slate-400">
              <div className="flex items-center gap-1.5">
                <Smartphone className="w-4 h-4 text-indigo-400" />
                <span className="font-medium text-slate-300">Android Emulator</span>
              </div>
              <button
                onClick={() => setPhoneTheme(phoneTheme === "light" ? "dark" : "light")}
                className="p-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 flex items-center gap-1"
                title="Toggle Android Device System Dark/Light Mode"
              >
                {phoneTheme === "light" ? <Moon className="w-3.5 h-3.5" /> : <Sun className="w-3.5 h-3.5" />}
                <span className="text-[10px]">{phoneTheme === "light" ? "Dark" : "Light"}</span>
              </button>
            </div>

            {/* Phone Frame */}
            <div
              className={`w-[340px] h-[680px] rounded-[40px] border-[10px] border-slate-800 shadow-2xl relative flex flex-col overflow-hidden transition-colors ${
                phoneTheme === "dark" ? "bg-slate-900 text-slate-100" : "bg-slate-50 text-slate-900"
              }`}
            >
              {/* Android Notch / Camera Island */}
              <div className="h-6 bg-slate-950 w-full flex items-center justify-between px-6 text-[10px] text-slate-400 select-none z-20 shrink-0">
                <span>09:41</span>
                <div className="w-16 h-3 bg-black rounded-full mx-auto" />
                <div className="flex items-center gap-1">
                  <span>5G</span>
                  <div className="w-2.5 h-2.5 rounded-full border border-slate-400" />
                </div>
              </div>

              {/* Android Top App Bar */}
              <div
                className={`p-4 border-b flex items-center justify-between shrink-0 ${
                  phoneTheme === "dark" ? "border-slate-800 bg-slate-900" : "border-slate-200 bg-white"
                }`}
              >
                <div>
                  <h2 className="font-bold text-base leading-tight">
                    {project.previewState.title}
                  </h2>
                  <p className="text-xs opacity-60">
                    {project.previewState.subtitle || "Jetpack Compose Screen"}
                  </p>
                </div>
                <button
                  onClick={handleAddNewCard}
                  className="p-2 rounded-full bg-indigo-600 hover:bg-indigo-500 text-white shadow-md transition-colors"
                  title="Add Task item in Compose State"
                >
                  <Plus className="w-4 h-4" />
                </button>
              </div>

              {/* Android Content Body */}
              <div className="flex-1 overflow-y-auto p-4 space-y-3">
                {/* Compose Progress Card */}
                <div
                  className={`p-4 rounded-2xl border ${
                    phoneTheme === "dark"
                      ? "bg-indigo-950/40 border-indigo-800/40 text-indigo-200"
                      : "bg-indigo-50 border-indigo-100 text-indigo-950"
                  }`}
                >
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-xs font-semibold uppercase tracking-wider opacity-80">
                      Jetpack Compose State
                    </span>
                    <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-indigo-600 text-white">
                      {cardsState.filter((c) => c.completed).length} / {cardsState.length} Done
                    </span>
                  </div>
                  <div className="w-full h-2 bg-indigo-200/40 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-indigo-600 transition-all duration-300"
                      style={{
                        width: `${
                          cardsState.length > 0
                            ? (cardsState.filter((c) => c.completed).length / cardsState.length) * 100
                            : 0
                        }%`
                      }}
                    />
                  </div>
                </div>

                {/* Items List */}
                <div className="space-y-2.5">
                  {cardsState.map((card) => (
                    <div
                      key={card.id}
                      onClick={() => toggleCardCompletion(card.id)}
                      className={`p-3.5 rounded-xl border transition-all cursor-pointer flex items-start gap-3 ${
                        phoneTheme === "dark"
                          ? "bg-slate-800/80 border-slate-700/80 hover:border-indigo-500"
                          : "bg-white border-slate-200 shadow-sm hover:border-indigo-400"
                      }`}
                    >
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          toggleCardCompletion(card.id);
                        }}
                        className={`mt-0.5 w-5 h-5 rounded-md border flex items-center justify-center shrink-0 transition-colors ${
                          card.completed
                            ? "bg-indigo-600 border-indigo-600 text-white"
                            : phoneTheme === "dark"
                            ? "border-slate-600"
                            : "border-slate-300"
                        }`}
                      >
                        {card.completed && <Check className="w-3.5 h-3.5" />}
                      </button>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between gap-1">
                          <h3
                            className={`font-semibold text-xs truncate ${
                              card.completed ? "line-through opacity-50" : ""
                            }`}
                          >
                            {card.title}
                          </h3>
                          {card.badge && (
                            <span className="text-[10px] px-1.5 py-0.5 rounded font-medium bg-indigo-100 text-indigo-700 shrink-0">
                              {card.badge}
                            </span>
                          )}
                        </div>
                        <p className="text-[11px] opacity-70 mt-0.5 line-clamp-2">
                          {card.subtitle}
                        </p>
                        {card.tag && (
                          <div className="mt-2 flex items-center gap-2">
                            <span className="text-[10px] px-2 py-0.5 rounded-md bg-slate-100 dark:bg-slate-700 opacity-80 font-mono">
                              #{card.tag}
                            </span>
                          </div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Android Bottom Navigation Bar */}
              <div
                className={`h-16 border-t px-6 flex items-center justify-around shrink-0 ${
                  phoneTheme === "dark" ? "border-slate-800 bg-slate-900" : "border-slate-200 bg-white"
                }`}
              >
                <button
                  onClick={() => setActivePhoneTab("home")}
                  className={`flex flex-col items-center gap-1 text-[11px] ${
                    activePhoneTab === "home" ? "text-indigo-600 font-bold" : "opacity-50"
                  }`}
                >
                  <Home className="w-4 h-4" />
                  <span>Tasks</span>
                </button>
                <button
                  onClick={() => setActivePhoneTab("explore")}
                  className={`flex flex-col items-center gap-1 text-[11px] ${
                    activePhoneTab === "explore" ? "text-indigo-600 font-bold" : "opacity-50"
                  }`}
                >
                  <Compass className="w-4 h-4" />
                  <span>Explore</span>
                </button>
                <button
                  onClick={() => setActivePhoneTab("settings")}
                  className={`flex flex-col items-center gap-1 text-[11px] ${
                    activePhoneTab === "settings" ? "text-indigo-600 font-bold" : "opacity-50"
                  }`}
                >
                  <Settings className="w-4 h-4" />
                  <span>Settings</span>
                </button>
              </div>

              {/* Android System Navigation Gesture Bar */}
              <div className="h-4 bg-slate-950 w-full flex items-center justify-center shrink-0">
                <div className="w-28 h-1 bg-slate-600 rounded-full" />
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
