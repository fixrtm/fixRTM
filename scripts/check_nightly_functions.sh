function need_nightly_build() {
  last_run_date="$(($(date -d "$(date "+%Y-%m-%dT15:00:00Z")" "+%s") - (60*60*24)))"
  last_commit_date="$(git log -1 --format=%ct)"

  if [ "$last_commit_date" -gt $last_run_date ]; then
    return 0
  else
    return 1
  fi
}

function cancel() {
    curl \
      -X POST \
      -H "Content-Type: application/vnd.github.v3+json" \
      -H "Authorization: token ${GITHUB_TOKEN}" \
      -H "User-Agent: check_nightly_functions_sh/cancel_and_exit" \
      "https://api.github.com/repos/$GITHUB_REPOSITORY/actions/runs/$GITHUB_RUN_ID/cancel"

    echo "sleep for 10 seconds"
    sleep 10
}
