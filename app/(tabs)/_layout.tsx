import type { BottomTabBarProps } from "@react-navigation/bottom-tabs";
import { Tabs } from "expo-router";
import { Navbar, type TabConfigItem } from "@/components/Navbar";

export const TABS_CONFIG: ReadonlyArray<TabConfigItem> = [
  { name: "Home", screenName: "index", iconName: "home" },
  { name: "Top Used", screenName: "topused", iconName: "schedule" },
  { name: "Search", screenName: "search", iconName: "search" },
  { name: "Settings", screenName: "settings", iconName: "settings" },
] as const;

function TabBar({ navigation, state }: BottomTabBarProps) {
  const currentScreenName = state.routes[state.index].name;
  return (
    <Navbar
      currentScreenName={currentScreenName}
      navigation={navigation}
      tabsConfig={TABS_CONFIG}
    />
  );
}

export default function TabsLayout() {
  return (
    <Tabs
      screenOptions={{ headerShown: false }}
      tabBar={(props) => <TabBar {...props} />}
    >
      <Tabs.Screen name="index" />
      <Tabs.Screen name="topused" />
      <Tabs.Screen name="search" />
      <Tabs.Screen name="settings" />
    </Tabs>
  );
}
