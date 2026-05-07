import { useFonts } from "expo-font";
import { Stack } from "expo-router";
import * as SplashScreen from "expo-splash-screen";
import { useEffect } from "react";
import { InvertColorsProvider } from "@/contexts/InvertColorsContext";
import { SelectedProvider } from "@/contexts/SelectedContext";
import { TopUsedProvider } from "@/contexts/TopUsedContext";

SplashScreen.preventAutoHideAsync();

export default function RootLayout() {
  const [loaded] = useFonts({
    "PublicSans-Regular": require("../assets/fonts/PublicSans-Regular.ttf"),
  });

  useEffect(() => {
    if (loaded) SplashScreen.hideAsync();
  }, [loaded]);

  if (!loaded) return null;

  return (
    <InvertColorsProvider>
      <SelectedProvider>
        <TopUsedProvider>
          <Stack screenOptions={{ headerShown: false, animation: "none" }}>
            <Stack.Screen name="(tabs)" />
            <Stack.Screen name="confirm" />
            <Stack.Screen name="settings/sort-frequents" />
          </Stack>
        </TopUsedProvider>
      </SelectedProvider>
    </InvertColorsProvider>
  );
}
