import { useCallback, useState } from "react";
import {
  Clipboard,
  FlatList,
  StyleSheet,
  ToastAndroid,
  View,
  useWindowDimensions,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { HapticPressable } from "@/components/HapticPressable";
import { StyledText } from "@/components/StyledText";
import { useInvertColors } from "@/contexts/InvertColorsContext";
import { useSelected } from "@/contexts/SelectedContext";
import { useTopUsed } from "@/contexts/TopUsedContext";
import { n } from "@/utils/scaling";

const COLS = 6;

export default function TopUsedScreen() {
  const { invertColors } = useInvertColors();
  const { width } = useWindowDimensions();
  const { selected, addEmoji, clearEmoji } = useSelected();
  const { topEmoji, trackEmoji } = useTopUsed();
  const [copied, setCopied] = useState(false);

  const bg = invertColors ? "white" : "black";
  const cellSize = width / COLS;

  const handleCopy = useCallback(() => {
    if (selected.length === 0) return;
    Clipboard.setString(selected.join(""));
    ToastAndroid.show("Copied", ToastAndroid.SHORT);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  }, [selected]);

  // Build rows of COLS
  const rows: string[][] = [];
  for (let i = 0; i < topEmoji.length; i += COLS) {
    rows.push(topEmoji.slice(i, i + COLS));
  }

  const renderRow = useCallback(
    ({ item }: { item: string[] }) => (
      <View style={styles.row}>
        {item.map((emoji, idx) => (
          <HapticPressable
            key={`${emoji}-${idx}`}
            onPress={() => {
              addEmoji(emoji);
              trackEmoji(emoji);
            }}
            style={{
              width: cellSize,
              height: cellSize,
              justifyContent: "center",
              alignItems: "center",
            }}
          >
            <StyledText style={[styles.emojiText, { fontSize: n(22) }]}>
              {emoji}
            </StyledText>
          </HapticPressable>
        ))}
      </View>
    ),
    [cellSize, addEmoji, trackEmoji]
  );

  if (topEmoji.length === 0) {
    return (
      <SafeAreaView style={[styles.root, { backgroundColor: bg }]}>
        {/* Header even when empty */}
        <View style={styles.header}>
          <HapticPressable onPress={handleCopy}>
            <StyledText style={[styles.headerBtn, copied && styles.headerBtnDone]}>
              {copied ? "COPIED" : "COPY"}
            </StyledText>
          </HapticPressable>
          <HapticPressable onPress={clearEmoji}>
            <StyledText style={styles.headerBtn}>CLEAR</StyledText>
          </HapticPressable>
        </View>
        <View style={styles.empty}>
          <StyledText style={styles.emptyText}>
            Tap emoji on Home or Search to build your Top Used list.
          </StyledText>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: bg }]}>
      {/* Header — COPY left, CLEAR right */}
      <View style={styles.header}>
        <HapticPressable onPress={handleCopy}>
          <StyledText style={[styles.headerBtn, copied && styles.headerBtnDone]}>
            {copied ? "COPIED" : "COPY"}
          </StyledText>
        </HapticPressable>
        <HapticPressable onPress={clearEmoji}>
          <StyledText style={styles.headerBtn}>CLEAR</StyledText>
        </HapticPressable>
      </View>

      {/* Shared selection tray */}
      {selected.length > 0 && (
        <View style={styles.tray}>
          <StyledText style={styles.trayText} numberOfLines={1} ellipsizeMode="tail">
            {selected.join("")}
          </StyledText>
        </View>
      )}

      {/* Grid */}
      <FlatList
        data={rows}
        keyExtractor={(_, i) => String(i)}
        renderItem={renderRow}
        overScrollMode="never"
        showsVerticalScrollIndicator={false}
        style={{ backgroundColor: bg }}
        contentContainerStyle={styles.grid}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  header: {
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    paddingHorizontal: n(18),
    paddingTop: n(8),
    paddingBottom: n(14),
  },
  headerBtn: { fontSize: n(23), letterSpacing: n(4) },
  headerBtnDone: { opacity: 0.35 },
  tray: {
    paddingHorizontal: n(22),
    paddingVertical: n(14),
  },
  trayText: { fontSize: n(28) },
  grid: { paddingTop: n(8) },
  row: { flexDirection: "row" },
  emojiText: { textAlign: "center" },
  empty: {
    flex: 1,
    paddingHorizontal: n(22),
    paddingTop: n(32),
  },
  emptyText: {
    fontSize: n(20),
    opacity: 0.4,
  },
});
