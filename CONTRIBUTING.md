# Contributing Guide

## Issues

Both feature requests and bug reports are welcome!

### Archive & Compression Formats

Because author of fixRTM loves command line on macOS, some windows-specific archive formats are very hard to extract.
Please choose one archive/compression format from the Recommended / Accepted list below.

#### Recommended

Those formats are easy to use, good for compatibility, and good for file size.

- `.zip` (with utf8 is recommended)
- `.gz` (for single file like log files)
- `.tar.gz` = `.tgz` (for multiple files. better compression than `.zip` in most case)
- `.tar.xz` (if you want more compression ratio than `.tar.gz`)
- `.tar.zstd` (if you want more compression ratio than `.tar.gz`)

#### Accepted

Those formats are not good enough to recommend.

- `.tar` (no compression)
- `.cpio` (no compression and not familiar to me)

#### Rejected

Those formats are not common for commandline use nor posix environment.

- `.7z`
- `.cab`

## Pull Requests

Pull Requests are welcome!

Before contribution, please read [LICENSE](/LICENSE) and
please agree to your contribution will be published under the license.

For small changes like fixing typo or documentation changes,
You can create Pull Requests without making issues.

Please follow [Conventional Commits] commit format.
There's check for this so it's (almost) required to follow this format.

I recommend you to use git hooks. you can install git hooks via ``./githooks/install.sh``.

[Conventional Commits]: https://www.conventionalcommits.org/en/v1.0.0/

Please append both `CHANGELOG-SNAPSHOS.md` and `CHANGELOG.md`
unless your change is fixing problem of feature which is not published.
If your change is published in snapshots but not in release, please update `CHANGELOG-SNAPSHOS.md`.

This project uses [mod-patching] to edit RTM sources.
To develop with [mod-patching], it's required to follow steps below:

Before start development or after changing RTM version,
please run ``./gradlew preparePatchingEnvironment``.

After checkout, please run ``./pm.apply-patches``.

Before commit, please run ``./pm.create-diff``.

For more details, please see [source-patching-development]

[mod-patching]: https://github.com/anatawa12/mod-patching
[source-patching-development]: https://github.com/anatawa12/mod-patching/blob/HEAD/docs/source-patching-development.md
