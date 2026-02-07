# Contributing to healthcareproject-backend

Thanks for your interest in contributing!

This project is an actively maintained backend service.
Please read the guidelines below before submitting issues or pull requests.

## Reporting Issues

- Search existing issues before creating a new one.
- Clearly describe the problem, expected behavior, and actual behavior.
- Include logs, stack traces, or screenshots if applicable.

## Contributing Code

1. Fork the repository.
2. Create a feature branch from `develop`.
3. Make your changes.
4. Ensure the project builds and tests pass.
5. Submit a Pull Request targeting the `develop` branch.

## Code Style

- Follow existing code conventions.
- Use meaningful variable and method names.
- Avoid introducing N+1 queries or unnecessary database calls.
- Prefer batch queries and projections for read-heavy APIs.

## Commit Messages

This project follows a simplified Conventional Commits style:

- feat: new feature
- fix: bug fix
- refactor: code refactoring without behavior change
- docs: documentation changes
- chore: build or infrastructure changes

## Code of Conduct

By participating in this project, you agree to follow the
[Code of Conduct](CODE_OF_CONDUCT.md).

## Important Notes

- Large changes should be discussed in an issue before submitting a PR.
- The maintainer may request changes or close PRs that do not align with the project direction.
