const fs = require("fs");
const path = require("path");

const srcPath = path.join(__dirname, "..", "utils", "emojiData.ts");
const src = fs.readFileSync(srcPath, "utf8");

const startMarker = "export const EMOJI_INDEX: EmojiEntry[] = [";
const startIdx = src.indexOf(startMarker);
if (startIdx === -1) throw new Error("start marker not found");
const arrayStart = startIdx + startMarker.length - 1; // position of the '['

// Find the matching closing bracket for the array literal.
let depth = 0;
let endIdx = -1;
for (let i = arrayStart; i < src.length; i++) {
  const c = src[i];
  if (c === "[") depth++;
  else if (c === "]") {
    depth--;
    if (depth === 0) {
      endIdx = i;
      break;
    }
  }
}
if (endIdx === -1) throw new Error("could not find end of array");

const arrayLiteral = src.slice(arrayStart, endIdx + 1);
// eslint-disable-next-line no-eval
const entries = eval(arrayLiteral);

console.error(`Parsed ${entries.length} entries`);

function kotlinString(s) {
  return s
    .replace(/\\/g, "\\\\")
    .replace(/"/g, '\\"')
    .replace(/\$/g, "\\$");
}

const outDir = path.join(
  __dirname,
  "..",
  "tool",
  "src",
  "main",
  "kotlin",
  "com",
  "zacksimpson",
  "emojis"
);

// Kotlin merges every top-level val initializer in a single file into one
// <clinit> static initializer. With ~1800 entries that blows the JVM's 64KB
// per-method bytecode limit, so the data has to be spread across multiple
// files (one initializer each) and concatenated at the end.
const CHUNK_SIZE = 150;
const chunks = [];
for (let i = 0; i < entries.length; i += CHUNK_SIZE) {
  chunks.push(entries.slice(i, i + CHUNK_SIZE));
}

const generatedFiles = [];

// Clean up any part files from a previous run with a different chunk count.
for (const name of fs.readdirSync(outDir)) {
  if (/^EmojiIndexPart\d+\.kt$/.test(name)) {
    fs.unlinkSync(path.join(outDir, name));
  }
}

chunks.forEach((chunk, idx) => {
  const lines = [];
  lines.push("package com.zacksimpson.emojis");
  lines.push("");
  lines.push(
    "// Auto-generated from the RN app's utils/emojiData.ts via scripts/convert-emoji-index.js — do not hand-edit."
  );
  lines.push(
    `// Part ${idx + 1}/${chunks.length} of EMOJI_INDEX, split across files so each file's <clinit> stays under the JVM's 64KB method size limit.`
  );
  lines.push(`internal val EMOJI_INDEX_PART_${idx}: List<EmojiEntry> = listOf(`);
  for (const e of chunk) {
    const emoji = kotlinString(e.emoji);
    const name = kotlinString(e.name);
    const keywords = e.keywords.map((k) => `"${kotlinString(k)}"`).join(", ");
    lines.push(`    EmojiEntry("${emoji}", "${name}", listOf(${keywords})),`);
  }
  lines.push(")");
  lines.push("");

  const fileName = `EmojiIndexPart${idx}.kt`;
  fs.writeFileSync(path.join(outDir, fileName), lines.join("\n"), "utf8");
  generatedFiles.push(fileName);
});

const indexLines = [];
indexLines.push("package com.zacksimpson.emojis");
indexLines.push("");
indexLines.push("data class EmojiEntry(val emoji: String, val name: String, val keywords: List<String>)");
indexLines.push("");
indexLines.push(
  `// Auto-generated from the RN app's utils/emojiData.ts via scripts/convert-emoji-index.js — do not hand-edit.`
);
indexLines.push(`// ${entries.length} emoji with names and keywords, assembled from EmojiIndexPart*.kt`);
indexLines.push("val EMOJI_INDEX: List<EmojiEntry> =");
indexLines.push(
  "    " + chunks.map((_, idx) => `EMOJI_INDEX_PART_${idx}`).join(" +\n    ")
);
indexLines.push("");
indexLines.push("fun searchEmoji(query: String): List<EmojiEntry> {");
indexLines.push("    val q = query.trim().lowercase()");
indexLines.push("    if (q.isEmpty()) return emptyList()");
indexLines.push("    return EMOJI_INDEX.filter { e ->");
indexLines.push("        e.name.lowercase().contains(q) || e.keywords.any { it.lowercase().contains(q) }");
indexLines.push("    }");
indexLines.push("}");
indexLines.push("");

fs.writeFileSync(path.join(outDir, "EmojiIndex.kt"), indexLines.join("\n"), "utf8");

console.error(`Wrote EmojiIndex.kt + ${generatedFiles.length} part files (chunk size ${CHUNK_SIZE})`);
