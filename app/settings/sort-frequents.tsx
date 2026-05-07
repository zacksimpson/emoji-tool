import { router } from "expo-router";
import { StyleSheet, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Header } from "@/components/Header";
import { HapticPressable } from "@/components/HapticPressable";
import { StyledText } from "@/components/StyledText";
import { useInvertColors } from "@/contexts/InvertColorsContext";
import { useTopUsed, type SortMode } from "@/contexts/TopUsedContext";
import { n } from "@/utils/scaling";

const OPTIONS: { label: string; value: SortMode }[] = [
  { label: "Top Used", value: "top-used" },
  { label: "Most Recent", value: "most-recent" },
];

export default function SortFrequentsScreen() {
  const { invertColors } = useInvertColors();
  const { sortMode, setSortMode } = useTopUsed();
  const bg = invertColors ? "white" : "black";

  const handleSelect = async (value: SortMode) => {
    await setSortMode(value);
    router.back();
  };

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: bg }]}>
      <Header headerTitle="Sort Frequents" />
      <View style={styles.content}>
        {OPTIONS.map((option) => (
          <HapticPressable
            key={option.value}
            onPress={() => handleSelect(option.value)}
            style={styles.option}
          >
            <StyledText
              style={[
                styles.optionText,
                sortMode === option.value && styles.selected,
              ]}
            >
              {option.label}
            </StyledText>
          </HapticPressable>
        ))}
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  content: {
    paddingHorizontal: n(22),
    paddingTop: n(16),
    gap: n(8),
  },
  option: {
    paddingVertical: n(10),
  },
  optionText: {
    fontSize: n(30),
  },
  selected: {
    textDecorationLine: "underline",
  },
});
