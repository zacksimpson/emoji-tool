import { FlatList, StyleSheet, View, useWindowDimensions } from "react-native";
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
  const { addEmoji } = useSelected();
  const { topEmoji } = useTopUsed();

  const bg = invertColors ? "white" : "black";
  const cellSize = width / COLS;

  // Build rows of COLS
  const rows: string[][] = [];
  for (let i = 0; i < topEmoji.length; i += COLS) {
    rows.push(topEmoji.slice(i, i + COLS));
  }

  if (topEmoji.length === 0) {
    return (
      <SafeAreaView style={[styles.root, { backgroundColor: bg }]}>
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
      <FlatList
        data={rows}
        keyExtractor={(_, i) => String(i)}
        renderItem={({ item }) => (
          <View style={styles.row}>
            {item.map((emoji, idx) => (
              <HapticPressable
                key={`${emoji}-${idx}`}
                onPress={() => addEmoji(emoji)}
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
        )}
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
