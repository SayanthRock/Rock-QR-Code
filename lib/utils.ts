// lib/utils.ts

export interface Card {
  id: number;
  title: string;
  description: string;
  color: string;
}

export const cardData: Card[] = [
  {
    id: 1,
    title: "Midnight Obsidian",
    description: "Supercharged violet aura with electric neon aqua gradients that flow organically over deep-space canvases.",
    color: "rgba(157, 78, 221, 0.8)" // Midnight Preset Primary
  },
  {
    id: 2,
    title: "Arctic Glacier",
    description: "Frosted silver-blue accents with high-contrast chiseled borders mimicking geological ice structures.",
    color: "rgba(142, 202, 230, 0.8)" // Arctic Preset Primary
  },
  {
    id: 3,
    title: "Abyssal Ocean",
    description: "Vivid wave-teal tones and translucent background cards reflecting deep sea glaze and wave physics.",
    color: "rgba(0, 180, 216, 0.8)" // Ocean Preset Primary
  },
  {
    id: 4,
    title: "Aurora Glow",
    description: "Luminous solar-wind green and fluorescent magenta contours responding smoothly as you scroll.",
    color: "rgba(0, 255, 204, 0.8)" // Aurora Preset Primary
  }
];

/**
 * Clean Utility helper mimicking shadcn's class merger
 */
export function cn(...inputs: string[]) {
  return inputs.filter(Boolean).join(" ");
}
