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
  ExternalLink,
  Box
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

            {/* Phone Frame - AR Spatial Viewfinder */}
            <div
              className={`w-[340px] h-[680px] rounded-[40px] border-[10px] border-slate-800 shadow-2xl relative flex flex-col overflow-hidden transition-colors ${
                phoneTheme === "dark" ? "bg-slate-950 text-slate-100" : "bg-slate-900 text-slate-100"
              }`}
            >
              {/* Android Notch / Camera Island */}
              <div className="h-6 bg-slate-950 w-full flex items-center justify-between px-6 text-[10px] text-slate-400 select-none z-30 shrink-0">
                <span>09:41</span>
                <div className="w-16 h-3 bg-black rounded-full mx-auto" />
                <div className="flex items-center gap-1">
                  <span>5G</span>
                  <div className="w-2.5 h-2.5 rounded-full border border-slate-400" />
                </div>
              </div>

              {/* AR Top HUD Bar */}
              <div className="p-3 bg-slate-950/80 backdrop-blur border-b border-slate-800/80 flex items-center justify-between z-20 shrink-0">
                <div className="flex items-center gap-2">
                  <div className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                  <div>
                    <h2 className="font-bold text-xs text-slate-100 leading-tight">
                      AR Spatial Viewfinder
                    </h2>
                    <p className="text-[10px] text-slate-400 font-mono">
                      ARCore 1.41.0 &bull; Jetpack Compose
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => setCardsState([])}
                  className="px-2 py-1 bg-rose-950/60 hover:bg-rose-900 text-rose-300 border border-rose-800/50 rounded text-[10px] transition-colors"
                >
                  Clear Anchors
                </button>
              </div>

              {/* AR Viewport & Camera Feed Background */}
              <div
                onClick={(e) => {
                  const rect = e.currentTarget.getBoundingClientRect();
                  const x = ((e.clientX - rect.left) / rect.width - 0.5) * 2;
                  const y = ((e.clientY - rect.top) / rect.height - 0.5) * 2;
                  const newAnchor: PreviewCard = {
                    id: Date.now().toString(),
                    title: `3D AR Anchor #${cardsState.length + 1}`,
                    subtitle: `Pos: (${x.toFixed(2)}m, ${y.toFixed(2)}m, -1.2m)`,
                    badge: "Anchor Locked",
                    value: "45° Rot",
                    tag: "3D Compose Node",
                    completed: true
                  };
                  setCardsState([...cardsState, newAnchor]);
                }}
                className="flex-1 relative bg-gradient-to-b from-slate-900 via-slate-950 to-indigo-950/50 overflow-hidden cursor-crosshair group select-none"
              >
                {/* Simulated AR Feature Point Cloud */}
                <div className="absolute inset-0 opacity-30 pointer-events-none bg-[radial-gradient(#818cf8_1px,transparent_1px)] [background-size:16px_16px]" />

                {/* AR Horizon Plane Grid */}
                <div className="absolute inset-x-0 bottom-12 h-36 border-t border-indigo-500/30 bg-gradient-to-t from-indigo-500/10 to-transparent transform perspective-500 rotate-x-60 pointer-events-none" />

                {/* Center AR Reticle Target */}
                <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                  <div className="w-24 h-24 rounded-full border border-indigo-400/50 flex items-center justify-center relative animate-pulse">
                    <div className="w-2 h-2 rounded-full bg-indigo-400" />
                    <div className="absolute top-0 w-px h-3 bg-indigo-400" />
                    <div className="absolute bottom-0 w-px h-3 bg-indigo-400" />
                    <div className="absolute left-0 w-3 h-px bg-indigo-400" />
                    <div className="absolute right-0 w-3 h-px bg-indigo-400" />
                  </div>
                </div>

                {/* Rendered 3D AR Objects / Anchors in Viewport */}
                {cardsState.map((card, idx) => {
                  const topPos = 25 + (idx % 3) * 22;
                  const leftPos = 20 + (idx % 2) * 45;
                  return (
                    <div
                      key={card.id}
                      style={{ top: `${topPos}%`, left: `${leftPos}%` }}
                      className="absolute -translate-x-1/2 -translate-y-1/2 flex flex-col items-center pointer-events-auto"
                    >
                      {/* 3D Model Visual Proxy */}
                      <div className="w-12 h-12 rounded-xl bg-gradient-to-tr from-indigo-600 to-purple-500 shadow-lg shadow-indigo-500/50 border border-white/30 flex items-center justify-center text-white transform hover:scale-110 transition-transform">
                        <Box className="w-6 h-6 animate-bounce" />
                      </div>
                      {/* Spatial Badge Tag */}
                      <div className="mt-1 bg-slate-900/90 backdrop-blur border border-indigo-500/40 text-indigo-300 px-2 py-0.5 rounded text-[9px] font-mono shadow whitespace-nowrap">
                        {card.title}
                      </div>
                    </div>
                  );
                })}

                {/* AR Surface Detection Status Overlay */}
                <div className="absolute top-3 inset-x-3 bg-slate-900/80 backdrop-blur border border-slate-800 p-2 rounded-lg flex items-center justify-between text-[11px] pointer-events-none">
                  <div className="flex items-center gap-1.5 text-emerald-400 font-medium">
                    <Sparkles className="w-3.5 h-3.5" />
                    <span>Plane Detected (Horizontal)</span>
                  </div>
                  <span className="text-[10px] text-slate-400 font-mono">
                    Tap screen to drop 3D model
                  </span>
                </div>

                {/* Live Distance Meter Overlay */}
                <div className="absolute bottom-3 left-3 right-3 bg-slate-900/90 backdrop-blur border border-indigo-500/30 p-2.5 rounded-xl flex items-center justify-between text-xs">
                  <div>
                    <span className="text-[10px] text-slate-400 block uppercase font-mono">
                      Spatial Anchor Distance
                    </span>
                    <span className="font-bold text-indigo-300 font-mono text-sm">
                      {cardsState.length > 1 ? "1.42 meters (55.9 in)" : "Tap surface for 2nd point"}
                    </span>
                  </div>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleAddNewCard();
                    }}
                    className="px-3 py-1.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-lg text-xs font-semibold shadow flex items-center gap-1 transition-colors"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    Drop Node
                  </button>
                </div>
              </div>

              {/* Android Bottom Navigation Bar */}
              <div className="h-14 border-t border-slate-800 bg-slate-950 px-4 flex items-center justify-around shrink-0 text-slate-400">
                <button
                  onClick={() => setActivePhoneTab("ar_view")}
                  className={`flex flex-col items-center gap-0.5 text-[10px] ${
                    activePhoneTab === "ar_view" ? "text-indigo-400 font-bold" : "opacity-60"
                  }`}
                >
                  <Box className="w-4 h-4" />
                  <span>3D AR</span>
                </button>
                <button
                  onClick={() => setActivePhoneTab("measure")}
                  className={`flex flex-col items-center gap-0.5 text-[10px] ${
                    activePhoneTab === "measure" ? "text-indigo-400 font-bold" : "opacity-60"
                  }`}
                >
                  <Compass className="w-4 h-4" />
                  <span>Ruler</span>
                </button>
                <button
                  onClick={() => setActivePhoneTab("settings")}
                  className={`flex flex-col items-center gap-0.5 text-[10px] ${
                    activePhoneTab === "settings" ? "text-indigo-400 font-bold" : "opacity-60"
                  }`}
                >
                  <Settings className="w-4 h-4" />
                  <span>Config</span>
                </button>
              </div>

              {/* Android System Navigation Gesture Bar */}
              <div className="h-4 bg-slate-950 w-full flex items-center justify-center shrink-0">
                <div className="w-28 h-1 bg-slate-700 rounded-full" />
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
