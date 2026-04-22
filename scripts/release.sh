#!/usr/bin/env bash
# Release helper for FontDrop.
#
# Bumps versionCode + versionName in app/build.gradle.kts, creates a signed
# commit + annotated tag, and (after confirmation) pushes both. The push of the
# tag triggers .github/workflows/release.yml, which builds and publishes the
# debug APK as a GitHub Release.
#
# Usage:
#   scripts/release.sh patch             # 0.1.0 -> 0.1.1
#   scripts/release.sh minor             # 0.1.3 -> 0.2.0
#   scripts/release.sh major             # 0.4.2 -> 1.0.0
#   scripts/release.sh 0.2.0-beta.1      # explicit semver (incl. prereleases)
#   scripts/release.sh patch --no-push   # stage locally, push manually
#
# `patch` / `minor` / `major` read the current `versionName` from
# app/build.gradle.kts, increment the requested segment, and always produce a
# stable (non-prerelease) version. Prerelease tags are only available through
# an explicit semver argument (e.g. 0.2.0-rc.1).
#
# Requirements: bash, git, macOS or GNU sed.
set -euo pipefail

BUILD_FILE="app/build.gradle.kts"
REMOTE="origin"
DEFAULT_BRANCH="main"

err() { printf 'error: %s\n' "$*" >&2; exit 1; }
info() { printf '  %s\n' "$*"; }

# ---- args ----
[ $# -ge 1 ] || err "Usage: $0 <patch|minor|major|X.Y.Z[-suffix]> [--no-push]"
INPUT="$1"
PUSH=1
if [ "${2-}" = "--no-push" ]; then PUSH=0; fi

# ---- repo guards (run before any mutation) ----
cd "$(git rev-parse --show-toplevel)"

BRANCH=$(git symbolic-ref --short HEAD)
[ "$BRANCH" = "$DEFAULT_BRANCH" ] || err "on branch '$BRANCH', releases must start from '$DEFAULT_BRANCH'"

git diff --quiet && git diff --cached --quiet \
    || err "working tree not clean. commit or stash first."

if git remote get-url "$REMOTE" >/dev/null 2>&1; then
    git fetch --quiet "$REMOTE" "$DEFAULT_BRANCH"
    local_sha=$(git rev-parse "$DEFAULT_BRANCH")
    remote_sha=$(git rev-parse "$REMOTE/$DEFAULT_BRANCH")
    [ "$local_sha" = "$remote_sha" ] \
        || err "local $DEFAULT_BRANCH is out of sync with $REMOTE/$DEFAULT_BRANCH"
fi

# ---- resolve version ----
# Read current versionName from build.gradle.kts as source of truth.
CURRENT=$(sed -nE 's/^[[:space:]]*versionName = "([^"]+)".*/\1/p' "$BUILD_FILE" | head -n 1)
[ -n "$CURRENT" ] || err "could not read versionName from $BUILD_FILE"

# Strip any prerelease suffix (after -) then split into major.minor.patch,
# defaulting missing segments to 0 so early projects with versionName = "1.0"
# still bump cleanly.
CURRENT_CORE="${CURRENT%%-*}"
IFS='.' read -r cur_major cur_minor cur_patch _rest <<< "$CURRENT_CORE"
cur_major=${cur_major:-0}
cur_minor=${cur_minor:-0}
cur_patch=${cur_patch:-0}

case "$INPUT" in
    patch)
        VERSION="$cur_major.$cur_minor.$((cur_patch + 1))"
        BUMP_MODE="patch"
        ;;
    minor)
        VERSION="$cur_major.$((cur_minor + 1)).0"
        BUMP_MODE="minor"
        ;;
    major)
        VERSION="$((cur_major + 1)).0.0"
        BUMP_MODE="major"
        ;;
    *)
        VERSION="$INPUT"
        BUMP_MODE="explicit"
        ;;
esac

if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z.-]+)?$ ]]; then
    err "'$VERSION' is not valid semver (expected 0.1.0 or 0.2.0-beta.1)"
fi
TAG="v$VERSION"

# ---- tag uniqueness ----
if git rev-parse --verify "refs/tags/$TAG" >/dev/null 2>&1; then
    err "tag $TAG already exists locally"
fi
if git ls-remote --exit-code --tags "$REMOTE" "refs/tags/$TAG" >/dev/null 2>&1; then
    err "tag $TAG already exists on $REMOTE"
fi

# ---- compute versionCode as (tag count + 1) ----
tag_count=$(git tag --list 'v*' | wc -l | tr -d '[:space:]')
VERSION_CODE=$((tag_count + 1))

# ---- show plan ----
printf '\nFontDrop release plan\n'
info "bump:         $BUMP_MODE"
info "current:      $CURRENT"
info "next:         $VERSION  (tag $TAG)"
info "versionCode:  $VERSION_CODE"
info "branch:       $BRANCH"

printf '\nCommits since previous tag:\n'
prev_tag=$(git describe --tags --abbrev=0 --match 'v*' 2>/dev/null || true)
if [ -n "$prev_tag" ]; then
    git --no-pager log --oneline "$prev_tag..HEAD" | sed 's/^/  /'
else
    git --no-pager log --oneline -n 10 HEAD | sed 's/^/  /'
    info "(no prior tag found; showing last 10 commits)"
fi

printf '\n'
read -r -p "Proceed? [y/N] " answer
case "$answer" in
    y|Y|yes|YES) ;;
    *) echo "aborted."; exit 0 ;;
esac

# ---- bump version in build.gradle.kts ----
# Portable sed: `-i.bak` + rm works on both BSD (macOS) and GNU.
sed -i.bak -E "s/versionCode = [0-9]+/versionCode = $VERSION_CODE/" "$BUILD_FILE"
sed -i.bak -E "s/versionName = \"[^\"]+\"/versionName = \"$VERSION\"/" "$BUILD_FILE"
rm "$BUILD_FILE.bak"

if git diff --quiet "$BUILD_FILE"; then
    err "no version change applied to $BUILD_FILE (pattern not matched?)"
fi

printf '\nVersion diff:\n'
git --no-pager diff "$BUILD_FILE" | sed 's/^/  /'

# ---- commit + tag ----
git add "$BUILD_FILE"
git commit --quiet -m "Release $TAG"
git tag -a "$TAG" -m "Release $TAG"

printf '\n✓ committed + tagged locally: %s\n' "$TAG"

if [ "$PUSH" -eq 1 ]; then
    git push --quiet "$REMOTE" "$DEFAULT_BRANCH"
    git push --quiet "$REMOTE" "$TAG"
    printf '✓ pushed %s and tag to %s\n' "$DEFAULT_BRANCH" "$REMOTE"
    printf '  GitHub Actions will build + publish the release APK.\n'
else
    printf '⚠ skipped push (--no-push). To finish:\n'
    printf '    git push %s %s\n' "$REMOTE" "$DEFAULT_BRANCH"
    printf '    git push %s %s\n' "$REMOTE" "$TAG"
fi
