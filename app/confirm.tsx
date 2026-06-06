import { router, useLocalSearchParams } from "expo-router";
import { StyleSheet, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { HapticPressable } from "@/components/HapticPressable";
import { StyledText } from "@/components/StyledText";
import { SwipeBackContainer } from "@/components/SwipeBackContainer";
import { useInvertColors } from "@/contexts/InvertColorsContext";
import { MaterialIcons } from "@expo/vector-icons";
import { n } from "@/utils/scaling";

export default function ConfirmScreen() {
  const { invertColors } = useInvertColors();
  const { message, confirmText, action, returnPath } = useLocalSearchParams<{
    message: string;
    confirmText: string;
    action: string;
    returnPath: string;
  }>();

  const bg = invertColors ? "white" : "black";
  const iconColor = invertColors ? "black" : "white";

  const handleBack = () => {
    if (router.canGoBack()) router.back();
  };

  const handleConfirm = () => {
    router.replace({ pathname: returnPath, params: { confirmed: "true", action } } as Parameters<typeof router.replace>[0]);
  };

  return (
    <SwipeBackContainer onSwipeBack={handleBack}>
      <SafeAreaView style={[styles.root, { backgroundColor: bg }]}>
        {/* Back arrow only — no title */}
        <View style={styles.header}>
          <HapticPressable onPress={handleBack}>
            <View style={styles.backButton}>
              <MaterialIcons color={iconColor} name="arrow-back-ios" size={n(28)} />
            </View>
          </HapticPressable>
        </View>

        {/* Centered message */}
        <View style={styles.body}>
          <StyledText style={styles.message}>{message}</StyledText>
        </View>

        {/* CONFIRM/RESET pushed toward bottom */}
        <View style={styles.footer}>
          <HapticPressable onPress={handleConfirm}>
            <StyledText style={styles.confirmBtn}>
              {confirmText ?? "CONFIRM"}
            </StyledText>
          </HapticPressable>
        </View>
      </SafeAreaView>
    </SwipeBackContainer>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1 },
  header: {
    paddingHorizontal: n(22),
    paddingVertical: n(5),
  },
  backButton: {
    width: n(32),
    height: n(32),
    alignItems: "center",
    paddingTop: n(6),
    paddingRight: n(4),
  },
  body: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    paddingHorizontal: n(40),
  },
  message: {
    fontSize: n(28),
    textAlign: "center",
    lineHeight: n(38),
  },
  footer: {
    alignItems: "center",
    paddingBottom: n(80),
  },
  confirmBtn: {
    fontSize: n(26),
    letterSpacing: n(4),
  },
});
