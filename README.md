# qasrl.org

## Setup

```
bundle install
```

## Usage

```
bundle exec jekyll serve 2>&1 | sed '/^        \*\* ERROR: directory/,/^        MORE INFO/d;'
```
See [pubdata] for details.
