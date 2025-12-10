# Contributing to Sensorbite

Thank you for your interest in contributing to Sensorbite! This document provides guidelines for contributing to the project.

## Getting Started

1. **Fork the repository**
2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/sensorbite.git
   cd sensorbite
   ```
3. **Create a branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

## Development Setup

### Prerequisites
- Java 17 or higher
- Gradle 8.5 or higher
- Docker (optional, for testing)

### Building the Project
```bash
./gradlew build
```

### Running Tests
```bash
./gradlew test
```

### Code Quality Checks
```bash
./gradlew qualityCheck
```

## Code Standards

### Code Formatting
We use Spotless with Google Java Format:
```bash
./gradlew spotlessApply
```

### Code Style
- Follow Java naming conventions
- Use meaningful variable names
- Keep methods short (<100 lines)
- Write self-documenting code
- Add JavaDoc for public APIs

### Testing
- Write unit tests for new features
- Maintain >80% code coverage
- All tests must pass before PR

### Commit Messages
Follow conventional commits format:
- `feat: add new feature`
- `fix: fix bug in component`
- `docs: update documentation`
- `test: add tests for feature`
- `refactor: refactor code`
- `chore: update dependencies`

## Pull Request Process

1. **Update documentation** if needed
2. **Add tests** for new features
3. **Run quality checks** locally
   ```bash
   ./gradlew qualityCheck
   ```
4. **Update CHANGELOG** with your changes
5. **Create Pull Request** with clear description
6. **Wait for review** from maintainers
7. **Address review comments** if any

## Code Review Guidelines

### For Contributors
- Respond to feedback promptly
- Make requested changes
- Keep discussions professional

### For Reviewers
- Be constructive and respectful
- Focus on code quality
- Test the changes locally

## Development Workflow

### 1. Before Starting
- Check existing issues
- Discuss major changes first
- Ensure no duplicate work

### 2. During Development
- Write clean code
- Add appropriate tests
- Update documentation
- Run quality checks frequently

### 3. Before Submitting PR
```bash
# Format code
./gradlew spotlessApply

# Run all checks
./gradlew qualityCheck

# Build project
./gradlew build
```

## Project Structure

```
src/
├── main/
│   ├── java/com/sensorbite/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Custom exceptions
│   │   ├── repository/      # Data repositories
│   │   ├── service/         # Business logic
│   │   └── evacuation/      # Evacuation module
│   └── resources/
│       ├── application.yml  # Configuration
│       └── logback-spring.xml  # Logging
└── test/
    └── java/com/sensorbite/  # Test classes
```

## Reporting Issues

### Bug Reports
Use the bug report template:
- Clear description
- Steps to reproduce
- Expected vs actual behavior
- Environment details
- Logs/screenshots

### Feature Requests
Use the feature request template:
- Problem statement
- Proposed solution
- Alternatives considered
- Use cases

## Community Guidelines

- Be respectful and inclusive
- Help others learn
- Provide constructive feedback
- Follow code of conduct

## Questions?

- Open an issue for discussion
- Check existing documentation
- Review closed issues/PRs

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to Sensorbite! 🎉
