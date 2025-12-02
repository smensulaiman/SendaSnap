# Coding Standards and Architecture Guidelines

## Overview
This document defines the coding standards, architecture patterns, and best practices for the SendaSnap Android application. All code should adhere to these guidelines to ensure consistency, maintainability, and code quality.

## General Principles

### Clean Code
- Write self-documenting code with clear, descriptive names
- Keep methods focused on a single responsibility
- Prefer composition over inheritance
- Use meaningful variable and method names that express intent
- Avoid magic numbers and strings - use constants or resources
- Keep methods short and focused (ideally under 50 lines)
- Extract complex logic into separate methods or utility classes
- Use early returns to reduce nesting and improve readability

### Code Organization
- Follow the existing package structure
- Group related functionality together
- Keep activities, fragments, adapters, and utilities in their respective packages
- Use consistent naming conventions across the codebase

## Import Management

### Import Optimization
- Remove all unused imports before committing code
- Use explicit imports instead of wildcard imports (`import java.util.*`)
- Organize imports in the following order:
  1. Android framework imports
  2. Third-party library imports
  3. Project-specific imports
- Use Android Studio's "Optimize Imports" feature (Ctrl+Alt+O / Cmd+Option+O)

### Import Rules
- Never use wildcard imports
- Remove imports for classes that are no longer used
- Keep imports organized and consistent

## Variable Management

### Unused Variables
- Remove all unused variables, fields, and parameters
- Remove unused private methods
- Remove unused constants and resources
- Use Android Studio's "Unused Declaration" inspection to identify unused code

### Variable Naming
- Use camelCase for variables and methods
- Use PascalCase for classes and interfaces
- Use UPPER_SNAKE_CASE for constants
- Use descriptive names that indicate purpose
- Avoid abbreviations unless they are widely understood

### Variable Scope
- Declare variables in the narrowest scope possible
- Prefer local variables over instance variables when possible
- Use `final` for variables that should not be reassigned

## Comments Policy

### No Comments Rule
- Do not write comments in code
- Code should be self-explanatory through naming and structure
- If code requires explanation, refactor it to be clearer
- Remove existing comments unless absolutely critical for understanding complex algorithms
- Exception: Only add comments for complex business logic that cannot be made clearer through refactoring

### Documentation
- Use JavaDoc only for public APIs and library interfaces
- Keep JavaDoc concise and focused on usage, not implementation
- Do not document obvious operations

## Architecture Patterns

### Activity and Fragment Guidelines
- Activities should handle navigation and high-level orchestration
- Fragments should handle UI logic and user interactions
- Use ViewBinding for all views
- Avoid direct findViewById calls
- Keep activities and fragments lean - delegate business logic to ViewModels or utility classes

### ViewBinding
- Always use ViewBinding instead of findViewById
- Inflate views using binding classes
- Null check binding in onDestroyView for fragments

### Context Management
- Never store Activity context in long-lived objects
- Use Application context for singleton services
- Check `isAdded()` and `getContext() != null` in fragments before using context
- Use `requireContext()` only when fragment is guaranteed to be attached
- Prefer `getContext()` with null checks for safety

### Lifecycle Awareness
- Remove listeners in appropriate lifecycle methods
- Unregister callbacks in onDestroy or onDestroyView
- Cancel background tasks when components are destroyed
- Use lifecycle-aware components when appropriate

### Error Handling
- Handle exceptions gracefully
- Log errors appropriately using Log.e() or Log.w()
- Provide user-friendly error messages
- Never silently swallow exceptions without logging

### Resource Management
- Close resources in finally blocks or use try-with-resources
- Release listeners and callbacks to prevent memory leaks
- Clear references to avoid memory leaks

## Code Style

### Formatting
- Use 4 spaces for indentation (not tabs)
- Keep lines under 120 characters when possible
- Use consistent brace style (opening brace on same line)
- Add blank lines between logical sections

### Method Structure
- Keep methods focused and single-purpose
- Limit method parameters (prefer objects for many parameters)
- Use early returns to reduce nesting
- Extract complex conditionals into well-named boolean methods

### Class Structure
- Order class members: constants, fields, constructors, methods
- Group related methods together
- Keep classes focused on a single responsibility
- Prefer smaller, focused classes over large monolithic classes

## Android-Specific Guidelines

### Permissions
- Request permissions at appropriate times
- Handle permission results properly
- Provide rationale for permission requests
- Handle permission denial gracefully

### Background Work
- Use appropriate threading mechanisms (Handler, Executor, Coroutines)
- Never perform network or database operations on main thread
- Use runOnUiThread() for UI updates from background threads
- Cancel background tasks when components are destroyed

### Firebase
- Always check for null before accessing Firebase data
- Handle Firebase errors appropriately
- Remove listeners when no longer needed
- Use proper error callbacks for Firebase operations

### Notifications
- Check notification permissions before showing notifications
- Use NotificationManagerCompat for compatibility
- Create notification channels for Android 8.0+
- Handle notification clicks with proper intents

## Code Review Checklist

Before submitting code, ensure:
- [ ] All unused imports are removed
- [ ] All unused variables and methods are removed
- [ ] No unnecessary comments are present
- [ ] Code follows naming conventions
- [ ] Methods are focused and not too long
- [ ] Error handling is appropriate
- [ ] Memory leaks are prevented (listeners removed, contexts not stored)
- [ ] Lifecycle methods are used correctly
- [ ] ViewBinding is used instead of findViewById
- [ ] Context usage is safe (null checks, lifecycle awareness)

## Refactoring Guidelines

### When to Refactor
- When code duplication is detected
- When methods become too long or complex
- When variable names are unclear
- When code structure makes it hard to understand

### Refactoring Principles
- Extract methods for repeated logic
- Extract constants for magic values
- Simplify complex conditionals
- Break down large classes into smaller, focused classes
- Improve naming to make code self-documenting

## Testing Considerations

- Write testable code by keeping business logic separate from UI
- Avoid static dependencies where possible
- Use dependency injection for better testability
- Keep methods pure (no side effects) when possible

## Performance Guidelines

- Avoid creating objects in loops
- Use StringBuilder for string concatenation in loops
- Cache expensive operations
- Use appropriate data structures
- Avoid unnecessary object creation

## Security Guidelines

- Never log sensitive information
- Validate user input
- Use secure storage for sensitive data
- Follow Android security best practices

## Maintenance

- Keep dependencies up to date
- Remove deprecated API usage
- Update code when patterns change
- Regularly review and refactor code

## Tools and Automation

- Use Android Studio's code inspection tools
- Run "Optimize Imports" before committing
- Use "Analyze Code" to find potential issues
- Configure code style settings in Android Studio
- Use lint to catch common issues

