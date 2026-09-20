---
name: start-task
description: "Start a new task branched fresh from origin/main, either in an isolated git worktree or in-place in the current checkout."
---

# Start Task

This skill starts a new task from a fresh `origin/main`, either in an isolated git worktree under
`.claude/worktrees/` or in-place in the current checkout. A worktree keeps the task fully separate
from the main checkout and any other in-progress task, at the cost of an extra setup step (copying
git-ignored local config into the new worktree). In-place is faster to start but ties up the
current checkout for this one task.

## Step-by-step workflow

### 1. Determine how to start

Check whether the user's request already specifies a mode (e.g. "cria numa worktree", "começa
in-place", "direto no checkout atual"). If it does, use that mode and skip straight to step 2.

Otherwise, ask the user directly: "Quer que eu crie isso numa worktree isolada, ou direto in-place
no checkout atual?" Do not assume a default — wait for their answer.

### 2. Determine the branch type and short description

Pick the `<type>` prefix using the table in [`branch-types.md`](../branch-types.md) (shared with
`create-pr`).

If the user's request makes the type and a short kebab-case description obvious, use them directly.
Otherwise ask the user.

### 3. Sync with origin

```bash
git remote update origin
```

### 4. Create the branch

#### Worktree mode

Check for collisions first:

```bash
git worktree list
git branch --list <type>/<short-description>
ls .claude/worktrees/<short-description>
```

If the branch already exists, the worktree path is already registered, or the directory already
exists on disk (even unregistered — e.g. a leftover from a worktree removed without
`git worktree remove`), stop and tell the user instead of overwriting anything.

```bash
git worktree add -b <type>/<short-description> .claude/worktrees/<short-description> origin/main
```

This creates the branch and checks it out in the new worktree in one step, based on the latest
`origin/main` fetched in step 3 — not on whatever the main checkout currently has checked out.

Then copy local build config into the new worktree, since a fresh worktree only contains tracked
files:

- `local.properties` (has `sdk.dir`; without it Gradle sync fails). Copy it if it exists at the
  repo root:
  ```bash
  cp local.properties .claude/worktrees/<short-description>/local.properties
  ```
- `.claude/settings.local.json`, so the new worktree's Claude Code session has the same permission
  settings instead of re-prompting for everything:
  ```bash
  cp .claude/settings.local.json .claude/worktrees/<short-description>/.claude/settings.local.json
  ```

Do **not** copy git-ignored build outputs or caches (`.gradle/`, `**/build/`, `.kotlin/`, `.idea/`,
`.externalNativeBuild`, `.cxx`, `.claude/memory/`, `.claude/worktrees/`). Those are regenerated
fresh per worktree; copying them can carry over stale state.

If `local.properties` doesn't exist at the repo root, skip copying it — Android Studio will
regenerate it on first sync, so just mention this to the user.

#### In-place mode

Check the current checkout is clean and not already mid-task:

```bash
git status -s
git branch --show-current
```

If there are uncommitted changes, stop and ask the user how to proceed (commit, stash, or use
worktree mode instead) — don't discard or stash anything without asking. If the current branch
isn't `main` (or doesn't track `origin/main`), tell the user and confirm before switching away from
it, since that would leave whatever they're on now behind.

```bash
git checkout -b <type>/<short-description> origin/main
```

No file copying is needed here — the checkout already has all the local git-ignored config in
place.

### 5. Report the result

Worktree mode: tell the user the absolute path to the new worktree and the branch name, and that
it's ready to open (new Claude Code session, or Android Studio via "Open" on that path).

In-place mode: confirm the branch name and that the current checkout is now on it, ready to work.

### 6. Start implementing immediately

If the user's request already described the task to do (bug to fix, feature to add, etc.) —
not just which branch/mode to use — don't stop after step 5 and wait for a "go ahead"/"pode
implementar". Continue straight into implementing that task in this same turn, right after the
short status report from step 5.

- Worktree mode: keep working in this same session — do not wait for a new session to be opened
  in the worktree. Point every file tool (Read/Edit/Write/Bash/etc.) at paths inside the new
  worktree directory (`.claude/worktrees/<short-description>/...`) instead of the main checkout,
  since tools work with any path regardless of the session's current working directory.
- In-place mode: the current checkout is already on the new branch, so just continue normally.

Only stop and wait for the user if their request genuinely gave no task description (e.g. they
only said "start a task in a worktree" with no further detail) — in that case, ask what to
implement.

## Edge cases

- If `git worktree add` fails because the branch or path already exists, stop — don't force or
  pick a different name silently. Ask the user how they want to resolve it.
- Never run worktree mode from inside another worktree; it should always target
  `.claude/worktrees/<short-description>` relative to the main repo root.
- In in-place mode, if `git checkout -b` fails because the branch name already exists, stop and ask
  the user how they want to resolve it.
