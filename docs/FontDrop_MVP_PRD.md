# FontDrop MVP PRD

## 1. Product Overview

**Product Name:** FontDrop  
**Type:** Font-first note application  
**Platform:** Android (MVP)  

### Vision
A note-taking app where users can instantly use custom fonts by simply adding font files — no installation, no setup.

### Core Value
> "Drop fonts. Write instantly."

---

## 2. Problem

Users who want to use custom fonts face:
- Complex installation processes
- System-level restrictions (Android/iOS)
- Poor preview experience
- No easy way to test fonts while writing

---

## 3. Solution

FontDrop allows users to:
- Add font files directly into a folder
- Instantly see and use them in notes
- Switch fonts in real-time while writing

---

## 4. Target Users

### Primary
- Designers
- Writers
- Webtoon creators
- Font enthusiasts

### Secondary
- General users who want aesthetic notes

---

## 5. Core Features (MVP Scope)

## 5.1 Font Folder System
- App provides a "Fonts" folder
- Supports:
  - .ttf
  - .otf
- Auto-scan on app launch
- Manual refresh button

---

## 5.2 Font List & Preview
- List of imported fonts
- Each item shows:
  - Font name
  - Preview text ("Aa", sample sentence)
- Tap to apply font

---

## 5.3 Note Editor
- Create / edit note
- Real-time font switching
- Basic controls:
  - Font size
  - Line spacing

---

## 5.4 Note List
- List of saved notes
- Show:
  - Title
  - Preview snippet
  - Last edited time

---

## 5.5 Font Apply UX
- Change font instantly
- No reload or lag
- Maintain cursor position

---

## 6. User Flow

### First Launch
1. Open app
2. See empty state
3. Tap "Open Fonts Folder"
4. Add font files
5. Return to app
6. Fonts appear

### Writing Flow
1. Create note
2. Start typing
3. Open font panel
4. Select font
5. See live change

---

## 7. Non-Goals (MVP)

- Cloud sync
- Collaboration
- Rich text formatting (bold, underline etc.)
- Font marketplace
- AI features

---

## 8. Technical Considerations

### Platform
- Android (Kotlin + Compose recommended)

### Font Handling
- Typeface.createFromFile
- Runtime font loading

### Storage
- Scoped Storage / SAF
- Internal + user-selected folder

### Performance
- Cache loaded fonts
- Avoid reloading on every render

---

## 9. Risks

- Android storage permissions complexity
- Font compatibility issues
- Performance with many fonts
- Rendering differences across devices

---

## 10. Success Metrics

### Activation
- % users who add at least 1 font

### Engagement
- Avg session time
- Notes created per user

### Retention
- D1 / D7 retention

---

## 11. MVP Timeline (Suggested)

### Week 1
- Project setup
- Font loading prototype

### Week 2
- Editor + font apply

### Week 3
- Font list UI
- Note list

### Week 4
- Polish + bug fixes

---

## 12. Future Roadmap

- Font tagging / favorites
- PDF / image export
- iOS version
- Cloud sync
- AI font recommendation
- Handwriting → font conversion

---

## 13. Final Statement

FontDrop is not just a note app.

It is:
> A writing experience where typography becomes part of expression.
