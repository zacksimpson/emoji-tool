import type { BottomTabBarProps } from "@react-navigation/bottom-tabs";
import { Tabs } from "expo-router";
import { Navbar } from "@/components/Navbar";

function TabBar({ navigation, state }: BottomTabBarProps) {
  const currentScreenName = state.routes[state.index].name;
  return (
    <Navbar
      currentScreenName={currentScreenName}
      navigation={navigation}
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
      <Tabs.Screen name="recents" />
    </Tabs>
  );
}
