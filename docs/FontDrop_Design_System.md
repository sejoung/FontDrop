# FontDrop Design System

## 1. Overview

FontDrop is a **font-first note app**.  
The product should feel like a writing tool, not a file manager and not a generic utility app.

The visual direction should balance these three qualities:

- **Editorial** — text, writing, note-taking
- **Crafted** — typography, pen, selection, refinement
- **Warm** — approachable, calm, creative

This means the design system should avoid:

- overly technical developer-tool aesthetics
- cold productivity-app styling
- excessive skeuomorphism
- cluttered “design tool” interfaces

---

## 2. Brand Keywords

### Primary keywords
- Warm
- Editorial
- Crafted
- Focused
- Calm

### Secondary keywords
- Premium
- Minimal
- Expressive
- Human
- Typographic

### Product personality
FontDrop should feel like:

> “A calm writing space where typography becomes part of expression.”

---

## 3. Brand Principles

### 3.1 Typography comes first
Typography is not decoration. It is core product behavior.

### 3.2 Writing context must stay visible
Even when emphasizing fonts, the product should still feel like a note app.

### 3.3 Warm over sterile
Use warm surfaces and grounded tones instead of sharp, cold UI.

### 3.4 Clear over decorative
Typography and layout should remain legible and calm, especially on mobile.

### 3.5 One expressive moment per screen
Every screen should have one focal point, not many competing accents.

---

## 4. Color System

### 4.1 Core palette

#### Ink
- `Ink / 900` — `#173C34`
- `Ink / 700` — `#2D5A4E`
- `Ink / 500` — `#4E786C`

Use for:
- primary text
- icons
- key emphasis
- selected states

#### Paper
- `Paper / 50` — `#FFF9F0`
- `Paper / 100` — `#F8EEDF`
- `Paper / 200` — `#EEDBC0`

Use for:
- app backgrounds
- cards
- soft surfaces

#### Gold
- `Gold / 400` — `#E4AE59`
- `Gold / 500` — `#D89C3F`
- `Gold / 600` — `#BB7E2E`

Use for:
- brand accent
- highlights
- active controls
- key badges

#### Clay
- `Clay / 300` — `#D8B48C`
- `Clay / 500` — `#B3825F`
- `Clay / 700` — `#875D40`

Use for:
- secondary accents
- illustration tones
- decorative but restrained use

### 4.2 Semantic colors

#### Text
- `Text / Primary` — `#173C34`
- `Text / Secondary` — `#4E786C`
- `Text / Tertiary` — `#7A8F89`
- `Text / Inverse` — `#FFF9F0`

#### Background
- `Background / Base` — `#FFF9F0`
- `Background / Elevated` — `#FFF4E3`
- `Background / Strong` — `#F3E5CF`

#### Border
- `Border / Soft` — `#E8D9C3`
- `Border / Default` — `#DCC7AB`
- `Border / Strong` — `#BFA37E`

#### Status
- `Success` — `#4E786C`
- `Warning` — `#D89C3F`
- `Error` — `#B85C4D`
- `Info` — `#7A8F89`

### 4.3 Color usage rules

#### Do
- Use warm paper backgrounds as the default surface
- Use deep ink for text and primary controls
- Use gold as a focused accent, not a constant fill
- Keep high contrast on editable text surfaces

#### Don’t
- Put gold on large surfaces too often
- Use too many saturated colors together
- Make every element “brand-colored”
- Use pure black unless necessary for accessibility testing

---

## 5. Typography System

FontDrop is typography-focused, so the UI typography must be disciplined.

### 5.1 UI font recommendation

For product UI, use a clean and neutral sans-serif:
- Inter
- SF Pro
- Pretendard

Recommended default:
- **Inter** for cross-platform consistency

### 5.2 Editorial display support
For brand moments, previews, or note emphasis, you may introduce:
- a serif face
- a handwriting-like accent face
- a selected user font preview

These should be used sparingly in UI chrome.

### 5.3 Type scale

#### Display
- `Display / L` — `40 / 48 / 700`
- `Display / M` — `32 / 40 / 700`
- `Display / S` — `28 / 36 / 600`

#### Heading
- `Heading / L` — `24 / 32 / 700`
- `Heading / M` — `20 / 28 / 700`
- `Heading / S` — `18 / 24 / 600`

#### Body
- `Body / L` — `16 / 24 / 400`
- `Body / M` — `14 / 20 / 400`
- `Body / S` — `12 / 18 / 400`

#### Label
- `Label / L` — `14 / 18 / 600`
- `Label / M` — `12 / 16 / 600`
- `Label / S` — `11 / 14 / 600`

### 5.4 Typographic rules
- Use tighter hierarchy, not too many font sizes per screen
- Keep note content readable first
- Use display sizes for onboarding, empty states, brand screens only
- Avoid decorative font use in controls and settings

---

## 6. Spacing System

Use an 8pt grid.

### Base spacing scale
- `2`
- `4`
- `8`
- `12`
- `16`
- `20`
- `24`
- `32`
- `40`
- `48`
- `64`

### Recommended usage
- inner compact padding: `8`
- default card padding: `16`
- screen section gap: `24`
- large layout gap: `32`

---

## 7. Radius System

Rounded corners should feel soft and premium.

- `Radius / XS` — `6`
- `Radius / S` — `10`
- `Radius / M` — `14`
- `Radius / L` — `20`
- `Radius / XL` — `28`

### Usage
- chips / pills: `9999`
- buttons: `14`
- cards: `20`
- sheets / modals: `28`
- app icon style blocks: `28+`

---

## 8. Shadow System

Shadows should be soft and low contrast.

### Shadow / 1
- y: `2`
- blur: `8`
- color: `rgba(23, 60, 52, 0.08)`

### Shadow / 2
- y: `6`
- blur: `18`
- color: `rgba(23, 60, 52, 0.10)`

### Shadow / 3
- y: `10`
- blur: `28`
- color: `rgba(23, 60, 52, 0.14)`

### Rule
Prefer elevation through:
- paper layering
- borders
- spacing
instead of strong shadows.

---

## 9. Iconography

Icons should feel:
- slightly rounded
- clean
- not overly geometric
- not too playful

### Icon style
- 2px stroke equivalent
- rounded caps/corners
- simple silhouettes
- avoid sharp, futuristic icon sets

### Product motif options
- note card
- pen nib
- typographic layer
- cursor
- page stack

Avoid:
- folder-heavy metaphors
- generic file-manager shapes
- overly literal font file icons

---

## 10. Illustration Direction

Illustrations should support:
- writing
- typography
- craft
- calm workflows

### Allowed illustration language
- layered letters
- note cards
- pen nib / ink motifs
- soft abstract shapes

### Avoid
- cartoon mascots
- heavy 3D realism
- busy productivity dashboard scenes

---

## 11. Layout Principles

### 11.1 One primary action per screen
The user should always know the next action.

### 11.2 Content first
Notes and type previews should remain central.

### 11.3 Clear grouping
Use cards and spacing, not many divider lines.

### 11.4 Calm rhythm
Avoid dense controls stacked without breathing room.

---

## 12. Core Components

### 12.1 App Bar
Use a soft elevated top bar.

**Specs**
- height: `56`
- background: `Background / Elevated`
- title: `Heading / S`
- icon color: `Ink / 900`

### 12.2 Primary Button
Use for key actions like:
- Add Font
- Create Note
- Apply Font

**Style**
- fill: `Ink / 900`
- text: `Text / Inverse`
- radius: `14`
- padding: `12 x 16`

### 12.3 Secondary Button
Use for lower-priority actions.

**Style**
- fill: `Paper / 100`
- border: `Border / Default`
- text: `Ink / 900`

### 12.4 Accent Button
Reserved for branded actions.

**Style**
- fill: `Gold / 400`
- text: `Ink / 900`

Use sparingly.

### 12.5 Input Field
**Style**
- surface: `#FFFFFF` or `Background / Elevated`
- border: `Border / Default`
- focus border: `Ink / 500`
- radius: `14`
- padding: `12`

### 12.6 Note Card
This is one of the core product surfaces.

**Style**
- background: `#FFFDF8`
- border: `Border / Soft`
- radius: `20`
- shadow: `Shadow / 1`
- padding: `16`

### 12.7 Font Preview Card
Should feel slightly more expressive than a note card.

Contains:
- font name
- style label
- preview text
- quick apply action

Recommended:
- slightly larger top/bottom padding
- stronger typographic contrast

### 12.8 Chips
Use for:
- style categories
- recent fonts
- filter states

**Selected**
- background: `Ink / 900`
- text: `Text / Inverse`

**Unselected**
- background: `Paper / 100`
- text: `Text / Secondary`

### 12.9 Bottom Sheet
Use for:
- font selection
- note actions
- export choices

**Style**
- radius top: `28`
- background: `#FFF9F0`
- handle: `Border / Strong`

---

## 13. Interaction States

### Buttons
#### Default
Clear shape and high contrast

#### Hover / Pressed
Slight darkening or reduced opacity

#### Disabled
- lower opacity
- no saturated accent
- maintain readable text contrast

### Inputs
#### Focused
Use ink-toned border, not bright blue

#### Error
Use warm error tone, avoid harsh red unless necessary

### Cards
#### Selected
- stronger border
- subtle inner tint
- possible accent marker

---

## 14. Motion Guidelines

Motion should feel:
- calm
- short
- soft
- useful

### Recommended durations
- tap response: `120ms`
- state change: `180ms`
- sheet / modal: `240ms`
- card reordering: `220ms`

### Motion behavior
- ease-out for entrances
- avoid bouncy motion
- use small opacity + translate changes

---

## 15. Writing Surface Rules

The note editor is the most important part of the product.

### Editor rules
- maximize readable width
- keep background quiet
- avoid visual noise near editable text
- typography controls should not overwhelm content

### Font preview inside editor
- changes should feel immediate
- transitions should be subtle
- preserve cursor stability and readability

---

## 16. Theme Modes

### Light mode
Light mode should be the primary designed experience.

### Dark mode
Dark mode should preserve warmth.

#### Dark palette suggestion
- `Dark / Base` — `#14211E`
- `Dark / Elevated` — `#1C2E2A`
- `Dark / Card` — `#243934`
- `Dark / Text Primary` — `#F8EEDF`
- `Dark / Accent` — `#E4AE59`

Avoid:
- cold gray-only dark mode
- neon accents

---

## 17. Accessibility

### Contrast
- UI text should meet accessible contrast
- editable note text must prioritize readability over style

### Touch targets
- minimum `44 x 44`

### Font scaling
- support dynamic text scaling where possible
- do not break editor layout at larger sizes

### States
- selection should never rely on color alone
- active controls should use shape, fill, and label changes

---

## 18. Recommended Screen Tone

### Home
- calm
- organized
- welcoming
- recent notes + font access

### Editor
- quiet
- focused
- minimal chrome

### Font Library
- expressive
- typographic
- still structured

### Settings
- neutral
- utility-oriented
- lower visual emphasis

---

## 19. Example Token Structure

```json
{
  "color": {
    "ink": {
      "900": "#173C34",
      "700": "#2D5A4E",
      "500": "#4E786C"
    },
    "paper": {
      "50": "#FFF9F0",
      "100": "#F8EEDF",
      "200": "#EEDBC0"
    },
    "gold": {
      "400": "#E4AE59",
      "500": "#D89C3F",
      "600": "#BB7E2E"
    }
  },
  "radius": {
    "s": 10,
    "m": 14,
    "l": 20,
    "xl": 28
  },
  "spacing": {
    "1": 4,
    "2": 8,
    "3": 12,
    "4": 16,
    "5": 20,
    "6": 24,
    "8": 32
  }
}
```

---

## 20. Tailwind-like Token Draft

```js
export const fontDropTheme = {
  colors: {
    ink: {
      900: "#173C34",
      700: "#2D5A4E",
      500: "#4E786C",
    },
    paper: {
      50: "#FFF9F0",
      100: "#F8EEDF",
      200: "#EEDBC0",
    },
    gold: {
      400: "#E4AE59",
      500: "#D89C3F",
      600: "#BB7E2E",
    },
    clay: {
      300: "#D8B48C",
      500: "#B3825F",
      700: "#875D40",
    },
  },
  radius: {
    xs: 6,
    sm: 10,
    md: 14,
    lg: 20,
    xl: 28,
  },
  spacing: {
    0: 0,
    1: 4,
    2: 8,
    3: 12,
    4: 16,
    5: 20,
    6: 24,
    8: 32,
    10: 40,
    12: 48,
    16: 64,
  },
};
```

---

## 21. Component Priority for MVP

Build these first:

1. App Bar
2. Primary / Secondary Button
3. Input Field
4. Note Card
5. Font Preview Card
6. Chip
7. Bottom Sheet
8. Editor Toolbar
9. Empty State
10. Toast / Snackbar

---

## 22. Final Direction

FontDrop should not feel like:
- a file app
- a developer utility
- a noisy design tool

It should feel like:

> “A warm, thoughtful note app where typography becomes a natural part of writing.”
