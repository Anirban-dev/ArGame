import React, { useState } from "react";
import { Info, ExternalLink, Sparkles, CheckCircle2, ChevronDown, ChevronUp, AlertCircle } from "lucide-react";

interface GeminiLinkBannerProps {
  onApplyPrompt: (promptText: string) => void;
}

export const GeminiLinkBanner: React.FC<GeminiLinkBannerProps> = ({ onApplyPrompt }) => {
  const [expanded, setExpanded] = useState(true);

  return (
    <div className="bg-gradient-to-r from-amber-500/10 via-indigo-500/10 to-purple-500/10 border-b border-amber-500/20 px-4 py-3">
      <div className="max-w-7xl mx-auto">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start gap-3">
            <div className="p-2 rounded-lg bg-amber-500/20 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5">
              <AlertCircle className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2 flex-wrap">
                <span className="font-semibold text-sm text-slate-800 dark:text-slate-100">
                  Gemini App Link Notice
                </span>
                <span className="inline-flex items-center gap-1 text-xs px-2 py-0.5 rounded-full bg-slate-200 dark:bg-slate-800 text-slate-700 dark:text-slate-300 font-mono">
                  gemini.google.com/app/7591d8d6bb79b484
                </span>
              </div>
              <p className="text-xs text-slate-600 dark:text-slate-300 mt-1 leading-relaxed">
                Gemini chat share URLs (<code className="bg-amber-100 dark:bg-amber-950/60 px-1 py-0.5 rounded text-amber-800 dark:text-amber-300">gemini.google.com/app/...</code>) are tied to your personal Google Account and cannot be directly fetched by external agents without account authentication.
              </p>
            </div>
          </div>
          <button
            onClick={() => setExpanded(!expanded)}
            className="p-1 rounded-md text-slate-500 hover:text-slate-700 dark:hover:text-slate-300 transition-colors shrink-0"
            title={expanded ? "Collapse details" : "Expand details"}
          >
            {expanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </button>
        </div>

        {expanded && (
          <div className="mt-3 pt-3 border-t border-slate-200/60 dark:border-slate-800/60 grid grid-cols-1 md:grid-cols-3 gap-3 text-xs">
            <div className="bg-white/80 dark:bg-slate-900/80 p-3 rounded-lg border border-slate-200 dark:border-slate-800">
              <div className="font-medium text-slate-900 dark:text-slate-100 flex items-center gap-1.5 mb-1">
                <CheckCircle2 className="w-4 h-4 text-emerald-500" />
                1. Full Kotlin Code Base Built
              </div>
              <p className="text-slate-500 dark:text-slate-400">
                We've initialized a complete production-grade Kotlin Jetpack Compose Android application below with <code className="text-indigo-600 dark:text-indigo-400">MainActivity.kt</code>, ViewModels, and Gradle files.
              </p>
            </div>

            <div className="bg-white/80 dark:bg-slate-900/80 p-3 rounded-lg border border-slate-200 dark:border-slate-800">
              <div className="font-medium text-slate-900 dark:text-slate-100 flex items-center gap-1.5 mb-1">
                <Sparkles className="w-4 h-4 text-indigo-500" />
                2. Live AI Generation Available
              </div>
              <p className="text-slate-500 dark:text-slate-400">
                If your Gemini link described a specific app idea (e.g. E-Commerce, Crypto, Weather, Fitness, Social), paste that prompt into the AI prompt bar below to render it live.
              </p>
            </div>

            <div className="bg-white/80 dark:bg-slate-900/80 p-3 rounded-lg border border-slate-200 dark:border-slate-800 flex flex-col justify-between">
              <div>
                <div className="font-medium text-slate-900 dark:text-slate-100 flex items-center gap-1.5 mb-1">
                  <ExternalLink className="w-4 h-4 text-blue-500" />
                  3. Quick Demo Presets
                </div>
                <p className="text-slate-500 dark:text-slate-400">
                  Try instantly updating your Android device preview to popular Kotlin app archetypes.
                </p>
              </div>
              <div className="flex gap-1.5 mt-2 flex-wrap">
                <button
                  onClick={() => onApplyPrompt("Create a sleek Fitness & Workout tracker Android app with calorie metrics, daily exercise streak, and Jetpack Compose progress cards.")}
                  className="px-2 py-1 bg-indigo-50 dark:bg-indigo-950/50 hover:bg-indigo-100 dark:hover:bg-indigo-900/50 text-indigo-700 dark:text-indigo-300 rounded font-medium text-[11px] transition-colors"
                >
                  Fitness App
                </button>
                <button
                  onClick={() => onApplyPrompt("Create a clean E-Commerce Shopping Android app with product catalog, cart summary, and checkout screen.")}
                  className="px-2 py-1 bg-emerald-50 dark:bg-emerald-950/50 hover:bg-emerald-100 dark:hover:bg-emerald-900/50 text-emerald-700 dark:text-emerald-300 rounded font-medium text-[11px] transition-colors"
                >
                  E-Commerce App
                </button>
                <button
                  onClick={() => onApplyPrompt("Create a modern Finance & Wallet Android app with transaction history, balance chart, and quick transfer action button.")}
                  className="px-2 py-1 bg-purple-50 dark:bg-purple-950/50 hover:bg-purple-100 dark:hover:bg-purple-900/50 text-purple-700 dark:text-purple-300 rounded font-medium text-[11px] transition-colors"
                >
                  Finance Wallet
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
