+++
pre = ""
title = "Developer's guide"
weight = 10
tags = ["development", "contributions", "commits"]
summary = "How to contribute"
+++

Contributions to this project are welcome. Just some notes:

* Use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/). The build auto-generates aggressive git hooks that enforce the rules.
* Contributions must be realized in forks of the project. Once a contribution is ready, please open a [pull request](https://github.com/AlchemistSimulator/Alchemist/compare).
* Keep in sync with the mainline (our `master` branch), preferably via rebasing.
* Commit often. Small pull requests targeting a small part of a larger work are very welcome if they can be merged individually.
* Do not introduce low quality code. All the new code must comply with the checker rules (that are quite strict) and must not introduce any other warning. Resolutions of existing warnings (if any is present) are very welcome instead.
* Fixes should include a regression test.
* New features must include appropriate test cases.
* Any change should cause an extension or modification of the documentation, that must be kept up to date

