import { router, useLocalSearchParams } from "expo-router";
import { useEffect } from "react";
import { StyleSheet, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Header } from "@/components/Header";
import { HapticPressable } from "@/components/HapticPressable";
import { StyledText } from "@/components/StyledText";
import { ToggleSwitch } from "@/components/ToggleSwitch";
import { useInvertColors } from "@/contexts/InvertColorsContext";
import { useTopUsed } from "@/contexts/TopUsedContext";
import { n } from "@/utils/scaling";

export default function SettingsScreen() {
  const { invertColors, setInvertColors } = useInvertColors();
  const { sortMode, resetTopUsed } = useTopUsed();
  const params = useLocalSearchParams<{ confirmed?: string; action?: string }>();

  const bg = invertColors ? "white" : "black";
  const dividerColor = invertColors ? "#DDDDDD" : "#1A1A1A";

  // Handle confirmation return
  useEffect(() => {
    if (params.confirmed === "true" && params.action === "resetTopUsed") {
      resetTopUsed();
      router.setParams({ confirmed: undefined, action: undefined });
    }
  }, [params.confirmed, params.action]);

  const sortLabel = sortMode === "top-used" ? "Top Used" : "Most Recent";

  const handleResetTopUsed = () => {
    router.push({
      pathname: "/confirm",
      params: {
        message: "All Top Used emoji data will be cleared.",
        confirmText: "RESET",
        action: "resetTopUsed",
        returnPath: "/(tabs)/settings",
      },
    });
  };

  return (
    <SafeAreaView style={[styles.root, { backgroundColor: bg }]}>
      <Header headerTitle="Settings" hideBackButton />
      <View style={styles.content}>

        {/* Invert Colors toggle */}
        <ToggleSwitch
          label="Invert colors"
          value={invertColors}
          onValueChange={setInvertColors}
        />

        {/* Sort Frequents selector */}
        <View style={[styles.divider, { backgroundColor: dividerColor }]} />
        <HapticPressable
          onPress={() => router.push("/settings/sort-frequents")}
          style={styles.selectorRow}
        >
          <StyledText style={styles.selectorLabel}>Sort Frequents</StyledText>
          <StyledText style={styles.selectorValue}>{sortLabel}</StyledText>
        </HapticPressable>

        {/* Reset Top Used */}
        <View style={[styles.divider, { backgroundColor: dividerColor }]} />
        <HapticPressable onPress={handleResetTopUsed} style={styles.resetRow}>
          <StyledText style={styles.resetText}>Reset Top Used</StyledText>
        </HapticPressable>

      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  content: {
    paddingHorizontal: n(22),
    paddingTop: n(16),
  },
  divider: {
    height: 1,
    marginVertical: n(16),
  },
  selectorRow: {
    flexDirection: "column",
  },
  selectorLabel: {
    fontSize: n(20),
    lineHeight: n(20),
    paddingTop: n(7.5),
  },
  selectorValue: {
    fontSize: n(30),
    paddingBottom: n(10),
  },
  resetRow: {
    paddingVertical: n(10),
  },
  resetText: {
    fontSize: n(30),
  },
});
