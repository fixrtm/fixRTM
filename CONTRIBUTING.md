# Contributing Guide

## Issues

Both feature requests and bug reports are welcome!

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
