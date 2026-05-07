import AsyncStorage from "@react-native-async-storage/async-storage";
import { createContext, type ReactNode, useContext, useEffect, useState } from "react";

export type SortMode = "top-used" | "most-recent";

interface TopUsedContextType {
  topEmoji: string[];
  trackEmoji: (emoji: string) => void;
  resetTopUsed: () => void;
  sortMode: SortMode;
  setSortMode: (mode: SortMode) => Promise<void>;
}

const TopUsedContext = createContext<TopUsedContextType>({
  topEmoji: [],
  trackEmoji: () => {},
  resetTopUsed: () => {},
  sortMode: "top-used",
  setSortMode: async () => {},
});

export const useTopUsed = () => useContext(TopUsedContext);

const STORAGE_KEY = "topUsedCounts";
const SORT_KEY = "topUsedSortMode";
const MAX_DISPLAY = 24;

export const TopUsedProvider = ({ children }: { children: ReactNode }) => {
  // counts: { [emoji]: number }
  const [counts, setCounts] = useState<Record<string, number>>({});
  // recency: ordered list of emoji, most recent first
  const [recency, setRecency] = useState<string[]>([]);
  const [sortMode, setSortModeState] = useState<SortMode>("top-used");

  useEffect(() => {
    AsyncStorage.getItem(STORAGE_KEY).then((raw) => {
      if (raw) {
        const parsed = JSON.parse(raw);
        setCounts(parsed.counts ?? {});
        setRecency(parsed.recency ?? []);
      }
    });
    AsyncStorage.getItem(SORT_KEY).then((raw) => {
      if (raw === "most-recent" || raw === "top-used") {
        setSortModeState(raw);
      }
    });
  }, []);

  const persist = async (
    newCounts: Record<string, number>,
    newRecency: string[]
  ) => {
    await AsyncStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ counts: newCounts, recency: newRecency })
    );
  };

  const trackEmoji = (emoji: string) => {
    setCounts((prev) => {
      const newCounts = { ...prev, [emoji]: (prev[emoji] ?? 0) + 1 };
      setRecency((prevRecency) => {
        const newRecency = [
          emoji,
          ...prevRecency.filter((e) => e !== emoji),
        ];
        persist(newCounts, newRecency);
        return newRecency;
      });
      return newCounts;
    });
  };

  const resetTopUsed = () => {
    setCounts({});
    setRecency([]);
    AsyncStorage.setItem(STORAGE_KEY, JSON.stringify({ counts: {}, recency: [] }));
  };

  const setSortMode = async (mode: SortMode) => {
    setSortModeState(mode);
    await AsyncStorage.setItem(SORT_KEY, mode);
  };

  // Derive top emoji based on sort mode
  const topEmoji =
    sortMode === "top-used"
      ? Object.entries(counts)
          .sort((a, b) => b[1] - a[1])
          .slice(0, MAX_DISPLAY)
          .map(([emoji]) => emoji)
      : recency.slice(0, MAX_DISPLAY);

  return (
    <TopUsedContext.Provider
      value={{ topEmoji, trackEmoji, resetTopUsed, sortMode, setSortMode }}
    >
      {children}
    </TopUsedContext.Provider>
  );
};
