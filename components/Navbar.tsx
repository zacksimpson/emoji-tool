import { MaterialIcons } from "@expo/vector-icons";
import type { BottomTabBarProps } from "@react-navigation/bottom-tabs";
import { router } from "expo-router";
import { StyleSheet, View } from "react-native";
import { useInvertColors } from "@/contexts/InvertColorsContext";
import { n } from "@/utils/scaling";
import { HapticPressable } from "./HapticPressable";

interface NavbarProps {
  currentScreenName: string;
  navigation: BottomTabBarProps["navigation"];
}

export function Navbar({ currentScreenName, navigation }: NavbarProps) {
  const { invertColors } = useInvertColors();
  const bg = invertColors ? "white" : "black";
  const iconColor = invertColors ? "black" : "white";
  const isOnRecents = currentScreenName === "recents";

  return (
    <View style={[styles.navbar, { backgroundColor: bg }]}>
      <HapticPressable
        onPress={() => navigation.navigate(isOnRecents ? "index" : "recents")}
      >
        <MaterialIcons color={iconColor} name="schedule" size={n(48)} />
      </HapticPressable>

      <HapticPressable onPress={() => router.push("/search")}>
        <MaterialIcons color={iconColor} name="search" size={n(48)} />
      </HapticPressable>

      <HapticPressable onPress={() => router.push("/settings")}>
        <MaterialIcons color={iconColor} name="settings" size={n(48)} />
      </HapticPressable>
    </View>
  );
}

const styles = StyleSheet.create({
  navbar: {
    flexDirection: "row",
    alignItems: "center",
    justifyContent: "space-between",
    paddingVertical: n(11),
    paddingHorizontal: n(20),
  },
});
