import express from "express";
import path from "path";
import { createServer as createViteServer } from "vite";
import { GoogleGenAI } from "@google/genai";
import dotenv from "dotenv";

dotenv.config();

async function startServer() {
  const app = express();
  const PORT = 3000;

  app.use(express.json({ limit: "5mb" }));

  // Initialize Gemini AI lazily/safely
  let aiClient: GoogleGenAI | null = null;
  function getGeminiClient() {
    if (!aiClient) {
      const apiKey = process.env.GEMINI_API_KEY;
      if (!apiKey) {
        throw new Error("GEMINI_API_KEY environment variable is missing.");
      }
      aiClient = new GoogleGenAI({ apiKey });
    }
    return aiClient;
  }

  // API Health Check
  app.get("/api/health", (req, res) => {
    res.json({ status: "ok", time: new Date().toISOString() });
  });

  // API Endpoint for generating/updating Kotlin Jetpack Compose code
  app.post("/api/generate-kotlin", async (req, res) => {
    try {
      const { prompt, currentCode, appCategory } = req.body;
      if (!prompt) {
        return res.status(400).json({ error: "Prompt is required" });
      }

      const client = getGeminiClient();

      const systemInstruction = `You are a Principal Android & Kotlin Jetpack Compose Architect.
Your task is to analyze the user's prompt (which may describe an app, feature, screen, or prompt from a Gemini conversation) and generate production-ready Kotlin Jetpack Compose code along with a JSON descriptor of UI state for live preview rendering.

Always return a JSON object strictly matching this schema:
{
  "appName": "String",
  "appDescription": "String",
  "packageName": "com.example.appname",
  "files": [
    {
      "path": "app/src/main/java/com/example/app/MainActivity.kt",
      "language": "kotlin",
      "content": "Full code..."
    },
    {
      "path": "app/src/main/java/com/example/app/ui/theme/Theme.kt",
      "language": "kotlin",
      "content": "Theme code..."
    },
    {
      "path": "app/build.gradle.kts",
      "language": "kotlin",
      "content": "Gradle code..."
    },
    {
      "path": "app/src/main/AndroidManifest.xml",
      "language": "xml",
      "content": "Manifest XML code..."
    }
  ],
  "previewState": {
    "title": "Screen Title",
    "theme": "light" | "dark",
    "primaryColor": "#6366f1",
    "screenType": "dashboard" | "e-commerce" | "social" | "fitness" | "finance" | "task" | "media" | "custom",
    "navigation": [
      { "id": "home", "label": "Home", "icon": "Home" },
      { "id": "explore", "label": "Explore", "icon": "Compass" },
      { "id": "notifications", "label": "Alerts", "icon": "Bell" },
      { "id": "profile", "label": "Profile", "icon": "User" }
    ],
    "cards": [
      {
        "id": "1",
        "title": "Sample Title",
        "subtitle": "Subtitle or detail",
        "badge": "Active",
        "value": "$1,240",
        "tag": "Featured"
      }
    ],
    "actionButtonText": "Create New Item"
  }
}
Return ONLY valid raw JSON. No markdown codeblock wrapper around the whole JSON if possible, or standard JSON inside triple backticks.`;

      const response = await client.models.generateContent({
        model: "gemini-2.5-flash",
        contents: [
          {
            role: "user",
            parts: [
              { text: systemInstruction },
              { text: `User Prompt / Description: ${prompt}\nApp Category: ${appCategory || "General"}\nCurrent Code Context:\n${currentCode || "None"}` }
            ]
          }
        ],
        config: {
          responseMimeType: "application/json",
          temperature: 0.2
        }
      });

      const text = response.text || "{}";
      const parsed = JSON.parse(text);

      return res.json({ success: true, data: parsed });
    } catch (error: any) {
      console.error("Error generating Kotlin code:", error);
      return res.status(500).json({ error: error?.message || "Failed to generate Kotlin code" });
    }
  });

  // Vite middleware in development
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), "dist");
    app.use(express.static(distPath));
    app.get("*", (req, res) => {
      res.sendFile(path.join(distPath, "index.html"));
    });
  }

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on http://localhost:${PORT}`);
  });
}

startServer();
