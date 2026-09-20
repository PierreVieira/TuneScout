---
name: finish-task
description: "Tear down a finished task once its PR has merged: remove its worktree, or switch back to main in-place, then delete the branch and sync origin."
---

# Finish Task

This skill cleans up after `start-task`, in either mode it can create a task in:

- **Worktree mode**: removes the worktree under `.claude/worktrees/` and deletes its branch.
- **In-place mode**: switches the main checkout back to `main` and deletes the branch that was
  checked out there.

In both modes this only runs after confirming the branch's PR has actually merged. Running this
skill (or the user asking to clean up / finish the task) is itself the user's authorization to
remove the worktree and branch once that merge is verified — do not ask for a second confirmation
before step 4's cleanup. Still stop and ask before anything not covered by that authorization: an
unmerged branch (step 3), or discarding uncommitted changes (see Edge cases).

All commands in this skill must run from the **main repo checkout**, not from inside a worktree
being removed — a worktree cannot remove itself, and `git branch -d` on a branch checked out
elsewhere will fail anyway.

## Step-by-step workflow

### 1. Identify the task, branch, and mode

```bash
git worktree list
git branch --show-current
```

- If the user named a task that matches a path under `.claude/worktrees/`, or the current working
  directory is inside one, that's **worktree mode**. Get its branch:
  ```bash
  git -C .claude/worktrees/<name> branch --show-current
  ```
- Otherwise, if the main checkout is currently on a branch other than `main`, that's **in-place
  mode** — the branch to clean up is that current branch.
- If it's ambiguous (e.g. more than one worktree exists and no task was named, or the main checkout
  is also mid-task), ask the user which task to finish.

### 2. Sync with origin

```bash
git remote update origin
```

### 3. Verify the branch is actually merged

Prefer checking the PR directly:

```bash
gh pr view <branch> --json state,mergedAt
```

If `gh` isn't available or there's no PR, fall back to:

```bash
git log origin/main..<branch> --oneline
```

An empty result means every commit on the branch is already in `origin/main`.

**If the branch is not merged** (PR still open, no PR at all, or commits not in `origin/main`),
stop. Tell the user exactly what's unmerged and ask for explicit confirmation before proceeding —
never delete an unmerged task silently.

### 4. Clean up

State exactly what's about to happen — the worktree path and branch being removed, or "switch the
current checkout back to `main` and delete `<branch>`" for in-place — then proceed without waiting
for a further confirmation; the merge check in step 3 plus the user's request to finish the task
already authorize this.

**Worktree mode:**

```bash
git worktree remove .claude/worktrees/<name>
git branch -d <branch>
git remote update origin
```

**In-place mode:**

```bash
git checkout main
git pull
git branch -d <branch>
```

In both modes, use `git branch -d` (safe delete), not `-D` — it refuses to delete a branch with
unmerged commits, which is a useful last safety check even after step 3.

### 5. Verify cleanup

```bash
git worktree list
git branch --list <branch>
```

Neither should show the removed task anymore. Report the result to the user.

## Edge cases

- **Worktree mode:** if `git worktree remove` fails because of uncommitted changes (modified or
  untracked files) in the worktree, stop and show what's there. Only pass `--force` after the user
  explicitly confirms discarding those changes.
- **In-place mode:** if `git checkout main` fails because of uncommitted changes, stop and ask the
  user how to proceed (commit, stash, or discard) — don't discard anything without asking.
- If `git branch -d` fails because the branch isn't fully merged (contradicting step 3, e.g. the PR
  used a merge commit strategy git doesn't recognize locally), stop and ask the user before using
  `-D`.
- If a worktree directory was already deleted from disk without `git worktree remove` (it will show
  as `prunable` in `git worktree list`), just run `git worktree prune` and then delete the branch as
  in step 4.
