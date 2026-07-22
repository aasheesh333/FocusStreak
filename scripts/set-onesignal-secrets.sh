#!/usr/bin/env bash
# Helper to set the OneSignal secrets used by FocusStreak's GitHub Actions.
#
# Requirements:
#   - GitHub CLI (gh) installed and authenticated against the repo
#
# Usage:
#   1. Run the script: ./scripts/set-onesignal-secrets.sh
#   2. Paste the OneSignal App ID and REST API Key when prompted.
#
# The secrets are read with hidden input and piped directly to `gh secret set`,
# so they are never written to disk or shown on screen.

set -e

if ! command -v gh &> /dev/null; then
    echo "Error: GitHub CLI (gh) is required. Install it from https://cli.github.com/"
    echo "Alternative: add the secrets manually in GitHub."
    exit 1
fi

if ! gh auth status &> /dev/null; then
    echo "Error: You are not logged in to gh. Run: gh auth login"
    exit 1
fi

echo ""
echo "Setting OneSignal secrets for the current GitHub repository."
echo "Find them in OneSignal Dashboard → Settings → Keys & IDs."
echo ""

read -rsp "OneSignal App ID: " ONESIGNAL_APP_ID
echo ""

read -rsp "OneSignal REST API Key (optional, press Enter to skip): " ONESIGNAL_REST_API_KEY
echo ""

if [ -z "$ONESIGNAL_APP_ID" ]; then
    echo "Error: ONESIGNAL_APP_ID cannot be empty."
    exit 1
fi

echo ""
echo "Sending secrets to GitHub..."

echo -n "$ONESIGNAL_APP_ID" | gh secret set ONESIGNAL_APP_ID

if [ -n "$ONESIGNAL_REST_API_KEY" ]; then
    echo -n "$ONESIGNAL_REST_API_KEY" | gh secret set ONESIGNAL_REST_API_KEY
    echo "Done. Set ONESIGNAL_APP_ID and ONESIGNAL_REST_API_KEY."
else
    echo "Done. Set ONESIGNAL_APP_ID only."
    echo "The test-push workflow needs ONESIGNAL_REST_API_KEY — add it later if needed."
fi

REPO_URL="https://github.com/$(gh repo view --json owner,name -q '.owner.login + "/" + .name')/settings/secrets/actions"
echo "Verify here: $REPO_URL"
