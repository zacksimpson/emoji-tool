import { useCallback, useState } from "react";
import {
  FlatList,
  StyleSheet,
  View,
  useWindowDimensions,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Header } from "@/components/Header";
import { HapticPressable } from "@/components/HapticPressable";
import { StyledText } from "@/components/StyledText";
import { TextInput } from "@/components/TextInput";
import { useInvertColors } from "@/contexts/InvertColorsContext";
import { useSelected } from "@/contexts/SelectedContext";
import { useTopUsed } from "@/contexts/TopUsedContext";
import { n } from "@/utils/scaling";
import { searchEmoji, type EmojiEntry } from "@/utils/emojiData";

const COLS = 6;

export default function SearchScreen() {
  const { invertColors } = useInvertColors();
  const { width } = useWindowDimensions();
  const { addEmoji } = useSelected();
  const { trackEmoji } = useTopUsed();
  const [query, setQuery] = useState("");

  const bg = invertColors ? "white" : "black";
  const cellSize = width / COLS;

  const results = query.trim().length > 0 ? searchEmoji(query) : [];

  const renderItem = useCallback(
    ({ item }: { item: EmojiEntry }) => (
      <HapticPressable
        onPress={() => {
          addEmoji(item.emoji);
          trackEmoji(item.emoji);
        }}
        style={{
          width: cellSize,
          height: cellSize,
          justifyContent: "center",
          alignItems: "center",
        }}
      >
        <StyledText style={[styles.emojiText, { fontSize: n(22) }]}>
          {item.emoji}
        </StyledText>
      </HapticPressable>
    ),
    [cellSize, addEmoji, trackEmoji]
  );

  return (
    <SafeAreaView edges={["top"]} style={[styles.root, { backgroundColor: bg }]}>
      <Header headerTitle="Search" hideBackButton />
      <View style={styles.inputWrapper}>
        <TextInput
          autoFocus={false}
          onChangeText={setQuery}
          placeholder="Search emoji..."
          value={query}
        />
      </View>
      {query.trim().length > 0 && results.length === 0 ? (
        <View style={styles.empty}>
          <StyledText style={styles.emptyText}>No results</StyledText>
        </View>
      ) : (
        <FlatList
          data={results}
          keyExtractor={(item) => item.emoji}
          renderItem={renderItem}
          numColumns={COLS}
          overScrollMode="never"
          showsVerticalScrollIndicator={false}
          style={{ backgroundColor: bg }}
          contentContainerStyle={styles.grid}
        />
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  inputWrapper: {
    paddingHorizontal: n(28),
    paddingTop: n(48),
  },
  grid: { paddingTop: n(8) },
  emojiText: { textAlign: "center" },
  empty: {
    flex: 1,
    alignItems: "flex-start",
    paddingHorizontal: n(22),
    paddingTop: n(32),
  },
  emptyText: { fontSize: n(20), opacity: 0.4 },
});
