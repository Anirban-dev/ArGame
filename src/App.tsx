import React, { useState, useRef } from "react";
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
  Box,
  Camera,
  Video,
  VideoOff,
  User,
  Cpu,
  Database,
  Crosshair,
  Zap,
  Activity,
  Shield,
  ArrowDown,
  Radio
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
  const [isWebcamActive, setIsWebcamActive] = useState(false);
  const videoRef = useRef<HTMLVideoElement | null>(null);

  // Persistent Multi-Game Architecture State
  const [activeGameModule, setActiveGameModule] = useState<"shooter" | "runner" | "puzzle">("shooter");
  const [playerTotalXp, setPlayerTotalXp] = useState(1450);
  const [playerCoins, setPlayerCoins] = useState(320);
  const [shooterScore, setShooterScore] = useState(850);
  const [runnerScore, setRunnerScore] = useState(1200);
  const [puzzleAnchors, setPuzzleAnchors] = useState(14);
  const [showArchDiagram, setShowArchDiagram] = useState(false);
  const [architectureLogs, setArchitectureLogs] = useState<string[]>([
    "[ProfileManager] User Auth: Verified (usr_ar_994 • Level 8)",
    "[SharedDataManager] Cloud Sync: Online (1,450 XP)",
    "[GameLauncherManager] Additive Load -> Dynamic Module 'TargetShooterGame' (IMiniGame)",
    "[IMiniGame] Initialized -> State: RUNNING"
  ]);

  const handleModuleSwitch = (module: "shooter" | "runner" | "puzzle") => {
    setActiveGameModule(module);
    const titleMap = {
      shooter: "AR Target Shooter",
      runner: "Spatial Speed Runner",
      puzzle: "AR Studio & Spatial Ruler"
    };
    setArchitectureLogs((prev) => [
      ...prev.slice(-12),
      `[GameLauncherManager] Unloading current module...`,
      `[GameLauncherManager] Dynamic Load -> IMiniGame '${titleMap[module]}'`,
      `[IMiniGame] State: RUNNING (Syncing events to SharedDataManager)`
    ]);
  };

  const handleGameAction = (points: number, eventDesc: string) => {
    setPlayerTotalXp((prev) => prev + points);
    if (activeGameModule === "shooter") {
      setShooterScore((prev) => prev + points);
    } else if (activeGameModule === "runner") {
      setRunnerScore((prev) => prev + points);
    } else {
      setPuzzleAnchors((prev) => prev + 1);
    }
    setArchitectureLogs((prev) => [
      ...prev.slice(-12),
      `[IMiniGame -> SharedData] Event: ${eventDesc} (+${points} XP)`,
      `[SharedDataManager] Sunk Progress -> Total XP: ${playerTotalXp + points}`
    ]);
  };

  const toggleWebcam = async () => {
    if (isWebcamActive) {
      if (videoRef.current && videoRef.current.srcObject) {
        const stream = videoRef.current.srcObject as MediaStream;
        stream.getTracks().forEach((track) => track.stop());
        videoRef.current.srcObject = null;
      }
      setIsWebcamActive(false);
    } else {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: "environment" }
        });
        if (videoRef.current) {
          videoRef.current.srcObject = stream;
        }
        setIsWebcamActive(true);
      } catch (err) {
        alert("Camera permission denied or camera unavailable in this browser context.");
      }
    }
  };
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

              {/* Top HUD - PERSISTENT CORE LAUNCHER BAR */}
              <div className="bg-slate-950 p-2.5 border-b border-indigo-900/60 z-20 shrink-0 select-none">
                {/* Profile & Auth Manager */}
                <div className="flex items-center justify-between text-[11px] pb-2 border-b border-slate-800/80">
                  <div className="flex items-center gap-1.5">
                    <div className="w-5 h-5 rounded-full bg-indigo-600 flex items-center justify-center text-white text-[10px] font-bold">
                      CP
                    </div>
                    <div>
                      <span className="font-semibold text-slate-200">CyberPilot_AR</span>
                      <span className="ml-1 text-[9px] text-indigo-400 font-mono">Lvl 8</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 font-mono text-[10px]">
                    <span className="text-amber-400 font-bold">{playerTotalXp} XP</span>
                    <span className="text-emerald-400 font-bold">{playerCoins} Coins</span>
                  </div>
                </div>

                {/* Shared Data & Core Controller Status */}
                <div className="mt-2 flex items-center justify-between text-[10px]">
                  <div className="flex items-center gap-1 text-slate-300">
                    <Cpu className="w-3 h-3 text-indigo-400" />
                    <span className="font-mono text-indigo-300 font-medium">IMiniGame Module:</span>
                  </div>
                  <button
                    onClick={() => setShowArchDiagram(!showArchDiagram)}
                    className="px-2 py-0.5 rounded bg-indigo-950 hover:bg-indigo-900 text-indigo-300 border border-indigo-700/60 text-[9px] flex items-center gap-1 font-mono transition-colors"
                  >
                    <Layers className="w-3 h-3 text-indigo-400" />
                    <span>{showArchDiagram ? "Hide Diagram" : "Arch Diagram"}</span>
                  </button>
                </div>

                {/* Game Selector Tabs (Dynamic Module Loader) */}
                <div className="mt-2 grid grid-cols-3 gap-1 bg-slate-900 p-1 rounded-lg border border-slate-800 text-[10px]">
                  <button
                    onClick={() => handleModuleSwitch("shooter")}
                    className={`py-1 rounded flex items-center justify-center gap-1 font-medium transition-colors ${
                      activeGameModule === "shooter"
                        ? "bg-indigo-600 text-white font-bold shadow"
                        : "text-slate-400 hover:text-slate-200"
                    }`}
                  >
                    <Crosshair className="w-3 h-3" />
                    <span>1. Shooter</span>
                  </button>
                  <button
                    onClick={() => handleModuleSwitch("runner")}
                    className={`py-1 rounded flex items-center justify-center gap-1 font-medium transition-colors ${
                      activeGameModule === "runner"
                        ? "bg-indigo-600 text-white font-bold shadow"
                        : "text-slate-400 hover:text-slate-200"
                    }`}
                  >
                    <Zap className="w-3 h-3" />
                    <span>2. Runner</span>
                  </button>
                  <button
                    onClick={() => handleModuleSwitch("puzzle")}
                    className={`py-1 rounded flex items-center justify-center gap-1 font-medium transition-colors ${
                      activeGameModule === "puzzle"
                        ? "bg-indigo-600 text-white font-bold shadow"
                        : "text-slate-400 hover:text-slate-200"
                    }`}
                  >
                    <Box className="w-3 h-3" />
                    <span>3. Studio</span>
                  </button>
                </div>
              </div>

              {/* AR Viewport & Camera Feed Background */}
              <div
                onClick={(e) => {
                  const rect = e.currentTarget.getBoundingClientRect();
                  const x = ((e.clientX - rect.left) / rect.width - 0.5) * 2;
                  const y = ((e.clientY - rect.top) / rect.height - 0.5) * 2;

                  if (activeGameModule === "shooter") {
                    handleGameAction(100, `Target Hit at (${x.toFixed(2)}m, ${y.toFixed(2)}m)`);
                  } else if (activeGameModule === "runner") {
                    handleGameAction(150, `Obstacle Dodged & Token Collected`);
                  } else {
                    const newAnchor: PreviewCard = {
                      id: Date.now().toString(),
                      title: `3D AR Node #${cardsState.length + 1}`,
                      subtitle: `Pos: (${x.toFixed(2)}m, ${y.toFixed(2)}m, -1.2m)`,
                      badge: "IMiniGame Anchor",
                      value: "45° Rot",
                      tag: "Spatial Studio",
                      completed: true
                    };
                    setCardsState([...cardsState, newAnchor]);
                    handleGameAction(50, `Spatial Anchor Placed`);
                  }
                }}
                className="flex-1 relative bg-gradient-to-b from-slate-900 via-slate-950 to-indigo-950/50 overflow-hidden cursor-crosshair group select-none"
              >
                {/* Live WebCam Stream Video */}
                <video
                  ref={videoRef}
                  autoPlay
                  playsInline
                  muted
                  className={`absolute inset-0 w-full h-full object-cover pointer-events-none transition-opacity duration-300 ${
                    isWebcamActive ? "opacity-75" : "opacity-0"
                  }`}
                />

                {/* Architecture Diagram Overlay (when toggled) */}
                {showArchDiagram && (
                  <div className="absolute inset-0 z-40 bg-slate-950/95 p-3 flex flex-col justify-between text-slate-200 font-mono text-[10px] backdrop-blur overflow-y-auto">
                    <div>
                      <div className="flex items-center justify-between pb-1.5 border-b border-indigo-800">
                        <span className="font-bold text-indigo-400 flex items-center gap-1">
                          <Layers className="w-3.5 h-3.5" /> ARCHITECTURE DIAGRAM
                        </span>
                        <button
                          onClick={() => setShowArchDiagram(false)}
                          className="text-slate-400 hover:text-white px-1.5 py-0.5 rounded bg-slate-800"
                        >
                          ✕ Close
                        </button>
                      </div>

                      {/* Main Launcher App Persistent Box */}
                      <div className="mt-2 p-2 rounded bg-indigo-950/70 border border-indigo-700/80">
                        <div className="font-bold text-center text-indigo-300 mb-1.5">
                          MAIN LAUNCHER APP (Persistent Core)
                        </div>
                        <div className="grid grid-cols-3 gap-1 text-[8px] text-center">
                          <div className="p-1 rounded bg-slate-900 border border-slate-700">
                            <User className="w-3 h-3 mx-auto text-indigo-400 mb-0.5" />
                            <span>Profile/Auth Manager</span>
                          </div>
                          <div className="p-1 rounded bg-slate-900 border border-slate-700">
                            <Cpu className="w-3 h-3 mx-auto text-indigo-400 mb-0.5" />
                            <span>Core Controller (Scene)</span>
                          </div>
                          <div className="p-1 rounded bg-slate-900 border border-slate-700">
                            <Database className="w-3 h-3 mx-auto text-indigo-400 mb-0.5" />
                            <span>Shared Data / Score</span>
                          </div>
                        </div>
                      </div>

                      {/* Communication Arrows */}
                      <div className="my-1.5 flex justify-around text-[8px] text-indigo-400 font-bold text-center">
                        <div className="flex flex-col items-center">
                          <ArrowDown className="w-3 h-3 animate-bounce" />
                          <span>Passes Input</span>
                        </div>
                        <div className="flex flex-col items-center">
                          <ArrowDown className="w-3 h-3 animate-bounce" />
                          <span>Loads/Unloads</span>
                        </div>
                        <div className="flex flex-col items-center">
                          <ArrowDown className="w-3 h-3 animate-bounce" />
                          <span>Sinks Progress</span>
                        </div>
                      </div>

                      {/* Dynamic Mini-Game Module Box */}
                      <div className="p-2 rounded bg-slate-900/90 border border-emerald-700/80">
                        <div className="font-bold text-center text-emerald-300 mb-1.5">
                          ACTIVE MINI-GAME MODULE (Dynamic IMiniGame)
                        </div>
                        <div className="grid grid-cols-3 gap-1 text-[8px] text-center">
                          <div className={`p-1 rounded border ${activeGameModule === "shooter" ? "bg-indigo-900 border-indigo-400 font-bold text-white" : "bg-slate-950 border-slate-800 opacity-60"}`}>
                            Game 1: Shooter
                          </div>
                          <div className={`p-1 rounded border ${activeGameModule === "runner" ? "bg-indigo-900 border-indigo-400 font-bold text-white" : "bg-slate-950 border-slate-800 opacity-60"}`}>
                            Game 2: Runner
                          </div>
                          <div className={`p-1 rounded border ${activeGameModule === "puzzle" ? "bg-indigo-900 border-indigo-400 font-bold text-white" : "bg-slate-950 border-slate-800 opacity-60"}`}>
                            Game 3: Studio
                          </div>
                        </div>
                      </div>
                    </div>

                    <div className="mt-2 pt-1 border-t border-slate-800 text-[9px] text-slate-400">
                      💡 Decoupled architecture: Launcher core manages user auth and score state while dynamic mini-game modules run via IMiniGame interface.
                    </div>
                  </div>
                )}

                {/* Webcam Controls & Info Banner */}
                <div className="absolute top-2 inset-x-2 z-10 flex items-center justify-between">
                  <button
                    onClick={toggleWebcam}
                    className={`px-2 py-1 flex items-center gap-1 border rounded text-[10px] font-mono shadow transition-colors backdrop-blur ${
                      isWebcamActive
                        ? "bg-emerald-950/90 text-emerald-300 border-emerald-700"
                        : "bg-slate-950/90 text-slate-300 border-slate-700"
                    }`}
                  >
                    {isWebcamActive ? <VideoOff className="w-3 h-3" /> : <Camera className="w-3 h-3" />}
                    <span>{isWebcamActive ? "Cam Active" : "WebCam Feed"}</span>
                  </button>
                  <span className="text-[9px] px-2 py-0.5 rounded-full bg-slate-950/80 border border-slate-800 text-slate-300 font-mono">
                    ARCore 1.41.0 &bull; Jetpack Compose
                  </span>
                </div>

                {/* Simulated AR Feature Point Cloud */}
                <div className="absolute inset-0 opacity-30 pointer-events-none bg-[radial-gradient(#818cf8_1px,transparent_1px)] [background-size:16px_16px]" />

                {/* AR Horizon Plane Grid */}
                <div className="absolute inset-x-0 bottom-12 h-36 border-t border-indigo-500/30 bg-gradient-to-t from-indigo-500/10 to-transparent transform perspective-500 rotate-x-60 pointer-events-none" />

                {/* GAME MODULE 1: AR SHOOTER TARGETS */}
                {activeGameModule === "shooter" && (
                  <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                    {/* Animated Target Spheres */}
                    <div className="absolute top-1/4 left-1/4 w-12 h-12 rounded-full border-2 border-rose-500 bg-rose-500/20 animate-ping flex items-center justify-center">
                      <div className="w-4 h-4 rounded-full bg-rose-500" />
                    </div>
                    <div className="absolute bottom-1/3 right-1/4 w-10 h-10 rounded-full border-2 border-rose-400 bg-rose-500/30 animate-pulse flex items-center justify-center">
                      <div className="w-3 h-3 rounded-full bg-rose-400" />
                    </div>

                    {/* Laser Crosshair */}
                    <div className="w-20 h-20 rounded-full border border-rose-400/60 flex items-center justify-center relative">
                      <div className="w-2 h-2 rounded-full bg-rose-400 animate-pulse" />
                      <div className="absolute top-0 w-px h-3 bg-rose-400" />
                      <div className="absolute bottom-0 w-px h-3 bg-rose-400" />
                      <div className="absolute left-0 w-3 h-px bg-rose-400" />
                      <div className="absolute right-0 w-3 h-px bg-rose-400" />
                    </div>

                    <div className="absolute top-14 bg-slate-950/90 border border-rose-500/40 px-3 py-1 rounded-full text-[10px] text-rose-300 font-mono shadow">
                      🎯 Tap view to Shoot Targets (+100 XP)
                    </div>
                  </div>
                )}

                {/* GAME MODULE 2: SPATIAL RUNNER */}
                {activeGameModule === "runner" && (
                  <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                    {/* Speed Lines */}
                    <div className="absolute inset-0 opacity-20 bg-[linear-gradient(to_bottom,transparent_0%,#818cf8_50%,transparent_100%)] animate-pulse" />
                    
                    {/* Floating Power Token */}
                    <div className="absolute top-1/3 w-14 h-14 rounded-2xl bg-amber-500/30 border-2 border-amber-400 flex flex-col items-center justify-center animate-bounce text-amber-300 font-bold text-xs">
                      <Zap className="w-5 h-5" />
                      <span className="text-[9px]">2x Token</span>
                    </div>

                    <div className="absolute top-14 bg-slate-950/90 border border-amber-500/40 px-3 py-1 rounded-full text-[10px] text-amber-300 font-mono shadow">
                      ⚡ Tap view to Dodge & Collect (+150 XP)
                    </div>
                  </div>
                )}

                {/* GAME MODULE 3: AR STUDIO */}
                {activeGameModule === "puzzle" && (
                  <>
                    <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                      <div className="w-24 h-24 rounded-full border border-indigo-400/50 flex items-center justify-center relative">
                        <div className="w-2 h-2 rounded-full bg-indigo-400" />
                      </div>
                      <div className="absolute top-14 bg-slate-950/90 border border-indigo-500/40 px-3 py-1 rounded-full text-[10px] text-indigo-300 font-mono shadow">
                        📦 Tap surface to Place 3D Anchor (+50 XP)
                      </div>
                    </div>

                    {cardsState.map((card, idx) => {
                      const topPos = 25 + (idx % 3) * 22;
                      const leftPos = 20 + (idx % 2) * 45;
                      return (
                        <div
                          key={card.id}
                          style={{ top: `${topPos}%`, left: `${leftPos}%` }}
                          className="absolute -translate-x-1/2 -translate-y-1/2 flex flex-col items-center pointer-events-auto z-10"
                        >
                          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-600 to-purple-500 shadow-lg shadow-indigo-500/50 border border-white/30 flex items-center justify-center text-white transform hover:scale-110 transition-transform">
                            <Box className="w-5 h-5 animate-bounce" />
                          </div>
                          <div className="mt-1 bg-slate-900/90 backdrop-blur border border-indigo-500/40 text-indigo-300 px-1.5 py-0.5 rounded text-[8px] font-mono shadow whitespace-nowrap">
                            {card.title}
                          </div>
                        </div>
                      );
                    })}
                  </>
                )}

                {/* Bottom Live Data Sink Overlay */}
                <div className="absolute bottom-2 inset-x-2 bg-slate-950/95 border border-indigo-500/40 p-2 rounded-xl text-[10px] z-20 shadow-xl">
                  <div className="flex items-center justify-between font-mono text-indigo-300 font-medium mb-1 border-b border-slate-800 pb-1">
                    <span className="flex items-center gap-1">
                      <Radio className="w-3 h-3 text-emerald-400 animate-pulse" />
                      <span>SHARED DATA SINK STREAM</span>
                    </span>
                    <span className="text-slate-400 text-[9px]">IMiniGame -&gt; SharedDataManager</span>
                  </div>
                  <div className="h-10 overflow-y-auto font-mono text-[9px] text-slate-400 space-y-0.5">
                    {architectureLogs.map((log, i) => (
                      <div
                        key={i}
                        className={
                          log.includes("Sunk") || log.includes("SharedData")
                            ? "text-emerald-400 font-medium"
                            : log.includes("Dynamic")
                            ? "text-indigo-300"
                            : ""
                        }
                      >
                        {log}
                      </div>
                    ))}
                  </div>
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
