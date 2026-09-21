---
name: create-pr
description: "Create a pull request following the project conventions: ktlint formatting, Conventional Commits title, description template and assignee."
---

# Create PR

This skill formats the code, commits any uncommitted changes, pushes the branch, and opens a pull
request with a clear title and description inferred from the actual changes.

## Step-by-step workflow

### 1. Gather context

Run these in parallel:
- `git status -s` to check for uncommitted changes
- `git diff --stat` to see unstaged changes
- `git log --oneline -1` to get the latest commit
- `git rev-parse --abbrev-ref HEAD` to get the current branch name
- `git log --oneline main..HEAD` to see all commits on this branch
- `git diff main...HEAD --stat` to see total changes vs main

### 2. Run ktlint formatter (if needed)

Check whether any `.kt` or `.kts` files are among the changed files gathered in step 1. If none are
present, skip this step entirely.

If there are Kotlin files changed, run from the project root:

```bash
./scripts/ktlint.sh --format
```

This is the ktlint CLI, which is exactly what the `static-analysis` workflow runs in CI. Do **not**
use `./gradlew ktlintFormat`: the Gradle plugin path is far slower.

If the command exits with a non-zero code, stop and notify the user:
> "ktlint found issues it couldn't fix automatically. Please review and fix the reported errors, then try again."

Do not proceed until the formatter succeeds.

Formatting rewrites files in place, so re-run `git status -s` afterwards if you need an up-to-date
list of changed files for the steps below.

### 3. Check the current branch

```bash
git branch --show-current
```

Also check the upstream tracking branch:

```bash
git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null
```

**If the current branch is `main`**, or the upstream tracking branch is `origin/main`, stop and ask
whether the user wants to use the `start-task` skill instead — it's the preferred way to start new
work and will ask whether to use a worktree or work in-place. If they'd rather branch directly here
without going through that flow, continue to step 4 to determine the prefix, then create the branch
in step 5.

**If the current branch is already a feature/fix/enhancement/refactor branch**, skip branch
creation and go directly to step 6.

### 4. Infer the change type

Inspect the uncommitted changes and any commits ahead of origin/main:

```bash
git remote update
git diff HEAD
git log origin/main..HEAD --oneline
```

Pick the `<type>` prefix using the table in [`branch-types.md`](../branch-types.md).

### 5. Create a new branch (if needed)

```bash
git checkout -b <type>/<short-description>
```

Examples: `fix/seek-bar-jumps-on-drag`, `feature/album-screen`, `chore/ci-unit-tests`.

### 6. Run the unit tests (if needed)

If every file the branch changes is Markdown (`.md`), as gathered in step 1 (commits ahead of
`main` and uncommitted changes alike), skip this step. No test can read those files, and the run
takes minutes.

Otherwise, run:

```bash
./gradlew testDebugUnitTest test
```

If tests fail, stop and report the failing tests. Do not push a red branch.

### 7. Commit all uncommitted changes

```bash
git add -A
git commit -m "<type>: <short description>"
```

The commit message must start with the type prefix, be imperative and concise, and not exceed 72
characters. Skip this step if there is nothing to commit.

### 8. Push the branch

```bash
git push -u origin HEAD
```

### 9. Create the pull request

```bash
gh pr create \
  --base main \
  --title "<type>: <short description>" \
  --body "<description>" \
  --assignee @me
```

**Title:** same format as the commit message.

**Description:** a clear summary of what changed and why, at a medium level of detail:
- Describe the problem being solved or the feature being added
- Mention the affected screens or areas of the app
- Explain the approach taken if it is not obvious
- Do NOT list every file changed or describe code line by line
- Keep it to 3–8 sentences or a short bullet list

### 10. Squash and merge (optional)

Ask the user whether to squash merge now. If yes:

```bash
gh pr merge --squash
```

Merge right away: `main` has no branch protection, so there is no need to wait for CI or pass
`--auto`. If the merge fails (e.g. a conflict with `main`), notify the user and stop.

Once the squash merge succeeds, immediately run the `finish-task` skill in the same turn — don't
wait for the user to ask for it. Asking for the squash merge is also the request to clean up the
task. `finish-task` handles both worktree and in-place tasks and checks the merge before removing
the branch (and the worktree, if there is one), so don't duplicate its logic here.

## Edge cases

- If `./scripts/ktlint.sh --format` fails, stop immediately. The remaining errors are ones ktlint
  cannot autocorrect (custom `tunescout-style:*` rules), so they must be fixed by hand.
- If the branch already has an open PR, notify the user instead of creating a duplicate.
- Always target `main` as the base branch.
