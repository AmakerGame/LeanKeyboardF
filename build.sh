#!/usr/bin/env bash
#
# build.sh <versionName> [flavors]
#
# Example:
#   ./build.sh 6.2.0
#   ./build.sh 6.2.0 origin
#
# What it does:
#   1. On the very first run: generates a real release keystore
#      (keystore/release.keystore.jks) with random passwords,
#      stored in keystore/keystore.properties next to it.
#   2. On every run after that: reuses the same keystore, so all
#      builds are signed with the same key.
#   3. Builds a signed release for every flavor (playstore, origin
#      by default). Each flavor produces a "universal" APK plus one
#      slimmer APK per CPU architecture (armeabi-v7a, arm64-v8a,
#      x86, x86_64) - 5 APKs per flavor, 10 total by default.
#   4. Copies every resulting APK into releases/<version>/.
#
# keystore/ is git-ignored on purpose - BACK IT UP YOURSELF.
# If you lose it you can never publish an update under the same
# signature again (Play Store, sideload upgrades, etc).

set -euo pipefail

VERSION_NAME="${1:-}"
FLAVORS="${2:-playstore,origin}"

if [[ -z "$VERSION_NAME" ]]; then
    echo "Usage: ./build.sh <versionName> [flavors]"
    echo "Example: ./build.sh 6.2.0"
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KEYSTORE_DIR="$SCRIPT_DIR/keystore"
KEYSTORE_FILE="$KEYSTORE_DIR/release.keystore.jks"
KEYSTORE_PROPS="$KEYSTORE_DIR/keystore.properties"
VERSIONCODE_FILE="$KEYSTORE_DIR/versioncode.txt"

random_password() {
    # openssl produces a fixed, finite amount of output, so head never has
    # to cut off a still-writing producer - unlike piping the endless
    # /dev/urandom stream through `tr | head`, which trips SIGPIPE
    # ("broken pipe") and, combined with `set -o pipefail` above, aborts
    # the whole script even though the password itself was generated fine.
    openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | head -c 24
}

# -------------------------------------------------------------
# 1. First run: generate a real signing key
# -------------------------------------------------------------
if [[ ! -f "$KEYSTORE_FILE" ]]; then
    echo "[build] No release keystore found - generating one now (first run)..."
    mkdir -p "$KEYSTORE_DIR"

    # PKCS12 (the default keystore format on modern JDKs, used even for
    # files named *.jks) requires the key password to match the store
    # password - keytool silently ignores a different -keypass, so using
    # two different random passwords here produced a keystore whose real
    # key password didn't match what we wrote to keystore.properties.
    STORE_PASS="$(random_password)"
    KEY_PASS="$STORE_PASS"
    KEY_ALIAS="leankeyboardf"

    keytool -genkeypair -v \
        -keystore "$KEYSTORE_FILE" \
        -alias "$KEY_ALIAS" \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass "$STORE_PASS" -keypass "$KEY_PASS" \
        -dname "CN=LeanKeyboardF, OU=Release, O=LeanKeyboardF, L=Unknown, S=Unknown, C=UA"

    cat > "$KEYSTORE_PROPS" <<EOF
storeFile=release.keystore.jks
storePassword=$STORE_PASS
keyAlias=$KEY_ALIAS
keyPassword=$KEY_PASS
EOF

    echo "1" > "$VERSIONCODE_FILE"

    echo "[build] Keystore created: $KEYSTORE_FILE"
    echo "[build] Passwords saved in: $KEYSTORE_PROPS"
    echo "[build] BACK UP the whole 'keystore' folder somewhere safe now."
    echo "[build] Losing it means you can never sign an update with the same key again."
else
    echo "[build] Reusing existing keystore: $KEYSTORE_FILE"
fi

# -------------------------------------------------------------
# 2. Auto-incrementing versionCode
# -------------------------------------------------------------
VERSION_CODE="$(cat "$VERSIONCODE_FILE" 2>/dev/null || echo 1)"
NEXT_VERSION_CODE=$((VERSION_CODE + 1))
echo "$NEXT_VERSION_CODE" > "$VERSIONCODE_FILE"

echo "[build] versionName=$VERSION_NAME  versionCode=$VERSION_CODE"

# -------------------------------------------------------------
# 3. Build every requested flavor
# -------------------------------------------------------------
TASKS=()
IFS=',' read -ra FLAVOR_LIST <<< "$FLAVORS"
for f in "${FLAVOR_LIST[@]}"; do
    cap="$(tr '[:lower:]' '[:upper:]' <<< "${f:0:1}")${f:1}"
    TASKS+=("assemble${cap}Release")
done

echo "[build] Running: gradlew ${TASKS[*]} -PappVersionName=$VERSION_NAME -PappVersionCode=$VERSION_CODE"
"$SCRIPT_DIR/gradlew" "${TASKS[@]}" -PappVersionName="$VERSION_NAME" -PappVersionCode="$VERSION_CODE"

# -------------------------------------------------------------
# 4. Collect the APKs
# -------------------------------------------------------------
OUT_DIR="$SCRIPT_DIR/releases/$VERSION_NAME"
mkdir -p "$OUT_DIR"

for f in "${FLAVOR_LIST[@]}"; do
    src_dir="$SCRIPT_DIR/leankeykeyboard/build/outputs/apk/$f/release"
    if [[ -d "$src_dir" ]]; then
        cp "$src_dir"/*.apk "$OUT_DIR/" 2>/dev/null || true
    fi
done

echo
echo "[build] Done. APKs copied to: $OUT_DIR"
ls -1 "$OUT_DIR"
