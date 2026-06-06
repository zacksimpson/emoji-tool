import { router, useLocalSearchParams } from "expo-router";
import { useEffect } from "react";
import { StyleSheet, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Header } from "@/components/Header";
import { HapticPressable } from "@/components/HapticPressable";
import { StyledText } from "@/components/StyledText";
import { SwipeBackContainer } from "@/components/SwipeBackContainer";
import { ToggleSwitch } from "@/components/ToggleSwitch";
import { useInvertColors } from "@/contexts/InvertColorsContext";
import { useTopUsed } from "@/contexts/TopUsedContext";
import { n } from "@/utils/scaling";

export default function SettingsScreen() {
  const { invertColors, setInvertColors } = useInvertColors();
  const { sortMode, resetTopUsed } = useTopUsed();
  const params = useLocalSearchParams<{ confirmed?: string; action?: string }>();

  const bg = invertColors ? "white" : "black";

  useEffect(() => {
    if (params.confirmed === "true" && params.action === "resetTopUsed") {
      resetTopUsed();
      router.setParams({ confirmed: undefined, action: undefined });
    }
  }, [params.confirmed, params.action]);

  const sortLabel = sortMode === "top-used" ? "Top Used" : "Most Recent";

  const handleBack = () => {
    if (router.canGoBack()) router.back();
  };

  const handleResetTopUsed = () => {
    router.push({
      pathname: "/confirm",
      params: {
        message: "All Top Used emoji data will be cleared.",
        confirmText: "RESET",
        action: "resetTopUsed",
        returnPath: "/settings",
      },
    });
  };

  return (
    <SwipeBackContainer onSwipeBack={handleBack}>
      <SafeAreaView edges={["top"]} style={[styles.root, { backgroundColor: bg }]}>
        <Header headerTitle="Settings" />

        <View style={styles.content}>
          <ToggleSwitch
            label="Invert colors"
            value={invertColors}
            onValueChange={setInvertColors}
          />

          <HapticPressable
            onPress={() => router.push("/settings/sort-frequents")}
            style={styles.selectorRow}
          >
            <StyledText style={styles.selectorLabel}>Sort Frequents</StyledText>
            <StyledText style={styles.selectorValue}>{sortLabel}</StyledText>
          </HapticPressable>

          <HapticPressable onPress={handleResetTopUsed} style={styles.resetRow}>
            <StyledText style={styles.resetText}>Reset Top Used</StyledText>
          </HapticPressable>
        </View>
      </SafeAreaView>
    </SwipeBackContainer>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  content: {
    paddingHorizontal: n(22),
    paddingTop: n(16),
    gap: n(4),
  },
  selectorRow: {
    paddingTop: n(20),
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
    paddingTop: n(20),
  },
  resetText: {
    fontSize: n(30),
  },
});
