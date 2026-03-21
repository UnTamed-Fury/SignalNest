/**
 * Parses GitHub webhook payloads into SignalNest events.
 * Supports: push, pull_request, issues, release, workflow_run,
 *           check_run, star, fork, issue_comment, create, delete,
 *           deployment_status, ping.
 */
export function parseGitHub(req) {
  const event   = req.headers['x-github-event'] ?? 'unknown';
  const payload = req.body;
  const repo    = payload?.repository?.full_name ?? 'unknown/repo';
  const sender  = payload?.sender?.login         ?? 'GitHub';

  let title = `[${repo}] GitHub ${event}`;
  let body  = '';

  switch (event) {
    case 'push': {
      const branch  = (payload.ref ?? '').replace('refs/heads/', '');
      const commits = payload.commits?.length ?? 0;
      const msg     = payload.commits?.[0]?.message?.split('\n')[0] ?? '';
      title = `🔀 Push to ${branch} — ${repo}`;
      body  = `${commits} commit${commits !== 1 ? 's' : ''}${msg ? ': ' + msg : ''} by ${sender}`;
      break;
    }
    case 'pull_request': {
      const action = payload.action;
      const pr     = payload.pull_request;
      title = `🔃 PR #${pr?.number} ${action} — ${repo}`;
      body  = `"${pr?.title}" by ${sender}`;
      break;
    }
    case 'issues': {
      const issue = payload.issue;
      title = `🐛 Issue #${issue?.number} ${payload.action} — ${repo}`;
      body  = `"${issue?.title}" by ${sender}`;
      break;
    }
    case 'issue_comment': {
      title = `💬 Comment on #${payload.issue?.number} — ${repo}`;
      body  = payload.comment?.body?.slice(0, 200) ?? '';
      break;
    }
    case 'release': {
      title = `🚀 Release ${payload.release?.tag_name} — ${repo}`;
      body  = payload.release?.name ?? payload.action;
      break;
    }
    case 'workflow_run': {
      const wf = payload.workflow_run;
      const icon = wf?.conclusion === 'success' ? '✅' : wf?.conclusion === 'failure' ? '❌' : '⏳';
      title = `${icon} ${wf?.name} ${wf?.conclusion ?? wf?.status} — ${repo}`;
      body  = `Branch: ${wf?.head_branch} · Triggered by ${sender}`;
      break;
    }
    case 'check_run': {
      const cr = payload.check_run;
      const icon = cr?.conclusion === 'success' ? '✅' : cr?.conclusion === 'failure' ? '❌' : '⏳';
      title = `${icon} ${cr?.name} — ${repo}`;
      body  = `${cr?.status} ${cr?.conclusion ?? ''} on ${cr?.head_sha?.slice(0, 7)}`;
      break;
    }
    case 'star':
      title = `⭐ ${repo} starred by ${sender}`;
      body  = `Total stars: ${payload.repository?.stargazers_count}`;
      break;
    case 'fork':
      title = `🍴 ${repo} forked by ${sender}`;
      body  = `Fork: ${payload.forkee?.full_name}`;
      break;
    case 'create':
      title = `🌿 ${payload.ref_type} created in ${repo}`;
      body  = `"${payload.ref}" by ${sender}`;
      break;
    case 'delete':
      title = `🗑️ ${payload.ref_type} deleted in ${repo}`;
      body  = `"${payload.ref}" by ${sender}`;
      break;
    case 'deployment_status': {
      const ds = payload.deployment_status;
      const icon = ds?.state === 'success' ? '✅' : ds?.state === 'failure' ? '❌' : '🚀';
      title = `${icon} Deploy ${ds?.state} — ${repo}`;
      body  = ds?.description ?? payload.deployment?.environment ?? '';
      break;
    }
    case 'ping':
      title = `🏓 Webhook connected — ${repo}`;
      body  = `zen: "${payload.zen}"`;
      break;
    default:
      title = `[${repo}] ${event}`;
      body  = `by ${sender}`;
  }

  return {
    title,
    body,
    source:     repo,
    group:      repo.split('/')[0] ?? 'github',
    category:   'normal',
    rawPayload: JSON.stringify(payload).slice(0, 2000),
    channel:    'remote',
  };
}
